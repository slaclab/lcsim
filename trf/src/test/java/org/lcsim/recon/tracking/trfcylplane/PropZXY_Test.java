/*
 * PropZXY_Test.java
 *
 * Created on July 24, 2007, 10:40 PM
 *
 * $Id: PropZXY_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
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
public class PropZXY_Test extends TestCase
{
    private boolean debug;
    // Assign track parameter indices.
    
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
    
    
    /** Creates a new instance of PropZXY_Test */
    public void testPropZXY()
    {
        String ok_prefix = "PropZXY (I): ";
        String error_prefix = "PropZXY test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropZXY. --------" );
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        double BFIELD = 2.0*TRFMath.BFAC;
        PropZXY prop = new PropZXY(BFIELD/TRFMath.BFAC);
        if(debug) System.out.println( prop );
        
        //********************************************************************
        
        // Here we propagate some tracks both forward and backward and then
        // the same track forward and backward but using method
        // that we checked very thoroughly before.
        
        if(debug) System.out.println( ok_prefix + "Check against correct propagation." );
        
        double z1[]  ={10.,      10.,     10.,     10.,     10.,     10.     };
        int sign_dz[]={ -1,        1,      -1,       1,       1,      -1     };
        double x[]   ={ -2.,       2.,      5.,      5.,     -2.,     -2.    };
        double y[]   ={  3.,       3.,      3.,      3.,      3.,      3.    };
        double dxdz[]={ 1.5,     -2.3,     0.,      1.5,    -1.5,      1.5   };
        double dydz[]={ 2.3,     -1.5,    -2.3,     0.,     -2.3,      0.    };
        double qp[]  ={ 0.01,    -0.01,    0.01,   -0.01,   -0.01,     0.01  };
        
        double u2b[]  ={ 10.,      20.,     20.,     10.,     10.,     10.};
        double u2[]   ={ 10.,      20.,     10.,     10.,     10.,     10.};
        double phi2[] ={5*Math.PI/6.,  5*Math.PI/6.,  Math.PI/6.,  Math.PI/6.,   5/3*Math.PI,  7/4*Math.PI};
        double phi2b[]={11*Math.PI/6,  11*Math.PI/6,  7*Math.PI/6, 7*Math.PI/6,  2/3*Math.PI,  3/4*Math.PI};
        
        double maxdiff = 1.e-8;
        int ntrk = 6;
        int i;
        for ( i=0; i<ntrk; ++i )
        {
            if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
            PropStat pstat = new PropStat();
            SurfZPlane szp1 = new SurfZPlane(z1[i]);
            SurfXYPlane sxyp2 = new SurfXYPlane(u2[i],phi2[i]);
            SurfXYPlane sxyp2b = new SurfXYPlane(u2b[i],phi2b[i]);
            
            TrackVector vec1 = new TrackVector();
            vec1.set(SurfZPlane.IX    , x[i]);     // x
            vec1.set(SurfZPlane.IY    , y[i]);     // y
            vec1.set(SurfZPlane.IDXDZ , dxdz[i]);  // dx/dz
            vec1.set(SurfZPlane.IDYDZ , dydz[i]);  // dy/dz
            vec1.set(SurfZPlane.IQP   , qp[i]);    //  q/p
            VTrack trv1 = new VTrack(szp1.newPureSurface(),vec1);
            if (sign_dz[i]==1) trv1.setForward();
            else trv1.setBackward();
            if(debug) System.out.println( " starting: " + trv1 );
            VTrack trv2f = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2f,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "  forward: " + trv2f );
            VTrack trv2b = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2b,sxyp2b,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " backward: " + trv2b );
            
            // Check against correct propagator that I checked somewhere else
            VTrack trv2fc = new VTrack(trv1);
            pstat = vec_propzxy(BFIELD,trv2fc,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "  forward: " + trv2fc );
            VTrack trv2bc = new VTrack(trv1);
            pstat = vec_propzxy(BFIELD,trv2bc,sxyp2b,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " backward: " + trv2bc );
            
            double difff =
                    sxyp2.vecDiff(trv2f.vector(),trv2fc.vector()).amax();
            double diffb =
                    sxyp2b.vecDiff(trv2b.vector(),trv2bc.vector()).amax();
            if(debug) System.out.println( "diffs: " + difff + ' ' + diffb );
            Assert.assertTrue( difff < maxdiff );
            Assert.assertTrue( diffb < maxdiff );
        }
        
        // Repeat the above with errors.
        if(debug) System.out.println( ok_prefix + "Check reversibility with errors." );
        double exx[] =   {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
        double exy[] =   {  0.01,  -0.01,   0.01,  -0.01,   0.01,  -0.01  };
        double eyy[] =   {  0.25,   0.25,   0.25,   0.25,   0.25,   0.25, };
        double exdx[] =  {  0.003, -0.003,  0.003, -0.003,  0.003, -0.003 };
        double eydx[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
        double edxdx[] = {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
        double exdy[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
        double edxdy[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
        double eydy[] =  {  0.04,  -0.04,   0.04,  -0.04,   0.04,  -0.04  };
        double edydy[] = {  0.02,   0.02,   0.02,   0.02,   0.02,   0.02  };
        double exqp[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
        double eyqp[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
        double edxqp[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
        double edyqp[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
        double eqpqp[] = {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
        
        maxdiff = 1.e-6;
        for ( i=0; i<ntrk; ++i )
        {
            if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
            PropStat pstat = new PropStat();
            SurfZPlane szp1 = new SurfZPlane(z1[i]);
            SurfXYPlane sxyp2 = new SurfXYPlane(u2[i],phi2[i]);
            SurfXYPlane sxyp2b = new SurfXYPlane(u2b[i],phi2b[i]);
            
            TrackVector vec1 = new TrackVector();
            vec1.set(SurfZPlane.IX    , x[i]);     // x
            vec1.set(SurfZPlane.IY    , y[i]);     // y
            vec1.set(SurfZPlane.IDXDZ , dxdz[i]);  // dx/dz
            vec1.set(SurfZPlane.IDYDZ , dydz[i]);  // dy/dz
            vec1.set(SurfZPlane.IQP   , qp[i]);    //  q/p
            
            TrackError err1 = new TrackError();
            err1.set(SurfZPlane.IX,SurfZPlane.IX       , exx[i]);
            err1.set(SurfZPlane.IX,SurfZPlane.IY       , exy[i]);
            err1.set(SurfZPlane.IY,SurfZPlane.IY       , eyy[i]);
            err1.set(SurfZPlane.IX,SurfZPlane.IDXDZ    , exdx[i]);
            err1.set(SurfZPlane.IY,SurfZPlane.IDXDZ    , eydx[i]);
            err1.set(SurfZPlane.IDXDZ,SurfZPlane.IDXDZ , edxdx[i]);
            err1.set(SurfZPlane.IX,SurfZPlane.IDYDZ    , exdy[i]);
            err1.set(SurfZPlane.IY,SurfZPlane.IDYDZ    , eydy[i]);
            err1.set(SurfZPlane.IDXDZ,SurfZPlane.IDYDZ , edxdy[i]);
            err1.set(SurfZPlane.IDYDZ,SurfZPlane.IDYDZ , edydy[i]);
            err1.set(SurfZPlane.IX,SurfZPlane.IQP      , exqp[i]);
            err1.set(SurfZPlane.IY,SurfZPlane.IQP      , eyqp[i]);
            err1.set(SurfZPlane.IDXDZ,SurfZPlane.IQP   , edxqp[i]);
            err1.set(SurfZPlane.IDYDZ,SurfZPlane.IQP   , edyqp[i]);
            err1.set(SurfZPlane.IQP,SurfZPlane.IQP     , eqpqp[i]);
            
            ETrack trv1 = new ETrack(szp1.newPureSurface(),vec1,err1);
            if (sign_dz[i]==1) trv1.setForward();
            else trv1.setBackward();
            if(debug) System.out.println( " starting: " + trv1 );
            
            ETrack trv2f = new ETrack(trv1);
            pstat = prop.errDirProp(trv2f,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "  forward: " + trv2f );
            ETrack trv2b = new ETrack(trv1);
            pstat = prop.errDirProp(trv2b,sxyp2b,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " backward: " + trv2b );
            
            // Check against correct propagator that I checked somewhere else
            ETrack trv2fc = new ETrack(trv1);
            TrackDerivative deriv = new TrackDerivative();
            pstat = vec_propzxy(BFIELD,trv2fc,sxyp2,PropDir.FORWARD,deriv);
            Assert.assertTrue( pstat.forward() );
            TrackError err = trv2fc.error();
            trv2fc.setError( err.Xform(deriv) );
            if(debug) System.out.println( "  forward: " + trv2fc );
            
            ETrack trv2bc = new ETrack(trv1);
            pstat = vec_propzxy(BFIELD,trv2bc,sxyp2b,PropDir.BACKWARD,deriv);
            Assert.assertTrue( pstat.backward() );
            err = trv2bc.error();
            trv2bc.setError( err.Xform(deriv) );
            if(debug) System.out.println( " backward: " + trv2bc );
            
            double difff =
                    sxyp2.vecDiff(trv2f.vector(),trv2fc.vector()).amax();
            double diffb =
                    sxyp2b.vecDiff(trv2b.vector(),trv2bc.vector()).amax();
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
        
        if(debug) System.out.println( ok_prefix + "Test Zero Field Propagation." );
        {
            PropZXY prop0 = new PropZXY(0.0);
            if(debug) System.out.println( prop0 );
            Assert.assertTrue( prop0.bField() == 0. );
            
            double u=10.;
            double phi=0.1;
            Surface srf = new SurfZPlane(13.0);
            VTrack trv0 = new VTrack(srf);
            TrackVector vec = new TrackVector();
            vec.set(SurfZPlane.IX, (u+1.0)*Math.cos(phi+0.1));
            vec.set(SurfZPlane.IY, (u+1.0)*Math.sin(phi+0.1));
            vec.set(SurfZPlane.IDXDZ, 0.1);
            vec.set(SurfZPlane.IDYDZ, 2.);
            vec.set(SurfZPlane.IQP, 0.);
            trv0.setVector(vec);
            trv0.setForward();
            Surface srf_to = new SurfXYPlane(u,phi);
            
            VTrack trv = new VTrack(trv0);
            VTrack trv_der = new VTrack(trv);
            PropStat pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
            if(debug) System.out.println(trv);
            Assert.assertTrue( pstat.success() );
            
            Assert.assertTrue( pstat.backward() );
            Assert.assertTrue(trv.surface().pureEqual(srf_to));
            
            check_zero_propagation(trv0,trv,pstat);
            check_derivatives(prop0,trv_der,srf_to);
            
            srf_to = new SurfXYPlane(u+1.,phi+0.1);
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
        if(debug) System.out.println( ok_prefix + "Test cloning." );
        Assert.assertTrue( prop.newPropagator() != null );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test the field." );
        Assert.assertTrue( prop.bField() == 2.0 );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
    }
    
    
    // Very well tested Z-XY propagator. Each new one can be tested against it
    
    private static PropStat
            vec_propzxy( double B, VTrack trv, Surface srf,
            PropDir dir)
    {
        TrackDerivative deriv = null;
        return vec_propzxy(B, trv, srf, dir, deriv);
        
    }
    
    private static PropStat
            vec_propzxy( double B, VTrack trv, Surface srf,
            PropDir dir,
            TrackDerivative deriv )
    {
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        // TrackVector vec1 = trv.vector();
        
        // Check origin is a ZPlane.
        Assert.assertTrue( srf1.pureType().equals(SurfZPlane.staticType()) );
        if ( !srf1.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        SurfZPlane szp1 = ( SurfZPlane ) srf1;
        
        
        // Fetch the zpos's of the planes and the starting track vector.
        int izpos  = SurfZPlane.ZPOS;
        
        double zpos = szp1.parameter(izpos);
        
        TrackVector vec = trv.vector();
        double a1 = vec.get(IX);                  // x
        double a2 = vec.get(IY);                  // y
        double a3 = vec.get(IDXDZ);               // dx/dz
        double a4 = vec.get(IDYDZ);               // dy/dz
        double a5 = vec.get(IQP_Z);               // q/p
        
        int sign_dz = 0;
        if(trv.isForward())  sign_dz =  1;
        if(trv.isBackward()) sign_dz = -1;
        if(sign_dz == 0)
        {
            System.out.println("PropZXY.vec_prop: Unknown direction of a track ");
            System.exit(1);
        }
        
        // Check destination is a XYPlane.
        Assert.assertTrue( srf.pureType().equals(SurfXYPlane.staticType()) );
        if ( !srf.pureType( ).equals(SurfXYPlane.staticType()) )
            return pstat;
        SurfXYPlane sxyp2 = ( SurfXYPlane ) srf;
        
        // Fetch the u and phi of the plane.
        int iphi  = SurfXYPlane.NORMPHI;
        int idist = SurfXYPlane.DISTNORM;
        
        double phi_n = sxyp2.parameter(iphi);
        double   u_n = sxyp2.parameter(idist);
        
        double sinphi_n = Math.sin(phi_n);
        double cosphi_n = Math.cos(phi_n);
        
        // check if du == 0 ( that is track moves parallel to the destination plane )
        double du_dz = a3*cosphi_n + a4*sinphi_n;
        if(du_dz == 0.) return pstat;
        
        double a_hat_phin = 1./du_dz;
        double a_hat_phin2 = a_hat_phin*a_hat_phin;
        
        double u  =  a1*cosphi_n + a2*sinphi_n;
        double b1 = -a1*sinphi_n + a2*cosphi_n;
        double b2 = zpos;
        double b3 = (a4*cosphi_n - a3*sinphi_n)*a_hat_phin;
        double b4 = a_hat_phin;
        double b5 = a5;
        
        
        int sign_du = 0;
        if(b4*sign_dz > 0) sign_du =  1;
        if(b4*sign_dz < 0) sign_du = -1;
        
        double b3_hat = Math.sqrt(1 + b3*b3);
        double b34_hat = Math.sqrt(1 + b3*b3 + b4*b4);
        double b3_hat2 = b3_hat*b3_hat;
        double b34_hat2 = b34_hat*b34_hat;
        double r = 1/(b5*B)*b3_hat/b34_hat;
        double cosphi =         -b3*sign_du/b3_hat;
        double sinphi =             sign_du/b3_hat;
        double rsinphi =  1./(b5*B)*sign_du/b34_hat;
        double rcosphi = -b3/(b5*B)*sign_du/b34_hat;
        
        double du = u_n - u;
        double norm = du/r - cosphi;
        
        // z-xy propagation failed : noway to the new plane
        if(Math.abs(norm)>1.) return pstat;
        
        double cat = Math.sqrt(1.-norm*norm);
        int sign_dphi = 0;
        
        if (dir.equals(PropDir.NEAREST))
        {
            
            System.out.println("PropZXY._vec_prop: Propagation in NEAREST direction is undefined");
            System.exit(1);
        }
        else if (dir.equals(PropDir.FORWARD))
        {
            if(b5>0.) sign_dphi =  1;
            if(b5<0.) sign_dphi = -1;
        }
        else if (dir.equals(PropDir.BACKWARD))
        {
            if(b5>0.) sign_dphi = -1;
            if(b5<0.) sign_dphi =  1;
        }
        else
        {
            System.out.println("PropZXY._vec_prop: Unknown direction." );
            System.exit(1);
        }
        
        int sign_cat = 0;
        if(du*sign_dphi*b5>0.) sign_cat =  1;
        if(du*sign_dphi*b5<0.) sign_cat = -1;
        if(du == 0.)
        {
            if(sinphi >=0. ) sign_cat =  1;
            if(sinphi < 0. ) sign_cat = -1;
        }
        
        double sin_dphi =  norm*sinphi + cat*cosphi*sign_cat;
        double cos_dphi = -norm*cosphi + cat*sinphi*sign_cat;
        
        int sign_sindphi = 0;
        if(sin_dphi> 0.) sign_sindphi =  1;
        if(sin_dphi< 0.) sign_sindphi = -1;
        if(sin_dphi == 0.) sign_sindphi = sign_dphi;
        
        double dphi = Math.PI*(sign_dphi - sign_sindphi) + sign_sindphi*Math.acos(cos_dphi);
        
        // check if dun == 0 ( that is track moves parallel to the destination plane)
        double du_n_du = cos_dphi - b3*sin_dphi;
        if(du_n_du==0.) return pstat;
        
        double a_hat_dphi = 1./du_n_du;
        double a_hat_dphi2 = a_hat_dphi*a_hat_dphi;
        double c_hat_dphi =     sin_dphi + b3*cos_dphi ;
        
        double b1_n = b1 + rsinphi*(1-cos_dphi) - rcosphi*sin_dphi;
        double b2_n = b2 + b4/(b5*B)*sign_du/b34_hat*dphi;
        double b3_n = c_hat_dphi*a_hat_dphi;
        double b4_n =         b4*a_hat_dphi;
        double b5_n = b5;
        
        double zpos_n = b2_n;
        
        // check if track crossed original plane during the propagation
        if (dir.equals(PropDir.FORWARD))
        {
            if((zpos_n - zpos)*sign_dz<0) return pstat;
        }
        else if (dir.equals(PropDir.BACKWARD))
        {
            if((zpos_n - zpos)*sign_dz>0) return pstat;
        }
        
        int sign_dun = 0;
        if(du_n_du*sign_du > 0) sign_dun =  1;
        if(du_n_du*sign_du < 0) sign_dun = -1;
        
        vec.set(IV     , b1_n);
        vec.set(IZ     , b2_n);
        vec.set(IDVDU  , b3_n);
        vec.set(IDZDU  , b4_n);
        vec.set(IQP_XY , b5_n);
        
        // Set the return status.
        
        if (dir.equals(PropDir.FORWARD))
        {
            pstat.setForward();
        }
        else if (dir.equals(PropDir.BACKWARD))
        {
            pstat.setBackward();
        }
        
        // Update trv
        trv.setSurface(srf.newPureSurface());
        trv.setVector(vec);
        // set new direction of the track
        if(sign_dun ==  1) trv.setForward();
        if(sign_dun == -1) trv.setBackward();
        
        // exit now if user did not ask for error matrix.
        if ( deriv == null ) return pstat;
        
        // du_da
        
        double du_da1 = cosphi_n;
        double du_da2 = sinphi_n;
        
        // db1_da
        
        double db1_da1 = - sinphi_n;
        double db1_da2 =   cosphi_n;
        
        // db3_da
        
        double db3_da3 = -a4*a_hat_phin2;
        double db3_da4 =  a3*a_hat_phin2;
        
        // db4_da
        
        double db4_da3 = - cosphi_n*a_hat_phin2;
        double db4_da4 = - sinphi_n*a_hat_phin2;
        
        // dr_db
        
        double dr_db3 = r*b3*b4*b4/(b3_hat2*b34_hat2);
        double dr_db4 = -r*b4/b34_hat2;
        double dr_db5 = -r/b5;
        
        // dcosphi_db
        
        double dcosphi_db3 = - sign_du/b3_hat - cosphi*b3/b3_hat2;
        
        // dsinphi_db
        
        double dsinphi_db3 = - sinphi*b3/b3_hat2;
        
        // dcat_db
        
        double dcat_db3 = norm/cat*(du/(r*r)*dr_db3 + dcosphi_db3 );
        double dcat_db4 = norm/cat* du/(r*r)*dr_db4;
        double dcat_db5 = norm/cat* du/(r*r)*dr_db5;
        double dcat_du  = norm/(cat*r);
        
        // dnorm_db
        
        double dnorm_db3 = - du/(r*r)*dr_db3 - dcosphi_db3;
        double dnorm_db4 = - du/(r*r)*dr_db4;
        double dnorm_db5 = - du/(r*r)*dr_db5;
        double dnorm_du  = - 1./r;
        
        // dcos_dphi_db
        
        double dcos_dphi_db3 = - cosphi*dnorm_db3 - norm*dcosphi_db3 +
                sign_cat*(sinphi*dcat_db3 + cat*dsinphi_db3);
        double dcos_dphi_db4 = - cosphi*dnorm_db4 + sign_cat*sinphi*dcat_db4;
        double dcos_dphi_db5 = - cosphi*dnorm_db5 + sign_cat*sinphi*dcat_db5;
        double dcos_dphi_du  = - cosphi*dnorm_du  + sign_cat*sinphi*dcat_du;
        
        // dsin_dphi_db
        
        double dsin_dphi_db3 = sinphi*dnorm_db3 + norm*dsinphi_db3 +
                sign_cat*(cosphi*dcat_db3 + cat*dcosphi_db3);
        double dsin_dphi_db4 = sinphi*dnorm_db4 + sign_cat*cosphi*dcat_db4;
        double dsin_dphi_db5 = sinphi*dnorm_db5 + sign_cat*cosphi*dcat_db5;
        double dsin_dphi_du  = sinphi*dnorm_du  + sign_cat*cosphi*dcat_du;
        
        // ddphi_db
        
        double ddphi_db3 = 0;
        double ddphi_db4 = 0;
        double ddphi_db5 = 0;
        double ddphi_du = 0;
        if(Math.abs(sin_dphi)>0.5)
        {
            ddphi_db3 = - dcos_dphi_db3/sin_dphi;
            ddphi_db4 = - dcos_dphi_db4/sin_dphi;
            ddphi_db5 = - dcos_dphi_db5/sin_dphi;
            ddphi_du  = - dcos_dphi_du /sin_dphi;
        }
        else
        {
            ddphi_db3 = dsin_dphi_db3/cos_dphi;
            ddphi_db4 = dsin_dphi_db4/cos_dphi;
            ddphi_db5 = dsin_dphi_db5/cos_dphi;
            ddphi_du  = dsin_dphi_du /cos_dphi;
        }
        
        // da_hat_dphi_db
        
        double da_hat_dphi_db3 = - a_hat_dphi2*
                (dcos_dphi_db3 - sin_dphi - b3*dsin_dphi_db3);
        double da_hat_dphi_db4 = - a_hat_dphi2*(dcos_dphi_db4 - b3*dsin_dphi_db4);
        double da_hat_dphi_db5 = - a_hat_dphi2*(dcos_dphi_db5 - b3*dsin_dphi_db5);
        double da_hat_dphi_du  = - a_hat_dphi2*(dcos_dphi_du  - b3*dsin_dphi_du );
        
        // dc_hat_dphi_db
        
        double dc_hat_dphi_db3 = b3*dcos_dphi_db3 + dsin_dphi_db3 + cos_dphi;
        double dc_hat_dphi_db4 = b3*dcos_dphi_db4 + dsin_dphi_db4;
        double dc_hat_dphi_db5 = b3*dcos_dphi_db5 + dsin_dphi_db5;
        double dc_hat_dphi_du  = b3*dcos_dphi_du  + dsin_dphi_du ;
        
        // db1_n_db
        
        double db1_n_db1 = 1;
        double db1_n_db3 = (dr_db3*sinphi+r*dsinphi_db3)*(1-cos_dphi)
        - rsinphi*dcos_dphi_db3
                - dr_db3*cosphi*sin_dphi - r*dcosphi_db3*sin_dphi
                - rcosphi*dsin_dphi_db3;
        double db1_n_db4 = dr_db4*sinphi*(1-cos_dphi) - rsinphi*dcos_dphi_db4
                - dr_db4*cosphi*sin_dphi - rcosphi*dsin_dphi_db4;
        double db1_n_db5 = dr_db5*sinphi*(1-cos_dphi) - rsinphi*dcos_dphi_db5
                - dr_db5*cosphi*sin_dphi - rcosphi*dsin_dphi_db5;
        double db1_n_du  = - rsinphi*dcos_dphi_du - rcosphi*dsin_dphi_du;
        
        // db2_n_db
        
        // double db2_n_db2 = 1.;
        double db2_n_db3 = 1./(b5*B)*b4*sign_du/b34_hat*
                ( - dphi*b3/b34_hat2 + ddphi_db3);
        double db2_n_db4 = 1./(b5*B)*sign_du/b34_hat*
                ( - dphi*b4*b4/b34_hat2 + b4*ddphi_db4 + dphi );
        double db2_n_db5 = 1./(b5*B)*b4*sign_du/b34_hat*( ddphi_db5 - dphi/b5);
        double db2_n_du  = 1./(b5*B)*b4*sign_du/b34_hat*ddphi_du;
        
        // db3_n_db
        
        double db3_n_db3 = a_hat_dphi*dc_hat_dphi_db3 + da_hat_dphi_db3*c_hat_dphi;
        double db3_n_db4 = a_hat_dphi*dc_hat_dphi_db4 + da_hat_dphi_db4*c_hat_dphi;
        double db3_n_db5 = a_hat_dphi*dc_hat_dphi_db5 + da_hat_dphi_db5*c_hat_dphi;
        double db3_n_du  = a_hat_dphi*dc_hat_dphi_du  + da_hat_dphi_du *c_hat_dphi;
        
        // db4_n_db
        
        double db4_n_db3 = b4*da_hat_dphi_db3;
        double db4_n_db4 = b4*da_hat_dphi_db4 + a_hat_dphi;
        double db4_n_db5 = b4*da_hat_dphi_db5;
        double db4_n_du  = b4*da_hat_dphi_du;
        
        // db5_n_db
        
        // double db5_n_db5 = 1.;
        
        // db1_n_da
        
        double db1_n_da1 = db1_n_du * du_da1 + db1_n_db1*db1_da1;
        double db1_n_da2 = db1_n_du * du_da2 + db1_n_db1*db1_da2;
        double db1_n_da3 = db1_n_db3*db3_da3 + db1_n_db4*db4_da3;
        double db1_n_da4 = db1_n_db3*db3_da4 + db1_n_db4*db4_da4;
        double db1_n_da5 = db1_n_db5;
        
        // db2_n_da
        
        double db2_n_da1 = db2_n_du * du_da1;
        double db2_n_da2 = db2_n_du * du_da2;
        double db2_n_da3 = db2_n_db3*db3_da3 + db2_n_db4*db4_da3;
        double db2_n_da4 = db2_n_db3*db3_da4 + db2_n_db4*db4_da4;
        double db2_n_da5 = db2_n_db5;
        
        // db3_n_da
        
        double db3_n_da1 = db3_n_du * du_da1;
        double db3_n_da2 = db3_n_du * du_da2;
        double db3_n_da3 = db3_n_db3*db3_da3 + db3_n_db4*db4_da3;
        double db3_n_da4 = db3_n_db3*db3_da4 + db3_n_db4*db4_da4;
        double db3_n_da5 = db3_n_db5;
        
        // db4_n_da
        
        double db4_n_da1 = db4_n_du * du_da1;
        double db4_n_da2 = db4_n_du * du_da2;
        double db4_n_da3 = db4_n_db3*db3_da3 + db4_n_db4*db4_da3;
        double db4_n_da4 = db4_n_db3*db3_da4 + db4_n_db4*db4_da4;
        double db4_n_da5 = db4_n_db5;
        
        // db5_n_da
        
        double db5_n_da1 = 0.;
        double db5_n_da2 = 0.;
        double db5_n_da3 = 0.;
        double db5_n_da4 = 0.;
        double db5_n_da5 = 1.;
        
        deriv.set(IV,IX        , db1_n_da1);
        deriv.set(IV,IY        , db1_n_da2);
        deriv.set(IV,IDXDZ     , db1_n_da3);
        deriv.set(IV,IDYDZ     , db1_n_da4);
        deriv.set(IV,IQP_Z     , db1_n_da5);
        deriv.set(IZ,IX        , db2_n_da1);
        deriv.set(IZ,IY        , db2_n_da2);
        deriv.set(IZ,IDXDZ     , db2_n_da3);
        deriv.set(IZ,IDYDZ     , db2_n_da4);
        deriv.set(IZ,IQP_Z     , db2_n_da5);
        deriv.set(IDVDU,IX     , db3_n_da1);
        deriv.set(IDVDU,IY     , db3_n_da2);
        deriv.set(IDVDU,IDXDZ  , db3_n_da3);
        deriv.set(IDVDU,IDYDZ  , db3_n_da4);
        deriv.set(IDVDU,IQP_Z  , db3_n_da5);
        deriv.set(IDZDU,IX     , db4_n_da1);
        deriv.set(IDZDU,IY     , db4_n_da2);
        deriv.set(IDZDU,IDXDZ  , db4_n_da3);
        deriv.set(IDZDU,IDYDZ  , db4_n_da4);
        deriv.set(IDZDU,IQP_Z  , db4_n_da5);
        deriv.set(IQP_XY,IX    , db5_n_da1);
        deriv.set(IQP_XY,IY    , db5_n_da2);
        deriv.set(IQP_XY,IDXDZ , db5_n_da3);
        deriv.set(IQP_XY,IDYDZ , db5_n_da4);
        deriv.set(IQP_XY,IQP_Z , db5_n_da5);
        
        return pstat;
    }
    
    //********************************************************************
    
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
        tmp.set(j, tmp.get(j)+dx);
        trv.setVector(tmp);
        if(forward) trv.setForward();
        else trv.setBackward();
        
        VTrack trv_pl = new VTrack(trv);
        pstat = prop.vecProp(trv_pl,srf);
        Assert.assertTrue(pstat.success());
        
        TrackVector vecpl = trv_pl.vector();
        
        tmp= new TrackVector(vec);
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
