/*
 * Class BasicReadoutChip
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
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.math.probability.Erf;
import org.lcsim.recon.tracking.digitization.sisim.ReadoutChip.ReadoutChannel;

/**
 * Basic readout chip class.  This class supports the minimal functions expected of
 * a readout chip.  The charge on a strip/pixel is digitized as an integer number
 * with a simple ADC with programmable resolution and dynamic range.  A chip with
 * 1-bit ADC resolution (binary readout) is treated as a special case.
 * 
 * Noise is added to strips with charge and random noise hits are generated as well.
 * Methods are provided to decode the charge and time (although the current
 * implementation always returns a time of 0).
 *
 * This implementation has thresholds that are settable in units of RMS noise of
 * each channel to enable simluation of highly optimized readout chains.  If
 * absolute thresholds are desired, GenericReadoutChip should be used instead.
 *
 * @author Tim Nelson
 */
public class BasicReadoutChip implements ReadoutChip
{

    private static Random _random = new Random();
    private static NormalDistribution _gaussian = new NormalDistributionImpl(0.0, 1.0);
    private static BinomialDistribution _binomial = new BinomialDistributionImpl(1, 1);
    private double _noise_threshold = 4;
    private double _neighbor_threshold = 4;
    private BasicChannel _channel = new BasicChannel();
    private ADC _adc = new ADC();

    /** Creates a new instance of BasicReadoutChip */
    public BasicReadoutChip()
    {
    }

    /**
     * Set the noise intercept (i.e., the noise for 0 strip/pixel capacitance).
     * Units are electrons of noise.
     *
     * @param noise_intercept noise for 0 capacitance
     */
    public void setNoiseIntercept(double noise_intercept)
    {
        _channel.setNoiseIntercept(noise_intercept);
    }

    /**
     * Set the noise slope (i.e., the proportionality between noise and capacitance).
     * Units are electrons of noise per fF of capacitance.
     *
     * @param noise_slope noise slope per unit capacitance
     */
    public void setNoiseSlope(double noise_slope)
    {
        _channel.setNoiseSlope(noise_slope);
    }

    /**
     * Set the threshold for reading out a channel.  Units are multiples of RMS noise.
     * 
     * @param noise_threshold
     */
    public void setNoiseThreshold(double noise_threshold)
    {
        _noise_threshold = noise_threshold;
    }

    /**
     * Set the threshold for reading a channel if its neighbor is
     * above the noise threshold.  Units are multiples of RMS noise.
     *
     * @param neighbor_threshold
     */
    public void setNeighborThreshold(double neighbor_threshold)
    {
        _neighbor_threshold = neighbor_threshold;
    }

    /**
     * Set the number of bits of ADC resolution
     *
     * @param nbits
     */
    public void setNbits(int nbits)
    {
        getADC().setNbits(nbits);
    }

    /**
     * Set the dynamic range of the ADC
     *
     * @param dynamic_range in fC
     */
    public void setDynamicRange(double dynamic_range)
    {
        getADC().setDynamicRange(dynamic_range);
    }

    /**
     * Return the BasicChannel associated with a given channel number.
     * For the basic readout, there is a single instance of BasicChannel
     * and thus the channel number is ignored.
     *
     * @param channel_number channel number
     * @return associated BasicReadoutChannel
     */
    public BasicChannel getChannel(int channel_number)
    {
        return _channel;
    }

    private ADC getADC()
    {
        return _adc;
    }

    /**
     * Given a collection of electrode data (i.e., charge on strips/pixels),
     * return a map associating the channel and it's list of raw data.
     *
     * @param data  electrode data from the charge distribution
     * @param electrodes  strip or pixel electrodes
     * @return  map containing the ADC counts for this sensor
     */
    public SortedMap<Integer, List<Integer>> readout(SiElectrodeDataCollection data, SiSensorElectrodes electrodes)
    {

        //  If there is no electrode data for this readout chip,  create an empty
        //  electrode data collection
        if (data == null)
        {
            data = new SiElectrodeDataCollection();
        }

        //  Add noise hits to the electrode data collection
        addNoise(data, electrodes);

        //  return the digitized charge data as a map that associates a hit
        //  channel with a list of raw data for the channel
        return digitize(data, electrodes);
    }

