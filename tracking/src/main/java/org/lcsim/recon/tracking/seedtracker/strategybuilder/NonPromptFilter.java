/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.fit.helicaltrack.HelixParamCalculator;

/**
 *
 * @author cozzy
 */
public class NonPromptFilter implements IParticleFilter {
    
    private EventHeader event; 
    private double minDist = 10.0; 
    private double minPT = 1.0; 
    public boolean passes(MCParticle p) {
        
        HelixParamCalculator calc = new HelixParamCalculator(p, event); 
        
        if (calc.getDCA() < minDist && calc.getZ0() < minDist) return false; 
        if (calc.getMCTransverseMomentum() < minPT) return false; 
        return true; 
        
    }
    
    public void setEvent(EventHeader event){
        this.event = event; 
    }

}
