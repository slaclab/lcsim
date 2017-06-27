/*
 * Kpix.java
 *
 * Created on April 20, 2007, 10:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.lcsim.recon.tracking.digitization.sisim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.BinomialDistributionImpl;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.special.Erf;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.units.clhep.SystemOfUnits;

/**
 *
 * @author tknelson
 */
public class Kpix implements ReadoutChip {

    private static final int VERSION_NUMBER = 1; // version number
    private static Random _random = new Random();
    private static NormalDistribution _gaussian = new NormalDistributionImpl(0.0,1.0);
    private static BinomialDistribution _binomial = new BinomialDistributionImpl(1,1);
    private double _noise_threshold = 4;
    private double _neighbor_threshold = 2;

    // Static values and defaults: DO NOT CHANGE
    //==========================================
    private static class ControlRegisters {

        private enum GainMode {

            SINGLE, DOUBLE
        }

        private enum Polarity {

            POSITIVE, NEGATIVE
        }
        private int _version_number = VERSION_NUMBER;
        private GainMode _gain_mode = GainMode.DOUBLE;
        private Polarity _polarity = Polarity.POSITIVE;
        private double _gain_crossover = 1.0;           // V

        public ControlRegisters() {
        }

        // Setters-
        // do not allow public setting of version number
        // do not allow setting gain crossover for now (more info needed for this)
        private void setVersionNumber(int version_number) {
            _version_number = version_number;
        }

        private void setPolarity(Polarity polarity) {
            _polarity = polarity;
        }

        private void setGainMode(GainMode gain_mode) {
            _gain_mode = gain_mode;
        }

        // Getters
        private int getVersionNumber() {
            return _version_number;
        }

        private GainMode getGainMode() {
            return _gain_mode;
        }

        private Polarity getPolarity() {
            return _polarity;
        }

        private double getGainCrossover() {
            return _gain_crossover;
        }

        // Encoding and decoding resgister information
        private int encoded() {
            return (getVersionNumber() << 1 | getGainMode().ordinal()) << 1 | getPolarity().ordinal();
        }

        private static ControlRegisters decoded(int encoded_registers) {
            ControlRegisters registers = new ControlRegisters();

            int polarity = encoded_registers & 0x1;
            int double_gain = (encoded_registers & 0x2) >> 1;
            int version_number = (encoded_registers & 0xFC) >> 2;

            registers.setVersionNumber(version_number);
            registers.setGainMode(GainMode.values()[double_gain]);
            registers.setPolarity(Polarity.values()[polarity]);

            return registers;
        }
    }
    // Fields
    ControlRegisters _control_registers = new ControlRegisters();
    KpixChannel _channel = new KpixChannel(_control_registers); // one per chip for now

    /** Creates a new instance of Kpix */
    public Kpix() {
    }
    
    // ReadoutChip Interface
    public KpixChannel getChannel(int channel_number) {
        return _channel;
    }

    /**
     * Set noise threshold for creating noise hits.  Units are number of sigma
     * above the RMS noise.
     * 
     * @param noise_threshold
     */
    public void setNoiseThreshold(double noise_threshold) {
        _noise_threshold = noise_threshold;
    }

    /**
     * Set the threshold for reading a channel if it's neighbor is
     * above the noise threshold.  Units are number of sigma above
     * the RMS noise.
     *
     * @param neighbor_threshold
     */
    public void setNeighborThreshold(double neighbor_threshold) {
        _neighbor_threshold = neighbor_threshold;
    }

    public SortedMap<Integer,List<Integer>> readout(SiElectrodeDataCollection data, SiSensorElectrodes electrodes)
    {
        if (data == null) data = new SiElectrodeDataCollection();
        addNoise(data,electrodes);
        return digitize(data,electrodes);
    }

    // Decoding hit information
    public double decodeCharge(RawTrackerHit hit) {
        Kpix.ControlRegisters control_registers = Kpix.ControlRegisters.decoded(hit.getADCValues()[0]);
        if (control_registers.getVersionNumber() != VERSION_NUMBER) {
            throw new RuntimeException("Attempting to reconstruct hits generated with Kpix version " + control_registers.getVersionNumber() +
                    " with Kpix version " + VERSION_NUMBER);
        }
        Kpix.KpixChannel.ReadoutRegisters readout_registers = Kpix.KpixChannel.ReadoutRegisters.decoded(hit.getADCValues()[1]);

        return (readout_registers.getAdcValue() + 0.5) / KpixChannel.computeGain(readout_registers, control_registers); // be clever about reconstructing charge
    }

