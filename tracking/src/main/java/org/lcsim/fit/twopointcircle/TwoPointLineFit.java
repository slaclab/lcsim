/*
 * Encapsulate parameters for a straight line through two hits.  The straight line
 * is parameterized by specifying a point of closest approach and the azimuthal
 * angle of the line.
 */

package org.lcsim.fit.twopointcircle;

/**
 * @author Richard Partridge
 */
public class TwoPointLineFit {
    private double _x0;
    private double _y0;
    private double _phi0;
    private double _s1;
    private double _s2;

    /**
     * Fully qualified constructor.
     *
     * @param x0 x coordinate of point of closest approach
     * @param y0 y coordinate of point of closest approach
     * @param phi0 angle of line segment
     * @param s1 distance to point 1
     * @param s2 distance to point 2
     */
    public TwoPointLineFit(double x0, double y0, double phi0, double s1, double s2) {

        _x0 = x0;
        _y0 = y0;
        _phi0 = phi0;
        _s1 = s1;
        _s2 = s2;
    }

    /**
     * Return the x coordinate of the point of closest approach
     *
     * @return x coordinate
     */
    public double x0() {
        return _x0;
    }

    /**
     * Return the y coordinate of the point of closest approach
     *
     * @return y coordinate
     */
    public double y0() {
        return _y0;
    }

    /**
     * Return the azimuthal angle of the line segment going from point 1 to point 2
     *
     * @return azimuthal angle
     */
    public double phi0() {
        return _phi0;
    }

    /**
     * Return the distance from the point of closest approach to the first point
     *
     * @return distance to point 1
     */
    public double s1() {
        return _s1;
    }

    /**
     * Return the distance from the point of closest approach to the second point
     *
     * @return distance to point 2
     */
    public double s2() {
        return _s2;
    }
}