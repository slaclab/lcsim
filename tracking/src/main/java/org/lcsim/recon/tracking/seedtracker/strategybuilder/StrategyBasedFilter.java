/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.fit.helicaltrack.HelixParamCalculator;

/**
 *A filter based on the cutoff values in a SeedStrategy. 
 * @author cozzy
 */
public class StrategyBasedFilter implements IParticleFilter {

    double pt; 
    double dca; 
    double z0; 
    double b;        
    Hep3Vector ip = new BasicHep3Vector(0., 0., 0.); 
    
    public StrategyBasedFilter(SeedStrategy strategy){
        pt = strategy.getMinPT();
        dca = strategy.getMaxDCA(); 
        z0 = strategy.getMaxZ0(); 
        
    }
    
   
    public boolean passes(MCParticle p) {
        HelixParamCalculator calc = new HelixParamCalculator(p, b); 
        return (
                Math.abs(calc.getDCA()) < dca  && 
                Math.abs(calc.getZ0()) < z0    &&
                calc.getMCTransverseMomentum() > pt
                );
    }
    
    public void setEvent(EventHeader event){
        b = event.getDetector().getFieldMap().getField(ip).z();
    }

}
