package org.lcsim.detector.solids;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

import hep.physics.vec.Hep3Vector;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * This class encapsulates the behavior of a right
 * regular polyhedron whose principal axis is aligned
 * with the z axis and whose parallel ends are made of
 * regular polygons. The polygons have a flat base at
 * the bottom. The polyhedron is allowed to be hollow
 * by specifying an inner radius for an inner polyhedron
 * coaxial along z. For now it must have the same number
 * of sides with the same orientation.
 *
 * @author Norman A. Graf
 *
 * @version $Id: RightRegularPolyhedron.java,v 1.4 2010/04/22 21:18:39 ngraf Exp $
 */
public class RightRegularPolyhedron extends AbstractSolid
{
    //input

    private int _nsides;
    private double _rmin;
    private double _rmax;
    private double _zmin;
    private double _zmax;
    //derived quantities
    private RegularPolygon _innerPolygon = null;
    private RegularPolygon _outerPolygon = null;
    private boolean _isHollow;

    /**
     * Fully qualified constructor for a hollow right regular polyhedron.
     *
     * @param name   the name of this solid
     * @param nsides the number of sides of the regular polygon forming the ends.
     * @param rmin   the radius of the inner polygon's inscribed circle
     * @param rmax   the radius of the outer polygon's circumscribed circle
     * @param zmin   the minimum z extent
     * @param zmax   the maximum z extent
     */
    public RightRegularPolyhedron(String name, int nsides, double rmin, double rmax, double zmin, double zmax)
    {
        super(name);
        if (nsides < 3)
            throw new RuntimeException("Cannot make a polyhedron with less than three sides!");
        _nsides = nsides;
        if (rmin >= rmax)
            throw new RuntimeException("Inner radius must be inside outer radius!");
        _rmin = rmin;
        _rmax = rmax;
        if (zmin >= zmax)
            throw new RuntimeException("Minimum z must be less than maximum z!");
        _zmin = zmin;
        _zmax = zmax;

        // need to calculate the circumscribed circle radius for the inner polygon
        double dPhi = 2. * PI / ((double) _nsides);
        double r = _rmin / cos(dPhi / 2.);
        _innerPolygon = new RegularPolygon(_nsides, r);
        _outerPolygon = new RegularPolygon(_nsides, _rmax);
        _isHollow = true;
    }

    /**
     * Fully qualified constructor for a solid right regular polyhedron.
     *
     * @param name   the name of this solid
     * @param nsides the number of sides of the regular polygon forming the ends.
     * @param radius the radius of the outer polygon's circumscribed circle
     * @param zmin   the minimum z extent
     * @param zmax   the maximum z extent
     */
    public RightRegularPolyhedron(String name, int nsides, double radius, double zmin, double zmax)
    {
        super(name);
        if (nsides < 3)
            throw new RuntimeException("Cannot make a polyhedron with less than three sides!");
        _nsides = nsides;
        _rmax = radius;
        if (zmin >= zmax)
            throw new RuntimeException("Minimum z must be less than maximum z!");
        _zmin = zmin;
        _zmax = zmax;
        _outerPolygon = new RegularPolygon(_nsides, _rmax);
        _isHollow = false;
    }

    public double getCubicVolume()
    {
        return volume();
    }

    /**
     * Determines whether a point lies within or outside of this solid.
     * 
     * @param pos the point to check
     * @return an enumeration of INSIDE or OUTSIDE ( SURFACE is reported as INSIDE)
     */
    public Inside inside(Hep3Vector pos)
    {
        //fail fast
        double z = pos.z();
        if (z < _zmin || z > _zmax)
        {
            return Inside.OUTSIDE;
        }

        double x = pos.x();
        double y = pos.y();
        double r2 = x * x + y * y;
        if (r2 > _rmax * _rmax)
            return Inside.OUTSIDE;
        if (r2 < _rmin * _rmin)
            return Inside.OUTSIDE;

        //need to be inside the outer polygon...
        Inside outer = _outerPolygon.inside(pos);
        if (outer.compareTo(Inside.OUTSIDE) == 0)
            return Inside.OUTSIDE;

        // and not inside the inner polygon
        if (_isHollow)
        {
            Inside inner = _innerPolygon.inside(pos);
            if (inner.compareTo(Inside.INSIDE) == 0)
                return Inside.OUTSIDE;
        }
        return Inside.INSIDE;
    }

    /**
     * Returns the area of the polygonal face
     * @return the area of polygon forming the face of this solid
     */
    public double polygonalArea()
    {
        double area = _outerPolygon.area();
        if (_isHollow)
            area -= _innerPolygon.area();
        return area;
    }

    /**
     * Is this a hollow polygon?
     * @return true if this polygon is hollow
     */
    public boolean isHollow()
    {
        return _isHollow;
    }

    /**
     * Returns the volume of this solid
     * @return the volume of this solid
     */
    public double volume()
    {
        return polygonalArea() * (_zmax - _zmin);
    }

    @Override
    public String toString()
    {
        NumberFormat formatter = new DecimalFormat("#0.00");

        StringBuffer sb = new StringBuffer("RightRegularAnnularPolyhedron\n");
        sb.append(" with " + _nsides + " sides from r= " + _rmin + " to " + _rmax + ": \n");
        sb.append(_outerPolygon.toString() + "\n");
        if (_isHollow)
            sb.append(_innerPolygon.toString() + "\n");
        sb.append("volume= " + volume() + "\n");
        return sb.toString();
    }

    /**
     *
     * @return the number of equal sides of the polygonal faces
     */
    public int getNumberOfSides()
    {
        return _nsides;
    }

    /**
     *
     * @return the minimum z extent of this solid
     */
    public double getZMin()
    {
        return _zmin;
    }

    /**
     *
     * @return the maximal z extent of this solid
     */
    public double getZMax()
    {
        return _zmax;
    }

    /**
     * The radius of the inscribe polygon if hollow.
     * Returns zero if the polygon is not hollow
     * @return the radius of the inscribed circle
     */
    public double getRMin()
    {
        return _rmin;
    }

    /**
     *
     * @return the radius of the circumscribed circle
     */
    public double getRMax()
    {
        return _rmax;
    }

    /**
     * The polygon representing the 2D end faces of the
     * inner boundary of this solid. Note that z=0.
     * @return  null if the solid is not hollow.
     */
    public RegularPolygon getInnerPolygon()
    {
        return _innerPolygon;
    }

    /**
     * The polygon representing the 2D end faces. Note that z=0.
     * @return The polygon representing the end faces
     *
     */
    public RegularPolygon getOuterPolygon()
    {
        return _outerPolygon;
    }

    private double t(Hep3Vector a, Hep3Vector b, Hep3Vector c)
    {
        double t = (a.y() - b.y()) * (c.x() - b.x()) - (a.x() - b.x()) * (c.y() - b.y());
        return t;
    }
}
