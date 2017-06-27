/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.diagnostic;

import org.lcsim.event.MCParticle;
import org.lcsim.fit.helicaltrack.HelicalTrackFitter;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.recon.tracking.seedtracker.SeedCandidate;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;
import org.lcsim.util.aida.AIDA;

/**
 *
 * @author cozzy
 */
public class SeedTrackerDiagnostics extends EmptySeedTrackerDiagnostics implements ISeedTrackerDiagnostics{

    AIDA aida = AIDA.defaultInstance();     
    private String prefix = ""; 
    
    public SeedTrackerDiagnostics(){
    }

    @Override
    public void fireCheckHitPairFailed(HelicalTrackHit hit1, HelicalTrackHit hit2) {
        for (MCParticle mcp : hit1.getMCParticles()) {
            if (hit2.getMCParticles().contains(mcp)) {
                System.out.println("Hits from same MC particle failed hit pair check");
                System.out.println("MC momentum: "+mcp.getMomentum().toString());
                System.out.println("Hit 1 position: "+hit1.getCorrectedPosition().toString());
                System.out.println("Hit 2 position: "+hit2.getCorrectedPosition().toString());
            }
        }
    }

    @Override
    public void fireCheckHitTripletFailed(HelicalTrackHit hit1, HelicalTrackHit hit2, HelicalTrackHit hit3) {
        for (MCParticle mcp : hit1.getMCParticles()) {
            if (hit2.getMCParticles().contains(mcp)) {
                if (hit3.getMCParticles().contains(mcp)) {
                    System.out.println("Hits from same MC particle failed hit triplet check");
                    System.out.println("MC momentum: "+mcp.getMomentum().toString());
                    System.out.println("Hit 1 position: "+hit1.getCorrectedPosition().toString());
                    System.out.println("Hit 2 position: "+hit2.getCorrectedPosition().toString());
                    System.out.println("Hit 3 position: "+hit3.getCorrectedPosition().toString());
               }
            }
        }
    }


    @Override
    public void fireCheckHitFailed(HelicalTrackHit hit, SeedCandidate seed) {
        if (seed.isTrueSeed()) {
            printmsg(seed, "True seed failed hit and seed check");
            System.out.println("Hit position: "+hit.getCorrectedPosition().toString());
            for (HelicalTrackHit ihit : seed.getHits()) {
                System.out.println("Seed hit position: "+ihit.getCorrectedPosition().toString());
            }
        }
    }

    @Override
    public void fireHelixFitFailed(SeedCandidate seed, HelicalTrackFitter.FitStatus status, boolean firstfit) {
        if (firstfit) printmsg(seed, "Initial fit of true seed failed");
        else printmsg(seed, "Helix fit of true seed failed with "+seed.getHits().size()+" hits");
    }

    @Override
    public void fireFailedChisqCut(SeedCandidate seed) {
        printmsg(seed, "True seed failed chisq cut - chisq = "+seed.getHelix().chisqtot());
    }

    @Override
    public void fireMergeKillingNewSeed(SeedCandidate seed, SeedCandidate newseed) {
         if (!seed.isTrueSeed() && newseed.isTrueSeed())
            printmsg(seed, "Merge keeping false seed instead of new true seed");
         if (seed.isTrueSeed() && newseed.isTrueSeed()) {
             if (newseed.getHits().size() > seed.getHits().size()) {
                 printmsg(seed, "Merge killing new true seed with more hits than old true seed");
                 System.out.println("New seed has "+newseed.getHits().size()
                         +" hits, old seed has "+seed.getHits().size()+" hits");
                 System.out.println("New seed has chisq = "+newseed.getHelix().chisqtot()+
                         ", old seed has chisq = "+seed.getHelix().chisqtot());
             }
         }
    }

    @Override
    public void fireMergeKillingOldSeed(SeedCandidate seed, SeedCandidate newseed) {
        if (seed.isTrueSeed() && !newseed.isTrueSeed())
            printmsg(seed, "Merge eliminating true seed duplicate in favor of false seed");
         if (seed.isTrueSeed() && newseed.isTrueSeed()) {
             if (newseed.getHits().size() < seed.getHits().size()) {
                 printmsg(seed, "Merge killing old true seed with more hits than new true seed");
                 System.out.println("New seed has "+newseed.getHits().size()
                         +" hits, old seed has "+seed.getHits().size()+" hits");
                 System.out.println("New seed has chisq = "+newseed.getHelix().chisqtot()+
                         ", old seed has chisq = "+seed.getHelix().chisqtot());
             }
         }
    }

    /**
     * Sets the prefix to be appended to the beginning of each plot (for example, a strategy name
     * @param str the new prefix
     */
    public void setPrefix(String str){
        this.prefix = str; 
    }
    
    @Override
    public void fireStrategyChanged(SeedStrategy strategy) {
        setPrefix("chisq cut = "+strategy.getMaxChisq()+"/");
        super.fireStrategyChanged(strategy);
    }

    private void printmsg(SeedCandidate seed, String message) {
        if (!seed.isTrueSeed()) return;
        for (MCParticle mcp : seed.getMCParticles()) {
            System.out.println(message+" p: "+mcp.getMomentum().toString());
        }
    }
}