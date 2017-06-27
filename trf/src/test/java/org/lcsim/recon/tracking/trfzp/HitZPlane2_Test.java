/*
 * HitZPlane2_Test.java
 *
 * Created on July 24, 2007, 11:03 PM
 *
 * $Id: HitZPlane2_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class HitZPlane2_Test extends TestCase
{
    private boolean debug;
    // calculate hm from a track vector
    static HitVector calc_hm(TrackVector vec)
    {
        double x_track = vec.get(SurfZPlane.IX);
        double y_track = vec.get(SurfZPlane.IY);
        
        return new HitVector( x_track, y_track );
    }
    
    // calculate dhm from error matrix
    static HitError calc_dhm(TrackError err)
    {
        double exx_track = err.get(SurfZPlane.IX,SurfZPlane.IX);
        double exy_track = err.get(SurfZPlane.IX,SurfZPlane.IY);
        double eyy_track = err.get(SurfZPlane.IY,SurfZPlane.IY);
        
        return new HitError( exx_track, exy_track, eyy_track);
    }
    
    // compare HitError and error matrix of the cluster
    
    static boolean hiteqsm(  HitError lhs,   HitError rhs)
    {
        if ( lhs.size() != rhs.size() || lhs.size()!=2 ) return false;
        return ( (lhs.get(0,0) == rhs.get(ClusZPlane2.IX,ClusZPlane2.IX)) &&
                (lhs.get(0,1) == rhs.get(ClusZPlane2.IX,ClusZPlane2.IY)) &&
                (lhs.get(1,1) == rhs.get(ClusZPlane2.IY,ClusZPlane2.IY)) );
    }
    
    // compare HitVector and vector of the cluster
    
    static boolean hiteqvec(  HitVector  lhs,   HitVector  rhs)
    {
        if ( lhs.size() != rhs.size() || lhs.size()!=2 ) return false;
        return ( (lhs.get(0) == rhs.get(ClusZPlane2.IX)) &&
                (lhs.get(1) == rhs.get(ClusZPlane2.IY)) );
    }
    
    
    //**********************************************************************
    
    /** Creates a new instance of HitZPlane2_Test */
    public void testHitZPlane2()
    {
        String component = "HitZPlane2";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        
        int IX = ClusZPlane2.IX;
        int IY = ClusZPlane2.IY;
        
        
        if(debug) System.out.println( ok_prefix + "Test cluster constructors." );
        List mcids = new ArrayList();
        mcids.add(new Integer(1));
        mcids.add(new Integer(2));
        
        double zpos1 = 10.0;
        double x1 = 1.0;
        double y1 = 1.1;
        HitError dhm1 = new HitError(2);
        dhm1.set(ClusZPlane2.IX,ClusZPlane2.IX, 0.02);
        dhm1.set(ClusZPlane2.IX,ClusZPlane2.IY, 0.01);
        dhm1.set(ClusZPlane2.IY,ClusZPlane2.IY, 0.03);
        SurfZPlane szp1 = new SurfZPlane(zpos1);
        ClusZPlane2 hcp1 = new ClusZPlane2(zpos1,x1,y1,dhm1.get(ClusZPlane2.IX,ClusZPlane2.IX),dhm1.get(ClusZPlane2.IY,ClusZPlane2.IY),dhm1.get(ClusZPlane2.IX,ClusZPlane2.IY));
        ClusZPlane2 hcp1a = new ClusZPlane2(zpos1,x1,y1,dhm1.get(ClusZPlane2.IX,ClusZPlane2.IX),dhm1.get(ClusZPlane2.IY,ClusZPlane2.IY),dhm1.get(ClusZPlane2.IX,ClusZPlane2.IY),mcids);
        
        Assert.assertTrue( hcp1.equals(hcp1a));
        Assert.assertTrue(hcp1.x() == x1 );
        Assert.assertTrue(hcp1.y() == y1 );
        Assert.assertTrue(hcp1.dX2() == 0.02 );
        Assert.assertTrue(hcp1.dY2() == 0.03 );
        Assert.assertTrue(hcp1.dXdY() == 0.01 );
        Assert.assertTrue(hcp1a.x() == x1 );
        Assert.assertTrue(hcp1a.y() == y1 );
        Assert.assertTrue(hcp1a.dX2() == 0.02 );
        Assert.assertTrue(hcp1a.dY2() == 0.03 );
        Assert.assertTrue(hcp1a.dXdY() == 0.01 );
        Assert.assertTrue(hcp1.mcIds().size()==0);
        Assert.assertTrue(hcp1a.mcIds().size()==2);
        
        if(debug) System.out.println( hcp1 );
        
        double zpos2 = 20.0;
        double x2 = 2.0;
        double y2 = 2.2;
        HitVector hm2 = new HitVector(2);
        hm2.set(ClusZPlane2.IX, x2);
        hm2.set(ClusZPlane2.IY, y2);
        HitError dhm2 = new HitError(2);
        dhm2.set(ClusZPlane2.IX,ClusZPlane2.IX,  0.022);
        dhm2.set(ClusZPlane2.IX,ClusZPlane2.IY,  0.012);
        dhm2.set(ClusZPlane2.IY,ClusZPlane2.IY,  0.032);
        ClusZPlane2 hcp2 = new ClusZPlane2(zpos2,x2,y2,dhm2.get(ClusZPlane2.IX,ClusZPlane2.IX),dhm2.get(ClusZPlane2.IY,ClusZPlane2.IY),dhm2.get(ClusZPlane2.IX,ClusZPlane2.IY));
        ClusZPlane2 hcp2a = new ClusZPlane2(zpos2,x2,y2,dhm2.get(ClusZPlane2.IX,ClusZPlane2.IX),dhm2.get(ClusZPlane2.IY,ClusZPlane2.IY),dhm2.get(ClusZPlane2.IX,ClusZPlane2.IY),mcids);
        
        Assert.assertTrue(hcp2.equals(hcp2a));
        Assert.assertTrue(hcp2.mcIds().size()==0);
        Assert.assertTrue(hcp2a.mcIds().size()==2);
        Assert.assertTrue(hcp2.x() == x2 );
        Assert.assertTrue(hcp2.y() == y2 );
        Assert.assertTrue(hcp2.dX2() == 0.022 );
        Assert.assertTrue(hcp2.dY2() == 0.032 );
        Assert.assertTrue(hcp2.dXdY() == 0.012 );
        Assert.assertTrue(hcp2a.x() == x2 );
        Assert.assertTrue(hcp2a.y() == y2 );
        Assert.assertTrue(hcp2a.dX2() == 0.022 );
        Assert.assertTrue(hcp2a.dY2() == 0.032 );
        Assert.assertTrue(hcp2a.dXdY() == 0.012 );
        
        HitVector hm1 = new HitVector(2);
        hm1.set(ClusZPlane2.IX, x1);
        hm1.set(ClusZPlane2.IY, y1);
        ClusZPlane2 hcp3 = new ClusZPlane2(zpos1,x1,y1,dhm1.get(ClusZPlane2.IX,ClusZPlane2.IX),dhm1.get(ClusZPlane2.IY,ClusZPlane2.IY),dhm1.get(ClusZPlane2.IX,ClusZPlane2.IY));
        ClusZPlane2 hcp3a = new ClusZPlane2(zpos1,x1,y1,dhm1.get(ClusZPlane2.IX,ClusZPlane2.IX),dhm1.get(ClusZPlane2.IY,ClusZPlane2.IY),dhm1.get(ClusZPlane2.IX,ClusZPlane2.IY),mcids);
        
        ClusZPlane2 hcp3b = new ClusZPlane2(hcp3a);
        ClusZPlane2 hcp3c =  new ClusZPlane2(hcp3);
        
        Assert.assertTrue(hcp3.x() == x1 );
        Assert.assertTrue(hcp3.y() == y1 );
        Assert.assertTrue(hcp3.dX2() == 0.02 );
        Assert.assertTrue(hcp3.dY2() == 0.03 );
        Assert.assertTrue(hcp3.dXdY() == 0.01 );
        Assert.assertTrue(hcp3a.x() == x1 );
        Assert.assertTrue(hcp3a.y() == y1 );
        Assert.assertTrue(hcp3a.dX2() == 0.02 );
        Assert.assertTrue(hcp3a.dY2() == 0.03 );
        Assert.assertTrue(hcp3a.dXdY() == 0.01 );
        
        // test two diferent constructors
        Assert.assertTrue(hcp3b.equals(hcp3a));
        Assert.assertTrue(hcp3.equals(hcp3c));
        Assert.assertTrue(hcp3.mcIds().size()==0);
        Assert.assertTrue(hcp3a.mcIds().size()==2);
        Assert.assertTrue(hcp3c.mcIds().size()==0);
        Assert.assertTrue(hcp3b.mcIds().size()==2);
        
        Assert.assertTrue( hcp3.equals(hcp1) );
        
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
        
        Assert.assertTrue( hcp1.surface().equals(szp1) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a hit." );
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        HitDerivative der_expect = new HitDerivative(2);
        der_expect.set(0,0,  1.);
        der_expect.set(1,1,  1.);
        vec.set(SurfZPlane.IX,  1.1);
        vec.set(SurfZPlane.IY,  40.0);
        err.set(SurfZPlane.IX,SurfZPlane.IX,  0.04);
        err.set(SurfZPlane.IX,SurfZPlane.IY,  0.03);
        err.set(SurfZPlane.IY,SurfZPlane.IY,  0.02);
        ETrack tre1 = new ETrack(szp1.newPureSurface(),vec,err);
        List tclus11 = hcp1.predict(tre1,hcp1);
        Assert.assertTrue( tclus11.size() == 1 );
        Hit pre11 = (Hit)tclus11.get(0);
        double maxdiff = 1.e-12;
        if(debug) System.out.println( pre11.size() );
        Assert.assertTrue( pre11.size() == 2);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( (( HitZPlane2)pre11).x() == pre11.predictedVector().get(IX) );
        Assert.assertTrue( (( HitZPlane2)pre11).y() == pre11.predictedVector().get(IY) );
        Assert.assertTrue( (( HitZPlane2)pre11).dX2() == pre11.predictedError().get(IX,IX) );
        Assert.assertTrue( (( HitZPlane2)pre11).dY2() == pre11.predictedError().get(IY,IY) );
        Assert.assertTrue( (( HitZPlane2)pre11).dXdY() == pre11.predictedError().get(IX,IY) );  Assert.assertTrue( hiteqvec(pre11.measuredVector(), hm1) );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( hiteqsm(pre11.measuredError(), dhm1) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( pre11.predictedVector().equals(calc_hm(vec)) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( pre11.predictedError().equals(calc_dhm(err)) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) -
                calc_hm(vec).get(0) + hm1.get(0)) < maxdiff );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(1) -
                calc_hm(vec).get(1) + hm1.get(1)) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different track."
                );
        vec.set(SurfZPlane.IX, 1.2);
        ETrack tre2 = new ETrack(szp1.newPureSurface(),vec,err);
        List tclus12 = hcp1.predict(tre2,hcp1);
        Assert.assertTrue( tclus12.size() == 1 );
        Hit pre12 = (Hit)tclus12.get(0);
        if(debug) System.out.println( pre12.size() );
        Assert.assertTrue( pre12.size() == 2);
        if(debug) System.out.println( pre12.measuredVector() );
        Assert.assertTrue( hiteqvec(pre12.measuredVector(), hm1) );
        if(debug) System.out.println( pre12.measuredError() );
        Assert.assertTrue( hiteqsm(pre12.measuredError(), dhm1) );
        if(debug) System.out.println( pre12.predictedVector() );
        Assert.assertTrue( pre12.predictedVector().equals(calc_hm(vec)) );
        if(debug) System.out.println( pre12.predictedError() );
        Assert.assertTrue( pre12.predictedError().equals(calc_dhm(err)) );
        if(debug) System.out.println( pre12.dHitdTrack() );
        Assert.assertTrue( pre12.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre12.differenceVector() );
        Assert.assertTrue( Math.abs(pre12.differenceVector().get(0) -
                calc_hm(vec).get(0) + hm1.get(0)) < maxdiff );
        Assert.assertTrue( Math.abs(pre12.differenceVector().get(1) -
                calc_hm(vec).get(1) + hm1.get(1)) < maxdiff );
        // same cluster ==> same hit (even though track changes)
        Assert.assertTrue( pre11.equals(pre11) );
        Assert.assertTrue( ! ( pre11.notEquals(pre11) ) );
        Assert.assertTrue( pre11.equals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different cluster."
                );
        List tclus22 = hcp2.predict(tre2,hcp2);
        Assert.assertTrue( tclus22.size() == 1 );
        Hit pre22 = (Hit)tclus22.get(0);
        if(debug) System.out.println( pre22.size() );
        Assert.assertTrue( pre22.size() == 2);
        if(debug) System.out.println( pre22.measuredVector() );
        Assert.assertTrue( hiteqvec(pre22.measuredVector(), hm2) );
        if(debug) System.out.println( pre22.measuredError() );
        Assert.assertTrue( hiteqsm(pre22.measuredError(), dhm2) );
        if(debug) System.out.println( pre22.predictedVector() );
        Assert.assertTrue( pre22.predictedVector().equals(calc_hm(vec)) );
        if(debug) System.out.println( pre22.predictedError() );
        Assert.assertTrue( pre22.predictedError().equals(calc_dhm(err)) );
        if(debug) System.out.println( pre22.dHitdTrack() );
        Assert.assertTrue( pre22.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre22.differenceVector() );
        Assert.assertTrue( Math.abs(pre22.differenceVector().get(0) -
                calc_hm(vec).get(0) + hm2.get(0)) < maxdiff );
        Assert.assertTrue( Math.abs(pre22.differenceVector().get(1) -
                calc_hm(vec).get(1) + hm2.get(1)) < maxdiff );
        // different cluster ==> different hit
        Assert.assertTrue( pre22.notEquals(pre11) );
        Assert.assertTrue( ! ( pre22.equals(pre11) ) );
        Assert.assertTrue( pre22.notEquals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Update hit." );
        if(debug) System.out.println( pre11 );
        pre11.update(tre2);
        if(debug) System.out.println( pre11 );
        Assert.assertTrue( pre11.size() == 2);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( hiteqvec(pre11.measuredVector(), hm1) );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( hiteqsm(pre11.measuredError(), dhm1) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( pre11.predictedVector().equals(calc_hm(vec)) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( pre11.predictedError().equals(calc_dhm(err)) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) -
                calc_hm(vec).get(0) + hm1.get(0)) < maxdiff );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(1) -
                calc_hm(vec).get(1) + hm1.get(1)) < maxdiff );
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check hit type and equality." );
        if(debug) System.out.println( HitZPlane2.staticType());
        if(debug) System.out.println( pre11.type() );
        if(debug) System.out.println( pre12.type() );
        Assert.assertTrue( pre11.type() != null );
        Assert.assertTrue( pre11.type().equals(HitZPlane2.staticType()) );
        Assert.assertTrue( pre11.type().equals(pre12.type()) );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
