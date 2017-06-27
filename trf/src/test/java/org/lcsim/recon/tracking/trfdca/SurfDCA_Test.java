/*
 * SurfDCA_Test.java
 *
 * Created on July 24, 2007, 9:03 PM
 *
 * $Id: SurfDCA_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfdca;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.spacegeom.CylindricalPath;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman Graf
 */
public class SurfDCA_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of SurfDCA_Test */
    public void testSurfDCA()
    {
        
        String ok_prefix = "test SurfDCA (I): ";
        String error_prefix = "test SurfDCA (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "------ Testing component SurfDCA. ------" );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Check indices." );
        Assert.assertTrue( SurfDCA.IRSIGNED == 0 );
        Assert.assertTrue( SurfDCA.IZ       == 1 );
        Assert.assertTrue( SurfDCA.IPHID    == 2 );
        Assert.assertTrue( SurfDCA.ITLM     == 3 );
        Assert.assertTrue( SurfDCA.IQPT     == 4 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor." );
        SurfDCA sdca1 = new SurfDCA();
        if(debug) System.out.println(sdca1 );
        SurfDCA sdca_x1 = new SurfDCA(0.1, 0.2);
        if(debug) System.out.println(sdca_x1 );
        SurfDCA sdca_x2 = new SurfDCA(0.1, 0.3, 0.01, 0.02);
        if(debug) System.out.println(sdca_x2 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test equality and ordering." );
        Assert.assertTrue( sdca1.pureEqual(sdca1) );
        Assert.assertTrue( ! sdca1.pureLessThan(sdca1) );
        Assert.assertTrue( sdca_x1.pureEqual(sdca_x1) );
        Assert.assertTrue( ! sdca_x1.pureLessThan(sdca_x1) );
        Assert.assertTrue( sdca_x2.pureEqual(sdca_x2) );
        Assert.assertTrue( ! sdca_x2.pureLessThan(sdca_x2) );
        
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test virtual constructor." );
        SurfDCA psdca4 = (SurfDCA) sdca1.newPureSurface();
        if ( !sdca1.equals(psdca4) )
        {
            if(debug) System.out.println(error_prefix + "Virtual construction failed." );
            System.exit(5);
        }
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test type." );
        if(debug) System.out.println(SurfDCA.staticType() );
        // method get_type is implemented in class Surface
        // it returns get_pure_type, which is implemented in SurfDCA
        if(debug) System.out.println(sdca1.type() );
        Assert.assertTrue( sdca1.type() != null );
        Assert.assertTrue( sdca1.type().equals(SurfDCA.staticType()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test parameter access." );
        if(debug) System.out.println(sdca_x2.parameter(SurfDCA.IX));
        Assert.assertTrue( sdca_x2.parameter(SurfDCA.IX) == 0.1);
        Assert.assertTrue( sdca_x2.parameter(SurfDCA.IY) == 0.3);
        Assert.assertTrue( sdca_x2.parameter(SurfDCA.IDXDZ) == 0.01);
        Assert.assertTrue( sdca_x2.parameter(SurfDCA.IDYDZ) == 0.02);
        Assert.assertTrue( sdca_x1.parameter(SurfDCA.IX) == 0.1);
        Assert.assertTrue( sdca_x1.parameter(SurfDCA.IY) == 0.2);
        Assert.assertTrue( sdca_x1.parameter(SurfDCA.IDXDZ) == 0.);
        Assert.assertTrue( sdca_x1.parameter(SurfDCA.IDYDZ) == 0.);
        Assert.assertTrue( sdca1.parameter(SurfDCA.IX) == 0.0);
        Assert.assertTrue( sdca1.parameter(SurfDCA.IY) == 0.0);
        Assert.assertTrue( sdca1.parameter(SurfDCA.IDXDZ) == 0.);
        Assert.assertTrue( sdca1.parameter(SurfDCA.IDYDZ) == 0.);
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "construct a Track Vector." );
        
        TrackVector tvec = new TrackVector();
        tvec.set(0, 1.0);                                // r_signed
        tvec.set(1, 2.0);                                // z
        tvec.set(2, 3.0);                                // phi_direction
        tvec.set(3, 4.0);                                // tlm
        tvec.set(4, 5.0);                                // qpt
        if(debug) System.out.println(" *** TrackVector tvec = " + tvec );
        if(debug) System.out.println(" *** r_signed      = " + tvec.get(0) );
        if(debug) System.out.println(" *** z             = " + tvec.get(1) );
        if(debug) System.out.println(" *** phi_direction = " + tvec.get(2) );
        if(debug) System.out.println(" *** tlm           = " + tvec.get(3) );
        if(debug) System.out.println(" *** qpt           = " + tvec.get(4) );
        
        double r    = Math.abs(tvec.get(0));
        double z    = tvec.get(1);
        double sign = 0.0;
        double phi  = 0.0;
        if ( tvec.get(0) != 0.0 )
        {
            sign = tvec.get(0)/Math.abs(tvec.get(0));
            phi  = tvec.get(2)-(sign*TRFMath.PI2);
            phi  = TRFMath.fmod2( phi, TRFMath.TWOPI );
        }
        double tlam = tvec.get(3);
        
        if(debug) System.out.println(" *** r   = " + r   );
        if(debug) System.out.println(" *** phi = " + phi );
        if(debug) System.out.println(" *** z   = " + z   );
        
        Assert.assertTrue( Math.abs(phi-( 1.4292)) < 1.e-4 ) ;      // for r_signed = +1
        //  Assert.assertTrue( fabs(phi-(-1.7124)) < 1.e-4 ) ;      // for r_signed = -1
        
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test crossing status." );
        
        double clam = 1.0/Math.sqrt(1.0+tlam*tlam);
        double slam = tlam/Math.sqrt(1.0+tlam*tlam);
        
        double dz_ds = slam;
        
        double alpha_in     = -2.0;
        double calf_in      = Math.cos(alpha_in);
        double salf_in      = Math.sin(alpha_in);
        double dr_ds_in     = clam*calf_in;
        double r_dphi_ds_in = clam*salf_in;
        CylindricalPath cpth_in = new CylindricalPath(r, phi, z, dr_ds_in, r_dphi_ds_in, dz_ds);
        SurfTest stest_in = new SurfTest(cpth_in);
        
        double alpha_out     =  1.0;
        double calf_out      = Math.cos(alpha_out);
        double salf_out      = Math.sin(alpha_out);
        double dr_ds_out     = clam*calf_out;
        double r_dphi_ds_out = clam*salf_out;
        CylindricalPath cpth_out = new CylindricalPath(r, phi, z, dr_ds_out, r_dphi_ds_out, dz_ds);
        SurfTest stest_out= new SurfTest(cpth_out);
        
        VTrack ton = new VTrack( sdca1.newPureSurface(), tvec );
        if(debug) System.out.println(" sdca1       = " + sdca1 );
        if(debug) System.out.println(" VTrack ton  = " + ton );
        VTrack tin= new VTrack(  stest_in.newPureSurface(), tvec );
        if(debug) System.out.println(" stest_in    = " + stest_in );
        if(debug) System.out.println(" VTrack tin  = " + tin );
        VTrack tout= new VTrack(  stest_out.newPureSurface(), tvec );
        if(debug) System.out.println(" stest_out   = " + stest_out );
        if(debug) System.out.println(" VTrack tout = " + tout );
        
        SpacePath sp_on  = ton.spacePath();
        SpacePath sp_in  = tin.spacePath();
        SpacePath sp_out = tout.spacePath();
        
        if(debug) System.out.println(" *** sp_on *** " );
        if(debug) System.out.println(sp_on  );
        if(debug) System.out.println(" *** sp_in *** " );
        if(debug) System.out.println(sp_in  );
        if(debug) System.out.println(" *** sp_out *** " );
        if(debug) System.out.println(sp_out );
        
        CrossStat xs1 = sdca1.pureStatus(ton);
        if(debug) System.out.println("xs1 = " + xs1 );
        Assert.assertTrue( xs1.at() && xs1.on() && !xs1.inside() && !xs1.outside()
        && !xs1.inBounds() && ! xs1.outOfBounds() );
        
        if(debug) System.out.println(stest_in );
        CrossStat xs2 = sdca1.pureStatus(tin);
        if(debug) System.out.println("xs2 = " + xs2 );
        Assert.assertTrue( !xs2.at() && !xs2.on() && xs2.inside() && !xs2.outside()
        && !xs2.inBounds() && ! xs2.outOfBounds() );
        
        if(debug) System.out.println(stest_out );
        CrossStat xs3 = sdca1.pureStatus(tout);
        if(debug) System.out.println("xs3 = " + xs3 );
        Assert.assertTrue( !xs3.at() && !xs3.on() && !xs3.inside() && xs3.outside()
        && !xs3.inBounds() && ! xs3.outOfBounds() );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test vector difference." );
        TrackVector tvec2 = new TrackVector();
        tvec2.set(0, 1.1);                                // r_signed
        tvec2.set(1, 2.2);                                // z
        tvec2.set(2, 3.3);                                // phi_direction
        tvec2.set(3, 4.4);                                // tlm
        tvec2.set(4, 5.5);                                // qpt
        TrackVector diff = new TrackVector(sdca1.vecDiff(tvec2,tvec));
        TrackVector ediff = new TrackVector();
        ediff.set(0, 0.1);
        ediff.set(1, 0.2);
        ediff.set(2, 0.3);
        ediff.set(3, 0.4);
        ediff.set(4, 0.5);
        TrackVector zero = new TrackVector(diff.minus(ediff));
        if ( zero.amax() > 1.e-10 )
        {
            if(debug) System.out.println( error_prefix + "Incorrect difference." );
            System.exit(9);
        }
        
        //  Assert.assertTrue( zero.amax() <= 1.e-10 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test space point." );
        SpacePoint spt = ton.spacePoint();
        if(debug) System.out.println("spt= "+spt);
        Assert.assertTrue( myequal(spt.rxy(), 1.0) );
        Assert.assertTrue( Math.abs(spt.phi()-phi) < 1.e-4 );
        Assert.assertTrue( spt.z()   == 2.0 );
        
        //********************************************************************
        
        TrackVector vec_1 = new TrackVector();
        vec_1.set(SurfDCA.IZ,0.3);
        VTrack trv_1 = new VTrack((new SurfDCA(0.1,0.2)));
        trv_1.setVector(vec_1);
        trv_1.setForward();
        
        if(debug) System.out.println("trv_1= "+trv_1);
        if(debug) System.out.println("trv_1.spacePoint()"+trv_1.spacePoint());
        
        Assert.assertTrue( myequal(trv_1.spacePoint().x(),0.1) );
        Assert.assertTrue( myequal(trv_1.spacePoint().y(),0.2) );
        Assert.assertTrue( myequal(trv_1.spacePoint().z(),0.3) );
        
        vec_1.set(SurfDCA.IRSIGNED, 0.3);
        vec_1.set(SurfDCA.IPHID, TRFMath.PI2);
        trv_1.setVector(vec_1);
        trv_1.setForward();
        Assert.assertTrue( myequal(trv_1.spacePoint().x(),0.1+0.3) );
        Assert.assertTrue( myequal(trv_1.spacePoint().y(),0.2) );
        Assert.assertTrue( myequal(trv_1.spacePoint().z(),0.3) );
        
        vec_1.set(SurfDCA.IRSIGNED, 0.3);
        vec_1.set(SurfDCA.IPHID, -Math.PI);
        trv_1.setVector(vec_1);
        trv_1.setForward();
        Assert.assertTrue( myequal(trv_1.spacePoint().x(),0.1) );
        Assert.assertTrue( myequal(trv_1.spacePoint().y(),0.2+0.3) );
        Assert.assertTrue( myequal(trv_1.spacePoint().z(),0.3) );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test space vector." );
        SpacePath svec = ton.spacePath();
        double lam = Math.atan(4.0);
        Assert.assertTrue( myequal(svec.rxy(), 1.0) );
        Assert.assertTrue( Math.abs(spt.phi()-phi) < 1.e-4 );
        Assert.assertTrue( svec.z()   == 2.0 );
        //TODO find and fix problem in this test.
//        Assert.assertTrue( myequal( svec.drxy(),     Math.cos(lam)*Math.cos(TRFMath.PI2) ) );
//        Assert.assertTrue( myequal( svec.rxy_dphi(), Math.cos(lam)*Math.sin(sign*TRFMath.PI2) ) );
//        Assert.assertTrue( myequal( svec.dz(),       Math.sin(lam)          ) );
//
//        //********************************************************************
//
//        vec_1.set(SurfDCA.IRSIGNED,0.3);
//        vec_1.set(SurfDCA.IPHID, -Math.PI);
//        trv_1.setVector(vec_1);
//        trv_1.setForward();
//        Assert.assertTrue( myequal(trv_1.spacePath().x(),0.1) );
//        Assert.assertTrue( myequal(trv_1.spacePath().y(),0.2+0.3) );
//        Assert.assertTrue( myequal(trv_1.spacePath().z(),0.3) );
//        Assert.assertTrue( myequal(trv_1.spacePath().dx(),-1.0) );
//        Assert.assertTrue( myequal(trv_1.spacePath().dz(),0.0) );
//        Assert.assertTrue( myequal(trv_1.spacePath().dy(),0.0) );
//        vec_1.set(SurfDCA.IRSIGNED,0.3);
//        vec_1.set(SurfDCA.IPHID, TRFMath.PI2);
//        trv_1.setVector(vec_1);
//        trv_1.setForward();
//        Assert.assertTrue( myequal(trv_1.spacePath().x(),0.1+0.3) );
//        Assert.assertTrue( myequal(trv_1.spacePath().y(),0.2) );
//        Assert.assertTrue( myequal(trv_1.spacePath().z(),0.3) );
//        Assert.assertTrue( myequal(trv_1.spacePath().dx(),0.0) );
//        Assert.assertTrue( myequal(trv_1.spacePath().dz(),0.0) );
//        Assert.assertTrue( myequal(trv_1.spacePath().dy(),1.0) );
//
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
    // comparison of doubles
    public static boolean myequal(double x1, double x2)
    {
        double small = 1.e-12;
        if ( Math.abs(x1-x2) < small ) return true;
        System.out.println("myequal: difference too large:" );
        System.out.println("value 1: " + x1 );
        System.out.println("value 2: " + x2 );
        System.out.println("   diff: " + (x1-x2) );
        System.out.println("maxdiff: " + small );
        return false;
    }
    
}
