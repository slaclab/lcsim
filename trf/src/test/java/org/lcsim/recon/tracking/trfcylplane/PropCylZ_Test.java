/*
 * PropCylZ_Test.java
 *
 * Created on July 24, 2007, 10:51 PM
 *
 * $Id: PropCylZ_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcylplane;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
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
import org.lcsim.recon.tracking.trfzp.SurfZPlane;

/**
 *
 * @author Norman Graf
 */
public class PropCylZ_Test extends TestCase
{
    private boolean debug;
    
    // Assign track parameter indices.
    private static final int   IX = SurfZPlane.IX;
    private static final int   IY   = SurfZPlane.IY;
    private static final int   IDXDZ = SurfZPlane.IDXDZ;
    private static final int   IDYDZ = SurfZPlane.IDYDZ;
    private static final int   IQP_Z  = SurfZPlane.IQP;
    
    private static final int   IPHI = SurfCylinder.IPHI;
    private static final int   IZ_CYL = SurfCylinder.IZ;
    private static final int   IALF = SurfCylinder.IALF;
    private static final int   ITLM = SurfCylinder.ITLM;
    private static final int   IQPT  = SurfCylinder.IQPT;
    
    //************************************************************************
    
    // compare two tracks  without errors
    
    private double compare(VTrack trv1,VTrack trv2)
    {
        Surface srf = trv2.surface();
        
        Assert.assertTrue(trv1.surface().equals(srf));
        
        double diff = srf.vecDiff(trv2.vector(),trv1.vector()).amax();
        
        return diff;
    }
    
    //**********************************************************************
    // compare two tracks  with errors
    
