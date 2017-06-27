/*
 * ResolutionModel.java
 *
 * Created on February 15, 2008, 6:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;

/**
 *
 * @author tknelson
 */
public interface ResolutionModel
{
    
    double calculateResolution(int iaxis, SiTrackerHit hit);
    
}
