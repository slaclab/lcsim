package org.lcsim.recon.tracking.spacegeom;

/** A point in 2-space. The default Space point is at the origin.
 * Derived classes can be used to set values in cartesian or cylindrical
 * coordinates.  Point is set in the constructor and cannot be changed.
 * The methods simply return different coordinates.
 * Transformations (translations, rotations, etc) are carried out by
 * external functions which return new TwoSpacePoints.
 *@author Norman A. Graf
 *@version 1.0
 */
public class TwoSpacePoint
{
    
    // data
    protected double _x;
    protected double _y;
    protected double _xy;
    protected double _phi;
    
    // static methods
    
    // Return if two doubles are close enough to be considered equal.
    // This is used to compare components for operator==.
    private static boolean equal(double x1, double x2)
    {
        //  	  double maxdif = 2.0*std::numeric_limits<double>::epsilon();
        double maxdif = 5.0e-16;
        double num = 2.0*(x2-x1);
        double den = x2 + x1;
        double dif = Math.abs(num/den);
        return dif <= maxdif;
    }
    
    // methods
    
    /** Default constructor.
     * Sets point to be the origin with phi =  0.
     */
    public TwoSpacePoint( )
    {
        _x = _y = 0.0;
        _xy = 0.0;
        _phi = 0.0;
    }
    
    // Cartesian x.
    /**
     * @return Cartesian x coordinate
     */
    public double x( )
    {
        return _x;
    }
    
    // Cartesian y.
    /**
     * @return Cartesian y coordinate
     */
    public double y( )
    {
        return _y;
    }
    
    // Cylindrical r.
    /**
     * @return  Cylindrical radius coordinate
     */
    public double rxy( )
    {
        return _xy;
    }
    
    // Cylindrical phi.
    /**
     * @return Cylindrical phi coordinate
     */
    public double phi( )
    {
        return _phi;
    }
    
    // cos(phi)
    /**
     * @return Cosine of Cylindrical phi coordinate
     */
    public double cosPhi( )
    {
        if ( _xy != 0. ) return _x/_xy;
        return Math.cos(_phi);
    }
    
    // sin(phi)
    /**
     * @return Sine of Cylindrical phi coordinate
     */
    public double sinPhi( )
    {
        if ( _xy != 0. ) return _y/_xy;
        return Math.sin(_phi);
    }
    
    /**
     * @return String representation of this class
     */
    public String toString()
    {
        return "TwoSpacePoint: \n    x: " + x() + " \n"
                + "    y: " + y() + "\n"
                + "  rxy: " + rxy() +  "\n"
                + "  phi: " + phi();
    }
    
    /** Checks object equality
     * @param tsp  TwoSpacePoint to compare with for equality
     * @return true of this TwoSpacePoint equals tsp
     */
    public boolean equals( TwoSpacePoint tsp )
    {
        return ( equal(x(),tsp.x()) && equal(y(),tsp.y()) );
    }
    
    /** Checks object inequality
     * @param tsp TwoSpacePoint to compare with for inequality
     * @return true of this TwoSpacePoint does not equal tsp
     */
    public boolean notEquals( TwoSpacePoint tsp )
    {
        return !equals(tsp);
    }
    
    // Return the distance between two space points.
    /**
     * @param tsp1 first TwoSpacePoint
     * @param tsp2 second TwoSpacePoint
     * @return Two diensional Euclidean distance between points
     */
    public static double distance(TwoSpacePoint tsp1, TwoSpacePoint tsp2 )
    {
        double dx = tsp2.x() - tsp1.x();
        double dy = tsp2.y() - tsp1.y();
        return Math.sqrt( dx*dx + dy*dy );
    }
    
}