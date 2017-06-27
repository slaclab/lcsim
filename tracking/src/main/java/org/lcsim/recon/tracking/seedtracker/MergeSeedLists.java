/*
 * MergeSeedLists.java
 *
 * Created on February 6, 2008, 9:54 AM
 *
 */
package org.lcsim.recon.tracking.seedtracker;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.recon.tracking.seedtracker.diagnostic.ISeedTrackerDiagnostics;

/**
 *
 * @author Richard Partridge
 * @version 1.0
 */
public class MergeSeedLists {

    private ISeedTrackerDiagnostics _diag = null;

    /** Creates a new instance of MergeSeedLists */
    public MergeSeedLists() {
    }

    public void setDiagnostic(ISeedTrackerDiagnostics diagnostic) {
        _diag = diagnostic;
    }

    public boolean Merge(List<SeedCandidate> seedlist, SeedCandidate newseed, SeedStrategy strategy) {
//        if(diag!=null) diag.fireMergeStartDiagnostics(newseedlist);

        //  Assume the new seed is better than all duplicates
        boolean best = true;
        
        //  Create a list of duplicates that are inferior
        List<SeedCandidate> duplist = new ArrayList<SeedCandidate>();

        //  Loop over all existing seeds
        for (SeedCandidate seed : seedlist) {

            //  See if the new seed is considered a duplicate of the current seed
            boolean dupe = isDuplicate(newseed, seed);

            if (dupe) {

                //  Check if the new seed is better than the existing seed
                boolean better = isBetter(newseed, seed, strategy);

                if (better) {

                    //  If the new seed is better, add the existing seed to the list for deletion
                    duplist.add(seed);

                } else {

                    //  If the new seed is inferior to an existing seed, leave everything unchanged
                    best = false;
                    if (_diag != null) _diag.fireMergeKillingNewSeed(seed, newseed);
                    break;
                }
            }
        }

        //  If the new seed is better than all duplicates, add it to the seed list and remove duplicates
        if (best) {
            seedlist.add(newseed);
            for (SeedCandidate seed : duplist) {
                seedlist.remove(seed);
                if (_diag != null) _diag.fireMergeKillingOldSeed(seed, newseed);
            }
        }

        return best;
    }

    public boolean isDuplicate(SeedCandidate seed1, SeedCandidate seed2) {
        int nduplicate = 0;
        for (HelicalTrackHit hit1 : seed1.getHits()) {
            for (HelicalTrackHit hit2 : seed2.getHits()) {
                if (hit1 == hit2) {
                    nduplicate++;
                    if (nduplicate > 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isBetter(SeedCandidate newseed, SeedCandidate oldseed, SeedStrategy strategy) {
        int hitdif = newseed.getHits().size() - oldseed.getHits().size();
        double chisqdif = newseed.getHelix().chisqtot() - oldseed.getHelix().chisqtot();
        if (hitdif > 1) {
            return true;
        }
        if (hitdif == 1) {
            return chisqdif < strategy.getBadHitChisq();
        }
        if (hitdif == 0) {
            return chisqdif < 0.;
        }
        if (hitdif == -1) {
            return chisqdif < -strategy.getBadHitChisq();
        }
        return false;
    }
}