    /**
     * Decode the hit charge stored in the RawTrackerHit
     *
     * @param hit raw hit
     * @return hit charge in units of electrons
     */
    public double decodeCharge(RawTrackerHit hit)
    {
        return getADC().decodeCharge(hit.getADCValues()[0]);
    }

    /**
     * Decode the hit time.  Currently, the basic readout chip ignores the
     * hit time and returns 0.
     *
     * @param hit raw hit data
     * @return hit time
     */
    public int decodeTime(RawTrackerHit hit)
    {
        return 0;
    }

    /**
     * Digitizes the hit channels in a SiElectrodeDataCollection.
     *
     * The SiElectrodeDataCollection is a map that associates a given channel with
     * it's SiElectrodeData.  The SiElectrodeData encapsulates the deposited charge
     * on an strip/pixel and any associated SimTrackerHits.
     *
     * The output of this class is a map that associates a channel number with
     * a list of raw data
     *
     * @param data electrode data collection
     * @return map associating channels with a list of raw data
     */
    private SortedMap<Integer, List<Integer>> digitize(SiElectrodeDataCollection data,
                                                       SiSensorElectrodes electrodes)
    {

        //  Create the map that associates a given sensor channel with it's list of raw data
        SortedMap<Integer, List<Integer>> chip_data = new TreeMap<Integer, List<Integer>>();

        //  Loop over the channels contained in the SiElectrodeDataCollection
        for (Integer channel : data.keySet())
        {

            //  Fetch the electrode data for this channel
            SiElectrodeData eldata = data.get(channel);

            //  Get the charge in units of electrons
            double charge = eldata.getCharge();

            // Get the noise RMS in units of electrons
            double noiseRMS = getChannel(channel).computeNoise(electrodes.getCapacitance(channel));

            //  If the charge is below the neighbor threshold, don't digitize it
            if (charge < _neighbor_threshold*noiseRMS)
            {
                continue;
            }

            //  If charge is between neighbor and noise thresholds, check it's neighbors
            if (charge < _noise_threshold*noiseRMS)
            {

                //  Loop over neighbors and look for a neighbor with charge above the noise
                boolean nbrhit = false;
                for (Integer nbr : electrodes.getNearestNeighborCells(channel))
                {

                    //  See if we have electrode data for this neighbor
                    SiElectrodeData nbrdata = data.get(nbr);
                    if (nbrdata == null)
                    {
                        continue;
                    }

                    //  See if we have found a neigbor above the noise threshold
                    if (nbrdata.getCharge() >= _noise_threshold)
                    {
                        nbrhit = true;
                        break;
                    }
                }

                //  If there were no neighbor channels above threshold, don't digitize it
                if (!nbrhit)
                {
                    continue;
                }
            }

            //  Calculate the ADC value for this channel and make sure it is positive
            int adc = getADC().convert(charge);
            if (adc <= 0)
            {
                continue;
            }

            //  Create a list containing the adc value - for the basic readout
            //  there is only 1 word of raw data
            List<Integer> channel_data = new ArrayList<Integer>();
            channel_data.add(adc);

            //  Save the list of raw data in the chip_data map
            chip_data.put(channel, channel_data);
        }

        return chip_data;
    }