    public int decodeTime(RawTrackerHit hit) {
        Kpix.ControlRegisters control_registers = Kpix.ControlRegisters.decoded(hit.getADCValues()[0]);
        if (control_registers.getVersionNumber() != VERSION_NUMBER) {
            throw new RuntimeException("Attempting to reconstruct hits generated with Kpix version " + control_registers.getVersionNumber() +
                    " with Kpix version " + VERSION_NUMBER);
        }
        Kpix.KpixChannel.ReadoutRegisters readout_registers = Kpix.KpixChannel.ReadoutRegisters.decoded(hit.getADCValues()[1]);
        return readout_registers.getTime();
    }

    // Internal stuff - all private
    private ControlRegisters getControlRegisters() {
        return _control_registers;
    }

    private void addNoise(SiElectrodeDataCollection data, SiSensorElectrodes electrodes) {

//        System.out.println("\n"+"Adding noise...");

        // Add full noise distribution to any cells with charge deposition
        //----------------------------------------------------------------
        for (Entry datum : data.entrySet()) {
            int channel = (Integer) datum.getKey();
            double noise = getChannel(channel).computeNoise(electrodes.getCapacitance(channel));
            int origCharge = ((SiElectrodeData) datum.getValue()).getCharge();
            int addedNoise = (int) Math.round(_random.nextGaussian() * noise);
//            System.out.println("Kpix::addNoise   channel  " + channel + "  charge = " + origCharge + " noise = " + addedNoise);
            if (addedNoise + origCharge < 0) {
//                System.out.println("Kpix::addNoise   preventing charge from going negative");
                addedNoise = -origCharge;
            }
            ((SiElectrodeData) datum.getValue()).addCharge(addedNoise);
//            System.out.println("Kpix::addNoise   new charge = " + ((SiElectrodeData) datum.getValue()).getCharge());
        }

        // Throw cluster seeds on all channels
        //------------------------------------
//        System.out.println("\n"+"Throw flyers...");

        int nelectrodes = electrodes.getNCells();
        int nelectrodes_empty = nelectrodes - data.size();
        double normalized_integration_limit = _noise_threshold;

        double integral = normalCDF(normalized_integration_limit);
        int nchannels_throw = drawBinomial(nelectrodes_empty, integral);

//        System.out.println("    # Empty channels: "+nelectrodes_empty);
//        System.out.println("    "+normalized_integration_limit+"-sigma integral: "+integral);
//        System.out.println("    Mean # channels: "+nelectrodes_empty*integral);
//        System.out.println("    Binomial draw: "+nchannels_throw);

        // Now throw Gaussian randoms above a threshold and put signals on unoccupied channels
        for (int ithrow = 0; ithrow < nchannels_throw; ithrow++) {
            // Throw to get a channel number
            int channel = _random.nextInt(nelectrodes);
            while (data.keySet().contains(channel)) {
                channel = _random.nextInt(nelectrodes);
            }

            double noise = getChannel(channel).computeNoise(electrodes.getCapacitance(channel));

//            System.out.println("        noise: "+noise);
//            System.out.println("        Gaussian above threshold: "+drawGaussianAboveThreshold(integral));

            // Throw Gaussian above threshold
            int charge = (int) Math.round(drawGaussianAboveThreshold(integral) * noise);
            data.add(channel, new SiElectrodeData(charge));
        }

        // Now throw to lower threshold on channels that neighbor hits until we are exhausted
        //-----------------------------------------------------------------------------------
        nchannels_throw = 1;
        while (nchannels_throw > 0) {
//            System.out.println("\n"+"Throw nieghbors...");

            // Get neighbor channels
            Set<Integer> neighbors = new HashSet<Integer>();
            for (int channel : data.keySet()) {
                neighbors.addAll(electrodes.getNearestNeighborCells(channel));
            }
            neighbors.removeAll(data.keySet());

            nelectrodes_empty = neighbors.size();
            normalized_integration_limit = _neighbor_threshold;
            
            integral = normalCDF(normalized_integration_limit);
            nchannels_throw = drawBinomial(nelectrodes_empty, integral);

//            System.out.println("    # Empty channels: "+nelectrodes_empty);
//            System.out.println("    "+normalized_integration_limit+"-sigma integral: "+integral);
//            System.out.println("    Mean # channels: "+nelectrodes_empty*integral);
//            System.out.println("    Binomial draw: "+nchannels_throw);

            // Now throw Gaussian randoms above a threshold and put signals on unoccupied channels
            for (int ithrow = 0; ithrow < nchannels_throw; ithrow++) {
                // Throw to get a channel number
                List<Integer> neighbors_list = new ArrayList<Integer>(neighbors);

                int channel = neighbors_list.get(_random.nextInt(nelectrodes_empty));

                while (data.keySet().contains(channel)) {
                    channel = neighbors_list.get(_random.nextInt(nelectrodes_empty));
                }

                double noise = getChannel(channel).computeNoise(electrodes.getCapacitance(channel));

//                System.out.println("        noise: "+noise);
//                System.out.println("        Gaussian above threshold: "+drawGaussianAboveThreshold(integral));

                // Throw Gaussian above threshold
                int charge = (int) Math.round(drawGaussianAboveThreshold(integral) * noise);
                data.add(channel, new SiElectrodeData(charge));
            }

        }

    }

