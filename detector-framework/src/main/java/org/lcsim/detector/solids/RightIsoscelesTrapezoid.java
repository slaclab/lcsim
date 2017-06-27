package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Math.abs;

/**
 * This class encapsulates the behavior of a polyhedron
 * whose primary axis is aligned with the z axis and
 * whose parallel ends are made of isosceles trapezoids.
 * The trapezoids have a flat base at the bottom.
 * Also known as a keystone, this is a specialization
 * of the Trd class.
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
 * @version $Id: RightIsoscelesTrapezoid.java,v 1.3 2010/04/27 18:55:34 ngraf Exp $
 */
public class RightIsoscelesTrapezoid extends AbstractSolid
{
    //input

    private double _zHalf;
    // derived quantities
    private IsoscelesTrapezoid _face = null;
    private Hep3Vector[] _vertices = new Hep3Vector[4];
    private double _volume;

    /**
     *  Fully qualified constructor
     * @param name  the name of this solid
     * @param bottomHalf the half-width of the base dimension
     * @param topHalf the half-width of the top dimension
     * @param yHalf the half-width in the y dimension
     * @param zHalf the half width in the z dimension
     */
    public RightIsoscelesTrapezoid(String name, double bottomHalf, double topHalf, double yHalf, double zHalf)
    {
        super(name);

        _zHalf = zHalf;

        // counterclockwise starting at lower left corner
        _vertices[0] = new BasicHep3Vector(-bottomHalf, -yHalf, 0.);
        _vertices[1] = new BasicHep3Vector(bottomHalf, -yHalf, 0.);
        _vertices[2] = new BasicHep3Vector(topHalf, yHalf, 0.);
        _vertices[3] = new BasicHep3Vector(-topHalf, yHalf, 0.);

        _face = new IsoscelesTrapezoid(bottomHalf, topHalf, yHalf);
        _volume = _face.area() * _zHalf * 2.;
    }

    public double getCubicVolume()
    {
        return _volume;
    }

    /**
     * Determines whether a point lies within or outside of this solid.
     *
     * @param pos the point to check
     * @return an enumeration of INSIDE or OUTSIDE ( SURFACE is reported as INSIDE )
     */
    public Inside inside(Hep3Vector pos)
    {
        //fail fast
        double z = pos.z();
        if (abs(z) > _zHalf)
            return Inside.OUTSIDE;

        //need to be inside the face...
        Inside outer = _face.inside(pos);
        if (outer.compareTo(Inside.OUTSIDE) == 0)
            return Inside.OUTSIDE;

        return Inside.INSIDE;
    }

    public double zMin()
    {
        return -_zHalf;
    }

    public double zMax()
    {
        return _zHalf;
    }
    
    /**
     * Return the IsoscelesTrapezoid which forms the face of this solid
     * @return the face of this solid
     */
    public IsoscelesTrapezoid face()
    {
        return _face;
    }

    @Override
    public String toString()
    {
        NumberFormat formatter = new DecimalFormat("#0.0000");

        StringBuffer sb = new StringBuffer("RightIsoscelesTrapezoid\n");
        sb.append(" thickness " + 2. * _zHalf + " \n");
        sb.append(_face.toString() + " \n");
        sb.append(" volume " + getCubicVolume() + " \n");
        return sb.toString();
    }

}
