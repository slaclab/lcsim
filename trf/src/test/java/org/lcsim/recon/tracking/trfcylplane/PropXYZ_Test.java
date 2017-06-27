/*
 * PropXYZ_Test.java
 *
 * Created on July 24, 2007, 10:46 PM
 *
 * $Id: PropXYZ_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
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
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfxyp.SurfXYPlane;
import org.lcsim.recon.tracking.trfzp.SurfZPlane;

/**
 *
 * @author Norman Graf
 */
public class PropXYZ_Test extends TestCase
{
    private boolean debug;
    
    private static final int IV = SurfXYPlane.IV;
    private static final int   IZ   = SurfXYPlane.IZ;
    private static final int   IDVDU = SurfXYPlane.IDVDU;
    private static final int   IDZDU = SurfXYPlane.IDZDU;
    private static final int   IQP_XY  = SurfXYPlane.IQP;
    
    private static final int   IX = SurfZPlane.IX;
    private static final int   IY   = SurfZPlane.IY;
    private static final int   IDXDZ = SurfZPlane.IDXDZ;
    private static final int   IDYDZ = SurfZPlane.IDYDZ;
    private static final int   IQP_Z  = SurfZPlane.IQP;
    
    /** Creates a new instance of PropXYZ_Test */
    public void testPropXYZ()
    {
        String ok_prefix = "PropXYZ (I): ";
        String error_prefix = "PropXYZ test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropXYZ. --------" );
        {
            if(debug) System.out.println( ok_prefix + "Test constructor." );
            double BFIELD = 2.0*TRFMath.BFAC;
            PropXYZ prop = new PropXYZ(BFIELD/TRFMath.BFAC);
            if(debug) System.out.println( prop );
            
            //********************************************************************
            
            // Here we propagate some tracks both forward and backward and then
            // the same track forward and backward but using method
            // that we checked very thoroughly before.
            
            if(debug) System.out.println( ok_prefix + "Check against correct propagation." );
            
            double u1[]  ={10.,      10.,     10.,     10.,     10.,     10.     };
            double phi1[]={5*Math.PI/6.,  5*Math.PI/6.,  Math.PI/6.,  Math.PI/6.,   5/3*Math.PI,  7/4*Math.PI  };
            int sign_du[]={ -1,        1,      -1,       1,       1,      -1     };
            double v[]   ={ -2.,       2.,      5.,      5.,     -2.,     -2.    };
            double z[]   ={  3.,       3.,      3.,      3.,      3.,      3.    };
            double dvdu[]={ 1.5,     -2.3,     0.,      0.,     -1.5,      0.    };
            double dzdu[]={-2.3,      1.5,    -2.3,     1.5,     2.3,     -1.5   };
            double qp[]  ={ 0.01,    -0.01,    0.01,   -0.01,   -0.01,     0.01  };
            
            double z2[]  ={ 10.,      20.,     10.,     10.,     10.,     10.};
            double z2b[] ={-10.,     -20.,    -20.,    -10.,    -10.,    -10.};
            
            double maxdiff = 1.e-10;
            int ntrk = 6;
            int i;
            
            for ( i=0; i<ntrk; ++i )
            {
                if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
                
                PropStat pstat = new PropStat();
                SurfXYPlane sxyp1 = new SurfXYPlane(u1[i],phi1[i]);
                SurfZPlane   szp2 = new SurfZPlane(z2[i]);
                SurfZPlane  szp2b = new SurfZPlane(z2b[i]);
                
                TrackVector vec1 = new TrackVector();
                vec1.set(SurfXYPlane.IV    , v[i]);     // v
                vec1.set(SurfXYPlane.IZ    , z[i]);     // z
                vec1.set(SurfXYPlane.IDVDU , dvdu[i]);  // dv/du
                vec1.set(SurfXYPlane.IDZDU , dzdu[i]);  // dz/du
                vec1.set(SurfXYPlane.IQP   , qp[i]);    //  q/p
                VTrack trv1 = new VTrack(sxyp1.newPureSurface(),vec1);
                if(sign_du[i]==1) trv1.setForward();
                else trv1.setBackward();
                if(debug) System.out.println( " starting: " + trv1 );
                
                VTrack trv2f = new VTrack(trv1);
                pstat = prop.vecDirProp(trv2f,szp2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "  forward: " + trv2f );
                VTrack trv2b = new VTrack(trv1);
                pstat = prop.vecDirProp(trv2b,szp2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( " backward: " + trv2b );
                
                // Check against correct propagator that I checked somewhere else
                VTrack trv2fc = new VTrack(trv1);
                pstat = vec_propxyz(BFIELD,trv2fc,szp2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "  forward: " + trv2fc );
                VTrack trv2bc = new VTrack(trv1);
                pstat = vec_propxyz(BFIELD,trv2bc,szp2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( " backward: " + trv2bc );
                
                double difff =
                        szp2.vecDiff(trv2f.vector(),trv2fc.vector()).amax();
                double diffb =
                        szp2b.vecDiff(trv2b.vector(),trv2bc.vector()).amax();
                if(debug) System.out.println( "diffs: " + difff + ' ' + diffb );
                Assert.assertTrue( difff < maxdiff );
                Assert.assertTrue( diffb < maxdiff );
                
            }
            
            // Repeat the above with errors.
            if(debug) System.out.println( ok_prefix + "Check reversibility with errors." );
            double evv[] =   {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
            double evz[] =   {  0.01,  -0.01,   0.01,  -0.01,   0.01,  -0.01  };
            double ezz[] =   {  0.25,   0.25,   0.25,   0.25,   0.25,   0.25, };
            double evdv[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double ezdv[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double edvdv[] = {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
            double evdz[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double edvdz[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double ezdz[] =  {  0.04,  -0.04,   0.04,  -0.04,   0.04,  -0.04  };
            double edzdz[] = {  0.02,   0.02,   0.02,   0.02,   0.02,   0.02  };
            double evqp[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double ezqp[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double edvqp[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double edzqp[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double eqpqp[] = {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
            
            for ( i=0; i<ntrk; ++i )
            {
                if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
                PropStat pstat = new PropStat();
                SurfXYPlane sxyp1 = new SurfXYPlane(u1[i],phi1[i]);
                SurfZPlane   szp2 = new SurfZPlane(z2[i]);
                SurfZPlane  szp2b = new SurfZPlane(z2b[i]);
                
                TrackVector vec1 = new TrackVector();
                vec1.set(SurfXYPlane.IV    , v[i]);     // v
                vec1.set(SurfXYPlane.IZ    , z[i]);     // z
                vec1.set(SurfXYPlane.IDVDU , dvdu[i]);  // dv/du
                vec1.set(SurfXYPlane.IDZDU , dzdu[i]);  // dz/du
                vec1.set(SurfXYPlane.IQP   , qp[i]);    //  q/p
                
                TrackError err1 = new TrackError();
                err1.set(SurfXYPlane.IV,SurfXYPlane.IV       , evv[i]);
                err1.set(SurfXYPlane.IV,SurfXYPlane.IZ       , evz[i]);
                err1.set(SurfXYPlane.IZ,SurfXYPlane.IZ       , ezz[i]);
                err1.set(SurfXYPlane.IV,SurfXYPlane.IDVDU    , evdv[i]);
                err1.set(SurfXYPlane.IZ,SurfXYPlane.IDVDU    , ezdv[i]);
                err1.set(SurfXYPlane.IDVDU,SurfXYPlane.IDVDU , edvdv[i]);
                err1.set(SurfXYPlane.IV,SurfXYPlane.IDZDU    , evdz[i]);
                err1.set(SurfXYPlane.IZ,SurfXYPlane.IDZDU    , ezdz[i]);
                err1.set(SurfXYPlane.IDVDU,SurfXYPlane.IDZDU , edvdz[i]);
                err1.set(SurfXYPlane.IDZDU,SurfXYPlane.IDZDU , edzdz[i]);
                err1.set(SurfXYPlane.IV,SurfXYPlane.IQP      , evqp[i]);
                err1.set(SurfXYPlane.IZ,SurfXYPlane.IQP      , ezqp[i]);
                err1.set(SurfXYPlane.IDVDU,SurfXYPlane.IQP   , edvqp[i]);
                err1.set(SurfXYPlane.IDZDU,SurfXYPlane.IQP   , edzqp[i]);
                err1.set(SurfXYPlane.IQP,SurfXYPlane.IQP     , eqpqp[i]);
                ETrack trv1 = new ETrack(sxyp1.newPureSurface(),vec1,err1);
                if(sign_du[i]==1)trv1.setForward();
                else trv1.setBackward();
                if(debug) System.out.println( " starting: " + trv1 );
                
                ETrack trv2f = new ETrack(trv1);
                pstat = prop.errDirProp(trv2f,szp2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "  forward: " + trv2f );
                ETrack trv2b = new ETrack(trv1);
                pstat = prop.errDirProp(trv2b,szp2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( " backward: " + trv2b );
                
                // Check against correct propagator that I checked somewhere else
                ETrack trv2fc = new ETrack(trv1);
                TrackDerivative deriv = new TrackDerivative();
                pstat = vec_propxyz(BFIELD,trv2fc,szp2,PropDir.FORWARD,deriv);
                Assert.assertTrue( pstat.forward() );
                TrackError err = trv2fc.error();
                trv2fc.setError( err.Xform(deriv) );
                if(debug) System.out.println( "  forward: " + trv2fc );
                
                ETrack trv2bc = new ETrack(trv1);
                pstat = vec_propxyz(BFIELD,trv2bc,szp2b,PropDir.BACKWARD,deriv);
                Assert.assertTrue( pstat.backward() );
                err = trv2bc.error();
                trv2bc.setError( err.Xform(deriv) );
                if(debug) System.out.println( " backward: " + trv2bc );
                
                double difff =
                        szp2.vecDiff(trv2f.vector(),trv2fc.vector()).amax();
                double diffb =
                        szp2b.vecDiff(trv2b.vector(),trv2bc.vector()).amax();
                if(debug) System.out.println( "diffs: " + difff + ' ' + diffb );
                Assert.assertTrue( difff < maxdiff );
                Assert.assertTrue( diffb < maxdiff );
                
                TrackError dfc = trv2f.error().minus(trv2fc.error());
                TrackError dbc = trv2b.error().minus(trv2bc.error());
                double edifff = dfc.amax();
                double ediffb = dbc.amax();
                if(debug) System.out.println( "err diffs: " + edifff + ' ' + ediffb );
                Assert.assertTrue( edifff < maxdiff );
                Assert.assertTrue( ediffb < maxdiff );
            }
            
            //********************************************************************
            
            if(debug) System.out.println( ok_prefix + "Test cloning." );
            Assert.assertTrue( prop.newPropagator() != null );
            
            //********************************************************************
            
            if(debug) System.out.println( ok_prefix + "Test the field." );
            Assert.assertTrue( prop.bField() == 2.0 );
            
            //********************************************************************
        }
                if(debug) System.out.println( ok_prefix + "Test Zero Field Propagation." );
                {
                    PropXYZ prop0 = new PropXYZ(0.0);
                    if(debug) System.out.println( prop0 );
                    Assert.assertTrue( prop0.bField() == 0. );
                    
                    double u=10.;
                    double phi=0.1;
                    double z=13.;
                    Surface srf = new SurfXYPlane(u,phi);
                    VTrack trv0 = new VTrack(srf);
                    TrackVector vec = new TrackVector();
                    vec.set(SurfXYPlane.IV, 0.1);
                    vec.set(SurfXYPlane.IZ, 15.);
                    vec.set(SurfXYPlane.IDVDU, 0.1);
                    vec.set(SurfXYPlane.IDZDU, 2.);
                    vec.set(SurfXYPlane.IQP, 0.);
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
                    
                    srf_to = new SurfZPlane(15.0);
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
                
                if(debug) System.out.println( ok_prefix
                        + "------------- All tests passed. -------------" );
                
    }
    //********************************************************************
    
    // Very well tested XY-Z propagator. Each new one can be tested against it
    private static PropStat
            vec_propxyz( double B, VTrack trv, Surface srf,
            PropDir dir)
    {
        TrackDerivative deriv = null;
        return vec_propxyz(B, trv, srf, dir, deriv);
        
    }
    
    
    private static PropStat
            vec_propxyz( double B, VTrack trv, Surface srf,
            PropDir dir,
            TrackDerivative deriv )
    {
        
        // v, z, dv/du,dz/du,q/p
        // b1 b2 b3    b4    b5
        
        // phi is polar angle of track momentum, R radius of the track in field B
        // dphi is angle of rotation of momentum
        // sphi is polar angle of the plane normal
        //
        // a1n = a1 + Rsin_phi*(cos_dphi - 1) + Rcos_phi*sin_dphi;
        // a2n = a2 - Rcos_phi*(cos_dphi - 1) + Rsin_phi*sin_dphi;
        // a3n = a3*cos_dphi - a4*sin_dphi;
        // a4n = a3*sin_dphi + a4*cos_dphi;
        // a5n = a5;
        
        // a1 = u*cos_sphi - b1*sin_sphi;
        // a2 = b1*cos_sphi + u*sin_sphi;
        // a3 = cos_sphi/b4 - b3/b4*sin_sphi;
        // a4 = b3/b4*cos_sphi + sin_sphi/b4;
        // a5 = b5;
        //
        // dz = zpos - b2
        
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        // TrackVector vec1 = trv.vector();
        
        // Check origin is a XYPlane.
        Assert.assertTrue( srf1.pureType().equals(SurfXYPlane.staticType()) );
        if ( !srf1.pureType( ).equals(SurfXYPlane.staticType()) )
            return pstat;
        SurfXYPlane sxyp1 = (SurfXYPlane) srf1;
        
        // Fetch the u and phi of the plane.
        int iphi  = SurfXYPlane.NORMPHI;
        int idist = SurfXYPlane.DISTNORM;
        
        double sphi = sxyp1.parameter(iphi);
        double    u = sxyp1.parameter(idist);
        
        TrackVector vec = trv.vector();
        double b1 = vec.get(IV);                  // v
        double b2 = vec.get(IZ);                  // z
        double b3 = vec.get(IDVDU);               // dv/du
        double b4 = vec.get(IDZDU);               // dz/du
        double b5 = vec.get(IQP_XY);              // q/p
        
        // check that dz != 0
        if(b4 == 0.) return pstat;
        
        double cos_sphi = Math.cos(sphi);
        double sin_sphi = Math.sin(sphi);
        
        double a1 = u*cos_sphi - b1*sin_sphi;
        double a2 = b1*cos_sphi + u*sin_sphi;
        double a3 = cos_sphi/b4 - b3/b4*sin_sphi;
        double a4 = b3/b4*cos_sphi + sin_sphi/b4;
        double a5 = b5;
        
        // Check destination is a ZPlane.
        Assert.assertTrue( srf.pureType().equals(SurfZPlane.staticType()) );
        if ( !srf.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        SurfZPlane szp2 = (SurfZPlane) srf;
        
        // Fetch the zpos's of the planes and the starting track vector.
        int izpos  = SurfZPlane.ZPOS;
        
        double zpos = szp2.parameter(izpos);
        
        double dz = zpos - b2;
        
        // if delta_z * dz/du*du/dt < 0 - fail forward
        // if delta_z * dz/du*du/dt > 0 - fail backward
        
        // if dz/du*du/dt > 0 and forward : dz>0; dz/du*du/dt < 0 dz<0
        // if dz/du*du/dt < 0 and backward : dz>0; dz/du*du/dt > 0 dz<0
        
        int sign_du = 0;
        if(trv.isForward())  sign_du =  1;
        if(trv.isBackward()) sign_du = -1;
        if(sign_du == 0)
        {
            System.out.println("vec_propxyz: Unknown direction of a track ");
            System.exit(1);
        }
        
        int sign_dz = 0;
        if(sign_du*b4>0.) sign_dz =  1;
        if(sign_du*b4<0.) sign_dz = -1;
        
        if (dir.equals(PropDir.NEAREST))
        {
            System.out.println(
                    "vec_propxyz: Propagation in NEAREST direction is undefined"
                    );
            System.exit(1);
        }
        else if (dir.equals(PropDir.FORWARD))
        {
            
            //   xy-z propagation failed
            if(sign_dz*dz<0.)  return pstat;
        }
        else if (dir.equals(PropDir.BACKWARD))
        {
            
            //   xy-z propagation failed
            if(sign_dz*dz>0.) return pstat;
        }
        else
        {
            System.out.println("vec_propxyz: Unknown direction." );
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
        
        double da34_hat_da3 = 0;
        double da34_hat_da4 = 0;
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
        
        // ddphi / db
        
        double ddphi_db2 = -B*a5*Math.sqrt(a34_hat2+1.)*sign_dz;
        
        // da3 / db
        
        double da3_db3 = -sin_sphi/b4;
        double da3_db4 = (-cos_sphi + b3*sin_sphi)/(b4*b4);
        
        
        // da4 / db
        
        double da4_db3 =  cos_sphi/b4;
        double da4_db4 = ( -sin_sphi - b3*cos_sphi)/(b4*b4);
        
        // da1/ db
        
        double da1n_db1 = -sin_sphi;
        double da1n_db2 = -Rsin_phi*sin_dphi*ddphi_db2 + Rcos_phi*cos_dphi*ddphi_db2;
        double da1n_db3 = da1n_da3*da3_db3 + da1n_da4*da4_db3;
        double da1n_db4 = da1n_da3*da3_db4 + da1n_da4*da4_db4;
        double da1n_db5 = da1n_da5;
        
        // da2/ db
        
        double da2n_db1 = cos_sphi;
        double da2n_db2 = Rcos_phi*sin_dphi*ddphi_db2 + Rsin_phi*cos_dphi*ddphi_db2;
        double da2n_db3 = da2n_da3*da3_db3 + da2n_da4*da4_db3;
        double da2n_db4 = da2n_da3*da3_db4 + da2n_da4*da4_db4;
        double da2n_db5 = da2n_da5;
        
        // da3/ db
        
        double da3n_db1 = 0.;
        double da3n_db2 = -a3*sin_dphi*ddphi_db2 - a4*cos_dphi*ddphi_db2;
        double da3n_db3 = da3n_da3*da3_db3 + da3n_da4*da4_db3;
        double da3n_db4 = da3n_da3*da3_db4 + da3n_da4*da4_db4;
        double da3n_db5 = da3n_da5;
        
        // da4/ db
        
        double da4n_db1 = 0.;
        double da4n_db2 = a3*cos_dphi*ddphi_db2 - a4*sin_dphi*ddphi_db2;
        double da4n_db3 = da4n_da3*da3_db3 + da4n_da4*da4_db3;
        double da4n_db4 = da4n_da3*da3_db4 + da4n_da4*da4_db4;
        double da4n_db5 = da4n_da5;
        
        // da5n
        
        double da5n_db1 = 0.;
        double da5n_db2 = 0.;
        double da5n_db3 = 0.;
        double da5n_db4 = 0.;
        double da5n_db5 = 1.;
        
        deriv.set(IX,IV        ,da1n_db1);
        deriv.set(IX,IZ        ,da1n_db2);
        deriv.set(IX,IDVDU     ,da1n_db3);
        deriv.set(IX,IDZDU     ,da1n_db4);
        deriv.set(IX,IQP_XY    ,da1n_db5);
        deriv.set(IY,IV        ,da2n_db1);
        deriv.set(IY,IZ        ,da2n_db2);
        deriv.set(IY,IDVDU     ,da2n_db3);
        deriv.set(IY,IDZDU     ,da2n_db4);
        deriv.set(IY,IQP_XY    ,da2n_db5);
        deriv.set(IDXDZ,IV     ,da3n_db1);
        deriv.set(IDXDZ,IZ     ,da3n_db2);
        deriv.set(IDXDZ,IDVDU  ,da3n_db3);
        deriv.set(IDXDZ,IDZDU  ,da3n_db4);
        deriv.set(IDXDZ,IQP_XY ,da3n_db5);
        deriv.set(IDYDZ,IV     ,da4n_db1);
        deriv.set(IDYDZ,IZ     ,da4n_db2);
        deriv.set(IDYDZ,IDVDU  ,da4n_db3);
        deriv.set(IDYDZ,IDZDU  ,da4n_db4);
        deriv.set(IDYDZ,IQP_XY ,da4n_db5);
        deriv.set(IQP_Z,IV     ,da5n_db1);
        deriv.set(IQP_Z,IZ     ,da5n_db2);
        deriv.set(IQP_Z,IDVDU  ,da5n_db3);
        deriv.set(IQP_Z,IDZDU  ,da5n_db4);
        deriv.set(IQP_Z,IQP_XY ,da5n_db5);
        
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
//        Assert.assertTrue( Math.abs(prod-st) < 1.e-7 );
//        Assert.assertTrue( Math.abs(Math.abs(prod) - moda*modb) < 1.e-7 );
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
        tmp.set(j,tmp.get(j)+dx);
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
