package org.lcsim.spacegeom;
// SpacePointVecTest.cpp

import junit.framework.TestCase;

import org.lcsim.spacegeom.CartesianPointVector;
import org.lcsim.spacegeom.CylindricalPointVector;
import org.lcsim.spacegeom.SpacePointVector;
import org.lcsim.spacegeom.SphericalPointVector;

// Test the SpacePointVector class.

public class CylindricalPointVectorTest extends TestCase
{
    boolean debug = false;
    //**********************************************************************
    
    public static boolean myequal(double x1, double x2)
    {
        return Math.abs(x2-x1) < 1.e-10;
    }
    
    //**********************************************************************
    
    public void testCylindricalPoint()
    {
        
        String name = "CylindricalPointVector";
        String ok_prefix = name + " test (I): ";
        String error_prefix = name + " test (E): ";
        
        if (debug) System.out.println( ok_prefix
                + "------- Testing component " + name + ". -------" );
        
        double x = 1.23;
        double y = 2.46;
        double z = 3.69;
        double vx = 0.23;
        double vy = 0.45;
        double vz = 0.67;
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing constructors." );
        // Create a spacevector
        CartesianPointVector cart = new CartesianPointVector(x,y,z,vx,vy,vz);
        if (debug) System.out.println( cart );
        
        // Create in cylindrical coordinates.
        CylindricalPointVector cyl = new CylindricalPointVector( cart.getStartPoint().rxy(), cart.getStartPoint().phi(), cart.getStartPoint().z(),
                cart.v_rxy(), cart.v_phi(), cart.v_z() );
        if (debug) System.out.println( cyl );
        
        // Create in spherical coordinates.
        SphericalPointVector sph = new SphericalPointVector( cyl.getStartPoint().rxyz(), cyl.getStartPoint().phi(), cyl.getStartPoint().theta(),
                cyl.v_rxyz(), cyl.v_theta(), cyl.v_phi() );
        if (debug) System.out.println( sph );
        
        assertTrue( myequal(sph.getStartPoint().x(),x) );
        assertTrue( myequal(sph.getStartPoint().y(),y) );
        assertTrue( myequal(sph.getStartPoint().z(),z) );
        assertTrue( myequal(sph.v_x(),vx) );
        assertTrue( myequal(sph.v_y(),vy) );
        assertTrue( myequal(sph.v_z(),vz) );
        
        // Create a cartesian coordinates from spacepoint
        CartesianPointVector cart2 = new CartesianPointVector( sph.getStartPoint(), sph.v_x(), sph.v_y(), sph.v_z() );
        if (debug) System.out.println( cart2 );
        
        // Create in cylindrical coordinates.
        CylindricalPointVector cyl2 = new CylindricalPointVector( cart2.getStartPoint(),
                cart2.v_rxy(), cart2.v_phi(), cart2.v_z() );
        if (debug) System.out.println( cyl2 );
        
        // Create in spherical coordinates.
        SphericalPointVector sph2 = new SphericalPointVector( cyl2.getStartPoint(),
                cyl2.v_rxyz(), cyl2.v_theta(), cyl2.v_phi() );
        if (debug) System.out.println( sph2 );
        
        assertTrue( myequal(sph2.getStartPoint().x(),x) );
        assertTrue( myequal(sph2.getStartPoint().y(),y) );
        assertTrue( myequal(sph2.getStartPoint().z(),z) );
        assertTrue( myequal(sph2.v_x(),vx) );
        assertTrue( myequal(sph2.v_y(),vy) );
        assertTrue( myequal(sph2.v_z(),vz) );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing equality." );
        assertTrue( sph.equals(sph) );
        assertTrue( sph.equals(sph2) );
        if( sph.notEquals(sph2) );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing assignment." );
        SpacePointVector svec = new SpacePointVector();
        if (debug) System.out.println( svec );
        if (debug) System.out.println( "testing assignment svec.magnitude() and 0 " + svec.magnitude() + " " + 0. );
        if (debug) System.out.println(  "spacepoint equal " + (svec.magnitude()==0.0) );
        //	Assert.assertTrue( SpacePoint.equal(svec.magnitude(),0.0) );
        svec = sph;
        if (debug) System.out.println( svec );
        if (debug) System.out.println( "testing assignment svec.x() and x " + svec.getStartPoint().x() + " " + x );
        assertTrue( myequal(svec.getStartPoint().x(),x) );
        assertTrue( myequal(svec.getStartPoint().y(),y) );
        assertTrue( myequal(svec.getStartPoint().z(),z) );
        assertTrue( myequal(svec.v_x(),vx) );
        assertTrue( myequal(svec.v_y(),vy) );
        assertTrue( myequal(svec.v_z(),vz) );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing normalizations." );
        double n0 = vx*vx + vy*vy + vz*vz;
        if (debug) System.out.println( "n0= " +n0 );
        double ncart = svec.v_x()*svec.v_x() +
                svec.v_y()*svec.v_y() +
                svec.v_z()*svec.v_z();
        if (debug) System.out.println( "ncart= " +ncart );
        double ncyl = svec.v_rxy()*svec.v_rxy() +
                // FIXME this isn't supposed to be here svec.v_phi()*svec.v_phi() +
                svec.v_z()*svec.v_z();
        if (debug) System.out.println( "ncyl= " +ncyl );
        double nsph = svec.v_rxyz()*svec.v_rxyz();
        // FIXME this shouldn't have worked before + svec.v_theta()*svec.v_theta() + svec.v_phi()*svec.v_phi();
        if (debug) System.out.println( "nsph= " +nsph );
        assertTrue( myequal(ncart,n0) );
        assertTrue( myequal(ncyl,n0) );
        assertTrue( myequal(nsph,n0) );
        assertTrue( myequal( Math.sqrt(n0), svec.magnitude() ) );
        //
        if (debug) System.out.println("Testing copy constructor and clone");
        SpacePointVector spv = (SpacePointVector) svec.clone();
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix
                + "------------- All tests passed. ------------" );
    }
    
}