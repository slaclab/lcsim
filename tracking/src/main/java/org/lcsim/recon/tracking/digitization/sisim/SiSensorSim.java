/*
 * SiSensorSim.java
 *
 * Created on May 9, 2007, 2:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.vec.Hep3Vector;
import java.util.Map;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.ChargeCarrier;

/**
 *
 * @author tknelson
 */
public interface SiSensorSim
{
    
    // Set sensor to process
    void setSensor(SiSensor sensor);
    
    // Process hits and produce electrode data
    Map<ChargeCarrier,SiElectrodeDataCollection> computeElectrodeData();
    
    // clear readout strips
    void clearReadout();
    
    // Correct position to centerplane of active sensor
    void lorentzCorrect(Hep3Vector position, ChargeCarrier carrier);
    
}
