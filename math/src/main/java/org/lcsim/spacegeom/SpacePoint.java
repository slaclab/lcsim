package org.lcsim.spacegeom;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.abs;
import static org.lcsim.spacegeom.Representation.Cartesian;
import hep.physics.vec.Hep3Vector;

import java.io.Serializable;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

/**
 * A SpacePoint represents a concrete point in 3D Space,
 * much like a coordinate.
 * SpacePoint objects know about their representation in 
 * cartesian, spherical and cylindrical coordinates.
 * For interoperability with Freehep classes SpacePoint implements the Hep3Vector interface.
 * 
 * In distinction to vectors, two points cannot be added, and multiplication with a scalar is not defined, neither is the addition of two points.
 *@version $Id: SpacePoint.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
public class SpacePoint implements Serializable, Cloneable, Hep3Vector
{
    Representation _representation;
    double _x;
    double _y;
    double _z;
    double _xy;
    double _xyz;
    double _phi;
    double _theta;
    
    public double[] v()
    {
        switch(_representation)
        {
            case Cartesian: return new double[] {_x, _y, _z};
            case Spherical: return new double[] {_xyz, _phi, _theta};
            case Cylindrical: return new double[] {_xy, _phi, _z};
            default: return new double[3];
        }
    }
    
    private void cartesianToCylindricalR()
    {
        _xy  = Math.sqrt(_x*_x+_y*_y);
    }
    
    private void cartesianToPhi()
    {
        _phi = Math.atan2(_y,_x);
    }
    
    private void cartesianToTheta()
    {
        if (Double.isNaN(_xy))
            cartesianToCylindricalR();
        _theta = Math.atan2(_xy,_z);
    }
    
    public double magnitude()
    {
        return _xyz;
    }
    
    public double magnitudeSquared()
    {
        return _xyz*_xyz;
    }
    
    
    /**
     * Default constructor.
     * Sets point to be the origin.
     */
    public SpacePoint()
    {
        _representation = Cartesian;
        _x = _y = _z = 0.0;
        _xy = _xyz = 0.0;
        _phi = _theta = 0.0;
    }
    
    /**
     *Copy constructor
     *
     * @param   spt SpacePoint to copy
     */
    public SpacePoint( SpacePoint spt )
    {
        _representation = spt._representation;
        _x = spt.x();
        _y = spt.y();
        _z = spt.z();
        _xy = spt.rxy();
        _xyz = spt.rxyz();
        _phi = spt.phi();
        _theta = spt.theta();
    }
    
    public SpacePoint(Hep3Vector vec)
    {
        _representation = Cartesian;
        _x = vec.x();
        _y = vec.y();
        _z = vec.z();
        _xyz = sqrt(_x*_x + _y*_y + _z*_z);
        _xy = _phi = _theta = Double.NaN;
    }
    
    private void cylindricalToCartesianX()
    {
        _x = _xy*cos(_phi);
    }
    
    private void sphericalToCartesianX()
    {
        _x = _xyz*cos(_phi)*sin(_theta);
    }
    
    private void sphericalToCartesianY()
    {
        _y = _xyz*sin(_phi)*sin(_theta);
    }
    
    private void sphericalToCartesianZ()
    {
        _z = _xyz*cos(_theta);
    }
    
    /**
     * Cartesian x
     * @return double
     */
    public double x()
    {
        if (Double.isNaN(_x))
            switch(_representation)
            {
                case Spherical: sphericalToCartesianX(); break;
                case Cylindrical: cylindricalToCartesianX(); break;
            }
            return _x;
    }
    
    
    private void cylindricalToCartesianY()
    {
        _y = _xy*sin(_phi);
    }
    
    
    /**
     * Cartesian y
     * @return double
     */
    public double y()
    {
        if (Double.isNaN(_y))
            switch(_representation)
            {
                case Spherical: sphericalToCartesianY(); break;
                case Cylindrical: cylindricalToCartesianY(); break;
            }
            return _y;
    }
    
    /**
     * Cartesian z
     * @return double
     */
    
    public double z()
    {
        if (Double.isNaN(_z))
            sphericalToCartesianZ();
        return _z;
    }
    
    private void sphericalToCylindricalR()
    {
        _xy = _xyz*Math.sin(_theta);
    }
    
    /**
     * Cylindrical r
     * @return double
     */
    public double rxy()
    {
        if (Double.isNaN(_xy))
            switch(_representation)
            {
                case Spherical: sphericalToCylindricalR(); break;
                case Cartesian: cartesianToCylindricalR(); break;
            }
            return _xy;
    }
    
    /**
     * Cylindrical phi
     * @return double
     */
    public double phi()
    {
        if (Double.isNaN(_phi))
            cartesianToPhi();
        return _phi;
    }
    
    /**
     * Spherical r
     * @return double
     */
    public double rxyz()
    {
        return _xyz;
    }
    
    /**
     * Spherical theta
     * @return double
     */
    public double theta()
    {
        if (Double.isNaN(_theta))
            switch(_representation)
            {
                case Cartesian: cartesianToTheta(); break;
                case Cylindrical: cylindricalToTheta(); break;
            }
            return _theta;
    }
    
    private void cylindricalToTheta()
    {
        _theta = Math.atan2(_xy,_z);
    }
    
    /**
     * cos(phi)
     * @return double
     */
    public double cosPhi()
    {
        if ( !Double.isNaN(_x) && !Double.isNaN(_xy) && _xy != 0. )
            return _x/_xy;
        if (Double.isNaN(_phi))
            cartesianToPhi();
        return cos(_phi);
    }
    
    /**
     * sin(phi)
     * @return double
     */
    public double sinPhi()
    {
        if (!Double.isNaN(_y) && !Double.isNaN(_xy) && _xy != 0. )
            return _y/_xy;
        if (Double.isNaN(_phi))
            cartesianToPhi();
        return sin(_phi);
    }
    
    /**
     * sin(theta)
     * @return double
     */
    public double sinTheta()
    {
        if ( !Double.isNaN(_xy) && _xyz != 0. )
            return _xy/_xyz;
        if (Double.isNaN(_theta))
            switch(_representation)
            {
                case Cartesian: cartesianToTheta(); break;
                case Cylindrical: cylindricalToTheta(); break;
            }
            return sin(_theta);
    }
    
    /**
     * cos(theta)
     * @return double
     */
    public double cosTheta()
    {
        if ( !Double.isNaN(_z) && _xyz != 0. )
            return _z/_xyz;
        if (Double.isNaN(_theta))
            switch(_representation)
            {
                case Cartesian: cartesianToTheta(); break;
                case Cylindrical: cylindricalToTheta(); break;
            }
            return cos(_theta);
    }
    
    /**
     * Output Stream
     *
     * @return  String representation of object
     */
    public String toString()
    {
        return  _representation + " SpacePoint: " + "\n" +
                "    x: " + x()     + "\n" +
                "    y: " + y()     + "\n" +
                "    z: " + z()     + "\n" +
                "  rxy: " + rxy()   + "\n" +
                " rxyz: " + rxyz()  + "\n" +
                "  phi: " + phi()   + "\n" +
                "theta: " + theta() + "\n" ;
    }
    
    
    /**
     * Tests for equality within errors
     * @param spt a SpacePoint to compare against
     * @param precision the precision of the comparison
     * @return true if each of the components is within precision
     * of the components of spt 
     */
    public boolean equals(SpacePoint spt, double precision)
    {
        return ( abs(x() - spt.x()) < precision ) &&
                ( abs(y() - spt.y()) < precision ) &&
                ( abs(z() - spt.z()) < precision );
    }
    
    /**
     * Tests for equality within errors
     * @param spt a Hep3Vector to compare against
     * @param precision the precision of the comparison
     * @return true if each of the components is within precision
     * of the components of spt 
     */
    public boolean equals(Hep3Vector spt, double precision)
    {
        return ( abs(x() - spt.x()) < precision ) &&
                ( abs(y() - spt.y()) < precision ) &&
                ( abs(z() - spt.z()) < precision );
    }

    /**
     * Tests for equality
     * @param   x SpacePoint to compare
     * @return  true if objects are equal
     */
    public boolean equals(SpacePoint x) {
    	return equals(x, 1e-10);
    }
    
    /**
     *Inequality
     *
     * @param   spt  SpacePoint to compare
     * @return  true if objects are <em> not </em> equal
     */
    public boolean notEquals(SpacePoint spt)
    {
        return ! (equals(spt));
    }
    
    /**
     * Return the distance between two space points.
     * @param spt1 SpacePoint 1
     * @param spt2 SpacePoint 2
     * @return Euclidean distance between points
     */
    public static double distance(SpacePoint spt1, SpacePoint spt2)
    {
        double dx = spt2.x() - spt1.x();
        double dy = spt2.y() - spt1.y();
        double dz = spt2.z() - spt1.z();
        return Math.sqrt( dx*dx + dy*dy + dz*dz );
    }
    
   /**
     * Return the opening angle between two space points, assuming the point of reference is the origin
     * @param spt1 SpacePoint 1
     * @param spt2 SpacePoint 2
     * @return opening angle between points
     */
    public static double openingAngle(SpacePoint p1, SpacePoint p2)
    {
        // should check on point being non-zero...
        //recall that cos(theta) = a . b / |a|*|b|
        double dot = p1.x()*p2.x()+p1.y()*p2.y()+p1.z()*p2.z();
        
        double d1 = sqrt(p1.x()*p1.x()+p1.y()*p1.y()+p1.z()*p1.z());
        double d2 = sqrt(p2.x()*p2.x()+p2.y()*p2.y()+p2.z()*p2.z());
       
        return acos(dot/(d1*d2));
    }        
    
    /**
     *Clone
     *
     * @return  a copy of this object
     */
    public Object clone()
    {
        Object o = null;
        try
        {
            o = super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
        }
        return o;
    }

    /**
     * @return array of doubles, cartesian representation
     */
    public double[] getCartesianArray()
    {
        return new double[] {_x, _y, _z};
    }
    
    /**
     * @return the representations of the object
     */
    public Representation getRepresentation()
    {
        return _representation;
    }
}

