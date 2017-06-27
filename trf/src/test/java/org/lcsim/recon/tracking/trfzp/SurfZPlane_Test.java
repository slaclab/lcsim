/*
 * SurfZPlane_Test.java
 *
 * Created on July 24, 2007, 11:00 PM
 *
 * $Id: SurfZPlane_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class SurfZPlane_Test extends TestCase
{
    private boolean debug;
    // comparison of doubles
    public static boolean myequal(double x1, double x2)
    {
        double small = 1.e-12;
        if ( Math.abs(x1-x2) < small ) return true;
        System.out.println( "myequal: difference too large:" );
        System.out.println( "value 1: " + x1 );
        System.out.println( "value 2: " + x2 );
        System.out.println( "   diff: " + (x1-x2) );
        System.out.println( "maxdiff: " + small );
        return false;
    }
    /** Creates a new instance of SurfZPlane_Test */
    public void testSurfZPlane()
    {
        String ok_prefix = "SurfZPlane test (I): ";
        String error_prefix = "SurfZPlane test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------ Testing component SurfZPlane. ------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor and get_parameter." );
        double z  = 12.34;
        SurfZPlane szp1 = new SurfZPlane(z);
        if(debug) System.out.println( szp1 );
        if ( szp1.parameter(SurfZPlane.ZPOS) != z )
        {
            if(debug) System.out.println( error_prefix + "Incorrect radius." );
            Assert.assertTrue(szp1.parameter(SurfZPlane.ZPOS) == z );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test copy constructor." );
        
        SurfZPlane szp1a = new SurfZPlane(szp1);
        Assert.assertTrue( szp1.equals(szp1a) );
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test type." );
        if(debug) System.out.println( SurfZPlane.staticType() );
        if(debug) System.out.println( szp1.type() );
        Assert.assertTrue( szp1.type() != null );
        Assert.assertTrue( szp1.type() == SurfZPlane.staticType() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test equality." );
        SurfZPlane szp2 = new SurfZPlane(12.34);
        SurfZPlane szp3 = new SurfZPlane(24.68);
        if ( !szp1.boundEqual(szp2) || !szp1.pureEqual(szp2) )
        {
            if(debug) System.out.println( error_prefix + "Equality failed."  );
            System.exit(3);
        }
        if ( szp1.boundEqual(szp3) || szp1.pureEqual(szp3) )
        {
            if(debug) System.out.println( error_prefix + "Inequality failed."  );
            System.exit(4);
        }
        
        //*******************************************************************
        {
            if(debug) System.out.println( ok_prefix + "Test comparision." );
            SurfZPlane ltz = new SurfZPlane(11.);
            SurfZPlane gtz = new SurfZPlane(13.);
            Assert.assertTrue(!gtz.pureLessThan(ltz));
            Assert.assertTrue(ltz.pureLessThan(gtz));
            Assert.assertTrue(!gtz.pureLessThan(gtz));
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test virtual constructor." );
        SurfZPlane szp4 = (SurfZPlane)szp1.newPureSurface();
        if ( !szp1.equals(szp4) )
        {
            if(debug) System.out.println( error_prefix + "Virtual construction failed." );
            System.exit(5);
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test crossing status." );
        TrackVector tvec = new TrackVector();
        tvec.set(SurfZPlane.IX, 1.0);
        tvec.set(SurfZPlane.IY, 2.0);
        tvec.set(SurfZPlane.IDXDZ, 3.0);
        tvec.set(SurfZPlane.IDYDZ, 4.0);
        tvec.set(SurfZPlane.IQP, 5.0);
        SurfZPlane sfin = new SurfZPlane(1.0);
        SurfZPlane sfout = new SurfZPlane(60.0);
        VTrack ton = new VTrack( szp1.newPureSurface(), tvec );
        ton.setForward();
        VTrack tin = new VTrack( sfin.newPureSurface(), tvec );
        tin.setForward();
        VTrack tout = new VTrack(sfout.newPureSurface(), tvec );
        tout.setForward();
        CrossStat xs1 = szp1.pureStatus(ton);
        if(debug) System.out.println( xs1 );
        Assert.assertTrue( xs1.at() && xs1.on() && !xs1.inside() && !xs1.outside()
        && !xs1.inBounds() && ! xs1.outOfBounds() );
        CrossStat xs2 = szp1.pureStatus(tin);
        Assert.assertTrue( !xs2.at() && !xs2.on() && xs2.inside() && !xs2.outside()
        && !xs2.inBounds() && ! xs2.outOfBounds() );
        CrossStat xs3 = szp1.pureStatus(tout);
        if(debug) System.out.println( xs3 );
        Assert.assertTrue( !xs3.at() && !xs3.on() && !xs3.inside() && xs3.outside()
        && !xs3.inBounds() && ! xs3.outOfBounds() );
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test vector difference." );
        TrackVector tvec2 = new TrackVector();
        tvec2.set(SurfZPlane.IX, 1.1);
        tvec2.set(SurfZPlane.IY, 2.2);
        tvec2.set(SurfZPlane.IDXDZ, 3.3);
        tvec2.set(SurfZPlane.IDYDZ, 4.4);
        tvec2.set(SurfZPlane.IQP, 5.5);
        TrackVector diff = szp1.vecDiff(tvec2,tvec);
        if(debug) System.out.println( "tvec2= "+tvec2 );
        if(debug) System.out.println( "tvec= "+tvec );
        if(debug) System.out.println( "diff= "+diff );
        TrackVector ediff = new TrackVector();
        ediff.set(SurfZPlane.IX, 0.1);
        ediff.set(SurfZPlane.IY, 0.2);
        ediff.set(SurfZPlane.IDXDZ, 0.3);
        ediff.set(SurfZPlane.IDYDZ, 0.4);
        ediff.set(SurfZPlane.IQP, 0.5);
        TrackVector zero = diff.minus(ediff);
        if(debug) System.out.println( ediff );
        if(debug) System.out.println( zero );
        if ( zero.amax() > 1.e-10 )
        {
            if(debug) System.out.println( error_prefix + "Incorrect difference." );
            System.exit(9);
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test space point." );
        SpacePoint spt = ton.spacePoint();
        if(debug) System.out.println( spt );
  /*
  Assert.assertTrue( is_equal( spt.x(), 1.0 ) );
  Assert.assertTrue( is_equal( spt.y(), 2.0 ) );
  Assert.assertTrue( is_equal( spt.z() , 12.34 ) );
   */
        Assert.assertTrue(   spt.x()== 1.0  );
        Assert.assertTrue(  spt.y()== 2.0  );
        Assert.assertTrue(   spt.z() == 12.34  );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test space vector." );
        SpacePath svec = ton.spacePath();
        if(debug) System.out.println( svec );
        Assert.assertTrue( svec.x() == 1.0 );
        Assert.assertTrue( svec.y() == 2.0 );
        Assert.assertTrue( myequal(svec.z(), 12.34) );
        Assert.assertTrue( myequal( svec.dz(), 1./Math.sqrt(26.)) );
        Assert.assertTrue( myequal( svec.dy(), 4./Math.sqrt(26.)) );
        Assert.assertTrue( myequal( svec.dx(), 3./Math.sqrt(26.)) );
        
        ton.setBackward();
        
        SpacePath bsvec = ton.spacePath();
        if(debug) System.out.println( bsvec );
        Assert.assertTrue( bsvec.x() == 1.0 );
        Assert.assertTrue( bsvec.y() == 2.0 );
        Assert.assertTrue( myequal(bsvec.z(), 12.34) );
        Assert.assertTrue( myequal( bsvec.dz(), -1./Math.sqrt(26.)) );
        Assert.assertTrue( myequal( bsvec.dy(), -4./Math.sqrt(26.)) );
        Assert.assertTrue( myequal( bsvec.dx(), -3./Math.sqrt(26.)) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
