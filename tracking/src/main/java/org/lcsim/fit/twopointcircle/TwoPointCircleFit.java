/*
 * Encapsulate results of a 2 point circle fit
 */

package org.lcsim.fit.twopointcircle;

/**
 * @author Richard Partridge
 */
public class TwoPointCircleFit {

    private double _xc;
    private double _yc;
    private double _rc;
    private boolean _cw;
    private double _s1;
    private double _s2;

    /**
     * Fully qualified constructor.
     *
     * @param xc x coordinate of circle center
     * @param yc y coordinate of circle center
     * @param rc circle radius
     * @param cw true if the closest hit to the DCA is in the clockwise direction
     * @param s1 arc length from the DCA to the first hit
     * @param s2 arc length from the DCA to the second hit
     */
    public TwoPointCircleFit(double xc, double yc, double rc, boolean cw, double s1, double s2) {
        _xc = xc;
        _yc = yc;
        _rc = rc;
        _cw = cw;
        _s1 = s1;
        _s2 = s2;
    }

    /**
     * Return the x coordinate of the circle center.
     *
     * @return x coordinate of the circle center
     */
    public double xc() {
        return _xc;
    }

    /**
     * Return the y coordinate of the circle center.
     *
     * @return y coordinate of the circle center
     */
    public double yc() {
        return _yc;
    }

    /**
     * Return the radius of the circle.
     *
     * @return circle radius
     */
    public double rc() {
        return _rc;
    }

    /**
     * Return true if the closest hit to the DCA is in a clockwise direction
     * relative to the the DCA
     *
     * @return true if hits are cw
     */
    public boolean cw() {
        return _cw;
    }

    /**
     * Return the arc length to the first hit.
     *
     * @return arc length to the first hit
     */
    public double s1() {
        return _s1;
    }

    /**
     * Return the arc length to the second hit.
     *
     * @return arc length to the second hit.
     */
    public double s2() {
        return _s2;
    }
}
