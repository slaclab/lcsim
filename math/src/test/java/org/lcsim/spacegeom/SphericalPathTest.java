package org.lcsim.spacegeom;
// CartesianPathTest.cpp

// Test the SpacePath class.

import junit.framework.TestCase;

import org.lcsim.spacegeom.CartesianPath;
import org.lcsim.spacegeom.CylindricalPath;
import org.lcsim.spacegeom.SpacePath;
import org.lcsim.spacegeom.SphericalPath;

public class SphericalPathTest extends TestCase
{
    //**********************************************************************
    boolean debug = false;
    private static final boolean myequal(double x1, double x2)
    {
        return Math.abs(x2-x1) < 1.e-10;
    }
    
    //**********************************************************************
    
    public void testSphericalPath()
    {
        
        String name = "SphericalPath";
        String ok_prefix = name + " test (I): ";
        String error_prefix = name + " test (E): ";
        
        if (debug) System.out.println( ok_prefix
                + "------- Testing component " + name + ". -------" );
        
        double x = 1.23;
        double y = 2.46;
        double z = 3.69;
        double dx = 0.23;
        double dy = 0.45;
        double dz = 0.67;
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing constructors." );
        // Create a space path element
        CartesianPath cart = new CartesianPath(x,y,z,dx,dy,dz);
        if (debug) System.out.println( cart );
        
        // Create in cylindrical coordinates.
        CylindricalPath cyl = new CylindricalPath( cart.getStartPoint().rxy(), cart.getStartPoint().phi(), cart.getStartPoint().z(),
                cart.drxy(), cart.rxy_dphi(), cart.dz() );
        if (debug) System.out.println( cyl );
        
        // Create in spherical coordinates.
        SphericalPath sph = new SphericalPath( cyl.getStartPoint().rxyz(), cyl.getStartPoint().phi(), cyl.getStartPoint().theta(),
                cyl.drxyz(), cyl.rxyz_dtheta(), cyl.rxy_dphi() );
        if (debug) System.out.println( sph );
        
        assertTrue( myequal(sph.getStartPoint().x(),x) );
        assertTrue( myequal(sph.getStartPoint().y(),y) );
        assertTrue( myequal(sph.getStartPoint().z(),z) );
        assertTrue( myequal(sph.dx(),dx) );
        assertTrue( myequal(sph.dy(),dy) );
        assertTrue( myequal(sph.dz(),dz) );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing assignment." );
        
        SpacePath dpth = new SpacePath();
        if (debug) System.out.println( dpth );
        assertTrue( dpth.magnitude() == 0.0 );
        dpth = sph;
        if (debug) System.out.println( dpth );
        assertTrue( myequal(dpth.getStartPoint().x(),x) );
        assertTrue( myequal(dpth.getStartPoint().y(),y) );
        assertTrue( myequal(dpth.getStartPoint().z(),z) );
        assertTrue( myequal(dpth.dx(),dx) );
        assertTrue( myequal(dpth.dy(),dy) );
        assertTrue( myequal(dpth.dz(),dz) );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix + "Testing normalizations." );
        double n0 = dx*dx + dy*dy + dz*dz;
        if (debug) System.out.println( n0 );
        double ncart = dpth.dx()*dpth.dx() +
                dpth.dy()*dpth.dy() +
                dpth.dz()*dpth.dz();
        if (debug) System.out.println( ncart );
        double ncyl = dpth.drxy()*dpth.drxy() +
                 // FIXME dpth.rxy_dphi()*dpth.rxy_dphi() +
                 dpth.dz()*dpth.dz();
        if (debug) System.out.println( ncyl );
        double nsph = dpth.drxyz()*dpth.drxyz();
                // FIXME dpth.rxy_dphi()*dpth.rxy_dphi() + dpth.rxyz_dtheta()*dpth.rxyz_dtheta();
        if (debug) System.out.println( nsph );
        assertTrue( myequal(ncart,n0) );
        assertTrue( myequal(ncyl,n0) );
        assertTrue( myequal(nsph,n0) );
        assertTrue( myequal( Math.sqrt(n0), dpth.magnitude() ) );
        
        //**********************************************************************
        
        if (debug) System.out.println( ok_prefix
                + "------------- All tests passed. ------------" );
        
    }
}
