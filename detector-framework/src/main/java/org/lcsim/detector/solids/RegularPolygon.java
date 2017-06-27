package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

/**
 *
 * This represents a polygon in 2D space.
 *
 * @author Norman A. Graf
 * @version $Id: RegularPolygon.java,v 1.5 2010/04/22 21:18:39 ngraf Exp $
 */
public class RegularPolygon
{
    private int _nsides;
    private double _r;
    Hep3Vector[] _vertices = null;
    private double _area;

    /**
     * Fully qualified constructor
     *
     * @param nsides the number of sides of the polygon
     * @param radius the radius of the circumscribed circle
     */
    public RegularPolygon(int nsides, double radius)
    {
        _nsides = nsides;
        _r = radius;
        // convention is that the polygon has a flat on the bottom
        double dPhi = 2. * PI / ((double) _nsides);
        double phi0 = -PI * (1. / (double) _nsides + .5);

        _vertices = new BasicHep3Vector[_nsides];

        for (int i = 0; i < _nsides; ++i)
        {
            double phi = phi0 + i * dPhi;
            double x = cos(phi);
            double y = sin(phi);
            _vertices[i] = new BasicHep3Vector(_r * x, _r * y, 0.);
        }
        _area = _nsides * _r * _r * sin(2. * PI / _nsides) / 2.;
    }
    
    /**
     * Return the coordinates of the polygon vertices.
     * Convention is that the polygon has a flat on the bottom.
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
     * The area of this polygon
     * @return the area of this polygon.
     */
    public double area()
    {
        return _area;
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
        double x = pos.x();
        double y = pos.y();
        double r2 = x * x + y * y;
        if (r2 > _r * _r)
            return Inside.OUTSIDE;

        double t = t(pos, _vertices[_nsides - 1], _vertices[0]);

        if (t < 0)
            return Inside.OUTSIDE;

        for (int i = 0; i < _nsides - 1; ++i)
        {
            t = t(pos, _vertices[i], _vertices[i + 1]);

            if (t < 0)
                return Inside.OUTSIDE;
        }
        return Inside.INSIDE;

    }

    @Override
    public String toString()
    {
        NumberFormat formatter = new DecimalFormat("#0.0000");

        StringBuffer sb = new StringBuffer("RegularPolygon\n");
        sb.append(" with " + _nsides + " sides with outer radius= " + _r + " and vertices: \n");
        for (int i = 0; i < _nsides; ++i)
        {
            sb.append(formatter.format(_vertices[i].x()) + " " + formatter.format(_vertices[i].y()) + " " + formatter.format(_vertices[i].z()) + "\n");
        }
        sb.append("area: "+_area);
        return sb.toString();
    }

    private double t(Hep3Vector a, Hep3Vector b, Hep3Vector c)
    {
        double t = (a.y() - b.y()) * (c.x() - b.x()) - (a.x() - b.x()) * (c.y() - b.y());
        return t;
    }
}
