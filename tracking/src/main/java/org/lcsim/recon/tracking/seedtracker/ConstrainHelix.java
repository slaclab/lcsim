/*
 * ConstrainHelix.java
 *
 * Created on April 2, 2008, 5:00 PM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.List;

import org.lcsim.constants.Constants;
import org.lcsim.fit.helicaltrack.HelicalTrackFit;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;

/**
 *
 * @author Richard Partridge
 * @version 1.0
 */
public class ConstrainHelix {
    private double _bfield = 0.;
    
    /**
     * Creates a new instance of ConstrainHelix
     */
    public ConstrainHelix() {
    }
    
    public void setConstraintChisq(SeedStrategy strategy, HelicalTrackFit helix, List<HelicalTrackHit> hits) {
        
        if (_bfield == 0.) throw new RuntimeException("B Field must be set before calling setConstraintChisq method");
        
        double nhchisq = 0.;
        
        //  Inflate chi^2 if |curvature| is too large
        double curvmax = Constants.fieldConversion * _bfield / strategy.getMinPT();
        double curv = Math.abs(helix.curvature());
        double dcurv = helix.covariance().diagonal(HelicalTrackFit.curvatureIndex);
        if (curv > curvmax) nhchisq += Math.pow(curv - curvmax, 2) / Math.abs(dcurv);
        
        //  Inflate chi^2 if |DCA| is too large
        double dcamax = strategy.getMaxDCA();
        double dca = Math.abs(helix.dca());
        double ddca = helix.covariance().diagonal(HelicalTrackFit.dcaIndex);
        if (dca > dcamax) {
            nhchisq += Math.pow(dca - dcamax, 2) / Math.abs(ddca);
        }
        
        //  Inflate chi^2 if |z0| is too large
        double z0max = strategy.getMaxZ0();
        double z0 = Math.abs(helix.z0());
        double dz0 = helix.covariance().diagonal(HelicalTrackFit.z0Index);
        if (z0 > z0max) {
            nhchisq += Math.pow(z0 - z0max, 2) / Math.abs(dz0);
        }

        //  Add the chi^2 penalty from the cross hits
        for (HelicalTrackHit hit : hits) {
            nhchisq += hit.chisq();
        }
        
        //  Set the non-holenomic chi squared term in the helix
        helix.setnhchisq(nhchisq);
        
        return;
    }

    public void setBField(double bfield) {
        _bfield = bfield;
        return;
    }
}
