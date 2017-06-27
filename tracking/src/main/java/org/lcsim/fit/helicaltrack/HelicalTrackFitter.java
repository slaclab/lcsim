package org.lcsim.fit.helicaltrack;
/*
 * HelicalTrackFitter.java
 *
 * Created on March 25, 2006, 6:11 PM
 *
 * $Id: HelicalTrackFitter.java,v 1.33 2011/02/03 18:44:28 partridge Exp $
 */

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.fit.circle.CircleFit;
import org.lcsim.fit.circle.CircleFitter;
import org.lcsim.fit.line.SlopeInterceptLineFit;
import org.lcsim.fit.line.SlopeInterceptLineFitter;
import org.lcsim.fit.zsegment.ZSegmentFit;
import org.lcsim.fit.zsegment.ZSegmentFitter;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 * Fit a helix to a set of space points.  First, a circle is fit to the x-y coordinates.
 * A straight-line fit is then performed on the s-z coordinates.  If there are too few
 * 3D hits to perform the s-z fit, the ZSegmentFitter is used to find the s-z parameters.
 *
 * For disk hits, the measured coordinate is r, not z.  In this case, we use
 * an estimate of the track slope to transform the uncertainty in r to an
 * equivalent uncertainty in z using dz = dr * slope.
 *
 * The r-phi and z coordinate measurements are assumed to be uncorrelated.  A block
 * diagonal covariance matrix is formed from the results of the circle and s-z fits,
 * ignoring any correlations between these fits.
 *
 * The resulting track parameters follow the "L3 Convention" (L3 Note 1666)
 * adopted by org.lcsim.
 * @author Norman Graf
 * @version 2.0 (R. Partridge)
 */
public class HelicalTrackFitter {

    private CircleFitter _cfitter = new CircleFitter();
    private SlopeInterceptLineFitter _lfitter = new SlopeInterceptLineFitter();
    private ZSegmentFitter _zfitter = new ZSegmentFitter();
    private CircleFit _cfit;
    private SlopeInterceptLineFit _lfit;
    private ZSegmentFit _zfit;
    private HelicalTrackFit _fit;
    private double _tolerance = 3.;

    /**
     * Status of the HelicalTrackFit.
     */
    public enum FitStatus {

        /**
         * Successful Fit.
         */
        Success,
        /**
         * CircleFit failed.
         */
        CircleFitFailed,
        /**
         * Inconsistent seed hits
         */
        InconsistentSeed,
        /**
         * s-z line fit failed.
         */
        LineFitFailed,
        /**
         * ZSegmentFit failed.
         */
        ZSegmentFitFailed
    };

    /**
     * Creates a new instance of HelicalTrackFitter.
     */
    public HelicalTrackFitter() {
    }

    /**
     * Perform a helix fit using the specified coordinates and uncertainties.
     *
     * This is the original fit method for HelicalTrackFitter and is being kept for
     * backwards compatibility.  Not recommened for new code, may be deprecated in the
     * future.
     * @param x array of x coordinates
     * @param y array of y coordinates
     * @param z array of z coordinates
     * @param drphi error in r-phi hit position
     * @param dz error in z coordinate (negative for an axial strip of length |dz|*sqrt(12))
     * @param np number of points
     * @return fit status
     */
    public FitStatus fit(double[] x, double[] y, double[] z, double[] drphi, double[] dz, int np) {
        List<HelicalTrackHit> hitcol = new ArrayList<HelicalTrackHit>();
        for (int i = 0; i < np; i++) {
            Hep3Vector pos = new BasicHep3Vector(x[i], y[i], z[i]);
            if (dz[i] > 0.) {
                HelicalTrackHit hit = new HelicalTrack3DHit(pos, MakeCov(pos, drphi[i], 0., dz[i]),
                        0., 0., null, "Unknown", 0, BarrelEndcapFlag.BARREL);
                hitcol.add(hit);
            } else {
                double zmin = z[i] - Math.abs(dz[i]);
                double zmax = z[i] + Math.abs(dz[i]);
                HelicalTrackHit hit = new HelicalTrack2DHit(pos, MakeCov(pos, drphi[i], 0., 0.),
                        0., 0., null, "Unknown", 0, BarrelEndcapFlag.BARREL, zmin, zmax);
                hitcol.add(hit);
            }
        }
        return fit(hitcol);
    }

