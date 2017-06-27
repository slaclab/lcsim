/*
 * SurfCylinder_Test.java
 *
 * Created on July 24, 2007, 7:58 PM
 *
 * $Id: SurfCylinder_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;

/**
 *
 * @author Norman Graf
 */
public class SurfCylinder_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of SurfCylinder_Test */
    public void testSurfCylinder()
    {
        String ok_prefix = "SurfCylinder test (I): ";
        String error_prefix = "SurfCylinder test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "------ Testing component SurfCylinder. ------");
        
        if(debug) System.out.println(ok_prefix + "Check indices.");
        Assert.assertTrue( SurfCylinder.RADIUS == 0 );
        Assert.assertTrue( SurfCylinder.IPHI == 0 );
        Assert.assertTrue( SurfCylinder.IZ   == 1 );
        Assert.assertTrue( SurfCylinder.IALF == 2 );
        Assert.assertTrue( SurfCylinder.ITLM == 3 );
        Assert.assertTrue( SurfCylinder.IQPT == 4 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor and get_parameter.");
        double r1 = 12.34;
        SurfCylinder scy1 = new SurfCylinder(r1);
        if(debug) System.out.println(scy1);
        if ( scy1.parameter(SurfCylinder.RADIUS) != r1 )
        {
            if(debug) System.out.println(error_prefix + "Incorrect radius.");
            System.exit(1);
        }
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test type.");
        if(debug) System.out.println(SurfCylinder.staticType());
        if(debug) System.out.println(scy1.type());
        Assert.assertTrue( scy1.type() != null);
        Assert.assertTrue( scy1.type().equals(SurfCylinder.staticType()) );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test equality and ordering.");
        SurfCylinder scy2 = new SurfCylinder(12.34);
        SurfCylinder scy3 = new SurfCylinder(24.68);
        Assert.assertTrue( scy1.boundEqual(scy2) );
        Assert.assertTrue( scy1.pureEqual(scy2) );
        Assert.assertTrue( ! scy1.boundEqual(scy3) );
        Assert.assertTrue( ! scy1.pureEqual(scy3) );
        Assert.assertTrue( scy2.pureLessThan(scy3) );
        Assert.assertTrue( ! scy3.pureLessThan(scy2) );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test virtual constructor.");
        SurfCylinder scy4 = (SurfCylinder) scy1.newPureSurface();
        if ( !scy1.equals(scy4) )
        {
            if(debug) System.out.println(error_prefix + "Virtual construction failed.");
            System.exit(5);
        }
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test crossing status.");
        TrackVector tvec = new TrackVector();
        tvec.set(SurfCylinder.IPHI,1.0);
        tvec.set(SurfCylinder.IZ,2.0);
        tvec.set(SurfCylinder.IALF,3.0);
        tvec.set(SurfCylinder.ITLM,4.0);
        tvec.set(SurfCylinder.IQPT,5.0);
        SurfCylinder scin = new SurfCylinder(6.0);
        SurfCylinder sout = new SurfCylinder(56.0);
        VTrack ton = new VTrack( scy1.newPureSurface(), tvec );
        VTrack tin = new VTrack( scin.newPureSurface(), tvec );
        VTrack tout = new VTrack( sout.newPureSurface(), tvec );
        CrossStat xs1 = scy1.pureStatus(ton);
        if(debug) System.out.println(xs1);
        
        Assert.assertTrue( xs1.at() && xs1.on() && !xs1.inside() && !xs1.outside()
        && !xs1.inBounds() && ! xs1.outOfBounds() );
        CrossStat xs2 = scy1.pureStatus(tin);
        Assert.assertTrue( !xs2.at() && !xs2.on() && xs2.inside() && !xs2.outside()
        && !xs2.inBounds() && ! xs2.outOfBounds() );
        CrossStat xs3 = scy1.pureStatus(tout);
        if(debug) System.out.println(xs3);
        Assert.assertTrue( !xs3.at() && !xs3.on() && !xs3.inside() && xs3.outside()
        && !xs3.inBounds() && ! xs3.outOfBounds() );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test vector difference.");
        TrackVector tvec2 = new TrackVector();
        tvec2.set(0,1.1 + 2.0*TRFMath.TWOPI);
        tvec2.set(1,2.2);
        tvec2.set(2,3.3);
        tvec2.set(3,4.4);
        tvec2.set(4,5.5);
        TrackVector diff = scy1.vecDiff(tvec2,tvec);
        if(debug) System.out.println(tvec2);
        if(debug) System.out.println(tvec);
        if(debug) System.out.println(diff);
        TrackVector ediff = new TrackVector();
        ediff.set(0,0.1);
        ediff.set(1,0.2);
        ediff.set(2,0.3);
        ediff.set(3,0.4);
        ediff.set(4,0.5);
        TrackVector zero = diff.minus(ediff);
        if(debug) System.out.println(ediff);
        if(debug) System.out.println(zero);
        
        if ( zero.amax() > 1.e-10 )
        {
            if(debug) System.out.println(error_prefix + "Incorrect difference.");
            System.exit(9);
        }
        
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test space point.");
        SpacePoint spt = ton.spacePoint();
        if(debug) System.out.println(spt);
        Assert.assertTrue( spt.rxy() == 12.34 );
        Assert.assertTrue( spt.phi() == 1.0 );
        Assert.assertTrue( spt.z() == 2.0 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test space vector.");
        SpacePath svec = ton.spacePath();
        double lam = Math.atan(4.0);
        if(debug) System.out.println(svec);
        if(debug) System.out.println("svec.rxy()= "+svec.rxy());
        Assert.assertTrue( svec.rxy() == 12.34 );
        Assert.assertTrue( svec.phi() == 1.0 );
        Assert.assertTrue( svec.z() == 2.0 );
        Assert.assertTrue( myequal( svec.drxy(),     Math.cos(lam)*Math.cos(3.0), debug ) );
        Assert.assertTrue( myequal( svec.rxy_dphi(), Math.cos(lam)*Math.sin(3.0), debug ) );
        Assert.assertTrue( myequal( svec.dz(),       Math.sin(lam), debug          ) );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test new surface.");
        if(debug) System.out.println(scy1.newSurface());
        Assert.assertTrue(scy1.newSurface()!=scy1);
        Assert.assertTrue(scy1.newSurface().equals(scy1));
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------");
        
        //********************************************************************
    }
    // comparison of doubles
    public static boolean myequal(double x1, double x2, boolean debug)
    {
        double small = 1.e-12;
        if ( Math.abs(x1-x2) < small ) return true;
        if(debug) System.out.println("myequal: difference too large:");
        if(debug) System.out.println("value 1: " + x1);
        if(debug) System.out.println("value 2: " + x2);
        if(debug) System.out.println("   diff: " + (x1-x2));
        if(debug) System.out.println("maxdiff: " + small);
        return false;
    }
}