    private SortedMap<Integer, List<Integer>> digitize(SiElectrodeDataCollection data, SiSensorElectrodes electrodes) {
        SortedMap<Integer, List<Integer>> chip_data = new TreeMap<Integer, List<Integer>>();
        for (Integer channel : data.keySet()) {
            KpixChannel.ReadoutRegisters readout_registers = getChannel(channel).computeReadoutRegisters(data.get(channel));
            if (readout_registers.getAdcValue() == 0) // supress readout of zeros
            {
                continue;
            } else {
                List<Integer> channel_data = new ArrayList<Integer>();
                channel_data.add(getControlRegisters().encoded());
                channel_data.add(readout_registers.encoded());
                chip_data.put(channel, channel_data);
            }
        }
        return chip_data;
    }

    public static double normalCDF(double normalized_integration_limit) {
        double integral = 0;
        try {
            integral = (1.0 - Erf.erf(normalized_integration_limit / Math.sqrt(2.0))) / 2.0;
        } catch (MathException no_convergence) {
            System.out.println("Warning: erf fails to converge!! ");
            System.out.println("    normalized integration limit: " + normalized_integration_limit);
        }
        return integral;
    }

    public static int drawBinomial(int ntrials, double probability) {
        _binomial.setNumberOfTrials(ntrials);
        _binomial.setProbabilityOfSuccess(probability);

        int nsuccess = 0;
        try {
            nsuccess = _binomial.inverseCumulativeProbability(_random.nextDouble());
        } catch (MathException exception) {
            throw new RuntimeException("Kpix failed to calculate inverse cumulative probability of binomial!");
        }
        return nsuccess;
    }

    /**
     * Return a random variable following normal distribution, but beyond
     * threshold provided during initialization.
     */
    public static double drawGaussianAboveThreshold(double prob_above_threshold) {
        double draw, cumulative_probability;

        draw = prob_above_threshold * _random.nextDouble();
        cumulative_probability = 1.0 - prob_above_threshold + draw;

        assert cumulative_probability < 1.0 : "cumulProb=" + cumulative_probability + ", draw=" + draw + ", probAboveThreshold=" + prob_above_threshold;
        assert cumulative_probability >= 0.0 : "cumulProb=" + cumulative_probability + ", draw=" + draw + ", probAboveThreshold=" + prob_above_threshold;

        double gaussian_random = 0;
        try {
            gaussian_random = _gaussian.inverseCumulativeProbability(cumulative_probability);
        } catch (MathException e) {
            System.out.println("MathException caught: " + e);
        }

        return gaussian_random;
    }

//==========================================================================
// KpixChannel - Class representing a single Kpix channel
//==========================================================================
    private static class KpixChannel implements ReadoutChannel {

        private static final double NORMAL_GAIN_CAP = 400E-15; // 400 fF
        private static final double DOUBLE_GAIN_CAP = 200E-15; // 200 fF
        private static final double LOW_GAIN_CAP = 10E-12; // 10pF
        private static final double ADC_GAIN = 2500; // count/V
        private static final double NOISE_INTERCEPT = 300; // electrons
        private static final double NOISE_SLOPE = 30; // electrons

//        private static final double NOISE_INTERCEPT = 0; // electrons
//        private static final double NOISE_SLOPE = 0; // electrons
        private static class ReadoutRegisters {

