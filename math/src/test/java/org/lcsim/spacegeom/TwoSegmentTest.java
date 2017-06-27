package org.lcsim.spacegeom;
// Test the TwoSegment class.
import junit.framework.TestCase;

import org.lcsim.spacegeom.CartesianTwoPoint;
import org.lcsim.spacegeom.TwoSegment;
import org.lcsim.spacegeom.TwoSpacePoint;

public class TwoSegmentTest extends TestCase
{
    boolean debug = false;
    public void testTwoSegmentTest()
    {
        
        String name = "TwoSegment";
        String ok_prefix = name + " test (I): ";
        String error_prefix = name + " test (E): ";
        
        if (debug) System.out.println( ok_prefix
                + "------- Testing component " + name + ". -------" );
        
        if (debug) System.out.println( ok_prefix + "Testing constructors." );
        
        TwoSpacePoint a = new CartesianTwoPoint(1.,1.);
        TwoSpacePoint b = new CartesianTwoPoint(2.,2.);
        TwoSegment seg = new TwoSegment(a,b);
        
        if (debug) System.out.println(seg);
        
        assertTrue(a.equals(seg.startPoint()));
        assertTrue(b.equals(seg.endPoint()));
        
        assertTrue(seg.length()==TwoSpacePoint.distance(a,b));
        
        
        if (debug) System.out.println( ok_prefix + "Testing intersection." );
        TwoSegment seg2 = new TwoSegment(a,b);
        
        assertTrue(TwoSegment.intersection(seg,seg2)==null);
        
        TwoSegment seg3 = new TwoSegment(new CartesianTwoPoint(-1.,-1.),new CartesianTwoPoint(1.,1.));
        TwoSegment seg4 = new TwoSegment(new CartesianTwoPoint(-1.,1.),new CartesianTwoPoint(1.,-1.));
        
        TwoSpacePoint intersection = TwoSegment.intersection(seg3, seg4);
        if (debug) System.out.println(intersection);
        assertTrue(intersection.x()==0.);
        assertTrue(intersection.y()==0.);
        
        
        // try another...
        TwoSegment t1 = new TwoSegment(new CartesianTwoPoint(0.25, 0), new CartesianTwoPoint(1.25, 2.0));
        TwoSegment t2 = new TwoSegment(new CartesianTwoPoint(0.05, 0), new CartesianTwoPoint(-0.947, 2.0));
        intersection = TwoSegment.intersection(t1, t2);
        if (debug) System.out.println(intersection);
        
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix
                + "------------- All tests passed. ------------" );
        
    }
    
}
