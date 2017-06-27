/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;

/**
 *
 * @author cozzy
 */
public class AllPassFilter implements IParticleFilter {

    public boolean passes(MCParticle p) {
        return true; 
    }

    public void setEvent(EventHeader event){
        return; 
    }
}