    /**
     * Perform a helix fit of the specified HelicalTrackHits.  Multiple scattering
     * errors are neglected when this method is used.  The track is assumed to travel
     * on a straight line from the origin to estimate the track slope (needed to
     * estimate an effective uncertainty in z for disk hits).
     * @param hitcol HelicalTrackHits to be fit
     * @return fit status
     */
    public FitStatus fit(List<HelicalTrackHit> hitcol) {
        Map<HelicalTrackHit, MultipleScatter> msmap = new HashMap<HelicalTrackHit, MultipleScatter>();
        return fit(hitcol, msmap, null);
    }

    /**
     * Perform a helix fit of the specified HelicalTrackHits, taking into account
     * multiple scattering errors.  If an approximate HelicalTrackFit is provided,
     * it will be used to obtain the track slope (needed to estimate an effective
     * uncertainty in z for disk hits).
     * @param hitcol HelicalTrackHits to be fit
     * @param msmap map giving multiple scattering errors for the hits
     * @param oldhelix approximate HelicalTrackFit (used to estimate track slope)
     * @return fit status
     */
    public FitStatus fit(List<HelicalTrackHit> hitcol, Map<HelicalTrackHit, MultipleScatter> msmap, HelicalTrackFit oldhelix) {

        //  Check that we have at least 3 hits
        int nhit = hitcol.size();

        //  Sort the hits to be monotonic in z so that we can follow a curling track
        //  It is assumed that the first hit on a track is closer to the origin than the last hit
        //  It is also assumed that the track won't curl through an angle > 180 degrees between
        //  neighboring points.  This might be a problem for curlers with small dip angle.
        Collections.sort(hitcol);

        //  See if the first hit is closer to the origin than the last hit
        //  If not, reverse the order of the hits
        double zfirst = hitcol.get(0).z();
        double zlast = hitcol.get(nhit - 1).z();
        if (Math.abs(zfirst) > Math.abs(zlast)) {
            Collections.reverse(hitcol);
        }

        //  Initialize the various fitter outputs
        _cfit = null;
        _lfit = null;
        _zfit = null;
        _fit = null;

        //  Create lists for the various types of hits
        List<HelicalTrackHit> circle_hits = new ArrayList<HelicalTrackHit>();
        List<HelicalTrackHit> pixel_hits = new ArrayList<HelicalTrackHit>();
        List<HelicalTrackHit> strip_hits = new ArrayList<HelicalTrackHit>();

        //  Sort the hits into the appropriate lists
        for (HelicalTrackHit hit : hitcol) {
            //  Hits to be used in the circle fit
            if (hit.drphi() > 0) circle_hits.add(hit);
            //  Pixel hits
            if (hit instanceof HelicalTrack3DHit) pixel_hits.add(hit);
            //  Strip hits
            if (hit instanceof HelicalTrack2DHit) strip_hits.add(hit);
            //  Cross hits
            if (hit instanceof HelicalTrackCross) pixel_hits.add(hit);
        }

        //  Check to make sure we have at least 3 circle hits before proceeding
        int nc = circle_hits.size();
        if (nc < 3) {
            return FitStatus.CircleFitFailed;
        }

        //  Create the objects that will hold the fit output
        double[] chisq = new double[2];
        int[] ndof = new int[2];
        double[] par = new double[5];
        SymmetricMatrix cov = new SymmetricMatrix(5);

        //  Setup for doing the circle fit
        double[] x = new double[nc];
        double[] y = new double[nc];
        double[] wrphi = new double[nc];

        //  Store the hit coordinates and weights in arrays for the circle fitter
        for (int i = 0; i < nc; i++) {
            HelicalTrackHit hit = circle_hits.get(i);
            //  Store the hit position
            x[i] = hit.x();
            y[i] = hit.y();
            //  Find the weight (= 1/uncertainty^2) for this hit
            //  First get the multiple scattering uncertainty
            double drphi_ms = 0.;
            if (msmap.containsKey(hit)) {
                drphi_ms = msmap.get(hit).drphi();
            }
            //  Get the hit resolution and combine uncertainties in quadrature
            double drphi_res = hit.drphi();
            wrphi[i] = 1. / (drphi_res * drphi_res + drphi_ms * drphi_ms);
        }

        //  Call the circle fitter and check for success
        boolean success = _cfitter.fit(x, y, wrphi, nc);
        if (!success) {
            return FitStatus.CircleFitFailed;
        }

        //  Get the results of the fit
        _cfit = _cfitter.getfit();

        //  Calculate the arc lengths from the DCA to each hit and check for backwards hits
        Map<HelicalTrackHit, Double> smap = getPathLengths(hitcol);

        //  If we are going around the circle in the wrong direction, fix it
        if (smap.get(circle_hits.get(nc-1)) < 0.) {

            //  Change the circle fit parameters to reverse direction
            _cfit = CircleFix(_cfitter.getfit());

            //  Flip the signs of the path lengths
            for (HelicalTrackHit hit : hitcol) {
                double oldpath = smap.get(hit);
                smap.put(hit, -oldpath);
            }
        }

        //  Check that things make sense
        for (HelicalTrackHit hit : smap.keySet()) {
            if (smap.get(hit) < 0.) return FitStatus.InconsistentSeed;
        }

        //  Save the chi^2 and dof for the circle fit
        chisq[0] = _cfit.chisq();
        ndof[0] = nc - 3;

        //  Save the circle fit parameters.  Note that the circle fitter has the
        //  opposite sign convention for d0 than has been adopted by org.lcsim
        //  (L3 Note 1666), so the sign of d0 is flipped.
        par[HelicalTrackFit.dcaIndex] = -1. * _cfit.dca();  // fix d0 sign convention
        par[HelicalTrackFit.phi0Index] = _cfit.phi();
        par[HelicalTrackFit.curvatureIndex] = _cfit.curvature();

        //  Save the covariance matrix, which is passed to us as an array of
        //  elements in "lower" order.  Note that the order of parameters
        //  in the circle fitter (curv, phi0, d0) is different from the
        //  ordering in the covariance matrix, so don't change this code
        //  unless you really know what you are doing!!
        //  Also, fix the d0 sign for the d0-omega and d0-phi0 terms.
        cov.setElement(HelicalTrackFit.curvatureIndex, HelicalTrackFit.curvatureIndex, _cfit.cov()[0]);
        cov.setElement(HelicalTrackFit.curvatureIndex, HelicalTrackFit.phi0Index, _cfit.cov()[1]);
        cov.setElement(HelicalTrackFit.phi0Index, HelicalTrackFit.phi0Index, _cfit.cov()[2]);
        cov.setElement(HelicalTrackFit.curvatureIndex, HelicalTrackFit.dcaIndex, -1. * _cfit.cov()[3]);  // fix d0 sign convention
        cov.setElement(HelicalTrackFit.phi0Index, HelicalTrackFit.dcaIndex, -1. * _cfit.cov()[4]);  // fix d0 sign convention
        cov.setElement(HelicalTrackFit.dcaIndex, HelicalTrackFit.dcaIndex, _cfit.cov()[5]);

        //  Check if we have enough pixel hits to do a straight-line fit of s vs z
        int npix = pixel_hits.size();
        if (npix > 1) {

            //  Setup for the line fit
            double[] s = new double[npix];
            double[] z = new double[npix];
            double[] dz = new double[npix];

            //  Store the coordinates and errors for the line fit
            for (int i = 0; i < npix; i++) {
                HelicalTrackHit hit = pixel_hits.get(i);
                z[i] = hit.z();
                dz[i] = HitUtils.zres(hit, msmap, oldhelix);
                s[i] = smap.get(hit);
            }

            //  Call the line fitter and check for success
            success = _lfitter.fit(s, z, dz, npix);
            if (!success) {
                return FitStatus.LineFitFailed;
            }

            //  Save the line fit, chi^2, and DOF
            _lfit = _lfitter.getFit();
            chisq[1] = _lfit.chisquared();
            ndof[1] = npix - 2;

            //  Save the line fit parameters
            par[HelicalTrackFit.z0Index] = _lfit.intercept();
            par[HelicalTrackFit.slopeIndex] = _lfit.slope();

            //  Save the line fit covariance matrix elements
            cov.setElement(HelicalTrackFit.z0Index, HelicalTrackFit.z0Index, Math.pow(_lfit.interceptUncertainty(), 2));
            cov.setElement(HelicalTrackFit.z0Index, HelicalTrackFit.slopeIndex, _lfit.covariance());
            cov.setElement(HelicalTrackFit.slopeIndex, HelicalTrackFit.slopeIndex, Math.pow(_lfit.slopeUncertainty(), 2));

        } else {

            //  Not enough pixel hits for a line fit, do a ZSegment fit

            //  If we have one barrel pixel hit, turn it into a pseudo strip hit
            if (npix == 1) {
                //  Get the pixel hit (there should only be 1)
                HelicalTrackHit hit = pixel_hits.get(0);
                //  Only do this for barrel hits
                if (hit.BarrelEndcapFlag() == BarrelEndcapFlag.BARREL) {
                //  Create a pseudo strip hit and add it to the list of strip hits
                    strip_hits.add(
                            HitUtils.PixelToStrip(hit, smap, msmap, oldhelix, _tolerance));
                }
            }

            //  We should always have enough hits for a ZSegment fit
            int nstrip = strip_hits.size();
            if (nstrip < 2) {
                throw new RuntimeException("Too few hits for a ZSegment fit");
            }

            //  Setup for the ZSegment fit
            double[] s = new double[nstrip];
            double[] zmin = new double[nstrip];
            double[] zmax = new double[nstrip];

            //  Store the path lengths and z limits for the ZSegment fit
            for (int i = 0; i < nstrip; i++) {
                HelicalTrack2DHit hit = (HelicalTrack2DHit) strip_hits.get(i);
                s[i] = smap.get(hit);
                //  Get the multiple scattering uncertainty and adjust z limits accordingly
                double dz = 0.;
                if (msmap.containsKey(hit)) {
                    dz = msmap.get(hit).dz();
                }
                zmin[i] = hit.zmin() - _tolerance * dz;
                zmax[i] = hit.zmax() + _tolerance * dz;
            }

            //  Call the ZSegment fitter and check for success
            success = _zfitter.fit(s, zmin, zmax);
            if (!success) {
                return FitStatus.ZSegmentFitFailed;
            }

            //  Save the ZSegment fit, chi^2, and DOF
            _zfit = _zfitter.getFit();
            chisq[1] = 0.;
            ndof[1] = 0;

            //  Save the ZSegment fit parameters
            par[HelicalTrackFit.z0Index] = _zfit.getCentroid()[0];
            par[HelicalTrackFit.slopeIndex] = _zfit.getCentroid()[1];

            //  Save the ZSegment fit covariance matrix elements
            cov.setElement(HelicalTrackFit.z0Index, HelicalTrackFit.z0Index,
                    _zfit.getCovariance().e(0, 0));
            cov.setElement(HelicalTrackFit.z0Index, HelicalTrackFit.slopeIndex,
                    _zfit.getCovariance().e(0, 1));
            cov.setElement(HelicalTrackFit.slopeIndex, HelicalTrackFit.slopeIndex,
                    _zfit.getCovariance().e(1, 1));
        }

        //  Include a chisq penalty in the s-z fit if we miss any axial strips
        if (strip_hits.size() > 0) {
            chisq[1] += MissedStripPenalty(strip_hits, par, cov, smap, msmap);
        }

        //  Create the HelicalTrackFit for this helix
        _fit = new HelicalTrackFit(par, cov, chisq, ndof, smap, msmap);

        return FitStatus.Success;
    }

