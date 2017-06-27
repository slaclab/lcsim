package org.lcsim.spacegeom;
/**
 * A class to handle operations in a 2D plane.
 *
 *@author Norman A. Graf
 *@version $Id: TwoD.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 *
 */
public class TwoD
{

    /**
     * Twice the signed area of the triangle determined by a,b,c; positive if counterclockwise, negative if clockwisew
     * @param a First point on the triangle.
     * @param b Second point on the triangle.
     * @param c Third point on the triangle.
     * @return the are of the traingle, positive if counterclockwise, negative if clockwisew
     */
    public static double area2(TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c)
    {
        double area=((c.x() - b.x())*(a.y() - b.y())) - ((a.x() - b.x())*(c.y() - b.y()));
        return area;
    }
    
    public static int areaSign( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c )
    {
        double area2;
        
        area2 = ( b.x() - a.x() ) * ( c.y() - a.y() ) -
                ( c.x() - a.x() ) * ( b.y() - a.y() );
        
        
        /* The area sign should be an integer. */
        if      ( area2 >  0.5 ) return  1;
        else if ( area2 < -0.5 ) return -1;
        else                     return  0;
    }

    /**
     * Determines whether a point is to the left of a line segment define by its endpoints.
     * @param a The first point on the segment.
     * @param b The second point on the segment.
     * @param c The point to check.
     * @return true if and only if the point c is to the left of the directed line through a and b.
     */
    public static boolean left( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c )
    {
        return  areaSign( a, b, c ) > 0;
    }
    
    /**
     * Determines whether a point is to the left or on a line segment define by its endpoints.
     * @param a The first point on the segment.
     * @param b The second point on the segment.
     * @param c The point to check.
     * @return true if and only if the point c is to the left or on the directed line through a and b.
     */
    public static boolean leftOn( TwoSpacePoint a, TwoSpacePoint b , TwoSpacePoint c)
    {
        return  areaSign( a, b, c) >= 0;
    }
    
    /**
     * Determines whether a point is on or collinear with a line segment define by its endpoints.
     * @param a The first point on the segment.
     * @param b The second point on the segment.
     * @param c The point to check.
     * @return true if and only if the point c is on or collinear with the directed line through a and b.
     */
    public static boolean collinear( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c)
    {
        return  areaSign( a, b, c) == 0;
    }
    
    /**
     * Determines whether a point is on a line segment define by its endpoints.
     * @param a The first point on the segment.
     * @param b The second point on the segment.
     * @param c The point to check.
     * @return true if and only if the point c is on the closed line segment through a and b.
     */
    public static boolean between( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c)
    {
        TwoSpacePoint      ba, ca;
        
        if ( ! collinear( a, b, c) )
            return  false;
        
        /* If ab not vertical, check betweenness on x; else on y. */
        if ( a.x() != b.x() )
            return ((a.x() <= c.x()) && (c.x() <= b.x())) ||
                    ((a.x() >= c.x()) && (c.x() >= b.x()));
        else
            return ((a.y() <= c.y()) && (c.y() <= b.y())) ||
                    ((a.y() >= c.y()) && (c.y() >= b.y()));
    }

    /**
     * Determines whether two lines defined by their segments intersect.
     * @param a The first point on the first line segment.
     * @param b The second point on the first line segment.
     * @param c The first point on the second line segment.
     * @param d The second point on the second line segment.
     * @return true if and only if the segments ab and cd intersect, properly or improperly.
     */
    public static boolean  intersect( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c, TwoSpacePoint d )
    {
        if      ( intersectProp( a, b, c, d ) )
            return  true;
        
        else if (   between( a, b, c)
        || between( a, b, d )
        || between( c, d, a )
        || between( c, d, b ) )
            return  true;
        
        else
            return  false;
    }
    
     /**
     * Determines whether two line segments intersect.
     * @param a The first point on the first line segment.
     * @param b The second point on the first line segment.
     * @param c The first point on the second line segment.
     * @param d The second point on the second line segment.
     * @return true if and only if the segments ab and cd intersect properly.
     */
    public static boolean intersectProp( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c, TwoSpacePoint d )
    {
        /* Eliminate improper cases. */
        if (
                collinear(a,b,c) ||
                collinear(a,b,d) ||
                collinear(c,d,a) ||
                collinear(c,d,b))
            return false;
        
        return
                xOr( left(a,b,c), left(a,b,d) )
                && xOr( left(c,d,a), left(c,d,b) );
    }
    
        /*---------------------------------------------------------------------
         *Exclusive or: true iff exactly one argument is true.
         */

    public static boolean xOr( boolean x, boolean y )
    {
        /* The arguments are negated to ensure that they are 0/1 values. */
        /* (Idea due to Michael Baldwin.) */
        return   !x ^ !y;
    }
    