            enum GainRange {

                NORMAL, LOW
            }
            private GainRange _gain_range = GainRange.NORMAL;
            private int _buffer_number = 0;
            private int _time = 0;
            private int _adc_value = 0;

            public ReadoutRegisters() {
            }

            private void setGainRange(GainRange gain_range) {
                _gain_range = gain_range;
            }

            private void setBufferNumber(int buffer_number) {
                _buffer_number = buffer_number;
            }

            private void setTime(int time) {
                _time = time;
            }

            private void setAdcValue(int adc_value) {
                _adc_value = adc_value;
            }

            private GainRange getGainRange() {
                return _gain_range;
            }

            private int getBufferNumber() {
                return _buffer_number;
            }

            private int getTime() {
                return _time;
            }

            private int getAdcValue() {
                return _adc_value;
            }

            private int encoded() {
                return ((((getGainRange().ordinal() << 12 | getBufferNumber()) << 8) | getTime()) << 8) | getAdcValue();
            }

            private static ReadoutRegisters decoded(int readout) {
                ReadoutRegisters registers = new ReadoutRegisters();
                int adc_value = readout & 0xFF;
                int time = (readout & 0xFF00) >> 8;
                int buffer_number = (readout & 0xFFF0000) >> 16;
                int gain_range = (readout & 0x10000000) >> 28;

                registers.setAdcValue(adc_value);
                registers.setTime(time);
                registers.setBufferNumber(buffer_number);
                registers.setGainRange(GainRange.values()[gain_range]);

                return registers;
            }
        }
        ControlRegisters _control_registers;

        /** Creates a new instance of KpixChannel */
        public KpixChannel(ControlRegisters control_registers) {
            _control_registers = control_registers;
        }

        // ReadoutChannel subinterface
        public double computeNoise(double capacitance) {
            return NOISE_INTERCEPT + capacitance * NOISE_SLOPE;
        }

        // Internal stuff - all private
        private ReadoutRegisters computeReadoutRegisters(SiElectrodeData data) {
            ReadoutRegisters registers = new ReadoutRegisters();
            registers.setTime(computeTime());
            registers.setBufferNumber(computeBufferNumber());
            registers.setGainRange(computeGainRange(data));
            registers.setAdcValue(computeAdcValue(data, registers));

            return registers;
        }

        private int computeTime() {
            return 0; // return all hits on bunch crossing 0 for now
        }

        private int computeBufferNumber() {
            return 0; // return all hits in first buffer for now
        }

        private ReadoutRegisters.GainRange computeGainRange(SiElectrodeData data) {
            if (data.getCharge() * computeNormalFEGain(_control_registers) < _control_registers.getGainCrossover()) {
                return ReadoutRegisters.GainRange.NORMAL;
            } else {
                return ReadoutRegisters.GainRange.LOW;
            }
        }

        private int computeAdcValue(SiElectrodeData data, ReadoutRegisters readout_registers) {
            double gain = computeGain(readout_registers, _control_registers);
            return (int) Math.floor(data.getCharge() * gain);
        }

        private static double computeGain(ReadoutRegisters readout_registers, ControlRegisters control_registers) {
            if (readout_registers.getGainRange() == ReadoutRegisters.GainRange.NORMAL) {
                return computeNormalFEGain(control_registers) * ADC_GAIN;
            } else {
                return computeLowFEGain(control_registers) * ADC_GAIN;
            }
        }

        private static double computeNormalFEGain(ControlRegisters control_registers) {
            double feedback_cap;
            if (control_registers.getGainMode() == ControlRegisters.GainMode.SINGLE) {
                feedback_cap = NORMAL_GAIN_CAP;
            } else {
                feedback_cap = DOUBLE_GAIN_CAP;
            }
            return SystemOfUnits.e_SI / feedback_cap;
        }

        private static double computeLowFEGain(ControlRegisters control_registers) {
            double feedback_cap;
            if (control_registers.getGainMode() == ControlRegisters.GainMode.SINGLE) {
                feedback_cap = LOW_GAIN_CAP + NORMAL_GAIN_CAP;
            } else {
                feedback_cap = LOW_GAIN_CAP + DOUBLE_GAIN_CAP;
            }
            return SystemOfUnits.e_SI / feedback_cap;
        }
    }
}