    /**
     * Add noise hits for this readout chip
     *
     * @param data electrode data collection
     * @param electrodes strip or pixel electrodes
     */
    private void addNoise(SiElectrodeDataCollection data, SiSensorElectrodes electrodes)
    {

        //  First add noise to the strips/pixels in the SiElectrodeDataCollection
        //  Loop over the entries in the SiElectrodeDataCollection (which extends TreeMap)
        for (Entry datum : data.entrySet())
        {

            //  Get the channel number and electrode data for this entry
            int channel = (Integer) datum.getKey();
            SiElectrodeData eldata = (SiElectrodeData) datum.getValue();

            //  Get the RMS noise for this channel in units of electrons
            double noise = getChannel(channel).computeNoise(electrodes.getCapacitance(channel));

            //  Add readout noise to the deposited charge
            int noise_charge = (int) Math.round(_random.nextGaussian() * noise);
            eldata.addCharge(noise_charge);
        }

        //  Add random noise hits where the noise charge exceeds the noise threshold

        //  Find the number of pixels/strips that are not currently hit
        int nelectrodes = electrodes.getNCells();
        int nelectrodes_empty = nelectrodes - data.size();

        //  Get the noise threshold in units of the noise charge
        double normalized_integration_limit = _noise_threshold;

        //  Calculate how many channels should get noise hits
        double integral = Erf.phic(normalized_integration_limit);
        int nchannels_throw = drawBinomial(nelectrodes_empty, integral);

        // Now throw Gaussian randoms above the seed threshold and put signals on unoccupied channels
        for (int ithrow = 0; ithrow < nchannels_throw; ithrow++)
        {
            // Throw to get a channel number
            int channel = _random.nextInt(nelectrodes);
            while (data.keySet().contains(channel))
            {
                channel = _random.nextInt(nelectrodes);
            }

            //  Calculate the noise for this channel in units of electrons
            double noise = getChannel(channel).computeNoise(electrodes.getCapacitance(channel));

            // Throw Gaussian above threshold
            int charge = (int) Math.round(drawGaussianAboveThreshold(integral) * noise);
            data.add(channel, new SiElectrodeData(charge));
        }

        // Now throw to lower threshold on channels that neighbor hits until we are exhausted
        //-----------------------------------------------------------------------------------
        nchannels_throw = 1;
        while (nchannels_throw > 0)
        {
            //            System.out.println("\n"+"Throw nieghbors...");

            // Get neighbor channels
            Set<Integer> neighbors = new HashSet<Integer>();
            for (int channel : data.keySet())
            {
                neighbors.addAll(electrodes.getNearestNeighborCells(channel));
            }
            neighbors.removeAll(data.keySet());

            nelectrodes_empty = neighbors.size();

            //  Get the noise threshold in units of the noise charge
            normalized_integration_limit = _neighbor_threshold;

            integral = Erf.phic(normalized_integration_limit);
            nchannels_throw = drawBinomial(nelectrodes_empty, integral);

            // Now throw Gaussian randoms above a threshold and put signals on unoccupied channels
            for (int ithrow = 0; ithrow < nchannels_throw; ithrow++)
            {
                // Throw to get a channel number
                List<Integer> neighbors_list = new ArrayList<Integer>(neighbors);

                int channel = neighbors_list.get(_random.nextInt(nelectrodes_empty));

                while (data.keySet().contains(channel))
                {
                    channel = neighbors_list.get(_random.nextInt(nelectrodes_empty));
                }

                double noise = getChannel(channel).computeNoise(electrodes.getCapacitance(channel));


                // Throw Gaussian above threshold
                int charge = (int) Math.round(drawGaussianAboveThreshold(integral) * noise);
                data.add(channel, new SiElectrodeData(charge));
            }

        }

    }

    public static int drawBinomial(int ntrials, double probability)
    {
        _binomial.setNumberOfTrials(ntrials);
        _binomial.setProbabilityOfSuccess(probability);

        int nsuccess = 0;
        try
        {
            nsuccess = _binomial.inverseCumulativeProbability(_random.nextDouble());
        } catch (MathException exception)
        {
            throw new RuntimeException("BasicReadoutChip failed to calculate inverse cumulative probability of binomial!");
        }
        return nsuccess;
    }