        /*---------------------------------------------------------------------
        segSegInt: Finds the point of intersection p between two closed
        segments ab and cd.  Returns p and a char with the following meaning:
        'e': The segments collinearly overlap, sharing a point.
        'v': An endpoint (vertex) of one segment is on the other segment,
        but 'e' doesn't hold.
        '1': The segments intersect properly (i.e., they share a point and
        neither 'v' nor 'e' holds).
        '0': The segments do not intersect (i.e., they share no points).
        Note that two collinear segments that share just one point, an endpoint
        of each, returns 'e' rather than 'v' as one might expect.
        ---------------------------------------------------------------------*/

    public char segSegInt( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c, TwoSpacePoint d, TwoSpacePoint p, TwoSpacePoint q )
    {
        double  s, t;       /* The two parameters of the parametric eqns. */
        double num, denom;  /* Numerator and denoninator of equations. */
        char code = '?';    /* Return char characterizing intersection. */
        //    p.x() = p.y() = 100.0;  /* For testing purposes only... */
        
        denom = a.x() * ( d.y() - c.y() ) +
                b.x() * ( c.y() - d.y() ) +
                d.x() * ( b.y() - a.y() ) +
                c.x() * ( a.y() - b.y() );
        
        /* If denom is zero, then segments are parallel: handle separately. */
        if (denom == 0.0)
            return  parallelInt(a, b, c, d, p, q);
        
        num =    a.x() * ( d.y() - c.y() ) +
                c.x() * ( a.y() - d.y() ) +
                d.x() * ( c.y() - a.y() );
        if ( (num == 0.0) || (num == denom) ) code = 'v';
        s = num / denom;
        System.out.println("SegSegInt: num=" + num + ",denom=" + denom + ",s="+s);
        
        num = -( a.x() * ( c.y() - b.y() ) +
                b.x() * ( a.y() - c.y() ) +
                c.x() * ( b.y() - a.y() ) );
        if ( (num == 0.0) || (num == denom) ) code = 'v';
        t = num / denom;
        System.out.println("SegSegInt: num=" +num + ",denom=" + denom + ",t=" + t);
        
        if      ( (0.0 < s) && (s < 1.0) &&
                (0.0 < t) && (t < 1.0) )
            code = '1';
        else if ( (0.0 > s) || (s > 1.0) ||
                (0.0 > t) || (t > 1.0) )
            code = '0';
        
        p._x = a.x() + s * ( b.x() - a.x() );
        p._y = a.y() + s * ( b.y() - a.y() );
        
        return code;
    }
    
    public char parallelInt( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c, TwoSpacePoint d, TwoSpacePoint p, TwoSpacePoint q )
    {
        if ( !collinear( a, b, c) )
            return '0';
        
        if ( between1( a, b, c ) && between1( a, b, d ) )
        {
            assigndi( p, c );
            assigndi( q, d );
            return 'e';
        }
        if ( between1( c, d, a ) && between1( c, d, b ) )
        {
            assigndi( p, a );
            assigndi( q, b );
            return 'e';
        }
        if ( between1( a, b, c ) && between1( c, d, b ) )
        {
            assigndi( p, c );
            assigndi( q, b );
            return 'e';
        }
        if ( between1( a, b, c ) && between1( c, d, a ) )
        {
            assigndi( p, c );
            assigndi( q, a );
            return 'e';
        }
        if ( between1( a, b, d ) && between1( c, d, b ) )
        {
            assigndi( p, d );
            assigndi( q, b );
            return 'e';
        }
        if ( between1( a, b, d ) && between1( c, d, a ) )
        {
            assigndi( p, d );
            assigndi( q, a );
            return 'e';
        }
        return '0';
                /*
                if ( Between1( a, b, c ) ) {
                Assigndi( p, c );
                return 'e';
                }
                if ( Between1( a, b, d ) ) {
                Assigndi( p, d );
                return 'e';
                }
                if ( Between1( c, d, a ) ) {
                Assigndi( p, a );
                return 'e';
                }
                if ( Between1( c, d, b ) ) {
                Assigndi( p, b );
                return 'e';
                }
                return '0';
                 */
    }
    
    public void assigndi( TwoSpacePoint p, TwoSpacePoint a )
    {
        p._x = a.x();
        p._y = a.y();
    }
    
        /*---------------------------------------------------------------------
        Returns TRUE iff point c lies on the closed segement ab.
        Assumes it is already known that abc are collinear.
        (This is the only difference with Between().)
        ---------------------------------------------------------------------*/

    public boolean between1( TwoSpacePoint a, TwoSpacePoint b, TwoSpacePoint c )
    {
        TwoSpacePoint      ba, ca;
        
        /* If ab not vertical, check betweenness on x; else on y. */
        if ( a.x() != b.x() )
            return ((a.x() <= c.x()) && (c.x() <= b.x())) ||
                    ((a.x() >= c.x()) && (c.x() >= b.x()));
        else
            return ((a.y() <= c.y()) && (c.y() <= b.y())) ||
                    ((a.y() >= c.y()) && (c.y() >= b.y()));
    }
}