    /**
     * Return the results of the most recent helix fit.  Returns null if the fit
     * was not successful.
     * @return HelicalTrackFit from the most recent helix fit
     */
    public HelicalTrackFit getFit() {
        return _fit;
    }

    /**
     * Return the circle fit for the most recent helix fit.  Returns null if the
     * circle fit was not successful.
     * @return circle fit from most recent helix fit
     */
    public CircleFit getCircleFit() {
        return _cfit;
    }

    /**
     * Return the s-z line fit for the most recent helix fit.  If the line fit failed
     * or was not performed due to not having enough 3D hits, null is returned.
     * @return line fit for most recent helix fit
     */
    public SlopeInterceptLineFit getLineFit() {
        return _lfit;
    }

    /**
     * Return the ZSegmentFit for the most recent helix fit.  If the ZSegmentFit
     * failed or was not performed, null is returned.
     * @return z segment fit for most recent helix fit
     */
    public ZSegmentFit getZSegmentFit() {
        return _zfit;
    }

    /**
     * Specify tolerance used in extending axial strips for ZSegmentFits to account
     * for multiple scattering.  Each end of the strip will be extended by the
     * product of the tolerance and the multiple scattering error.
     * @param tolerance tolerance
     */
    public void setTolerance(double tolerance) {
        _tolerance = tolerance;
        return;
    }