    /**
     * Return a random variable following normal distribution, but beyond
     * threshold provided during initialization.
     */
    public static double drawGaussianAboveThreshold(double prob_above_threshold)
    {
        double draw, cumulative_probability;

        draw = prob_above_threshold * _random.nextDouble();
        cumulative_probability = 1.0 - prob_above_threshold + draw;

        assert cumulative_probability < 1.0 : "cumulProb=" + cumulative_probability + ", draw=" + draw + ", probAboveThreshold=" + prob_above_threshold;
        assert cumulative_probability >= 0.0 : "cumulProb=" + cumulative_probability + ", draw=" + draw + ", probAboveThreshold=" + prob_above_threshold;

        double gaussian_random = 0;
        try
        {
            gaussian_random = _gaussian.inverseCumulativeProbability(cumulative_probability);
        } catch (MathException e)
        {
            System.out.println("MathException caught: " + e);
        }

        return gaussian_random;
    }

    /**
     * BasicChannel class representing a single channel's behavior
     *
     * Note that binary readout is a special case.  Anything positive value
     * passed to a binary ADC for digitization is assumed to have crossed t
     * hreshold and is assigned a value of 1.  Decoding binary readout results
     * in either 0 or dynamic_range.
     */
    private class BasicChannel implements ReadoutChannel
    {

        private double _noise_intercept = 0.;
        private double _noise_slope = 0.;

        /**
         * Set the noise (in electrons) for 0 capacitance
         *
         * @param noise_intercept noise intercept
         */
        private void setNoiseIntercept(double noise_intercept)
        {
            _noise_intercept = noise_intercept;
        }

        /**
         * Set the capacitative noise slope (in electrons / pF)
         *
         * @param noise_slope noise slope
         */
        private void setNoiseSlope(double noise_slope)
        {
            _noise_slope = noise_slope;
        }

        /**
         * Return the noise in electrons for a given strip/pixel capacitance
         *
         * @param capacitance capacitance in pF
         * @return noise in electrons
         */
        public double computeNoise(double capacitance)
        {
            return _noise_intercept + _noise_slope * capacitance;
        }
    }

    /**
     * ADC class representing analog to digital converter.
     */
    private class ADC
    {

        private int _nbits = 8;
        private double _dynamic_range = 20.;

        /**
         * Set the ADC resolution in number of bits.
         *
         * @param nbits number of bits
         */
        private void setNbits(int nbits)
        {
            _nbits = nbits;
        }

        /**
         * Set the dynamic range in fC
         *
         * @param dynamic range
         */
        private void setDynamicRange(double dynamic_range)
        {
            _dynamic_range = dynamic_range;
        }

        /**
         * Compute the maximum ADC value
         *
         * @return largest possible ADC value according to # of bits
         */
        private int maxADCValue()
        {
            return (int) Math.pow(2, _nbits) - 1;
        }

        /**
         * Compute the conversion constant in ADC/fC
         *
         * @return conversion constant for ADC
         */
        private double conversionConstant()
        {
            return maxADCValue() / _dynamic_range;
        }

        /**
         * Perform analog to digital conversion
         *
         * @return digital ADC output between 0 and maxADCValue
         */
        public int convert(double charge)
        {
            if (_nbits != 1)
            {
                return Math.max(0, Math.min(maxADCValue(), (int) Math.floor(charge * 1.602e-4 * conversionConstant())));
            }
            else
            {
                if (charge <= 0.0)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
        }

        /**
         * Decode charge from ADC value
         *
         * @return charge specified by a given ADC value
         */
        public double decodeCharge(int adc_value)
        {
            if (_nbits != 1)
            {
                return (adc_value + 0.5) / (1.602e-4 * conversionConstant());
            }
            else
            {
                return adc_value*_dynamic_range;
            }

        }
    }
}
