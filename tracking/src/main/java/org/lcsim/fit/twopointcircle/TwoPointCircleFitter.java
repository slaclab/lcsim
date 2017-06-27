/*
 * Find the circle passing through two points and a fixed impact parameter.
 * Typically, there are two circles that satisfy these criteria.  The exception
 * is when a straight line connecting the two points has the specified impact
 * parameter, where there is only one solution.
 * 
 * Optionally, you can provide a minimum circle radius.  If neither circle
 * satisfies the minimum circle radius cut, the fit fails.  If one circle
 * satisfies the minimum radius cut, but the other solution does not, then a
 * second solution is found by setting the radius to the minimum radius and
 * allowing the impact parameter to be smaller than the specified value.
 *
 * This class is used by the seedtracker track finding algorithm to check if
 * a two hit trial seed satisfies the specified pT and impact parameter cuts.
 * The two solutions found will generally correspond to the minimum and maximum
 * radius circles given the two hits and the impact parameter cut.  However,
 * if the two hits are consistent with a straight-line with an impact parameter
 * less than or equal to the specified value, there is no upper limit on the
 * circle radius.  A method is provided to check for this special case and
 * return the straight-line parameters.
 */

package org.lcsim.fit.twopointcircle;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.TrackerHit;

/**
 * @author Richard Partridge
 */
public class TwoPointCircleFitter {
    private double _rmin;
    private List<TwoPointCircleFit> _circlefits;
    private TwoPointLineFit _linefit;
    private boolean _debug = false;
    private static double _eps = 1e-6;
    private static double twopi = 2. * Math.PI;

    /**
     * Constructor specifying a minimum radius.  If the minimum radius is >0,
     * the algorithm will enforce this constraint even if it means having a
     * smaller impact parameter or fewer / no successful fits.
     *
     * @param rmin minimum circle radius
     */
    public TwoPointCircleFitter(double rmin) {
        _rmin = rmin;
        _circlefits = new ArrayList<TwoPointCircleFit>();
    }

    /**
     * Constructor with no minimum radius.
     */
    public TwoPointCircleFitter() {
        this(0.);
    }

    /**
     * Fit a circle given two TrackerHits and the impact parameter.
     *
     * @param hit1 hit #1
     * @param hit2 hit #2
     * @param dmax impact parameter
     * @return fit status - true if at least one circle fit is found
     */
    public boolean FitCircle(TrackerHit hit1, TrackerHit hit2, double dmax) {
        double[] pos1 = hit1.getPosition();
        double[] pos2 = hit2.getPosition();
        return FitCircle(pos1[0], pos1[1], pos2[0], pos2[1], dmax);
    }

