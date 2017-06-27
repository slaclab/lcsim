package org.lcsim.spacegeom;
import junit.framework.TestCase;

import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.CylindricalPoint;
import org.lcsim.spacegeom.SphericalPoint;

import org.lcsim.spacegeom.SpacePoint;

public class CartesianPointTest extends TestCase
{
    boolean debug = false;
    // SpacePointTest.java
    
    // Test the SpacePoint class.
    
    public static boolean myequal(double x1, double x2)
    {
        return Math.abs(x2-x1) < 1.e-12;
    }
    
    //**********************************************************************
    double x;
    double y;
    double z;
    String name;
    String ok_prefix;
    String error_prefix;
    
    public void setUp() throws Exception {
        super.setUp();
        x = 1.23;
        y = 2.46;
        z = 3.69;
     
        name = "CartesianPoint";
        ok_prefix = name + " test (I): ";
        error_prefix = name + " test (E): ";
        if (debug) System.out.println(" ----- Testing component " + name + ". -------");
    }
    
    public void testCartesianPoint() {
        if (debug) System.out.println(ok_prefix + "Testing constructors.");
        // Create a space point
        CartesianPoint cart = new CartesianPoint(x,y,z);
        if (debug) System.out.println(cart);
        
        // Create in cylindrical coordinates.
        CylindricalPoint cyl = new CylindricalPoint( cart.rxy(), cart.phi(), cart.z() );
        if (debug) System.out.println(cyl);
        
        // Create in spherical coordinates.
        SphericalPoint sph = new SphericalPoint( cyl.rxyz(), cyl.phi(), cyl.theta() );
        if (debug) System.out.println(sph);
        
        assertTrue( myequal(sph.x(),x) ||
                myequal(sph.y(),y) ||
                myequal(sph.z(),z) );
        if (debug) System.out.println( ok_prefix + "Testing assignment.");
        SpacePoint spt = new SpacePoint();
        if (debug) System.out.println(spt);
        spt = sph;
        if (debug) System.out.println(spt);
        assertTrue( myequal(spt.x(),sph.x()) ||
                myequal(spt.y(),sph.y()) ||
                myequal(spt.z(),sph.z()) );
        //**********************************************************************
        
        if (debug) System.out.println(ok_prefix + "Test cos and sin returns.");
        assertTrue( myequal( spt.cosPhi(), Math.cos( spt.phi() ) ));
        assertTrue( myequal( spt.sinPhi(), Math.sin( spt.phi() ) ));
        assertTrue( myequal( spt.cosTheta(), Math.cos( spt.theta() ) ));
        assertTrue( myequal( spt.sinTheta(), Math.sin( spt.theta() ) ));
        
        //**********************************************************************
        
        if (debug) System.out.println(ok_prefix + "Test equality.");
        CartesianPoint spt2 = new CartesianPoint(spt.x(),spt.y(),spt.z());
        assertTrue( spt.equals(spt2));
        assertTrue( !spt.notEquals(spt2) );
        if (debug) System.out.println(" spt = spt2" );
        CartesianPoint spt3 = new CartesianPoint(spt.x()+0.1,spt.y(),spt.z());
        assertTrue( spt.notEquals(spt3) );
        assertTrue( !spt.equals(spt3) );
        if (debug) System.out.println(" spt != spt3" );
        CartesianPoint spt4 = new CartesianPoint(spt.x(),spt.y()+0.2,spt.z());
        assertTrue( spt.notEquals(spt4));
        if (debug) System.out.println(" spt != spt4" );
        CartesianPoint spt5 = new CartesianPoint(spt.x(),spt.y(),spt.z()+0.3);
        assertTrue( spt.notEquals(spt5));
        if (debug) System.out.println(" spt != spt5" );
        //**********************************************************************
        
        if (debug) System.out.println(ok_prefix + "Test distance function.");
        assertTrue( SpacePoint.distance(cart,cart) == 0.0 );
        double dx = 1.1;
        double dy = 2.2;
        double dz = 3.3;
        double dist0 = Math.sqrt(dx*dx+dy*dy+dz*dz);
        CartesianPoint cart2 = new CartesianPoint(x+dx,y+dy,z+dz);
        if (debug) System.out.println(cart2);
        double dist = SpacePoint.distance(cart,cart2);
        if (debug) System.out.println("Distance = " + dist0 + " " + dist);
        assertTrue( Math.abs( dist - dist0 ) < 1.e-12 );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix
                + "------------- All tests passed. ------------");
    }
}


