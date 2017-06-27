/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.diagnostic;

import java.util.List;
import java.util.Set;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.fit.helicaltrack.HelicalTrackFitter;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.recon.tracking.seedtracker.HitManager;
import org.lcsim.recon.tracking.seedtracker.MaterialManager;
import org.lcsim.recon.tracking.seedtracker.SeedCandidate;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;

/**
 *
 * @author cozzy
 */
public interface ISeedTrackerDiagnostics {
      
    /**
     * SeedTracker will call this method with the event's bField
     * @param bField the magnetic field, in Teslas 
     */
    public void setBField(double bField);
    
    /**
     * SeedTracker will call this method with the current event
     * @param event the current event
     */
    public void setEvent(EventHeader event);
    
    /**
     * SeedTracker will call this method with t
     * @param hm the HitManager
     */
    public void setHitManager(HitManager hm);
    
    /**SeedTracker will pass the MaterialManager with this method
     * 
     * @param mm the MaterialManager
     */
    public void setMaterialManager(MaterialManager mm);
    
    /**
     * Fired whenever a new strategy is employed and passes the strategy. 
     * @param strategy
     */
    public void fireStrategyChanged(SeedStrategy strategy);

    public void fireCheckHitPairFailed(HelicalTrackHit hit1, HelicalTrackHit hit2);

    public void fireCheckHitTripletFailed(HelicalTrackHit hit1, HelicalTrackHit hit2, HelicalTrackHit hit3);
    
    public void fireCheckHitFailed(HelicalTrackHit hit, SeedCandidate seed);

    public void fireHelixFitFailed(SeedCandidate seed, HelicalTrackFitter.FitStatus status, boolean firstfit);

    public void fireFailedChisqCut(SeedCandidate seed);

    public void fireMergeKillingNewSeed(SeedCandidate seed, SeedCandidate newseed);

    public void fireMergeKillingOldSeed(SeedCandidate seed, SeedCandidate newseed);
    
    /**
     * This is fired at the end of the finding routine
     * @param trackseeds
     * @param mcp_seeds
     */
    public void fireFinderDone(List<SeedCandidate> trackseeds, Set<MCParticle> mcp_seeds);

}