    /**
     * Fit a circle given coordinates for two points and the impact parameter.
     *
     * @param x1 x coordinate of first point
     * @param y1 y coordinate of first point
     * @param x2 x coordinate of second point
     * @param y2 y coordinate of second point
     * @param dmax impact parameter
     * @return fit status - true if at least one circle fit is found
     */
    public boolean FitCircle(double x1, double y1, double x2, double y2, double dmax) {

        //  Clear the list of fits from the previous time the method was called
        _circlefits.clear();
        _linefit = null;

        //  Calculate some useful quantities
        double r1sq = x1*x1 + y1*y1;
        double r2sq = x2*x2 + y2*y2;
        double dmaxsq = dmax*dmax;

        //  If either point is inside maximum IP cut, throw an exception since
        //  we are under-constrained.
        if (r1sq < dmaxsq || r2sq < dmaxsq)
            throw new RuntimeException("Cannot handle case where hit is inside maximum impact parameter cut");

        //  Get the u = (r2 - r1)/|r2 - r1| unit vector
        double ux = x2 - x1;
        double uy = y2 - y1;
        double u = Math.sqrt(ux*ux + uy*uy);
        ux = ux / u;
        uy = uy / u;
    if (u<_eps)return false;

        //  get the midpoint vector
        double mx = 0.5 * (x1 + x2);
        double my = 0.5 * (y1 + y2);
        double msq = mx*mx + my*my;

        //  Get the unit vector normal to u
        double vx = uy;
        double vy = -ux;

        //  Get the impact parameter for an infinite momentum track with hits 1 & 2
        double ipinf = (x1*y2 - y1*x2) / u;

        //  Create an array to hold alpha, the distance of the circle center from the midpoint between hits 1 and 2
        int nalpha = 2;
        double alpha[] = new double[nalpha];

        //  First calculate the denominator used in the calculation of alpha
        double denom = 2. * (ipinf*ipinf - dmaxsq);

        //  Check if we are consistent with a straight-line track
        boolean sltrack = denom < _eps;

        //  Check for the singular case that occurs when a straight-line track going through the two hits
        //  is tangent to a circle of radius _dMax
        if (Math.abs(denom) < _eps) {

            //  Singular case - only one finite momentum solution
            double beta = msq - 0.25 * u*u - dmaxsq;
            nalpha = 1;
            alpha[0] = (u*u * dmaxsq - beta*beta) / (4. * beta * ipinf);

        } else {

            //  Non-singular case - find the two solutions for alpha
            double r1dotr2 = x1*x2 + y1*y2;
            double term1 = -ipinf * (r1dotr2 - dmaxsq) / denom;
            double term2 = Math.abs(dmax * Math.sqrt((r1sq - dmaxsq) * (r2sq - dmaxsq)) / denom);
            
            //  Make sure alpha[0] is the solution with the largest circle radius
            if (term1 < 0.) term2 = -term2;
            alpha[0] = term1 + term2;
            alpha[1] = term1 - term2;
        }

        //  Loop over the two solutions
        for (int i = 0; i<nalpha; i++) {

            //  Find the circle radius and check if it exceeds the minimum value
            double rcurv = Math.sqrt(alpha[i]*alpha[i] + 0.25 * u*u);
            if (rcurv < _rmin) {

                //  The first iteration has the largest circle radius - if it doesn't pass the cut and the track
                //  isn't consistent with a straight-line track, we can't form a circle that passes the pT cut
                if (i == 0 && !sltrack) return false;

                //  If we get here, either the first solution passed the pT cut or we are consistent with a
                //  straight-line track.  Find the circle with the minimum radius and the new alpha
                rcurv = _rmin;
                double newalpha = Math.sqrt(_rmin*_rmin - 0.25 * u*u);

                //  Figure out the sign of alpha.  If we are consistent with a
                //  straight-line track, then the sign should be opposite for the
                //  second (low radius) solution.  If we are not consistent with
                //  a straight-line track, the second solution should have the same
                //  sign as the first solution.
                int isign = 1;
                if (i == 1 && sltrack && alpha[0] >= 0.) isign = -1;
                if (i == 1 && !sltrack && alpha[0] < 0.) isign = -1;
                alpha[i] = isign * newalpha;
            }

            //  Find the center of the circle
            double xc = mx + alpha[i] * vx;
            double yc = my + alpha[i] * vy;
            double rc = Math.sqrt(xc*xc + yc*yc);

            //  Find the point of closest approach
            double x0 = xc * (1. - rcurv / rc);
            double y0 = yc * (1. - rcurv / rc);

            //  Make some checks if we have debugging turned on
            if (_debug) {
                double c1 = Math.sqrt((x1-xc)*(x1-xc)+(y1-yc)*(y1-yc));
                if (Math.abs(c1-rcurv) > _eps * c1) throw new RuntimeException("Error in circle finding - c1 = "+c1+" rcurv = "+rcurv);
                double c2 = Math.sqrt((x2-xc)*(x2-xc)+(y2-yc)*(y2-yc));
                if (Math.abs(c2-rcurv) > _eps * c2) throw new RuntimeException("Error in circle finding - c2 = "+c2+" rcurv = "+rcurv);
                double c3 = Math.sqrt((x0-xc)*(x0-xc)+(y0-yc)*(y0-yc));
                if (Math.abs(c3-rcurv) > _eps * c3) throw new RuntimeException("Error in circle finding - c3 = "+c3+" rcurv = "+rcurv);
                double r0 = Math.sqrt(x0*x0 + y0*y0);
                if (r0 > dmax+100*_eps) throw new RuntimeException("Invalid DCA point for solution "+i+" r0: "+r0+" dmax: "+dmax);
                if (rcurv < _rmin) throw new RuntimeException("Invalid circle radius for solution "+i+" rcurv: "+rcurv+" rmin: "+_rmin);
            }

            //  Find the x-y arc lengths to hits 1 and 2
            //  First find azimuthal angles for the dca and hit positions relative to the circle center
            double phi0 = Math.atan2(y0-yc, x0-xc);
            double phi1 = Math.atan2(y1-yc, x1-xc);
            double phi2 = Math.atan2(y2-yc, x2-xc);

            //  Find the angle between the hits and the DCA under the assumption that |dphi| < pi
            double dphi1 = phi1 - phi0;
            if (dphi1 > Math.PI) dphi1 -= twopi;
            if (dphi1 < -Math.PI) dphi1 += twopi;
            double dphi2 = phi2 - phi0;
            if (dphi2 > Math.PI) dphi2 -= twopi;
            if (dphi2 < -Math.PI) dphi2 += twopi;

            //  Find the hit closest to the DCA and use this hit to determine the circle "direction"
            boolean cw;
            if (Math.abs(dphi1) < Math.abs(dphi2)) cw = dphi1 < 0.;
            else cw = dphi2 < 0.;

            //  Find the arc lengths to points 1 and 2
            double s1 = -dphi1 * rcurv;
            double s2 = -dphi2 * rcurv;
            if (!cw) {
                s1 = -s1;
                s2 = -s2;
            }

            // Fix the case when dphi1 & dphi2 have opposite signs, making an arc length negative
            if (s1 < 0.) s1 += twopi * rcurv;
            if (s2 < 0.) s2 += twopi * rcurv;
 
            _circlefits.add(new TwoPointCircleFit(xc, yc, rcurv, cw, s1, s2));
        }

        //  If we are consistent with a straight-line track, calculate the straight-line
        //  distances to the hits
        if (sltrack) {

            //  Find the distances from the straight-line DCA to the hits
            double s1 = (x1*ux + y1*uy);
            double s2 = (x2*ux + y2*uy);

            //  If these distances have opposite sign, we violate causality for a hit originating at the DCA
            if (s1*s2 < 0.) {

                //  Calculate the parameters of the line fit
                double x0 = x1 - s1 * ux;
                double y0 = y1 - s1 * uy;
                double phi = Math.atan2(uy, ux);

                //  Flip the signs of the path lengths and direction to make path lengths positive
                if (s1 < 0.) {
                    s1 = -s1;
                    s2 = -s2;
                    phi = phi + Math.PI;
                    if (phi > Math.PI) phi += twopi;
                }

                //  Save a new TwoPointLineFit
                _linefit = new TwoPointLineFit(x0, y0, phi, s1, s2);
            }

            //  We should always find a valid circle above for this case - check to make sure
            if (_debug & _circlefits.size() == 0) throw new RuntimeException("No circle found for hits consistent with infinite momentum");
        }

        return _circlefits.size() > 0;
    }

    /**
     * Get the list of TwoPointCircleFits that are found.
     *
     * @return list of circle fits
     */
    public List<TwoPointCircleFit> getCircleFits() {
        return _circlefits;
    }

    /**
     * Get the TwoPointLineFit if the two hits are consistent with a straight-line
     * track that passes within the circle defined by the impact parameter cut.
     * If the line connecting the two hits has a larger impact parameter, then
     * a null pointer is returned.
     *
     * @return pointer to line fit (null if no line fit is found)
     */
    public TwoPointLineFit getLineFit() {
        return _linefit;
    }

    /**
     * Set the minimum circle radius.
     *
     * @param rmin minimum radius
     */
    public void setRMin(double rmin) {
        _rmin = rmin;
    }

    /**
     * Turn on/off the debugging checks.
     *
     * @param debug state of debug flag
     */
    public void setDebug(boolean debug) {
        _debug = debug;
    }
}