package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.PI;

/**
 * This class represents an isosceles trapezoid
 * The trapezoid has a flat base at the bottom.
 * Also known as a keystone.
 *
 *           top
 *    __________________       ^
 *    \                /       |
 *     \              /        |
 *      \            /      Y  |
 *       \          /          |
 *        \________/           +------------->
 *                                    X
 *          bottom
 *
 *
 * @author Norman A. Graf
 *
 * @version $Id: IsoscelesTrapezoid.java,v 1.2 2010/04/27 22:42:18 ngraf Exp $
 */
public class IsoscelesTrapezoid
{

    private double _bottomHalf;
    private double _topHalf;
    private double _heightHalf;
    private Hep3Vector[] _vertices = new Hep3Vector[4];
    private double _area;
    private double _baseAngle;
    private double _topAngle;

    /**
     * Fully qualified constructor
     * @param bottomHalf the half-width of the base dimension
     * @param topHalf the half-width of the top dimension
     * @param heightHalf the half-height
     */
    public IsoscelesTrapezoid(double bottomHalf, double topHalf, double heightHalf)
    {
        _bottomHalf = bottomHalf;
        _topHalf = topHalf;
        _heightHalf = heightHalf;
        _area = 2. * (_bottomHalf + _topHalf) * _heightHalf;
        // counterclockwise starting at lower left corner
        _vertices[0] = new BasicHep3Vector(-_bottomHalf, -_heightHalf, 0.);
        _vertices[1] = new BasicHep3Vector(_bottomHalf, -_heightHalf, 0.);
        _vertices[2] = new BasicHep3Vector(_topHalf, _heightHalf, 0.);
        _vertices[3] = new BasicHep3Vector(-_topHalf, _heightHalf, 0.);

        double delta = atan((_topHalf - _bottomHalf) / (2. * _heightHalf));
        _baseAngle = (_topHalf >_bottomHalf) ? PI / 2. + delta : PI/2.-delta;
        _topAngle = PI-_baseAngle;

    }

    /**
     * Return the coordinates of the trapezoid vertices.
     * Convention is that the trapezoid has a flat on the bottom.
     * First vertex is the beginning of this flat, and proceeds
     * counter-clockwise.
     * The z coordinate is always zero for these vertices.
     * @return The list of vertices in the XY plane.
     */
    public Hep3Vector[] getVertices()
    {
        return _vertices;
    }

    /**
     * The area of this trapezoid
     * @return the area of this trapezoid.
     */
    public double area()
    {
        return _area;
    }

    /**
     * Determines whether a point lies within or outside of this trapezoid.
     *
     * @param pos the point to check
     * @return an enumeration of INSIDE or OUTSIDE ( SURFACE is reported as INSIDE )
     */
    public Inside inside(Hep3Vector pos)
    {
        //fail fast
        double y = pos.y();
        if (abs(y) > _heightHalf)
            return Inside.OUTSIDE;

        // now the sides...
        if (t(pos, _vertices[1], _vertices[2]) < 0)
            return Inside.OUTSIDE;
        if (t(pos, _vertices[3], _vertices[0]) < 0)
            return Inside.OUTSIDE;

        return Inside.INSIDE;
    }

    @Override
    public String toString()
    {
        NumberFormat formatter = new DecimalFormat("#0.0000");

        StringBuffer sb = new StringBuffer("IsoscelesTrapezoid\n");
        sb.append(" base " + 2. * _bottomHalf + " top " + 2. * _topHalf + " height " + 2. * _heightHalf + " \n");
        sb.append(" area " + _area + " \n");
        return sb.toString();
    }

    public double baseAngle()
    {
        return _baseAngle;
    }

    public double topAngle()
    {
        return _topAngle;
    }

    private double t(Hep3Vector a, Hep3Vector b, Hep3Vector c)
    {
        double t = (a.y() - b.y()) * (c.x() - b.x()) - (a.x() - b.x()) * (c.y() - b.y());
        return t;
    }
    
}
