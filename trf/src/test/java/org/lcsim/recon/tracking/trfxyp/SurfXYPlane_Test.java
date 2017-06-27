/*
 * SurfXYPlane_Test.java
 *
 * Created on July 24, 2007, 10:36 PM
 *
 * $Id: SurfXYPlane_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

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
public class SurfXYPlane_Test extends TestCase
{
    private boolean debug;
    
    // comparison of doubles
    static boolean myequal(double x1, double x2)
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
    /** Creates a new instance of SurfXYPlane_Test */
    public void testSurfXYPlane()
    {
        String ok_prefix = "SurfXYPlane test (I): ";
        String error_prefix = "SurfXYPlane test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------ Testing component SurfXYPlane. ------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor and get_parameter." );
        double dist  = 12.34;
        double phi   = Math.PI/3.;
        SurfXYPlane sxyp1 = new SurfXYPlane(dist,phi);
        if(debug) System.out.println( sxyp1 );
        if ( sxyp1.parameter(SurfXYPlane.NORMPHI) != phi )
        {
            if(debug) System.out.println( error_prefix + "Incorrect phi of normal" );
            System.exit(1);
        }
        if ( sxyp1.parameter(SurfXYPlane.DISTNORM) != dist )
        {
            if(debug) System.out.println( error_prefix + "Incorrect normal value" );
            System.exit(2);
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test copy constructor." );
        
        SurfXYPlane sxypc = new SurfXYPlane(sxyp1);
        Assert.assertTrue( sxypc.equals(sxyp1) );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test type." );
        if(debug) System.out.println( SurfXYPlane.staticType() );
        if(debug) System.out.println( sxyp1.type() );
        Assert.assertTrue( sxyp1.type() != null );
        Assert.assertTrue( sxyp1.type().equals(SurfXYPlane.staticType()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test equality." );
        SurfXYPlane sxyp2 = new SurfXYPlane(12.34,Math.PI/3.);
        SurfXYPlane sxyp3= new SurfXYPlane(24.68,Math.PI/3.);
        SurfXYPlane sxyp4= new SurfXYPlane(12.34,Math.PI/5.);
        if ( !sxyp1.boundEqual(sxyp2) || !sxyp1.pureEqual(sxyp2) )
        {
            if(debug) System.out.println( error_prefix + "Equality failed."  );
            System.exit(3);
        }
        if ( sxyp1.boundEqual(sxyp4) || sxyp1.pureEqual(sxyp4) )
        {
            if(debug) System.out.println( error_prefix + "Inequality failed on phi"  );
            System.exit(4);
        }
        if ( sxyp1.boundEqual(sxyp3) || sxyp1.pureEqual(sxyp3) )
        {
            if(debug) System.out.println( error_prefix + "Inequality failed on normal"  );
            System.exit(5);
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test comparison." );
        {
            SurfXYPlane lt= new SurfXYPlane(10.,Math.PI/3.);
            SurfXYPlane ltp= new SurfXYPlane(10.,Math.PI/6.);
            SurfXYPlane gt= new SurfXYPlane(11.,Math.PI/2.);
            SurfXYPlane gtd= new SurfXYPlane(11,Math.PI/3.);
            SurfXYPlane gtdp= new SurfXYPlane(11,Math.PI/6.);
            Assert.assertTrue(lt.pureLessThan(gt));
            Assert.assertTrue(!gt.pureLessThan(lt));
            Assert.assertTrue(ltp.pureLessThan(lt));
            Assert.assertTrue(!lt.pureLessThan(ltp));
            Assert.assertTrue(lt.pureLessThan(gtd));
            Assert.assertTrue(!gtd.pureLessThan(lt));
            Assert.assertTrue(lt.pureLessThan(gtdp));
            Assert.assertTrue(!gtdp.pureLessThan(lt));
            Assert.assertTrue(!lt.pureLessThan(lt));
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test virtual constructor." );
        SurfXYPlane psxyp5 = (SurfXYPlane)sxyp1.newPureSurface();
        if ( !sxyp1.equals(psxyp5) )
        {
            if(debug) System.out.println( error_prefix + "Virtual construction failed." );
            System.exit(6);
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test crossing status." );
        TrackVector tvec = new TrackVector();
        tvec.set(SurfXYPlane.IV, 1.0);
        tvec.set(SurfXYPlane.IZ, 2.0);
        tvec.set(SurfXYPlane.IDVDU, 3.0);
        tvec.set(SurfXYPlane.IDZDU, 4.0);
        tvec.set(SurfXYPlane.IQP, 5.0);
        SurfXYPlane sfin= new SurfXYPlane(1.0,Math.PI/5.);
        SurfXYPlane sfout = new SurfXYPlane(60.0,Math.PI/5.);
        VTrack ton = new VTrack( sxyp1.newPureSurface(), tvec );
        ton.setForward();
        VTrack tin= new VTrack(  sfin.newPureSurface() , tvec );
        tin.setForward();
        VTrack tout= new VTrack(  sfout.newPureSurface() , tvec );
        tout.setForward();
        CrossStat xs1 = sxyp1.pureStatus(ton);
        if(debug) System.out.println( xs1 );
        Assert.assertTrue( xs1.at() && xs1.on() && !xs1.inside() && !xs1.outside()
        && !xs1.inBounds() && ! xs1.outOfBounds() );
        CrossStat xs2 = sxyp1.pureStatus(tin);
        Assert.assertTrue( !xs2.at() && !xs2.on() && xs2.inside() && !xs2.outside()
        && !xs2.inBounds() && ! xs2.outOfBounds() );
        CrossStat xs3 = sxyp1.pureStatus(tout);
        if(debug) System.out.println( xs3 );
        Assert.assertTrue( !xs3.at() && !xs3.on() && !xs3.inside() && xs3.outside()
        && !xs3.inBounds() && ! xs3.outOfBounds() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test vector difference." );
        TrackVector tvec2 = new TrackVector();
        tvec2.set(SurfXYPlane.IV, 1.1);
        tvec2.set(SurfXYPlane.IZ, 2.2);
        tvec2.set(SurfXYPlane.IDVDU, 3.3);
        tvec2.set(SurfXYPlane.IDZDU, 4.4);
        tvec2.set(SurfXYPlane.IQP, 5.5);
        TrackVector diff = sxyp1.vecDiff(tvec2,tvec);
        if(debug) System.out.println( tvec2 );
        if(debug) System.out.println( tvec );
        if(debug) System.out.println( diff );
        TrackVector ediff = new TrackVector();
        ediff.set(SurfXYPlane.IV, 0.1);
        ediff.set(SurfXYPlane.IZ, 0.2);
        ediff.set(SurfXYPlane.IDVDU, 0.3);
        ediff.set(SurfXYPlane.IDZDU, 0.4);
        ediff.set(SurfXYPlane.IQP, 0.5);
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
        double csin = Math.sin(Math.PI/3.);
        Assert.assertTrue( myequal(spt.x(), 12.34/2.-csin) );
        Assert.assertTrue( myequal(spt.y(), 12.34*csin+0.5) );
        Assert.assertTrue(         spt.z() == 2.0 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test space vector." );
        SpacePath svec = ton.spacePath();
        if(debug) System.out.println( svec );
        double ds = 1./Math.sqrt(26.);
        Assert.assertTrue( myequal(svec.x(), 12.34/2.-csin) );
        Assert.assertTrue( myequal(svec.y(), 12.34*csin+0.5) );
        Assert.assertTrue(         svec.z() == 2.0 );
        Assert.assertTrue( myequal( svec.dz(), 4.*ds) );
        Assert.assertTrue( myequal( svec.dy(), ds*csin+3.*ds/2. ) );
        Assert.assertTrue( myequal( svec.dx(), ds/2.-3.*ds*csin ) );
        
        ton.setBackward();
        SpacePath bsvec = ton.spacePath();
        if(debug) System.out.println( bsvec );
        
        ds = -1./Math.sqrt(26.);
        
        Assert.assertTrue( myequal(bsvec.x(), 12.34/2.-csin) );
        Assert.assertTrue( myequal(bsvec.y(), 12.34*csin+0.5) );
        Assert.assertTrue(         bsvec.z() == 2.0 );
        Assert.assertTrue( myequal( bsvec.dz(), 4.*ds) );
        Assert.assertTrue( myequal( bsvec.dy(), ds*csin+3.*ds/2. ) );
        Assert.assertTrue( myequal( bsvec.dx(), ds/2.-3.*ds*csin ) );
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
