/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.diagnostic;

import org.lcsim.event.EventHeader;
import org.lcsim.recon.tracking.seedtracker.HitManager;
import org.lcsim.recon.tracking.seedtracker.MaterialManager;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;

/**
 *This class defines some common functionality that most classes implementing ISeedTrackerDiagnostics would want
 * @author cozzy
 */
public abstract class AbstractSeedTrackerDiagnostics {

    protected SeedStrategy currentStrategy; 
    protected double bField; 
    protected EventHeader event;
    protected HitManager hitManager;
    protected MaterialManager materialManager; 
    
    public void setBField(double bField) {
        this.bField=bField;
    }
    
    public void setEvent(EventHeader event) {
        this.event = event; 
    }
    
    public void setHitManager(HitManager hm){
        this.hitManager = hm;
    }
    
    public void setMaterialManager(MaterialManager mm){
        this.materialManager=mm;
    }
    
    public void fireStrategyChanged(SeedStrategy strategy){
        this.currentStrategy = strategy; 
    }
}
