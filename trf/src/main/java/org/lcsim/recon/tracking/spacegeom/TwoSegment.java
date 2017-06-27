package org.lcsim.recon.tracking.spacegeom;
import static java.lang.Math.atan2;
import static java.lang.Math.PI;
/**
 * A line segment in two dimensions.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class TwoSegment
{
    private TwoSpacePoint _a;
    private TwoSpacePoint _b;
    private double _length;
    
    /**
     * Constructor
     *
     * @param   a beginning TwoSpacePoint
     * @param   b ending TwoSpacePoint
     */
    public TwoSegment(TwoSpacePoint a, TwoSpacePoint b)
    {
        _a = a;
        _b = b;
        _length = TwoSpacePoint.distance(_a,_b);
    }
    
    
    /**
     * Fetch the starting point
     *
     * @return beginning TwoSpacePoint
     */
    public TwoSpacePoint startPoint()
    {
        return _a;
    }
    
    
    /**
     * Fetch the ending point
     *
     * @return  ending TwoSpacePoint
     */
    public TwoSpacePoint endPoint()
    {
        return _b;
    }
    
    
    /**
     * Return the length of the segment
     *
     * @return  length of the segment
     */
    public double length()
    {
        return _length;
    }
    
    /**
     * Return the azimuthal angle of this segment, the angle from point 1 to 2
     * zero & 3pi/4 is vertical
     * pi and 2pi is horizontal
     * @return the angle from point 1 to point 2
     */
    public double phi()
    {
        double x = _a.x() - _b.x();
        double y = _a.y() - _b.y();
        double phi = atan2(y,x);
        if(phi<0) phi+= 2.*PI;
        return phi;
    }
    
    
    /**
     * calculate the minimum distance between this segment and a point p
     *
     * @param   p The TwoSpacePoint
     * @return  the distance between the point and this segment
     */
    public double minimumDistance(TwoSpacePoint p)
    {
        double u = ( ( ( p.x() - _a.x() ) * ( _b.x() - _a.x() ) ) +
        ( ( p.y() - _a.y() ) * ( _b.y() - _a.y() ) ) )/(_length*_length);
        double xint = ( _a.x() + u * (_b.x() - _a.x()) ) - p.x();
        double yint = ( _a.y() + u * (_b.y() - _a.y()) ) - p.y();
        double dist = Math.sqrt( xint*xint + yint*yint );
        return dist;
    }
    
    
    
    /**
     * return intersection point of two line segments
     *
     * @param   a The first line segment
     * @param   b The second line segment
     * @return    The intersection point of the two segments.
     *            null if lines are parallel or intersect outside of segment boundaries.
     */
    public static TwoSpacePoint intersection(TwoSegment a, TwoSegment b)
    {
        // set some things up
        double x1 = a.startPoint().x();
        double y1 = a.startPoint().y();
        double x2 = a.endPoint().x();
        double y2 = a.endPoint().y();
        double x3 = b.startPoint().x();
        double y3 = b.startPoint().y();
        double x4 = b.endPoint().x();
        double y4 = b.endPoint().y();
        
        double denom = (y4-y3)*(x2-x1)-(x4-x3)*(y2-y1);
        
        if(denom==0) return null;// lines are parallel
        
        double numa = (x4-x3)*(y1-y3)-(y4-y3)*(x1-x3);
        
        if(numa==0) return null; // lines are coincident, no unique intersection
        
        double ua = numa/denom;
        
        if(ua<0 || ua >1.) return null; // intersect outside of segment a
        
        double numb = (x2-x1)*(y1-y3)-(y2-y1)*(x1-x3);
        double ub = numb/denom;
        if(ub<0 || ub >1.) return null; // intersect outside of segment b
        
        // should now have a valid intersection point...
        double xend = x3+ub*(x4-x3);
        double yend = y3+ub*(y4-y3);
        
        return new CartesianTwoPoint(xend, yend);
        
    }
    
    
    /** String representation of this object
     *
     *
     * @return  String representation of this object
     */
    public String toString()
    {
        return "TwoSegment from "+_a+" \n to "+_b +" \n length: "+_length;
    }
    
}
