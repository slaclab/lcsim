/*
 * StripResolutionModel.java
 *
 * Created on February 15, 2008, 6:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

/**
 *
 * @author tknelson
 */
public class StripResolutionModel implements ResolutionModel
{
    
    /** Creates a new instance of StripResolutionModel */
    public StripResolutionModel()
    {
    }
    
    public double calculateResolution(int iaxis, SiTrackerHit hit)
    {
        
        return 0;
    }
    
}
