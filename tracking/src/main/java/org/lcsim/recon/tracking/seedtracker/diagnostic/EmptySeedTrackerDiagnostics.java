/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.diagnostic;

import java.util.List;
import java.util.Set;
import org.lcsim.event.MCParticle;
import org.lcsim.fit.helicaltrack.HelicalTrackFitter;
import org.lcsim.recon.tracking.seedtracker.SeedCandidate;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;

/**
 * This is the class you probably want to extend if you want to create your own diagnostics.
 * As it extends from AbstractSeedTrackerDiagnostics, it already implements some common methods, 
 * so it is different from NullDiagnostics. 
 * 
 * @author cozzy
 */
public class EmptySeedTrackerDiagnostics extends AbstractSeedTrackerDiagnostics implements ISeedTrackerDiagnostics {

    public void fireCheckHitPairFailed(HelicalTrackHit hit1, HelicalTrackHit hit2) {return;}
    public void fireCheckHitTripletFailed(HelicalTrackHit hit1, HelicalTrackHit hit2, HelicalTrackHit hit3) {return;}
    public void fireCheckHitFailed(HelicalTrackHit hit, SeedCandidate seed) {return;}
    public void fireFinderDone(List<SeedCandidate> trkseeds, Set<MCParticle> mcp_seeds) {return;}
    public void fireFailedChisqCut(SeedCandidate seed) {return;}
    public void fireHelixFitFailed(SeedCandidate seed, HelicalTrackFitter.FitStatus status, boolean firstfit) {return;}
    public void fireMergeKillingNewSeed(SeedCandidate seed, SeedCandidate newseed) {return;}
    public void fireMergeKillingOldSeed(SeedCandidate seed, SeedCandidate newseed) {return;}

}
