/*
 * PropDCACyl_Test.java
 *
 * Created on July 24, 2007, 9:57 PM
 *
 * $Id: PropDCACyl_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfdca;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfcyl.SurfCylinder;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman Graf
 */
public class PropDCACyl_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropDCACyl_Test */
    public void testPropDCACyl()
    {
        String ok_prefix = "PropDCACyl (I): ";
        String error_prefix = "PropDCACyl test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "-------- Testing component PropDCACyl. --------" );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor." );
        double bfield = 2.0;
        PropDCACyl prop = new PropDCACyl(bfield);
        if(debug) System.out.println(prop );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test cloning." );
        Assert.assertTrue( prop.newPropagator() != null);
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "Test the magnetic field." );
        Assert.assertTrue( prop.bField() == bfield );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "Test propagation of a track - without error." );
        
        PropStat pstat = new PropStat();
        
        TrackVector tvec = new TrackVector();
        tvec.set(0, 10.0);                                // r_signed
        tvec.set(1,  2.0);                                // z
        tvec.set(2,  3.0);                                // phi_direction
        tvec.set(3,  4.0);                                // tlm
        tvec.set(4,  5.0);                                // qpt
        SurfDCA sdca = new SurfDCA();
        VTrack trv = new VTrack( sdca.newPureSurface(),tvec );
        SpacePoint spt = trv.spacePoint();
        
        if(debug) System.out.println(" *** Propagation to Cylinder of same r *** " );
        SurfCylinder scys = new SurfCylinder(10.0);
        VTrack trvsf = new VTrack(trv);
        pstat = prop.vecDirProp(trvsf,scys,PropDir.FORWARD);
        Assert.assertTrue( pstat.success() );
        SpacePoint sptsf = trvsf.spacePoint();
        TrackVector tvec_scys = trvsf.vector();
        if(debug) System.out.println(" sptsf = " );
        if(debug) System.out.println(  sptsf );
        if(debug) System.out.println(" tvec_scys = " + tvec_scys );
        Assert.assertTrue( sptsf.rxy() == tvec.get(SurfDCA.IRSIGNED)   );
        Assert.assertTrue( tvec_scys.get(SurfCylinder.IZ) ==   tvec.get(SurfDCA.IZ) );
        Assert.assertTrue( tvec_scys.get(SurfCylinder.IPHI) == spt.phi() );
        Assert.assertTrue( tvec_scys.get(SurfCylinder.ITLM) == tvec.get(SurfDCA.ITLM) );
        Assert.assertTrue( tvec_scys.get(SurfCylinder.IQPT) == tvec.get(SurfDCA.IQPT) );
        
        SurfCylinder scyl = new SurfCylinder(15.);
        
        if(debug) System.out.println(" *** Forward propagation *** " );
        VTrack trv2f = new VTrack(trv);
        if(debug) System.out.println(" before propagation: trv2f = " + trv2f );
        SpacePoint spt2f = trv2f.spacePoint();
        if(debug) System.out.println(" before propagation: spt2f = " );
        if(debug) System.out.println(spt2f );
        Assert.assertTrue( spt2f.rxy() == 10.0 );
        Assert.assertTrue( Math.abs(spt2f.phi()-spt.phi()) < 1.e-4 );
        Assert.assertTrue( spt2f.z()   == 2.0 );
        pstat = prop.vecDirProp(trv2f,scyl,PropDir.FORWARD);
        Assert.assertTrue( pstat.forward() );
        if(debug) System.out.println("  after propagation: trv2f = " + trv2f );
        spt2f = trv2f.spacePoint();
        if(debug) System.out.println("  after propagation: spt2f = " );
        if(debug) System.out.println(spt2f );
        Assert.assertTrue( spt2f.rxy() == 15.0 );
        
        if(debug) System.out.println(" *** Backward propagation *** " );
        VTrack trv2b = new VTrack(trv);
        if(debug) System.out.println(" before propagation: trv2b = " + trv2b );
        SpacePoint spt2b = trv2b.spacePoint();
        if(debug) System.out.println(" before propagation: spt2b = " );
        if(debug) System.out.println(spt2b );
        Assert.assertTrue( spt2b.rxy() == 10.0 );
        Assert.assertTrue( Math.abs(spt2b.phi()-spt.phi()) < 1.e-4 );
        Assert.assertTrue( spt2b.z()   == 2.0 );
        pstat = prop.vecDirProp(trv2b,scyl,PropDir.BACKWARD);
        Assert.assertTrue( pstat.backward() );
        if(debug) System.out.println("  after propagation: trv2b = " + trv2b );
        spt2b = trv2b.spacePoint();
        if(debug) System.out.println("  after propagation: spt2b = " );
        if(debug) System.out.println(spt2b );
        Assert.assertTrue( spt2b.rxy() == 15.0 );
        
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "Test propagation of a track - with error." );
        
        //  PropStat pstat = new PropStat();
        
        //  TrackVector tvec = new TrackVector();
        //  tvec.set(0, 10.0);                                // r_signed
        //  tvec.set(1,  2.0);                                // z
        //  tvec.set(2,  3.0);                                // phi_direction
        //  tvec.set(3,  4.0);                                // tlm
        //  tvec.set(4,  5.0);                                // qpt
        TrackError err = new TrackError();
        err.set(0,0, 2.0);
        err.set(0,1, 1.0);
        err.set(0,2, 1.0);
        err.set(0,3, 1.0);
        err.set(0,4, 1.0);
        err.set(1,1, 3.0);
        err.set(1,2, 1.0);
        err.set(1,3, 1.0);
        err.set(1,4, 1.0);
        err.set(2,2, 4.0);
        err.set(2,3, 1.0);
        err.set(2,4, 1.0);
        err.set(3,3, 5.0);
        err.set(3,4, 1.0);
        err.set(4,4, 6.0);
        //  SurfDCA sdca;
        ETrack etrv = new ETrack( sdca.newPureSurface(),tvec,err );
        SpacePoint espt = etrv.spacePoint();
        
        //  SurfCylinder scyl = new SurfCylinder(15.);
        
        if(debug) System.out.println(" *** Forward propagation *** " );
        ETrack etrv2f = new ETrack(etrv);
        if(debug) System.out.println(" before propagation: etrv2f = " + etrv2f );
        SpacePoint espt2f = etrv2f.spacePoint();
        if(debug) System.out.println(" before propagation: espt2f = " );
        if(debug) System.out.println(espt2f );
        Assert.assertTrue( espt2f.rxy() == 10.0 );
        Assert.assertTrue( Math.abs(espt2f.phi()-espt.phi()) < 1.e-4 );
        Assert.assertTrue( espt2f.z()   == 2.0 );
        if(debug) System.out.println("scyl= "+scyl);
        if(debug) System.out.println("\n* etrv2f before: "+etrv2f);
        pstat = prop.errDirProp(etrv2f,scyl,PropDir.FORWARD);
        if(debug) System.out.println("\n* etrv2f after: "+etrv2f);
        if(debug) System.out.println("pstat after propagation= "+pstat);
        Assert.assertTrue( pstat.forward() );
        if(debug) System.out.println("  after propagation: etrv2f = " + etrv2f );
        espt2f = etrv2f.spacePoint();
        if(debug) System.out.println("  after propagation: espt2f = " );
        if(debug) System.out.println(espt2f );
        Assert.assertTrue( espt2f.rxy() == 15.0 );
        //********************************************************************
        
        // Test non 0.0 DCA
        if(debug) System.out.println("\n\n ***Testing non zero DCA \n\n");
        int NUM = 10;
        double[] rad =
        {20.,2.0,Math.sqrt(5.)};
        // double[] prec = { 1e-5,1e-5, 1e-5};
        double[] xv   =
        { 0.5 , 0., 2. , 2.,    2.,   2.,  2. ,2.,  2. ,2.};
        double[] yv   =
        { 0.4 , 0., -1., -1.,  -1.,  -1., -1. ,-1., -1. ,-1.};
        double[] r    =
        { 1. , 0., 0.1, -0.1, -0.1, 0.1 , 0. , 0., -0.1 , 0.1};
        double[] phid =
        { 0.1, 0., 0.2, 0.2, 0.2 , 0.2 ,  0. , 0.,  0. , 0.};
        double[] z    =
        {  2., 2., 2. , 2.,    2.,    2., 2. , 3., 2. , 3.};
        double[] tlm  =
        { -4., 4., 4. , 4.,    4.,   -4., 4. , 2., 4. , 2.};
        double[] qpt  =
        { 0.1, 0., 0.01, 0.01,-0.01,-0.01,-0.01 , 0.01,0.0 ,0.};
        for( int j=0; j <3 ;++j)
        {
            for(int i=0 ; i< NUM; ++i )
            {
                if(debug) System.out.println("\n\n\n\n "+j+" "+i);
                SurfDCA srf = new SurfDCA(xv[i],yv[i]);
                TrackVector vec = new TrackVector();
                TrackError errnzdca = new TrackError();
                for(int k=1;k<=5;++k)  errnzdca.set(k-1,k-1,k);
                vec.set(SurfDCA.IRSIGNED, r[i]);
                vec.set(SurfDCA.IZ, z[i]);
                vec.set(SurfDCA.IPHID, phid[i]);
                vec.set(SurfDCA.ITLM, tlm[i]);
                vec.set(SurfDCA.IQPT, qpt[i]);
                ETrack tre = new ETrack( srf.newPureSurface());
                tre.setVector(vec);
                tre.setError(errnzdca);
                tre.setForward();
                SurfCylinder cyl = new SurfCylinder(rad[j]);
                PropDCACyl propdca_cyl = new PropDCACyl( -2.);
                PropCylDCA propcyl_dca = new PropCylDCA(-2.);
                ETrack tre0 = new ETrack(tre);
                ETrack treb = new ETrack(tre);
                PropStat pstatnzdca = propdca_cyl.errDirProp(tre,cyl,PropDir.FORWARD);
                Assert.assertTrue( pstatnzdca.success());
                pstatnzdca = propdca_cyl.errDirProp(treb,cyl,PropDir.BACKWARD);
                Assert.assertTrue( pstatnzdca.success());
                check_derivatives(propdca_cyl,tre0,cyl);
                ETrack tre1 = new ETrack(tre);
                ETrack tre1b = new ETrack(treb);
                pstatnzdca = propcyl_dca.errProp(tre,srf);
                Assert.assertTrue( pstatnzdca.success());
                pstatnzdca = propcyl_dca.errProp(treb,srf);
                Assert.assertTrue( pstatnzdca.success());
                check_derivatives(propcyl_dca,tre1,srf);
                if(debug) System.out.println("checking equality \n"+tre0+" \n"+tre);
                if(debug) System.out.println("about_equal: "+about_equal(tre0,tre));
                //Assert.assertTrue(about_equal(tre0,tre));
                check_derivatives(propcyl_dca,tre1b,srf);
                //    Assert.assertTrue(about_equal(tre0,treb));
                
            }
        }
        
        //********************************************************************
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    public static boolean about_equal(ETrack tre1, ETrack tre2)
    {
        boolean equal = true;
        double maxdif = 1.e-6;
        // Check vector.
        TrackVector vdiff = tre1.surface().vecDiff( tre1.vector(), tre2.vector() );
        for (int i=0; i<5; ++i)
        {
            double adif =Math.abs( vdiff.get(i) );
            if ( ! (adif < maxdif) )
            {
                System.out.println("vecdif(" + i + ") = " + adif );
                equal = false;
            }
        }
        // Check errors.
        TrackError ediff = tre1.error().minus(tre2.error());
        TrackError eavg = tre1.error().plus(tre2.error());
        for (int i=0; i<5; ++i)
        {
            for (int j=0; j<=i; ++j)
            {
                double adif = Math.abs( ediff.get(i,j) );
                double afrac = ediff.get(i,j)/Math.sqrt(eavg.get(i,i)*eavg.get(j,j));
                if ( !(adif < maxdif) )
                {
                    System.out.println("errdif(" + i + "," + j + ") = " + adif + "   frac = " + afrac );
                    equal = false;
                }
            }
        }
        return equal;
    }
    
    public static void check_derivatives(  Propagator prop,  VTrack trv0,  Surface srf)
    {
        check_derivatives(    prop,    trv0,    srf, 1.e-5);
    }
    
    public static void check_derivatives(  Propagator prop,  VTrack trv0,  Surface srf,double prec)
    {
        for(int i=0;i<4;++i)
        {
            for(int j=0;j<4;++j)
            {
                check_derivative(prop,trv0,srf,i,j,prec);
            }
        }
    }
    
    
    public static void check_derivative(  Propagator prop,  VTrack trv0,  Surface srf,int i,int j,double prec)
    {
        
        double dx = 1.e-3;
        VTrack trv = new VTrack(trv0);
        TrackVector vec = trv.vector();
        boolean forward = trv0.isForward();
        
        VTrack trv_0 = new VTrack(trv0);
        TrackDerivative der = new TrackDerivative();
        PropStat pstat = prop.vecProp(trv_0,srf,der);
        Assert.assertTrue(pstat.success());
        
        TrackVector tmp=vec;
        double tmpValue = tmp.get(j);
        tmp.set(j, tmpValue+dx);
        trv.setVector(tmp);
        if(forward) trv.setForward();
        else trv.setBackward();
        
        VTrack trv_pl = new VTrack(trv);
        pstat = prop.vecProp(trv_pl,srf);
        Assert.assertTrue(pstat.success());
        
        TrackVector vecpl = trv_pl.vector();
        
        tmp=vec;
        tmpValue = tmp.get(j);
        tmp.set(j, tmpValue-dx);
        trv.setVector(tmp);
        if(forward) trv.setForward();
        else trv.setBackward();
        
        VTrack trv_mn = trv;
        pstat = prop.vecProp(trv_mn,srf);
        Assert.assertTrue(pstat.success());
        
        TrackVector vecmn = trv_mn.vector();
        
        double dy = (vecpl.get(i)-vecmn.get(i))/2.;
        
        double dy1 = vecpl.get(i)-trv_0.vector(i);
        
        if( !TRFMath.isZero(dy) )
        {
            if( Math.abs((dy1-dy)/dy) > 0.1 )
                return;
        }
        else
        {
            if( Math.abs(dy1) > 1e-5 )
                return;
        }
        if( Math.abs(Math.abs(dy1)-Math.abs(dy)) > 0.01 )
            dy=dy1;
        
        double dydx = dy/dx;
        
        double didj = der.get(i,j);
        
        if( Math.abs(didj) > 1e-10 )
            Assert.assertTrue( Math.abs((dydx - didj)/didj) < prec );
        else
            Assert.assertTrue( Math.abs(dydx) < prec );
    }
}