    /**
     * Return the tolerance being used to extend axial strips for ZSegmentFits.
     * @return tolerance
     */
    public double getTolerance() {
        return _tolerance;
    }

    public void setReferencePoint(double x, double y) {
        _cfitter.setreferenceposition(x, y);
        return;
    }

    /**
     * Create a Cartesian covariance matrix given uncertainties in polar
     * coordinates.  It is assumed that the polar coordinate uncertainties
     * are uncorrelated.
     * @param pos hit position
     * @param drphi uncertainty in the r*phi coordinate
     * @param dr uncertainty in the r coordinate
     * @param dz uncertainty in the z coordinate
     * @return covariance matrix
     */
    private SymmetricMatrix MakeCov(Hep3Vector pos, double drphi, double dr, double dz) {

        //  Get the x, y, and r coordinates
        double x = pos.x();
        double y = pos.y();
        double r2 = x * x + y * y;

        //  Create a new covariance matrix and set the non-zero elements
        SymmetricMatrix cov = new SymmetricMatrix(3);
        cov.setElement(0, 0, (y * y * drphi * drphi + x * x * dr * dr) / r2);
        cov.setElement(0, 1, x * y * (dr * dr - drphi * drphi) / r2);
        cov.setElement(1, 1, (x * x * drphi * drphi + y * y * dr * dr) / r2);
        cov.setElement(2, 2, dz * dz);

        return cov;
    }

