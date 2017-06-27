/*
 * HelicalTrackCross.java
 *
 * Created on May 1, 2008, 11:02 AM
 *
 */

package org.lcsim.fit.helicaltrack;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.RawTrackerHit;

/**
 * Encapsulate cross (stereo) hit information needed by HelicalTrackFitter.
 * The separation between the two sensor planes makes the hit position and
 * covariance matrix depend on the track direction (track curvature between
 * the sensor planes is assumed to be negligible).  The nominal hit position
 * is calculated assuming the track originates from the origin.  A corrected
 * hit position and covariance matrix, which accounts for the track direction
 * and uncertainties in the track direction, can be calculated as well.
 *
 * Note that the nominal position will have large uncertainties associated
 * with not knowing the track direction.
 * @author Richard Partridge
 * @version 1.0
 */
public class HelicalTrackCross extends HelicalTrackHit {
    private HelicalTrackStrip _strip1;
    private HelicalTrackStrip _strip2;
    private HelicalTrackFit _helix;
    private static int _type = 3;
    
    /**
     * Creates a new instance of HelicalTrackCross
     * @param strip1 First of the two strips that form this cross
     * @param strip2 Second of the two strips that form this cross
     */
    public HelicalTrackCross(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {
        super();
        init(strip1,strip2);

    }
    
    
    /**
     * Creates a new instance of HelicalTrackCross
     */
    public HelicalTrackCross() {
        super();
    }
    
    public void init(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {
        init(HitUtils.PositionFromOrigin(strip1, strip2), HitUtils.CovarianceFromOrigin(strip1, strip2),
                strip1.dEdx()+strip2.dEdx(), 0.5*(strip1.time() + strip2.time()), _type, null,
                strip1.detector(), strip1.layer(), strip1.BarrelEndcapFlag());
        _strip1 = strip1;
        _strip2 = strip2;
        _helix = null;
        init();
    }
    
    
    private void init() {
        
        //  Put the raw hits from the strips into the hit list
        if (_strip1.rawhits() != null) {
            for (RawTrackerHit rawhit : (List<RawTrackerHit>) _strip1.rawhits()) {
                super.addRawHit(rawhit);
            }
        }
        if (_strip2.rawhits() != null) {
            for (RawTrackerHit rawhit : (List<RawTrackerHit>) _strip2.rawhits()) {
                super.addRawHit(rawhit);
            }
        }
        
        //  Check if the sensors are parallel to each other
        if (VecOp.cross(_strip1.w(),_strip2.w()).magnitude() > _epsParallel) {
            throw new RuntimeException("Trying to construct a stereo hit from non-parallel sensor planes eps=" + Double.toString(_epsParallel) + " value=" + Double.toString(VecOp.cross(_strip1.w(),_strip2.w()).magnitude()));
        }
        
        //  Check that the normals point in the same direction
        if (VecOp.dot(_strip1.w(),_strip2.w()) < 0.) {
            throw new RuntimeException("Trying to construct a stereo hit using an in-consistent coordinate system!");
        }
        
        //  Calculate v1hat . u2hat, which is equivalent to sin(alpha) where alpha is the stereo angle
        double salpha = HitUtils.v1Dotu2(_strip1, _strip2);
        if (Math.abs(salpha) < _epsStereoAngle) {
            throw new RuntimeException("Trying to construct a stereo hit using parallel strips!");
        }
    }
    
    
    /**
     * Return a list of HelicalTrackStrips that contains the two strips that
     * form the cross.
     * @return list of the two strips that form this cross
     */
    public List<HelicalTrackStrip> getStrips() {
        List<HelicalTrackStrip> striplist = new ArrayList<HelicalTrackStrip>();
        striplist.add(_strip1);
        striplist.add(_strip2);
        return striplist;
    }

    public void setTrackDirection(HelicalTrackFit helix) {

        if (helix != null) {
            if (helix.equals(_helix)) return;
            TrackDirection trkdir = HelixUtils.CalculateTrackDirection(helix, helix.PathMap().get(this));
            setTrackDirection(trkdir, helix.covariance());
            _helix = helix;
        } else {
            resetTrackDirection();
        }
    }

    /**
     * Set the track direction to be used in calculating the corrected position
     * and covariance matrix.  Calling this method will cause the corrected
     * position and covariance matrix to be recalculated and stored and stored
     * in the parent class.
     * @param trkdir TrackDirection object containing direction and derivatives
     * @param hcov covariance matrix for helix parameters
     */
    public void setTrackDirection(TrackDirection trkdir, SymmetricMatrix hcov) {
        //  Get the corrected position and covariance matrix
        Hep3Vector poscor = HitUtils.PositionOnHelix(trkdir, _strip1, _strip2);
        SymmetricMatrix covcor = HitUtils.CovarianceOnHelix(trkdir, hcov, _strip1, _strip2);
        //  Retrieve the nominal position and covariance matrix (i.e., from the origin methods)
        Hep3Vector pos = new BasicHep3Vector(super.getPosition());
        SymmetricMatrix cov = new SymmetricMatrix(3, super.getCovMatrix(), true);
        //  Check to make sure we have sane errors in r-phi, r, and z - problems can occur
        //  if the track direction is nearly parallel to the sensor plane
        boolean errok = (drphicalc(poscor, covcor) < drphicalc(pos, cov)   + _eps) &&
                (drcalc(poscor, covcor)    < drcalc(pos, cov)      + _eps) &&
                (Math.sqrt(covcor.e(2,2))  < Math.sqrt(cov.e(2,2)) + _eps);
        if (errok) {
            super.setCorrectedPosition(poscor);
            super.setCorrectedCovMatrix(covcor);
            super.setChisq(ChisqPenalty(trkdir, hcov));
        } else {
            resetTrackDirection();
        }
        return;
    }
    
    /**
     * Reset the corrected hit position and covariance matrix to their
     * nominal values (i.e., for a track coming from the origin with
     * unknown track direction).  This method should be called before
     * the first attempt to fit a helix to erase the "memory" of the
     * previous helix fit.  Once there is some knowledge of the track
     * direction (eg from one or more previous helix fits), invoking
     * the setTrackDirection method will update the hit position and
     * covariance matrix and significantly improve the hit position
     * resolution for cross hits.
     */
    public void resetTrackDirection() {
        super.setCorrectedPosition(HitUtils.PositionFromOrigin(_strip1, _strip2));
        super.setCorrectedCovMatrix(HitUtils.CovarianceFromOrigin(_strip1, _strip2));
        super.setChisq(0.);
        _helix = null;
    }
    
    /**
     * Calculate a chi^2 penalty if one or both unmeasured coordinates for the hit lie
     * outside the extant of their respective strips.
     * @param trkdir track direction
     * @param hcov helix covariance matrix
     * @return chi^2 penalty
     */
    private double ChisqPenalty(TrackDirection trkdir, SymmetricMatrix hcov) {
        double chisq = 0.;
        double v1 = HitUtils.UnmeasuredCoordinate(trkdir, _strip1, _strip2);
        double v2 = HitUtils.UnmeasuredCoordinate(trkdir, _strip2, _strip1);
        double dv1 = HitUtils.dv(trkdir, hcov, _strip1, _strip2);
        double dv2 = HitUtils.dv(trkdir, hcov, _strip2, _strip1);
        //  Check if strip coordinates are within strip limits, and if so, add a chisq penalty
        if (v1 < _strip1.vmin()) chisq += Math.pow((v1 - _strip1.vmin()) / dv1, 2);
        if (v1 > _strip1.vmax()) chisq += Math.pow((v1 - _strip1.vmax()) / dv1, 2);
        if (v2 < _strip2.vmin()) chisq += Math.pow((v2 - _strip2.vmin()) / dv2, 2);
        if (v2 > _strip2.vmax()) chisq += Math.pow((v2 - _strip2.vmax()) / dv2, 2);
        return chisq;
    }
}