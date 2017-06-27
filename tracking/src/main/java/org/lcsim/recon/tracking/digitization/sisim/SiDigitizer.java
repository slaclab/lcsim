/*
 * SiDigitizer.java
 *
 * Created on April 2, 2008, 6:50 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import java.util.List;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.event.RawTrackerHit;

/**
 *
 * @author tknelson
 */
public interface SiDigitizer
{
    public String getName();
    
    public List<RawTrackerHit> makeHits(IDetectorElement detector);
    
    public List<RawTrackerHit> makeHits(SiSensor sensor);
}
