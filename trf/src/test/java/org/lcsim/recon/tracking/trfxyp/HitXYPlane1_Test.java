/*
 * HitXYPlane1_Test.java
 *
 * Created on July 24, 2007, 10:31 PM
 *
 * $Id: HitXYPlane1_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman Graf
 */
public class HitXYPlane1_Test extends TestCase
{
    private boolean debug;
    
    static double wv = 0.001;
    static double wz = 0.345;
    
    // calculate avz from a track vector
    static double calc_avz(TrackVector vec)
    {
        double v_track = vec.get(SurfXYPlane.IV);
        double z_track = vec.get(SurfXYPlane.IZ);
        
        return( wv*v_track + wz*z_track );
    }
    
    // calculate eaxy from error matrix
    static double calc_eavz(TrackError err)
    {
        double evv_track = err.get(SurfXYPlane.IV,SurfXYPlane.IV);
        double evz_track = err.get(SurfXYPlane.IV,SurfXYPlane.IZ);
        double ezz_track = err.get(SurfXYPlane.IZ,SurfXYPlane.IZ);
        
        return( evv_track*wv*wv + 2.*evz_track*wv*wz + ezz_track*wz*wz );
    }
    
    /** Creates a new instance of HitXYPlane1_Test */
    public void testHitXYPlane1()
    {
        String component = "HitXYPlane1";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        if(debug) System.out.println( ok_prefix + "Test cluster constructors." );
        List mcids = new ArrayList();
        mcids.add(new Integer(1));
        mcids.add(new Integer(2));
        double dist1 = 10.0;
        double phi1 = Math.PI/3.;
        double avz1 = 1.0;
        double davz1 = 0.01;
        SurfXYPlane szp1 = new SurfXYPlane(dist1,phi1);
        ClusXYPlane1 tst1 = new ClusXYPlane1(dist1,phi1,wv,wz,avz1,davz1);
        
        ClusXYPlane1 tst1a = new ClusXYPlane1(dist1,phi1,wv,wz,avz1,davz1,mcids);
        
        if(debug) System.out.println( tst1 );
        if(debug) System.out.println( tst1a );
        //  ClusXYPlane1 hcp1c = hcp1;
        //  if(debug) System.out.println( hcp1c );
        double dist2 = 20.0;
        double phi2 = Math.PI/4.;
        double avz2 = 2.0;
        double davz2 = 0.02;
        ClusXYPlane1 tst2 = new ClusXYPlane1(dist2,phi2,wv,wz,avz2,davz2);
        
        Assert.assertTrue(tst1.mcIds().size()==0 );
        Assert.assertTrue(tst1a.mcIds().size()==2 );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cluster type ID." );
        if(debug) System.out.println( tst1.type() );
        Assert.assertTrue( tst1.type() != null );
        Assert.assertTrue( tst1.type().equals(tst2.type()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cluster accessors." );
        if(debug) System.out.println( tst1 );
        if(debug) System.out.println( tst1.surface() );
        if(debug) System.out.println( tst1.aVZ() );
        if(debug) System.out.println( tst1.daVZ() );
        if(debug) System.out.println( tst1.wV() );
        if(debug) System.out.println( tst1.wZ() );
        Assert.assertTrue( tst1.surface().equals(szp1) );
        Assert.assertTrue( tst1.aVZ() == avz1 );
        Assert.assertTrue( tst1.daVZ() == davz1 );
        Assert.assertTrue( tst1.wV() == wv );
        Assert.assertTrue( tst1.wZ() == wz );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a hit." );
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        HitDerivative der_expect = new HitDerivative(1);
        der_expect.set(0,0, wv);
        der_expect.set(0,1, wz);
        vec.set(SurfXYPlane.IV, 1.1);
        vec.set(SurfXYPlane.IZ, 40.0);
        err.set(SurfXYPlane.IV,SurfXYPlane.IV, 0.04);
        err.set(SurfXYPlane.IV,SurfXYPlane.IZ, 0.03);
        err.set(SurfXYPlane.IZ,SurfXYPlane.IZ, 0.02);
        ETrack tre1 = new ETrack(szp1.newPureSurface(),vec,err);
        List tclus11 = tst1.predict(tre1,tst1);
        Assert.assertTrue( tclus11.size() == 1 );
        Hit pre11 = (Hit) tclus11.get(0);
        double maxdiff = 1.e-12;
        if(debug) System.out.println( pre11.size() );
        Assert.assertTrue( pre11.size() == 1);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredVector().get(0), avz1 ) );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredError().get(0,0), davz1*davz1 ) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedVector().get(0), calc_avz(vec) ) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedError().get(0,0), calc_eavz(err) ) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect ) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) -
                calc_avz(vec) + avz1) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different track."
                );
        vec.set(SurfXYPlane.IV, 1.2);
        ETrack tre2 = new ETrack(szp1.newPureSurface(),vec,err);
        List tclus12 = tst1.predict(tre2,tst1);
        Assert.assertTrue( tclus12.size() == 1 );
        Hit pre12 = (Hit)tclus12.get(0);
        if(debug) System.out.println( pre12.size() );
        Assert.assertTrue( pre12.size() == 1);
        if(debug) System.out.println( pre12.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre12.measuredVector().get(0), avz1 ) );
        if(debug) System.out.println( pre12.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre12.measuredError().get(0,0), davz1*davz1 ) );
        if(debug) System.out.println( pre12.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre12.predictedVector().get(0), calc_avz(vec) ) );
        if(debug) System.out.println( pre12.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre12.predictedError().get(0,0), calc_eavz(err) ) );
        if(debug) System.out.println( pre12.dHitdTrack() );
        Assert.assertTrue( pre12.dHitdTrack().equals(der_expect ) );
        if(debug) System.out.println( pre12.differenceVector() );
        Assert.assertTrue( Math.abs(pre12.differenceVector().get(0) -
                calc_avz(vec) + avz1) < maxdiff );
        // same cluster ==> same hit (even though track changes)
        Assert.assertTrue( pre11.equals(pre11) );
        Assert.assertTrue( ! ( pre11.notEquals(pre11) ) );
        Assert.assertTrue( pre11.equals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different cluster."
                );
        List tclus22 = tst2.predict(tre2,tst2);
        Assert.assertTrue( tclus22.size() == 1 );
        Hit pre22 = (Hit)tclus22.get(0);
        if(debug) System.out.println( pre22.size() );
        Assert.assertTrue( pre22.size() == 1);
        if(debug) System.out.println( pre22.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre22.measuredVector().get(0), avz2 ) );
        if(debug) System.out.println( pre22.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre22.measuredError().get(0,0), davz2*davz2 ) );
        if(debug) System.out.println( pre22.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre22.predictedVector().get(0), calc_avz(vec) ) );
        if(debug) System.out.println( pre22.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre22.predictedError().get(0,0), calc_eavz(err) ) );
        if(debug) System.out.println( pre22.dHitdTrack() );
        Assert.assertTrue( pre22.dHitdTrack().equals(der_expect)  );
        if(debug) System.out.println( pre22.differenceVector() );
        Assert.assertTrue( Math.abs(pre22.differenceVector().get(0) -
                calc_avz(vec) + avz2) < maxdiff );
        // different cluster ==> different hit
        Assert.assertTrue( pre22.notEquals(pre11) );
        Assert.assertTrue( ! ( pre22.equals(pre11) ) );
        Assert.assertTrue( pre22.notEquals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Update hit." );
        if(debug) System.out.println( pre11 );
        pre11.update(tre2);
        if(debug) System.out.println( pre11 );
        Assert.assertTrue( pre11.size() == 1);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredVector().get(0), avz1 ) );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredError().get(0,0), davz1*davz1 ) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedVector().get(0), calc_avz(vec) ) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedError().get(0,0), calc_eavz(err) ) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) -
                calc_avz(vec) + avz1) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check hit type and equality." );
        if(debug) System.out.println( HitXYPlane1.staticType());
        if(debug) System.out.println( pre11.type() );
        if(debug) System.out.println( pre12.type() );
        Assert.assertTrue( pre11.type() != null );
        Assert.assertTrue( pre11.type().equals(HitXYPlane1.staticType()) );
        Assert.assertTrue( pre11.type().equals(pre12.type()) );
        
        //********************************************************************
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
