/*
 * HitZPlane1_Test.java
 *
 * Created on July 24, 2007, 11:06 PM
 *
 * $Id: HitZPlane1_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

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
public class HitZPlane1_Test extends TestCase
{
    private boolean debug;
    static double wx = 0.001;
    static double wy = 0.345;
    
    // calculate axy from a track vector
    static double calc_axy(TrackVector vec)
    {
        double x_track = vec.get(SurfZPlane.IX);
        double y_track = vec.get(SurfZPlane.IY);
        
        return( wx*x_track + wy*y_track );
    }
    
    // calculate eaxy from error matrix
    static double calc_eaxy(TrackError err)
    {
        double exx_track = err.get(SurfZPlane.IX,SurfZPlane.IX);
        double exy_track = err.get(SurfZPlane.IX,SurfZPlane.IY);
        double eyy_track = err.get(SurfZPlane.IY,SurfZPlane.IY);
        
        return( exx_track*wx*wx + 2.*exy_track*wx*wy + eyy_track*wy*wy );
    }
    /** Creates a new instance of HitZPlane1_Test */
    public void testHitZPlane1()
    {
        String component = "HitZPlane1";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        if(debug) System.out.println( ok_prefix + "Test cluster constructors." );
        List mcids = new ArrayList();
        mcids.add( new Integer(1));
        mcids.add(new Integer(2));
        double zpos1 = 10.0;
        double axy1 = 1.0;
        double daxy1 = 0.01;
        SurfZPlane szp1 = new SurfZPlane(zpos1);
        ClusZPlane1 hcp1 = new ClusZPlane1(zpos1,wx,wy,axy1,daxy1);
        ClusZPlane1 hcp1a = new ClusZPlane1(zpos1,wx,wy,axy1,daxy1,mcids);
        ClusZPlane1 hcp1b = new ClusZPlane1(hcp1a);
        ClusZPlane1 hcp1d = new ClusZPlane1(hcp1);
        Assert.assertTrue(hcp1.equals(hcp1a));
        Assert.assertTrue(hcp1b.equals(hcp1a));
        Assert.assertTrue(hcp1.equals(hcp1d));
        Assert.assertTrue(hcp1.mcIds().size() == 0 );
        Assert.assertTrue(hcp1d.mcIds().size() == 0 );
        Assert.assertTrue(hcp1a.mcIds().size() == 2 );
        Assert.assertTrue(hcp1b.mcIds().size() == 2 );
        
        double zpos2 = 20.0;
        double axy2 = 2.0;
        double daxy2 = 0.02;
        ClusZPlane1 hcp2 = new ClusZPlane1(zpos2,wx,wy,axy2,daxy2);
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cluster type ID." );
        if(debug) System.out.println( hcp1.type() );
        if(debug) System.out.println( hcp2.type() );
        Assert.assertTrue( hcp1.type() != null );
        Assert.assertTrue( hcp1.type().equals(hcp2.type()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cluster accessors." );
        if(debug) System.out.println( hcp1 );
        if(debug) System.out.println( hcp1.surface() );
        if(debug) System.out.println( hcp1.aXY() );
        if(debug) System.out.println( hcp1.daXY() );
        if(debug) System.out.println( hcp1.wX() );
        if(debug) System.out.println( hcp1.wY() );
        Assert.assertTrue( hcp1.surface().equals(szp1) );
        Assert.assertTrue( hcp1.aXY() == axy1 );
        Assert.assertTrue( hcp1.daXY() == daxy1 );
        Assert.assertTrue( hcp1.wX() == wx );
        Assert.assertTrue( hcp1.wY() == wy );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a hit." );
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        HitDerivative der_expect = new HitDerivative(1);
        der_expect.set(0,0, wx);
        der_expect.set(0,1, wy);
        vec.set(SurfZPlane.IX, 1.1);
        vec.set(SurfZPlane.IY, 40.0);
        err.set(SurfZPlane.IX,SurfZPlane.IX, 0.04);
        err.set(SurfZPlane.IX,SurfZPlane.IY, 0.03);
        err.set(SurfZPlane.IY,SurfZPlane.IY, 0.02);
        ETrack tre1 = new ETrack(szp1.newPureSurface(),vec,err);
        List tclus11 = hcp1.predict(tre1,hcp1);
        Assert.assertTrue( tclus11.size() == 1 );
        Hit pre11 = (Hit) tclus11.get(0);
        double maxdiff = 1.e-12;
        if(debug) System.out.println( pre11.size() );
        Assert.assertTrue( pre11.size() == 1);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( pre11.measuredVector().get(0) == axy1 );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredError().get(0,0), daxy1*daxy1 ) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedVector().get(0), calc_axy(vec) ) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedError().get(0,0), calc_eaxy(err) ) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) -
                calc_axy(vec) + axy1) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different track."
                );
        vec.set(SurfZPlane.IX, 1.2);
        ETrack tre2 = new ETrack(szp1.newPureSurface(),vec,err);
        List tclus12 = hcp1.predict(tre2,hcp1);
        Assert.assertTrue( tclus12.size() == 1 );
        Hit pre12 = (Hit) tclus12.get(0);
        if(debug) System.out.println( pre12.size() );
        Assert.assertTrue( pre12.size() == 1);
        if(debug) System.out.println( pre12.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre12.measuredVector().get(0), axy1 ) );
        if(debug) System.out.println( pre12.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre12.measuredError().get(0,0), daxy1*daxy1 ) );
        if(debug) System.out.println( pre12.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre12.predictedVector().get(0), calc_axy(vec) ) );
        if(debug) System.out.println( pre12.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre12.predictedError().get(0,0), calc_eaxy(err) ) );
        if(debug) System.out.println( pre12.dHitdTrack() );
        Assert.assertTrue(   pre12.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre12.differenceVector() );
        Assert.assertTrue( Math.abs(pre12.differenceVector().get(0) -
                calc_axy(vec) + axy1) < maxdiff );
        // same cluster ==> same hit (even though track changes)
        Assert.assertTrue( pre11.equals(pre11) );
        Assert.assertTrue( ! ( pre11.notEquals(pre11) ) );
        Assert.assertTrue( pre11.equals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different cluster."
                );
        List tclus22 = hcp2.predict(tre2,hcp2);
        Assert.assertTrue( tclus22.size() == 1 );
        Hit pre22 = (Hit) tclus22.get(0);
        if(debug) System.out.println( pre22.size() );
        Assert.assertTrue( pre22.size() == 1);
        if(debug) System.out.println( pre22.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre22.measuredVector().get(0), axy2 ) );
        if(debug) System.out.println( pre22.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre22.measuredError().get(0,0), daxy2*daxy2 ) );
        if(debug) System.out.println( pre22.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre22.predictedVector().get(0), calc_axy(vec) ) );
        if(debug) System.out.println( pre22.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre22.predictedError().get(0,0), calc_eaxy(err) ) );
        if(debug) System.out.println( pre22.dHitdTrack() );
        Assert.assertTrue( pre22.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre22.differenceVector() );
        Assert.assertTrue( Math.abs(pre22.differenceVector().get(0) -
                calc_axy(vec) + axy2) < maxdiff );
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
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredVector().get(0), axy1 ) );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredError().get(0,0), daxy1*daxy1 ) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedVector().get(0), calc_axy(vec) ) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedError().get(0,0), calc_eaxy(err) ) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) -
                calc_axy(vec) + axy1) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check hit type and equality." );
        if(debug) System.out.println( HitZPlane1.staticType());
        if(debug) System.out.println( pre11.type() );
        if(debug) System.out.println( pre12.type() );
        Assert.assertTrue( pre11.type() != null);
        Assert.assertTrue( pre11.type().equals(HitZPlane1.staticType()) );
        Assert.assertTrue( pre11.type().equals(pre12.type()) );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