    private double[] compare(ETrack trv1,ETrack trv2 )
    {
        double[] tmp = new double[2];
        Surface srf = trv2.surface();
        
        Assert.assertTrue(trv1.surface().equals(srf));
        
        tmp[0] = srf.vecDiff(trv2.vector(),trv1.vector()).amax();
        
        TrackError dfc = trv2.error().minus(trv1.error());
        tmp[1] = dfc.amax();
        
        
        return tmp;
    }
    /** Creates a new instance of PropCylZ_Test */
    public void testPropCylZ()
    {
        String ok_prefix = "PropCylZ (I): ";
        String error_prefix = "PropCylZ test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropCylZ. --------" );
        {
            if(debug) System.out.println( ok_prefix + "Test constructor." );
            double BFIELD = 2.0;
            PropCylZ prop = new PropCylZ(BFIELD);
            if(debug) System.out.println( prop );
            
            PropCylZ_Test tst = new PropCylZ_Test();
            
            //********************************************************************
            
            // Here we propagate some tracks both forward and backward and then
            // the same track forward and backward but using method
            // that we checked very thoroughly before.
            
            if(debug) System.out.println( ok_prefix + "Check against correct propagation." );
            
            PropCylZ propcylz = new PropCylZ(BFIELD/TRFMath.BFAC);
            
            double phi[]   ={  Math.PI/5,   Math.PI/6,   Math.PI/6,   4/3*Math.PI,  5/3*Math.PI,  5/3*Math.PI };
            double z[]     ={  1.5,   -2.3,    0.,     1.5,    -1.5,    -1.5    };
            double alpha[] ={  0.1,   -0.1,    0.,     0.2,    -0.2,     0.     };
            double tlm[]   ={  2.3,   -1.5,   -2.3,    2.3,    -2.3,     2.3    };
            double qpt[]   ={  0.01,  -0.01,   0.01,  -0.01,   -0.01,    0.01   };
            
            double z2[]    ={  5.5,   -6.0,   -5.0,    6.0,    -5.7,     4.0    };
            double z2b[]   ={ -4.0,    4.0,    5.0,   -3.0,     3.0,    -6.0    };
            
            double maxdiff = 1.e-10;
            double diff;
            int ntrk = 6;
            int i;
            
            for ( i=0; i<ntrk; ++i )
            {
                if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
                PropStat pstat = new PropStat();
                SurfCylinder scy1 = new SurfCylinder(10.);
                SurfZPlane   szp2 = new SurfZPlane(z2[i]);
                SurfZPlane   szp2b = new SurfZPlane(z2b[i]);
                
                TrackVector vec1 = new TrackVector();
                
                vec1.set(IPHI   , phi[i]);            // phi
                vec1.set(IZ_CYL , z[i]);              // z
                vec1.set(IALF   , alpha[i]);          // alpha
                vec1.set(ITLM   , tlm[i]);            // tan(lambda)
                vec1.set(IQPT   , qpt[i]);            // q/pt
                
                VTrack trv1 = new VTrack(scy1.newPureSurface(),vec1);
                
                if(debug) System.out.println( "\n starting: " + trv1 );
                
                VTrack trv2f = new VTrack(trv1);
                pstat = propcylz.vecDirProp(trv2f,szp2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "\n  forward: " + trv2f );
                
                VTrack trv2f_my = new VTrack(trv1);
                pstat = tst.vec_propcylz(BFIELD,trv2f_my,szp2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "\n  forward my: " + trv2f_my );
                diff = tst.compare(trv2f_my,trv2f);
                
                if(debug) System.out.println( "\n diff: " + diff );
                Assert.assertTrue( diff < maxdiff );
                
                VTrack trv2b = new VTrack(trv1);
                pstat = propcylz.vecDirProp(trv2b,szp2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( "\n  backward: " + trv2b );
                
                VTrack trv2b_my = new VTrack(trv1);
                pstat = tst.vec_propcylz(BFIELD,trv2b_my,szp2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( "\n  backward my: " + trv2b_my );
                diff = tst.compare(trv2b_my,trv2b);
                
                if(debug) System.out.println( "\n diff: " + diff );
                Assert.assertTrue( diff < maxdiff );
                
            }
            //********************************************************************
            
            // Repeat the above with errors.
            if(debug) System.out.println( ok_prefix + "Check against correct propagation with errors." );
            
            double epp[] = {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
            double epz[] = {  0.01,  -0.01,   0.01,  -0.01,   0.01,  -0.01,   0.01  };
            double ezz[] = {  0.25,   0.25,   0.25,   0.25,   0.25,   0.25,   0.25  };
            double epa[] = {  0.003, -0.003,  0.003, -0.003,  0.003, -0.003,  0.003 };
            double eza[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004,  0.004 };
            double eaa[] = {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
            double epl[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004,  0.004 };
            double eal[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004,  0.004 };
            double ezl[] = {  0.04,  -0.04,   0.04,  -0.04,   0.04,  -0.04,   0.04  };
            double ell[] = {  0.02,   0.02,   0.02,   0.02,   0.02,   0.02,   0.02  };
            double epc[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004,  0.004 };
            double ezc[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004,  0.004 };
            double eac[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004,  0.004 };
            double elc[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004,  0.004 };
            double ecc[] = {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
            
            maxdiff = 1.e-8;
            double ediff;
            ntrk = 6;
            
            for ( i=0; i<ntrk; ++i )
            {
                if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
                PropStat pstat = new PropStat();
                SurfCylinder scy1 = new SurfCylinder(10.);
                SurfZPlane   szp2 = new SurfZPlane(z2[i]);
                SurfZPlane   szp2b = new SurfZPlane(z2b[i]);
                
                TrackVector vec1 = new TrackVector();
                
                vec1.set(IPHI  ,  phi[i]);            // phi
                vec1.set(IZ_CYL,  z[i]);              // z
                vec1.set(IALF  ,  alpha[i]);          // alpha
                vec1.set(ITLM  ,  tlm[i]);            // tan(lambda)
                vec1.set(IQPT  ,  qpt[i]);            // q/pt
                
                TrackError err1 = new TrackError();
                
                err1.set(IPHI,IPHI    ,  epp[i]);
                err1.set(IPHI,IZ_CYL  ,  epz[i]);
                err1.set(IZ_CYL,IZ_CYL,  ezz[i]);
                err1.set(IPHI,IALF    ,  epa[i]);
                err1.set(IZ_CYL,IALF  ,  eza[i]);
                err1.set(IALF,IALF    ,  eaa[i]);
                err1.set(IPHI,ITLM    ,  epl[i]);
                err1.set(IZ_CYL,ITLM  ,  ezl[i]);
                err1.set(IALF,ITLM    ,  eal[i]);
                err1.set(ITLM,ITLM    ,  ell[i]);
                err1.set(IPHI,IQPT    ,  epc[i]);
                err1.set(IZ_CYL,IQPT  ,  ezc[i]);
                err1.set(IALF,IQPT    ,  eac[i]);
                err1.set(ITLM,IQPT    ,  elc[i]);
                err1.set(IQPT,IQPT    ,  ecc[i]);
                
                ETrack trv1 = new ETrack(scy1.newPureSurface(),vec1,err1);
                
                if(debug) System.out.println( "\n starting: " + trv1 );
                
                ETrack trv2f = new ETrack(trv1);
                pstat = propcylz.errDirProp(trv2f,szp2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "\n  forward: " + trv2f );
                
                ETrack trv2f_my = new ETrack(trv1);
                TrackDerivative deriv = new TrackDerivative();
                pstat = tst.vec_propcylz(BFIELD,trv2f_my,szp2,PropDir.FORWARD,deriv);
                Assert.assertTrue( pstat.forward() );
                TrackError err = trv2f_my.error();
                trv2f_my.setError( err.Xform(deriv) );
                if(debug) System.out.println( "\n  forward my: " + trv2f_my );
                double[] diffs = tst.compare(trv2f_my,trv2f);
                
                if(debug) System.out.println( "\n diff: " + diffs[0] + ' ' + "ediff: "+ diffs[1] );
                Assert.assertTrue( diffs[0] < maxdiff );
                Assert.assertTrue( diffs[1] < maxdiff );
                
                
                ETrack trv2b = new ETrack(trv1);
                pstat = propcylz.errDirProp(trv2b,szp2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( "\n  backward: " + trv2b );
                
                ETrack trv2b_my = new ETrack(trv1);
                pstat = tst.vec_propcylz(BFIELD,trv2b_my,szp2b,PropDir.BACKWARD,deriv);
                Assert.assertTrue( pstat.backward() );
                err = trv2b_my.error();
                trv2b_my.setError( err.Xform(deriv) );
                if(debug) System.out.println( "\n  backward my: " + trv2b_my );
                diffs = tst.compare(trv2b_my,trv2b);
                
                if(debug) System.out.println( "\n diff: " + diffs[0] + ' ' + "ediff: "+ diffs[1] );
                Assert.assertTrue( diffs[0] < maxdiff );
                Assert.assertTrue( diffs[1] < maxdiff );
                
            }
            
            //********************************************************************
            
            if(debug) System.out.println( ok_prefix + "Test cloning." );
            Assert.assertTrue( prop.newPropagator() != null );
            
            //********************************************************************
            
            if(debug) System.out.println( ok_prefix + "Test the field." );
            Assert.assertTrue( prop.bField() == 2.0 );
            
        }
                
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix + "Test Zero Field Propagation." );
                {
                    PropCylZ prop0 = new PropCylZ(0.0);
                    if(debug) System.out.println( prop0 );
                    Assert.assertTrue( prop0.bField() == 0. );
                    
                    double z=6.;
                    Surface srf = new SurfCylinder(13.0);
                    VTrack trv0 = new VTrack(srf);
                    TrackVector vec = new TrackVector();
                    vec.set(SurfCylinder.IPHI, 0.1);
                    vec.set(SurfCylinder.IZ, 10.);
                    vec.set(SurfCylinder.IALF, 0.1);
                    vec.set(SurfCylinder.ITLM, 2.);
                    vec.set(SurfCylinder.IQPT, 0.);
                    trv0.setVector(vec);
                    trv0.setForward();
                    Surface srf_to = new SurfZPlane(z);
                    
                    VTrack trv = new VTrack(trv0);
                    VTrack trv_der = new VTrack(trv);
                    PropStat pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    if(debug) System.out.println(trv);
                    Assert.assertTrue( pstat.success() );
                    
                    Assert.assertTrue( pstat.backward() );
                    Assert.assertTrue(trv.surface().pureEqual(srf_to));
                    
                    check_zero_propagation(trv0,trv,pstat);
                    check_derivatives(prop0,trv_der,srf_to);
                    
                    srf_to = new SurfZPlane(10.);
                    trv = new VTrack(trv0);
                    trv_der = new VTrack(trv);
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    if(debug) System.out.println(trv);
                    Assert.assertTrue( pstat.success() );
                    
                    Assert.assertTrue( pstat.same() );
                    Assert.assertTrue(trv.surface().pureEqual(srf_to));
                    
                    check_zero_propagation(trv0,trv,pstat);
                    check_derivatives(prop0,trv_der,srf_to);
                    
                }
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix
                        + "------------- All tests passed. -------------" );
                //********************************************************************
    }
    
    
    // Very well tested Cyl-Z propagator. Each new one can be tested against it
    private PropStat
            vec_propcylz( double B, VTrack trv,  Surface srf,
            PropDir dir )
    {
        TrackDerivative deriv = null;
        return vec_propcylz(B, trv, srf, dir, deriv);
    }
    private PropStat
            vec_propcylz( double B, VTrack trv,  Surface srf,
            PropDir dir,
            TrackDerivative deriv  )
    {
        
        // phi, z, alpha=phi_dir-phi, tan(lambda)=pz/pt, q/pt
        // c1   c2   c3                c4                 c5
        //
        // x   y   dx/dz dy/dz q/p
        // a1  a2  a3    a4    a5
        //
        // phi is polar angle of track momentum, R radius of the track in field B
        // dphi is angle of rotation of momentum
        // Rcyl is cylinder radius
        //
        // a1n = a1 + Rsin_phi*(cos_dphi - 1) + Rcos_phi*sin_dphi;
        // a2n = a2 - Rcos_phi*(cos_dphi - 1) + Rsin_phi*sin_dphi;
        // a3n = a3*cos_dphi - a4*sin_dphi;
        // a4n = a3*sin_dphi + a4*cos_dphi;
        // a5n = a5;
        
        // a1 = Rcyl*cos_c1;
        // a2 = Rcyl*sin_c1;
        // a3 = cos(c1+c3)/c4;
        // a4 = sin(c1+c3)/c4;
        // a5 = c5/sqrt(1+c4*c4);
        //
        // dz = zpos - c2
        
        
        // construct return status
        PropStat pstat = new PropStat();;
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        // TrackVector vec1 = trv.vector();
        
        // Check origin is a Cylinder.
        Assert.assertTrue( srf1.pureType().equals(SurfCylinder.staticType()) );
        if ( ! srf1.pureType( ).equals(SurfCylinder.staticType()) )
            return pstat;
        SurfCylinder scy1 = (SurfCylinder) srf1;
        
        
        // Fetch the R of the cylinder and the starting track vector.
        int ir  = SurfCylinder.RADIUS;
        double Rcyl = scy1.parameter(ir);
        
        TrackVector vec = trv.vector();
        double c1 = vec.get(IPHI);                 // phi
        double c2 = vec.get(IZ_CYL);               // z
        double c3 = vec.get(IALF);                 // alpha
        double c4 = vec.get(ITLM);                 // tan(lambda)
        double c5 = vec.get(IQPT);                 // q/pt
        
        // check that dz != 0
        if(c4 == 0.) return pstat;
        
        double cos_c1 = Math.cos(c1);
        double sin_c1 = Math.sin(c1);
        double cos_dir = Math.cos(c1+c3);
        double sin_dir = Math.sin(c1+c3);
        double c4_hat2 = 1+c4*c4;
        double c4_hat  = Math.sqrt(c4_hat2);
        
        double a1 = Rcyl*cos_c1;
        double a2 = Rcyl*sin_c1;
        double a3 = cos_dir/c4;
        double a4 = sin_dir/c4;
        double a5 = c5/c4_hat;
        
        
        // if tan(lambda)=dz/dst > 0 : dz>0; dzdst < 0 dz<0
        
        int sign_dz = 0;
        if(c4 > 0) sign_dz =  1;
        if(c4 < 0) sign_dz = -1;
        
        
        // Check destination is a ZPlane.
        Assert.assertTrue( srf.pureType().equals(SurfZPlane.staticType()) );
        if ( ! srf.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        SurfZPlane szp2 = (SurfZPlane) srf;
        
        // Fetch the zpos's of the planes.
        int izpos  = SurfZPlane.ZPOS;
        
        double zpos = szp2.parameter(izpos);
        
        double dz = zpos - c2;
        
        
        if (dir.equals(PropDir.NEAREST))
        {
            
            if(debug) System.out.println("vec_propcylz: Propagation in NEAREST direction is undefined");
            System.exit(1);
        }
        else if (dir.equals(PropDir.FORWARD))
        {
            //   cyl-z propagation failed
            if(sign_dz*dz<0.)  return pstat;
        }
        else if (dir.equals(PropDir.BACKWARD))
        {
            //   cyl-z propagation failed
            if(sign_dz*dz>0.) return pstat;
        }
        else
        {
            if(debug) System.out.println("vec_propcylz: Unknown direction." );
            System.exit(1);
        }
        
        double a34_hat = sign_dz*Math.sqrt(a3*a3 + a4*a4);
        double a34_hat2 = a34_hat*a34_hat;
        double dphi = B*dz*a5*Math.sqrt(1.+a34_hat2)*sign_dz;
        double cos_dphi = Math.cos(dphi);
        double sin_dphi = Math.sin(dphi);
        
        double Rcos_phi = 1./(B*a5)*a3/Math.sqrt(1.+a34_hat2)*sign_dz;
        double Rsin_phi = 1./(B*a5)*a4/Math.sqrt(1.+a34_hat2)*sign_dz;
        
        double a1n = a1 + Rsin_phi*(cos_dphi - 1) + Rcos_phi*sin_dphi;
        double a2n = a2 - Rcos_phi*(cos_dphi - 1) + Rsin_phi*sin_dphi;
        double a3n =  a3*cos_dphi - a4*sin_dphi;
        double a4n =  a3*sin_dphi + a4*cos_dphi;
        double a5n = a5;
        
        vec.set(IX    , a1n);
        vec.set(IY    , a2n);
        vec.set(IDXDZ , a3n);
        vec.set(IDYDZ , a4n);
        vec.set(IQP_Z , a5n);
        
        // Update trv
        trv.setSurface(srf.newPureSurface());
        trv.setVector(vec);
        
        // set new direction of the track
        
        if(sign_dz ==  1) trv.setForward();
        if(sign_dz == -1) trv.setBackward();
        
        // Set the return status.
        
        if (dir.equals(PropDir.FORWARD))
        {
            pstat.setForward();
        }
        else if (dir.equals(PropDir.BACKWARD))
        {
            pstat.setBackward();
        }
        
        // exit now if user did not ask for error matrix.
        if ( deriv == null ) return pstat;
        
        //da34_hat
        
        double da34_hat_da3 = 0.;
        double da34_hat_da4 = 0.;
        if(Math.abs(a3)>=Math.abs(a4))
        {
            int sign3=1;
            if(a3<0) sign3 = -1;
            if(a3 !=0.)
            {
                da34_hat_da3 = sign_dz*sign3/Math.sqrt(1.+(a4/a3)*(a4/a3) );
                da34_hat_da4 = sign_dz*(a4/Math.abs(a3))/Math.sqrt(1.+(a4/a3)*(a4/a3) );
            }
            else
            {
                da34_hat_da3 = 0.;
                da34_hat_da4 = 0.;
            }
        }
        if(Math.abs(a4)>Math.abs(a3))
        {
            int sign4=1;
            if(a4<0) sign4 = -1;
            if(a4 !=0.)
            {
                da34_hat_da3 = sign_dz*(a3/Math.abs(a4))/Math.sqrt(1.+(a3/a4)*(a3/a4) );
                da34_hat_da4 = sign_dz*sign4/Math.sqrt(1.+(a3/a4)*(a3/a4) );
            }
            else
            {
                da34_hat_da3 = 0.;
                da34_hat_da4 = 0.;
            }
        }
        
        // ddphi / da
        
        double ddphi_da3 = B*dz*a5*a34_hat*sign_dz/Math.sqrt(1.+a34_hat2)*da34_hat_da3;
        double ddphi_da4 = B*dz*a5*a34_hat*sign_dz/Math.sqrt(1.+a34_hat2)*da34_hat_da4;
        double ddphi_da5 = B*dz*Math.sqrt(a34_hat2+1.)*sign_dz;
        
        // dRsin_phi
        
        double dRsin_phi_da3 = -Rsin_phi*a34_hat/(1.+a34_hat2)*da34_hat_da3;
        double dRsin_phi_da4 = -Rsin_phi*a34_hat/(1.+a34_hat2)*da34_hat_da4 +
                sign_dz/(B*a5)/Math.sqrt(1.+a34_hat2);
        double dRsin_phi_da5 = -Rsin_phi/a5;
        
        // dRcos_phi
        
        double dRcos_phi_da3 = -Rcos_phi*a34_hat/(1.+a34_hat2)*da34_hat_da3 +
                sign_dz/(B*a5)/Math.sqrt(1.+a34_hat2);
        double dRcos_phi_da4 = -Rcos_phi*a34_hat/(1.+a34_hat2)*da34_hat_da4;
        double dRcos_phi_da5 = -Rcos_phi/a5;
        
        // da1n first two are simple because dR,dphi _da1,_da2 = 0.
        
        // double da1n_da1 = 1.;
        double da1n_da3 = dRsin_phi_da3*(cos_dphi-1.) + dRcos_phi_da3*sin_dphi
                - Rsin_phi*sin_dphi*ddphi_da3 + Rcos_phi*cos_dphi*ddphi_da3;
        double da1n_da4 = dRsin_phi_da4*(cos_dphi-1.) + dRcos_phi_da4*sin_dphi
                - Rsin_phi*sin_dphi*ddphi_da4 + Rcos_phi*cos_dphi*ddphi_da4;
        double da1n_da5 = dRsin_phi_da5*(cos_dphi-1.) + dRcos_phi_da5*sin_dphi
                - Rsin_phi*sin_dphi*ddphi_da5 + Rcos_phi*cos_dphi*ddphi_da5;
        
        // da2n first two are simple because dR,dphi _da1,_da2 = 0.
        
        // double da2n_da2 = 1.;
        double da2n_da3 = -dRcos_phi_da3*(cos_dphi-1.) + dRsin_phi_da3*sin_dphi
                + Rcos_phi*sin_dphi*ddphi_da3 + Rsin_phi*cos_dphi*ddphi_da3;
        double da2n_da4 = -dRcos_phi_da4*(cos_dphi-1.) + dRsin_phi_da4*sin_dphi
                + Rcos_phi*sin_dphi*ddphi_da4 + Rsin_phi*cos_dphi*ddphi_da4;
        double da2n_da5 = -dRcos_phi_da5*(cos_dphi-1.) + dRsin_phi_da5*sin_dphi
                + Rcos_phi*sin_dphi*ddphi_da5 + Rsin_phi*cos_dphi*ddphi_da5;
        
        // da3n first two are simple because dphi _da1,_da2 = 0.
        
        double da3n_da3 = cos_dphi - a3*sin_dphi*ddphi_da3 - a4*cos_dphi*ddphi_da3;
        double da3n_da4 = - sin_dphi - a3*sin_dphi*ddphi_da4 - a4*cos_dphi*ddphi_da4;
        double da3n_da5 = - a3*sin_dphi*ddphi_da5 - a4*cos_dphi*ddphi_da5;
        
        // da4n first two are simple because dphi _da1,_da2 = 0.
        
        double da4n_da3 =   sin_dphi + a3*cos_dphi*ddphi_da3 - a4*sin_dphi*ddphi_da3;
        double da4n_da4 =   cos_dphi + a3*cos_dphi*ddphi_da4 - a4*sin_dphi*ddphi_da4;
        double da4n_da5 =   a3*cos_dphi*ddphi_da5 - a4*sin_dphi*ddphi_da5;
        
        // da5n
        
        // double da5n_da5 = 1.;
        
        // ddphi / dc
        
        double ddphi_dc2 = -B*a5*Math.sqrt(a34_hat2+1.)*sign_dz;
        
        // da1 / dc
        
        double da1_dc1 = -Rcyl*sin_c1;
        
        // da2 / dc
        
        double da2_dc1 =  Rcyl*cos_c1;
        
        // da3 / dc
        
        double da3_dc1 = -sin_dir/c4;
        double da3_dc3 = -sin_dir/c4;
        double da3_dc4 = -cos_dir/(c4*c4);
        
        // da4 / dc
        
        double da4_dc1 =  cos_dir/c4;
        double da4_dc3 =  cos_dir/c4;
        double da4_dc4 = -sin_dir/(c4*c4);
        
        // da5 / dc
        
        double da5_dc4 =  -c5*c4/(c4_hat*c4_hat2);
        double da5_dc5 =   1./c4_hat;
        
        // da1n/ dc
        
        double da1n_dc1 = da1_dc1 + da1n_da3*da3_dc1 + da1n_da4*da4_dc1;
        double da1n_dc2 = -Rsin_phi*sin_dphi*ddphi_dc2 + Rcos_phi*cos_dphi*ddphi_dc2;
        double da1n_dc3 = da1n_da3*da3_dc3 + da1n_da4*da4_dc3;
        double da1n_dc4 = da1n_da3*da3_dc4 + da1n_da4*da4_dc4 + da1n_da5*da5_dc4;
        double da1n_dc5 = da1n_da5*da5_dc5;
        
        // da2n/ dc
        
        double da2n_dc1 = da2_dc1 + da2n_da3*da3_dc1 + da2n_da4*da4_dc1;
        double da2n_dc2 = Rcos_phi*sin_dphi*ddphi_dc2 + Rsin_phi*cos_dphi*ddphi_dc2;
        double da2n_dc3 = da2n_da3*da3_dc3 + da2n_da4*da4_dc3;
        double da2n_dc4 = da2n_da3*da3_dc4 + da2n_da4*da4_dc4 + da2n_da5*da5_dc4;
        double da2n_dc5 = da2n_da5*da5_dc5;
        
        // da3n/ dc
        
        double da3n_dc1 = da3n_da3*da3_dc1 + da3n_da4*da4_dc1;
        double da3n_dc2 = -a3*sin_dphi*ddphi_dc2 - a4*cos_dphi*ddphi_dc2;
        double da3n_dc3 = da3n_da3*da3_dc3 + da3n_da4*da4_dc3;
        double da3n_dc4 = da3n_da3*da3_dc4 + da3n_da4*da4_dc4 + da3n_da5*da5_dc4;
        double da3n_dc5 = da3n_da5*da5_dc5;
        
        // da4n/ dc
        
        double da4n_dc1 = da4n_da3*da3_dc1 + da4n_da4*da4_dc1;
        double da4n_dc2 = a3*cos_dphi*ddphi_dc2 - a4*sin_dphi*ddphi_dc2;
        double da4n_dc3 = da4n_da3*da3_dc3 + da4n_da4*da4_dc3;
        double da4n_dc4 = da4n_da3*da3_dc4 + da4n_da4*da4_dc4 + da4n_da5*da5_dc4;
        double da4n_dc5 = da4n_da5*da5_dc5;
        
        // da5n/ dc
        
        double da5n_dc1 = 0.;
        double da5n_dc2 = 0.;
        double da5n_dc3 = 0.;
        double da5n_dc4 = da5_dc4;
        double da5n_dc5 = da5_dc5;
        
        deriv.set(IX,IPHI      , da1n_dc1);
        deriv.set(IX,IZ_CYL    , da1n_dc2);
        deriv.set(IX,IALF      , da1n_dc3);
        deriv.set(IX,ITLM      , da1n_dc4);
        deriv.set(IX,IQPT      , da1n_dc5);
        deriv.set(IY,IPHI      , da2n_dc1);
        deriv.set(IY,IZ_CYL    , da2n_dc2);
        deriv.set(IY,IALF      , da2n_dc3);
        deriv.set(IY,ITLM      , da2n_dc4);
        deriv.set(IY,IQPT      , da2n_dc5);
        deriv.set(IDXDZ,IPHI   , da3n_dc1);
        deriv.set(IDXDZ,IZ_CYL , da3n_dc2);
        deriv.set(IDXDZ,IALF   , da3n_dc3);
        deriv.set(IDXDZ,ITLM   , da3n_dc4);
        deriv.set(IDXDZ,IQPT   , da3n_dc5);
        deriv.set(IDYDZ,IPHI   , da4n_dc1);
        deriv.set(IDYDZ,IZ_CYL , da4n_dc2);
        deriv.set(IDYDZ,IALF   , da4n_dc3);
        deriv.set(IDYDZ,ITLM   , da4n_dc4);
        deriv.set(IDYDZ,IQPT   , da4n_dc5);
        deriv.set(IQP_Z,IPHI   , da5n_dc1);
        deriv.set(IQP_Z,IZ_CYL , da5n_dc2);
        deriv.set(IQP_Z,IALF   , da5n_dc3);
        deriv.set(IQP_Z,ITLM   , da5n_dc4);
        deriv.set(IQP_Z,IQPT   , da5n_dc5);
        
        return pstat;
    }
    
    private static void  check_zero_propagation( VTrack trv0, VTrack trv, PropStat pstat)
    {
        
        SpacePoint sp = trv.spacePoint();
        SpacePoint sp0 = trv0.spacePoint();
        
        SpacePath sv = trv.spacePath();
        SpacePath sv0 = trv0.spacePath();
        
        Assert.assertTrue( Math.abs(sv0.dx() - sv.dx())<1e-7 );
        Assert.assertTrue( Math.abs(sv0.dy() - sv.dy())<1e-7 );
        Assert.assertTrue( Math.abs(sv0.dz() - sv.dz())<1e-7 );
        
        double x0 = sp0.x();
        double y0 = sp0.y();
        double z0 = sp0.z();
        double x1 = sp.x();
        double y1 = sp.y();
        double z1 = sp.z();
        
        double dx = sv.dx();
        double dy = sv.dy();
        double dz = sv.dz();
        
        double prod = dx*(x1-x0)+dy*(y1-y0)+dz*(z1-z0);
        double moda = Math.sqrt((x1-x0)*(x1-x0)+(y1-y0)*(y1-y0) + (z1-z0)*(z1-z0));
        double modb = Math.sqrt(dx*dx+dy*dy+dz*dz);
        double st = pstat.pathDistance();
        Assert.assertTrue( Math.abs(prod-st) < 1.e-7 );
        Assert.assertTrue( Math.abs(Math.abs(prod) - moda*modb) < 1.e-7 );
    }
    
    private static void check_derivatives( Propagator prop, VTrack trv0, Surface srf)
    {
        for(int i=0;i<4;++i)
            for(int j=0;j<4;++j)
                check_derivative(prop,trv0,srf,i,j);
    }
    
    private static void check_derivative( Propagator prop, VTrack trv0, Surface srf,int i,int j)
    {
        
        double dx = 1.e-3;
        VTrack trv = new VTrack(trv0);
        TrackVector vec = trv.vector();
        boolean forward = trv0.isForward();
        
        VTrack trv_0 = new VTrack(trv0);
        TrackDerivative der = new TrackDerivative();
        PropStat pstat = prop.vecProp(trv_0,srf,der);
        Assert.assertTrue(pstat.success());
        
        TrackVector tmp = new TrackVector(vec);
        tmp.set(j, tmp.get(j)+dx);
        trv.setVector(tmp);
        if(forward) trv.setForward();
        else trv.setBackward();
        
        VTrack trv_pl = new VTrack(trv);
        pstat = prop.vecProp(trv_pl,srf);
        Assert.assertTrue(pstat.success());
        
        TrackVector vecpl = trv_pl.vector();
        
        tmp = new TrackVector(vec);
        tmp.set(j, tmp.get(j)-dx);
        trv.setVector(tmp);
        if(forward) trv.setForward();
        else trv.setBackward();
        
        VTrack trv_mn = new VTrack(trv);
        pstat = prop.vecProp(trv_mn,srf);
        Assert.assertTrue(pstat.success());
        
        TrackVector vecmn = trv_mn.vector();
        
        double dy = (vecpl.get(i)-vecmn.get(i))/2.;
        
        double dydx = dy/dx;
        
        double didj = der.get(i,j);
        
        if( Math.abs(didj) > 1e-10 )
            Assert.assertTrue( Math.abs((dydx - didj)/didj) < 1e-4 );
        else
            Assert.assertTrue( Math.abs(dydx) < 1e-4 );
    }
    
}
