package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.vec.Hep3Vector;

import java.util.List;
import java.util.Map;

import org.lcsim.detector.tracker.silicon.ChargeCarrier;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.event.SimTrackerHit;

/**
 *
 * @author tknelson
 */
public interface SiSensorSim
{
    
    // Set sensor to process
    void setSensor(SiSensor sensor);
    
    // Set the list of hits to process
    void setHits(List<SimTrackerHit> hits);
    
    // Process hits and produce electrode data
    Map<ChargeCarrier,SiElectrodeDataCollection> computeElectrodeData();
    
    // clear readout strips
    void clearReadout();
    
    // Correct position to centerplane of active sensor
    void lorentzCorrect(Hep3Vector position, ChargeCarrier carrier);
    
}
