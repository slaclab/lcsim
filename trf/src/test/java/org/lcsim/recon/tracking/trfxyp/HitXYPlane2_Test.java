/*
 * HitXYPlane2_Test.java
 *
 * Created on July 24, 2007, 10:24 PM
 *
 * $Id: HitXYPlane2_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

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
public class HitXYPlane2_Test extends TestCase
{
    private boolean debug;
    
    // calculate hm from a track vector
    static HitVector calc_hm(TrackVector vec)
    {
        double v_track = vec.get(SurfXYPlane.IV);
        double z_track = vec.get(SurfXYPlane.IZ);
        
        return new HitVector( v_track, z_track );
    }
    
    // calculate dhm from error matrix
    static HitError calc_dhm(TrackError err)
    {
        double evv_track = err.get(SurfXYPlane.IV,SurfXYPlane.IV);
        double evz_track = err.get(SurfXYPlane.IV,SurfXYPlane.IZ);
        double ezz_track = err.get(SurfXYPlane.IZ,SurfXYPlane.IZ);
        
        return new HitError( evv_track, evz_track, ezz_track);
    }
    
    // compare HitError and error matrix of the cluster
    
    static boolean hiteqsm(  HitError lhs,   HitError  rhs)
    {
        if ( lhs.size() != rhs.size() || lhs.size()!=2 ) return false;
        return ( (lhs.get(0,0) == rhs.get(ClusXYPlane2.IV,ClusXYPlane2.IV)) &&
                (lhs.get(0,1) == rhs.get(ClusXYPlane2.IV,ClusXYPlane2.IZ)) &&
                (lhs.get(1,1) == rhs.get(ClusXYPlane2.IZ,ClusXYPlane2.IZ)) );
    }
    
    // compare HitVector and vector of the cluster
    
    static boolean hiteqvec(  HitVector  lhs,   HitVector  rhs)
    {
        if ( lhs.size() != rhs.size() || lhs.size()!=2 ) return false;
        return ( (lhs.get(0) == rhs.get(ClusXYPlane2.IV)) &&
                (lhs.get(1) == rhs.get
                (ClusXYPlane2.IZ)) );
    }
    /** Creates a new instance of HitXYPlane2_Test */
    public void testHitXYPlane2()
    {
        String component = "HitXYPlane2";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        if(debug) System.out.println( ok_prefix + "Test cluster constructors." );
        
        int IV = ClusXYPlane2.IV;
        int IZ = ClusXYPlane2.IZ;
        
        
        List mcids = new ArrayList();
        mcids.add(new Integer(1));
        mcids.add(new Integer(2));
        
        double dist1 = 10.0;
        double phi1 = Math.PI/3.;
        double v1 = 1.0;
        double z1 = 1.1;
        HitError dhm1 = new HitError(2);
        dhm1.set(ClusXYPlane2.IV,ClusXYPlane2.IV, 0.02);
        dhm1.set(ClusXYPlane2.IV,ClusXYPlane2.IZ, 0.01);
        dhm1.set(ClusXYPlane2.IZ,ClusXYPlane2.IZ, 0.03);
        SurfXYPlane szp1 = new SurfXYPlane(dist1,phi1);
        ClusXYPlane2 hcp1 = new ClusXYPlane2(dist1,phi1,v1,z1,dhm1.get(ClusXYPlane2.IV,ClusXYPlane2.IV),dhm1.get(ClusXYPlane2.IZ,ClusXYPlane2.IZ),dhm1.get(ClusXYPlane2.IV,ClusXYPlane2.IZ));
        ClusXYPlane2 hcp1a = new ClusXYPlane2(dist1,phi1,v1,z1,dhm1.get(ClusXYPlane2.IV,ClusXYPlane2.IV),dhm1.get(ClusXYPlane2.IZ,ClusXYPlane2.IZ),dhm1.get(ClusXYPlane2.IV,ClusXYPlane2.IZ),mcids);
        
        Assert.assertTrue(hcp1.equals(hcp1a));
        Assert.assertTrue(hcp1.v() == v1 );
        //  Assert.assertTrue(hcp1.get_dhm().equals(dhm1) );
        //  Assert.assertTrue(hcp1.get_hm().equals(HitVector(v1,z1)) );
        Assert.assertTrue(hcp1.z() == z1 );
        Assert.assertTrue(hcp1.dV2() == 0.02 );
        Assert.assertTrue(hcp1.dZ2() == 0.03 );
        Assert.assertTrue(hcp1.dVdZ() == 0.01 );
        Assert.assertTrue(hcp1a.v() == v1 );
        Assert.assertTrue(hcp1a.z() == z1 );
        Assert.assertTrue(hcp1a.dV2() == 0.02 );
        Assert.assertTrue(hcp1a.dZ2() == 0.03 );
        Assert.assertTrue(hcp1a.dVdZ() == 0.01 );
        Assert.assertTrue(hcp1.mcIds().size()==0);
        Assert.assertTrue(hcp1a.mcIds().size()==2);
        
        double dist2 = 20.0;
        double phi2 = Math.PI/5.;
        double v2 = 2.0;
        double z2 = 2.2;
        HitVector hm2 = new HitVector(2);
        hm2.set(ClusXYPlane2.IV, v2);
        hm2.set(ClusXYPlane2.IZ, z2);
        HitError dhm2 =  new HitError(2);
        dhm2.set(ClusXYPlane2.IV,ClusXYPlane2.IV, 0.022);
        dhm2.set(ClusXYPlane2.IV,ClusXYPlane2.IZ, 0.012);
        dhm2.set(ClusXYPlane2.IZ,ClusXYPlane2.IZ, 0.032);
                /*
                ClusXYPlane2 hcp2 = new ClusXYPlane2(dist2,phi2,v2,z2,dhm2);
                ClusXYPlane2 hcp2a = new ClusXYPlane2(dist2,phi2,v2,z2,dhm2);
                Assert.assertTrue( hcp2.equals(hcp2a) );
                //  ClusterPtr phcp2 = &hcp2;
                Assert.assertTrue(hcp2.get_v() == v2 );
                Assert.assertTrue(hcp2.get_z() == z2 );
                Assert.assertTrue(hcp2.get_dv2() == 0.022 );
                Assert.assertTrue(hcp2.get_dz2() == 0.032 );
                Assert.assertTrue(hcp2.get_dvdz() == 0.012 );
                Assert.assertTrue(hcp2a.get_v() == v2 );
                Assert.assertTrue(hcp2a.get_z() == z2 );
                Assert.assertTrue(hcp2a.get_dv2() == 0.022 );
                Assert.assertTrue(hcp2a.get_dz2() == 0.032 );
                Assert.assertTrue(hcp2a.get_dvdz() == 0.012 );
                 */
        HitVector hm1 = new HitVector(2);
        hm1.set(ClusXYPlane2.IV, v1);
        hm1.set(ClusXYPlane2.IZ, z1);
        
        ClusXYPlane2 hcp1b = new ClusXYPlane2(hcp1);
        if(debug) System.out.println( ok_prefix + "test copy constructor");
        
        Assert.assertTrue( hcp1b.equals(hcp1) );
        //********************************************************************
        ClusXYPlane2 hcp2 = new ClusXYPlane2(dist2,phi2,v2,z2,dhm2.get(ClusXYPlane2.IV,ClusXYPlane2.IV),dhm2.get(ClusXYPlane2.IZ,ClusXYPlane2.IZ),dhm2.get(ClusXYPlane2.IV,ClusXYPlane2.IZ),mcids);
        
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
        TrackVector vec =  new TrackVector();
        TrackError err = new TrackError();
        HitDerivative der_expect = new HitDerivative(2);
        der_expect.set(0,0,  1.);
        der_expect.set(1,1,  1.);
        vec.set(SurfXYPlane.IV,  1.1);
        vec.set(SurfXYPlane.IZ, 40.0);
        err.set(SurfXYPlane.IV,SurfXYPlane.IV, 0.04);
        err.set(SurfXYPlane.IV,SurfXYPlane.IZ, 0.03);
        err.set(SurfXYPlane.IZ,SurfXYPlane.IZ, 0.02);
        ETrack tre1 = new ETrack(szp1.newPureSurface(),vec,err);
        List tclus11 = hcp1.predict(tre1,hcp1);
        Assert.assertTrue( tclus11.size() == 1 );
        Hit pre11 = (Hit) tclus11.get(0);
        double maxdiff = 1.e-12;
        if(debug) System.out.println( pre11.size() );
        Assert.assertTrue( pre11.size() == 2);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( ((HitXYPlane2)pre11).v() == pre11.predictedVector().get(IV) );
        Assert.assertTrue( ((HitXYPlane2)pre11).z() == pre11.predictedVector().get(IZ) );
        Assert.assertTrue( ((HitXYPlane2)pre11).dV2() == pre11.predictedError().get(IV,IV) );
        Assert.assertTrue( ((HitXYPlane2)pre11).dZ2() == pre11.predictedError().get(IZ,IZ) );
        Assert.assertTrue( ((HitXYPlane2)pre11).dVdZ() == pre11.predictedError().get(IZ,IV) );
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
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different track."
                );
        vec.set(SurfXYPlane.IV, 1.2);
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
        if(debug) System.out.println( HitXYPlane2.staticType());
        if(debug) System.out.println( pre11.type() );
        if(debug) System.out.println( pre12.type() );
        Assert.assertTrue( pre11.type() != null );
        Assert.assertTrue( pre11.type().equals(HitXYPlane2.staticType()) );
        Assert.assertTrue( pre11.type().equals(pre12.type()) );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
    }
    
}
