package org.lcsim.recon.tracking.spacegeom;
import java.io.*;
import static java.lang.Math.sqrt;
import static java.lang.Math.acos;

/**  Describes a space point.
 * The default Space point is at the origin.
 * Derived classes can be used to set values in cartesian, cylindrical
 * or spherical coordinates.  Point is set in the constructor and
 * cannot be changed.  The methods simply return different coordinates.
 * Transformations (translations, rotations, etc) are carried out by
 * external functions which return new SpacePoints.
 *
 */

public class SpacePoint implements Serializable, Cloneable
{
    double _x;
    double _y;
    double _z;
    double _xy;
    double _xyz;
    double _phi;
    double _theta;
    
    /**
     * Default constructor.
     * Sets point to be the origin.
     */
    public SpacePoint()
    {
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
        _x = spt.x();
        _y = spt.y();
        _z = spt.z();
        _xy = spt.rxy();
        _xyz = spt.rxyz();
        _phi = spt.phi();
        _theta = spt.theta();
    }
    
    /**
     * Cartesian x
     * @return double
     */
    public double x()
    { 
        return _x;
    }
    
    /**
     * Cartesian y
     * @return double
     */
    public double y()
    { 
        return _y;
    }
    
    /**
     * Cartesian z
     * @return double
     */
    
    public double z()
    { 
        return _z;
    }
    
    /**
     * Cylindrical r
     * @return double
     */
    public double rxy()
    { 
        return _xy;
    }
    
    /**
     * Cylindrical phi
     * @return double
     */
    public double phi()
    { 
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
        return _theta;
    }
    
    /**
     * cos(phi)
     * @return double
     */
    public double cosPhi()
    {
        if ( _xy!= 0. ) return _x/_xy;
        return Math.cos(_phi);
    }
    
    /**
     * sin(phi)
     * @return double
     */
    public double sinPhi()
    {
        if ( _xy != 0. ) return _y/_xy;
        return Math.sin(_phi);
    }
    
    /**
     * sin(theta)
     * @return double
     */
    public double sinTheta()
    {
        if ( _xyz != 0. ) return _xy/_xyz;
        return Math.sin(_theta);
    }
    
    /**
     * cos(theta)
     * @return double
     */
    public double cosTheta()
    {
        if ( _xyz != 0. ) return _z/_xyz;
        return Math.cos(_theta);
    }
    
    /**
     * Output Stream
     *
     * @return  String representation of object
     */
    public String toString()
    {
        return  "SpacePoint: " + "\n" +
                "    x: " + x()     + "\n" +
                "    y: " + y()     + "\n" +
                "    z: " + z()     + "\n" +
                "  rxy: " + rxy()   + "\n" +
                " rxyz: " + rxyz()  + "\n" +
                "  phi: " + phi()   + "\n" +
                "theta: " + theta() + "\n" ;
    }
    
    
    /**
     *Equality
     *
     * @param   spt SpacePoint to compare
     * @return  true if objects are equal
     */
    public boolean equals(SpacePoint spt)
    {
        return ( x()==spt.x() ) &&
                ( y()==spt.y() ) &&
                ( z()==spt.z() );
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
 
    // for legacy reasons
    // long name to make it visible in code
    /**
     * for legacy code
     * @deprecated
     */
    @Deprecated public double[] getCartesianArray() {
        return new double[] {_x, _y, _z};
    }
}