    /**
     * Check if the circle finder picks a "backwards" solution with the
     * charged particle travelling in the wrong direction (e.g. clockwise
     * when the actual track is going counter-clockwise).  If this happens,
     * return a new circle fit that reverses the initial direction and
     * changes the sign of the curvature and DCA.  If the circle fit has the
     * particle travelling in the right direction, return the original fit.
     * @param oldfit circle fit to be checked
     * @param hitlist list of hits used for the circle fit
     * @return fixed circle fit
     */
    private CircleFit CircleFix(CircleFit oldfit) {

        //  Reverse the direction by changing the sign of dca, curv, and adding pi to phi0
        double dca = -oldfit.dca();
        double phi0 = oldfit.phi() + Math.PI;
        if (phi0 > 2. * Math.PI) {
            phi0 -= 2. * Math.PI;
        }
        double curv = -oldfit.curvature();

        //  Also fix the affected covariance matrix elements
        double[] cov = oldfit.cov();
        cov[1] = -cov[1]; // curv - phi0 element
        cov[4] = -cov[4]; // phi0 - dca element

        //  Return a new circle fit with the updated parameters
        return new CircleFit(oldfit.xref(), oldfit.yref(), curv, phi0, dca, oldfit.chisq(), cov);
    }

    /**
     * Find the x-y path lengths for a list of hits.
     * @param hits list of hits
     * @return map containing the path lengths of the hits
     */
    private Map<HelicalTrackHit, Double> getPathLengths(List<HelicalTrackHit> hits) {

        //  Create a map to store the arc lengths
        Map<HelicalTrackHit, Double> smap = new HashMap<HelicalTrackHit, Double>();

        //  Initialize looper tracking and iterate over ordered list of hits
        double slast = 0.;
        int ilast = -1;
        double s;
        for (int i = 0; i < hits.size(); i++) {

            // Retrieve the next hit ordered by z coordinate and check hit type
            HelicalTrackHit hit = hits.get(i);
            if (hit instanceof HelicalTrack2DHit) {

                //  Axial hit - measure from the DCA (can't handle loopers)
                s = HelixUtils.PathLength(_cfit, hit);

            } else {

                if (ilast < 0) {
                    //  For the first 3D hit, measure from the DCA
                    s = HelixUtils.PathLength(_cfit, hit);

                } else {
                    //  For subsequent hits, add in the arc length from the previous 3D hit
                    s = slast + HelixUtils.PathLength(_cfit, hits.get(ilast), hit);
                }

                //  Update info on the last 3D hit
                ilast = i;
                slast = s;
            }

            //  Save the arc length for this hit
            smap.put(hit, s);
        }
        return smap;
    }

