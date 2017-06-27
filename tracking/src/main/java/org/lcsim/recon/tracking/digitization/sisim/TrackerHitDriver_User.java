/*
 * TrackerHitDriver_User.java
 *
 * Created on April 4, 2008, 4:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import org.lcsim.util.Driver;

/**
 *
 * @author tknelson
 */
public class TrackerHitDriver_User extends Driver
{
    
    TrackerHitDriver _trackerhit_driver;
    
    /** Creates a new instance of TrackerHitDriver_User */
    public TrackerHitDriver_User()
    {
        _trackerhit_driver = new TrackerHitDriver();
        
        _trackerhit_driver.setReadout("SiTrackerBarrel_RO");
        _trackerhit_driver.setReadout("SiTrackerEndcap_RO");
        _trackerhit_driver.setReadout("SiTrackerForward_RO");
        //_trackerhit_driver.addReadout("SiVertexBarrel_RO");
        //_trackerhit_driver.addReadout("SiVertexEndcap_RO");
        _trackerhit_driver.addElementToProcess("SiTrackerBarrel");
        _trackerhit_driver.addElementToProcess("SiTrackerForward");
        _trackerhit_driver.addElementToProcess("SiTrackerEndcap");
        //_trackerhit_driver.addElementToProcess("SiVertexBarrel");
        //_trackerhit_driver.addElementToProcess("SiVertexEndcap");
        
        super.add( _trackerhit_driver );
    }
    
    // Collection names
    //-----------------
    public String getRawHitsName()
    {
        return _trackerhit_driver.getRawHitsName();
    }
    
    public String getStripHits1DName()
    {
        return _trackerhit_driver.getStripHits1DName();
    }
    
//    public String getPixelHitsName()
//    {
//        return _trackerhit_driver.getPixelHitsName();
//    }
    
    public String getStripHits2DName()
    {
        return _trackerhit_driver.getStripHits2DName();
    }
    
    
}
