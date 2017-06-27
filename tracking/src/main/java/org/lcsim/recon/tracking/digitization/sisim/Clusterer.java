/*
 * Clusterer.java
 *
 * Created on April 2, 2008, 6:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import java.util.List;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.event.RawTrackerHit;

/**
 *
 * @author tknelson
 */
public interface Clusterer
{
    public String getName();
    
    public List<SiTrackerHit> makeHits(IDetectorElement detector);
    
    public List<SiTrackerHit> makeHits(SiSensor sensor);
    
    public List<SiTrackerHit> makeHits(SiSensor sensor, SiSensorElectrodes electrodes, List<RawTrackerHit> hits);
}
