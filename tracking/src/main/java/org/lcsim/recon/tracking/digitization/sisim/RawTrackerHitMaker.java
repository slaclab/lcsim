/*
 * RawTrackerHitMaker.java
 *
 * Created on January 17, 2008, 11:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IReadout;
import org.lcsim.detector.tracker.silicon.ChargeCarrier;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.base.BaseRawTrackerHit;

/**
 *
 * @author tknelson
 */
public class RawTrackerHitMaker implements SiDigitizer
{
    private static String _NAME = "RawTrackerHitMaker";
    
    private SiSensorSim _si_simulation;
    private ReadoutChip _readout_chip;
    
    Class _input_type = SimTrackerHit.class;
    Class _output_type = RawTrackerHit.class;
    
    /**
     * Creates a new instance of RawTrackerHitMaker
     */
    public RawTrackerHitMaker(SiSensorSim si_simulation, ReadoutChip readout_chip)
    {
        _si_simulation = si_simulation;
        _readout_chip = readout_chip;
    }
    
    public String getName()
    {
        return _NAME;
    }
    
    public List<RawTrackerHit> makeHits(IDetectorElement detector)
    {
        List<RawTrackerHit> raw_hits = new ArrayList<RawTrackerHit>();
        List<SiSensor> sensors = detector.findDescendants(SiSensor.class);

        // Loop over all sensors
        for (SiSensor sensor : sensors)
        {
            raw_hits.addAll(makeHits(sensor));
        }
        
        // Return hit list
        return raw_hits;
    }
    
    
    public List<RawTrackerHit> makeHits(SiSensor sensor)
    {
        
        List<RawTrackerHit> raw_hits = new ArrayList<RawTrackerHit>();
        
        // Get SimTrackerHits
        IReadout ro = sensor.getReadout();
        List<SimTrackerHit> sim_hits = ro.getHits(SimTrackerHit.class);
        
//        if (sim_hits.size() != 0)
//        {
//            System.out.println("# SimTrackerHits: "+sim_hits.size());
//        }
        
        // Perform charge deposition simulation
        _si_simulation.setSensor(sensor);
        Map<ChargeCarrier,SiElectrodeDataCollection> electrode_data = _si_simulation.computeElectrodeData();

        // Loop over electrodes and digitize with readout chip
        for (ChargeCarrier carrier : ChargeCarrier.values())
        {
            if (sensor.hasElectrodesOnSide(carrier))
            {
                SortedMap<Integer,List<Integer>> digitized_hits = _readout_chip.readout(electrode_data.get(carrier),sensor.getReadoutElectrodes(carrier));

                // Make RawTrackerHits
                for (Integer readout_cell : digitized_hits.keySet())
                {
                    int time = 0;
                    long cell_id = sensor.makeStripId(readout_cell,carrier.charge()).getValue();

                    //  Retrieve the list of integer data from the readout chip and store as an array of shorts
                    List<Integer> readout_data = digitized_hits.get(readout_cell);
                    short[] adc_values = new short[readout_data.size()];
                    for (int i=0; i<readout_data.size(); i++) {
                        adc_values[i] = readout_data.get(i).shortValue();
                    }
                    
                    Set<SimTrackerHit> simulated_hits = electrode_data.get(carrier).get(readout_cell).getSimulatedHits();
                    IDetectorElement detector_element = sensor;
                    
                    RawTrackerHit raw_hit = new BaseRawTrackerHit(time,cell_id,adc_values,new ArrayList<SimTrackerHit>(simulated_hits),detector_element);
                    
                    // Put hits onto readout and hit list
                    ro.addHit(raw_hit);
                    raw_hits.add(raw_hit);
                }
            }
            
        }
        
        _si_simulation.clearReadout();
        
//        if (sim_hits.size() != 0)
//        {
//            System.out.println("# RawTrackerHits: "+raw_hits.size());
//        }
        
        return raw_hits;
    }
    
    
}

