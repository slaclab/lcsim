package org.lcsim.spacegeom;
//package spacegeom;
// Test the spacepoint class.
import junit.framework.TestCase;

import org.lcsim.spacegeom.CartesianTwoPoint;
import org.lcsim.spacegeom.CylindricalTwoPoint;
import org.lcsim.spacegeom.TwoSpacePoint;


public class CartesianTwoPointTest extends TestCase
{
    boolean debug = false;
    private static boolean myequal(double x1, double x2)
    {
        return (Math.abs(x2-x1) < 1.e-12);
    }
    
    //**********************************************************************
    
    public void testCartesianPoint()
    {
        
        String name = "CartesianTwoPoint";
        String ok_prefix = name + " test (I): ";
        String error_prefix = name + " test (E): ";
        
        if (debug) System.out.println( ok_prefix
                + "------- Testing component " + name + ". -------" );
        
        double x = 1.23;
        double y = 2.46;
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing constructors." );
        // Create a space point
        CartesianTwoPoint cart = new CartesianTwoPoint(x,y);
        if (debug) System.out.println( cart );
        
        // Create in cylindrical coordinates.
        CylindricalTwoPoint cyl = new CylindricalTwoPoint( cart.rxy(), cart.phi() );
        if (debug) System.out.println( cyl );
        
        if ( ! myequal(cyl.x(),x) || ! myequal(cyl.y(),y) )
        {
            if (debug) System.out.println( error_prefix + "Mismatch." );
            System.exit(1);
        }
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing assignment." );
        TwoSpacePoint spt = new TwoSpacePoint();
        if (debug) System.out.println( spt );
        spt = cyl;
        if (debug) System.out.println( spt );
        if ( ! myequal(spt.x(),cyl.x()) || ! myequal(spt.y(),cyl.y()) )
        {
            if (debug) System.out.println( error_prefix + "Mismatch." );
            System.exit(1);
        }
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Test cos and sin returns." );
        assertTrue( myequal( spt.cosPhi(), Math.cos( spt.phi() ) ) );
        assertTrue( myequal( spt.sinPhi(), Math.sin( spt.phi() ) ) );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Test equality." );
        CartesianTwoPoint spt2 = new CartesianTwoPoint(spt.x(),spt.y());
        assertTrue( spt.equals(spt2) );
        assertTrue( ! (spt.notEquals(spt2)) );
        CartesianTwoPoint spt3 = new CartesianTwoPoint(spt.x()+0.1,spt.y());
        assertTrue( ! (spt.equals(spt3)) );
        assertTrue( spt.notEquals(spt3) );
        CartesianTwoPoint spt4 = new CartesianTwoPoint(spt.x(),spt.y()+0.2);
        assertTrue( spt.notEquals(spt4) );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Test distance function." );
        assertTrue( TwoSpacePoint.distance(cart,cart) == 0.0 );
        double dx = 1.1;
        double dy = 2.2;
        double dist0 = Math.sqrt(dx*dx+dy*dy);
        CartesianTwoPoint cart2 = new CartesianTwoPoint(x+dx,y+dy);
        if (debug) System.out.println( cart );
        if (debug) System.out.println( cart2 );
        double dist = TwoSpacePoint.distance(cart,cart2);
        if (debug) System.out.println( "Distance = " + dist0 + " " + dist );
        assertTrue( Math.abs( dist - dist0 ) < 1.e-12 );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix
                + "------------- All tests passed. ------------" );
        
    }
}