    /**
     * Calculate a chisq penalty if any axial strip hits lie outside the strip
     * boundaries in z.
     * @param hitcol list of hits to check
     * @param smap map of x-y path lengths
     * @param msmap map of multiple scatter uncertainties
     * @return chisq penalty
     */
    private double MissedStripPenalty(List<HelicalTrackHit> hitcol, double[] par, SymmetricMatrix cov,
            Map<HelicalTrackHit, Double> smap, Map<HelicalTrackHit, MultipleScatter> msmap) {
        //  Get the line fit parameters and uncertainties
        double z0 = par[HelicalTrackFit.z0Index];
        double slope = par[HelicalTrackFit.slopeIndex];
        double cov_z0z0 = cov.e(HelicalTrackFit.z0Index, HelicalTrackFit.z0Index);
        double cov_slsl = cov.e(HelicalTrackFit.slopeIndex, HelicalTrackFit.slopeIndex);
        double cov_z0sl = cov.e(HelicalTrackFit.z0Index, HelicalTrackFit.slopeIndex);
        //  Chisq will hold the penalty
        double chisq = 0.;
        //  Loop over HelicalTrack2DHits
        for (HelicalTrackHit hit : hitcol) {
            if (hit instanceof HelicalTrack2DHit) {
                //  Find the pathmap to this hit
                double s = smap.get(hit);
                //  Find the predicted z coordinate
                double zhelix = z0 + s * slope;
                //  Find the uncertainty^2 in the z coordinate
                double dzsq = cov_z0z0 + 2. * s * cov_z0sl + s * s * cov_slsl;
                //  Find the multiple scattering error
                double dz_ms = 0.;
                if (msmap.containsKey(hit)) {
                    dz_ms = msmap.get(hit).dz();
                }
                //  Add the multiple scattering uncertainty
                dzsq += dz_ms * dz_ms;
                //  Find the limits in z for the strip
                double zmin = ((HelicalTrack2DHit) hit).zmin();
                double zmax = ((HelicalTrack2DHit) hit).zmax();
                //  Calculate a chisq penalty if the predicted z is not on the strip
                if (zhelix < zmin) {
                    chisq += (zhelix - zmin) * (zhelix - zmin) / dzsq;
                }
                if (zhelix > zmax) {
                    chisq += (zhelix - zmax) * (zhelix - zmax) / dzsq;
                }
            }
        }
        return chisq;
    }
}
