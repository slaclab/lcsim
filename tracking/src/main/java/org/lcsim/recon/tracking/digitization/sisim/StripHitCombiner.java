/*
 * StripHitCombiner.java
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
import org.lcsim.detector.tracker.silicon.SiTrackerModule;

/**
 *
 * @author tknelson
 */
public interface StripHitCombiner
{
    public String getName();
    
    public List<SiTrackerHitStrip2D> makeHits(IDetectorElement detector);
    
    public List<SiTrackerHitStrip2D> makeHits(SiTrackerModule module);
    
    public List<SiTrackerHitStrip2D> makeHits(SiSensor sensor);   
}
