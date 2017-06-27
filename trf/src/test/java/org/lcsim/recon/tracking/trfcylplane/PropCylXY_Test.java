/*
 * PropCylXY_Test.java
 *
 * Created on July 24, 2007, 10:53 PM
 *
 * $Id: PropCylXY_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
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
import org.lcsim.recon.tracking.trfxyp.SurfXYPlane;

/**
 *
 * @author Norman Graf
 */
public class PropCylXY_Test extends TestCase
{
    private boolean debug;
    
    // Assign track parameter indices.
    
    private static final int IV = SurfXYPlane.IV;
    private static final int IZ   = SurfXYPlane.IZ;
    private static final int IDVDU = SurfXYPlane.IDVDU;
    private static final int IDZDU = SurfXYPlane.IDZDU;
    private static final int IQP_XY  = SurfXYPlane.IQP;
    
    private static final int IPHI = SurfCylinder.IPHI;
    private static final int IZ_CYL = SurfCylinder.IZ;
    private static final int IALF = SurfCylinder.IALF;
    private static final int ITLM = SurfCylinder.ITLM;
    private static final int IQPT  = SurfCylinder.IQPT;
    
    
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
    /** Creates a new instance of PropCylXY_Test */
    public void testPropCylXY()
    {
        String ok_prefix = "PropCylXY (I): ";
        String error_prefix = "PropCylXY test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropCylXY. --------" );
        
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        double BFIELD = 2.0;
        PropCylXY prop = new PropCylXY(BFIELD);
        if(debug) System.out.println( prop );
        
        PropCylXY_Test tst = new PropCylXY_Test();
        
        //********************************************************************
        
        // Here we propagate some tracks both forward and backward and then
        // the same track forward and backward but using method
        // that we checked very thoroughly before.
        
        if(debug) System.out.println( ok_prefix + "Check against correct propagation." );
        
        PropCylXY propcylxy = new PropCylXY(BFIELD/TRFMath.BFAC);
        
        double phi[]    ={ Math.PI/5,   Math.PI/6,   Math.PI/6,   4/3*Math.PI,  5/3*Math.PI,  5/3*Math.PI };
        double z[]      ={ 1.5,   -2.3,    0.,     1.5,    -1.5,     1.5    };
        double alpha[]  ={ 0.1,   -0.1,    0.,     0.2,    -0.2,     0.     };
        double tlm[]    ={ 2.3,   -1.5,   -2.3,    2.3,    -2.3,     0.     };
        double qpt[]    ={ 0.01,  -0.01,   0.01,  -0.01,   -0.01,    0.01   };
        
        double u2b[]    ={ 6.,     6.,     6.,     6.,      6.,      6.     };
        double phic2b[] ={ Math.PI/6.,  Math.PI/5.,  Math.PI/7.,  5/3*Math.PI,  4/3*Math.PI,  7/4*Math.PI };
        double u2[]     ={ 15.,    15.,    15.,    15.,     15.,     15.    };
        double phic2[]  ={ Math.PI/6.,  Math.PI/5.,  Math.PI/7.,  5/3*Math.PI,  4/3*Math.PI,  7/4*Math.PI };
        
        double maxdiff = 1.e-10;
        double diff;
        int ntrk = 6;
        int i;
        
        for ( i=0; i<ntrk; ++i )
        {
            if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
            PropStat pstat = new PropStat();
            SurfCylinder scy1 = new SurfCylinder(10.);
            SurfXYPlane  sxyp2 = new SurfXYPlane(u2[i],phic2[i]);
            SurfXYPlane  sxyp2b= new SurfXYPlane(u2b[i],phic2b[i]);
            
            TrackVector vec1 = new TrackVector();
            
            vec1.set(IPHI   , phi[i]);            // phi
            vec1.set(IZ_CYL , z[i]);              // z
            vec1.set(IALF   , alpha[i]);          // alpha
            vec1.set(ITLM   , tlm[i]);            // tan(lambda)
            vec1.set(IQPT   , qpt[i]);            // q/pt
            
            VTrack trv1 = new VTrack(scy1.newPureSurface(),vec1);
            
            if(debug) System.out.println( "\n starting: " + trv1 );
            
            
            VTrack trv2f = new VTrack(trv1);
            pstat = propcylxy.vecDirProp(trv2f,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "\n  forward: " + trv2f );
            
            VTrack trv2f_my = new VTrack(trv1);
            pstat = tst.vec_propcylxy(BFIELD,trv2f_my,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "\n  forward my: " + trv2f_my );
            diff = tst.compare(trv2f_my,trv2f);
            
            if(debug) System.out.println( "\n diff: " + diff );
            Assert.assertTrue( diff < maxdiff );
            
            
            VTrack trv2b = new VTrack(trv1);
            pstat = propcylxy.vecDirProp(trv2b,sxyp2b,PropDir.BACKWARD_MOVE);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( "\n  backward: " + trv2b );
            
            VTrack trv2b_my = new VTrack(trv1);
            pstat = tst.vec_propcylxy(BFIELD,trv2b_my,sxyp2b,PropDir.BACKWARD);
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
        double ezl[] = {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004,  0.004 };
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
            SurfXYPlane  sxyp2 = new SurfXYPlane(u2[i],phic2[i]);
            SurfXYPlane  sxyp2b = new SurfXYPlane(u2b[i],phic2b[i]);
            
            TrackVector vec1 = new TrackVector();
            
            vec1.set(IPHI   ,  phi[i]);            // phi
            vec1.set(IZ_CYL ,  z[i]);              // z
            vec1.set(IALF   ,  alpha[i]);          // alpha
            vec1.set(ITLM   ,  tlm[i]);            // tan(lambda)
            vec1.set(IQPT   ,  qpt[i]);            // q/pt
            
            TrackError err1 = new TrackError();
            
            err1.set(IPHI,IPHI     ,  epp[i]);
            err1.set(IPHI,IZ_CYL   ,  epz[i]);
            err1.set(IZ_CYL,IZ_CYL ,  ezz[i]);
            err1.set(IPHI,IALF     ,  epa[i]);
            err1.set(IZ_CYL,IALF   ,  eza[i]);
            err1.set(IALF,IALF     ,  eaa[i]);
            err1.set(IPHI,ITLM     ,  epl[i]);
            err1.set(IZ_CYL,ITLM   ,  ezl[i]);
            err1.set(IALF,ITLM     ,  eal[i]);
            err1.set(ITLM,ITLM     ,  ell[i]);
            err1.set(IPHI,IQPT     ,  epc[i]);
            err1.set(IZ_CYL,IQPT   ,  ezc[i]);
            err1.set(IALF,IQPT     ,  eac[i]);
            err1.set(ITLM,IQPT     ,  elc[i]);
            err1.set(IQPT,IQPT     ,  ecc[i]);
            
            ETrack trv1 = new ETrack(scy1.newPureSurface(),vec1,err1);
            
            if(debug) System.out.println( "\n starting: " + trv1 );
            
            ETrack trv2f = new ETrack(trv1);
            pstat = propcylxy.errDirProp(trv2f,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "\n  forward: " + trv2f );
            
            ETrack trv2f_my = new ETrack(trv1);
            TrackDerivative deriv = new TrackDerivative();
            pstat = tst.vec_propcylxy(BFIELD,trv2f_my,sxyp2,PropDir.FORWARD,deriv);
            Assert.assertTrue( pstat.forward() );
            TrackError err = trv2f_my.error();
            trv2f_my.setError( err.Xform(deriv) );
            if(debug) System.out.println( "\n  forward my: " + trv2f_my );
            double[] diffs = tst.compare(trv2f_my,trv2f);
            
            if(debug) System.out.println( "\n diff: " + diffs[0] + ' ' + "ediff: "+ diffs[1] );
            Assert.assertTrue( diffs[0] < maxdiff );
            Assert.assertTrue( diffs[1] < maxdiff );
            
            
            ETrack trv2b = new ETrack(trv1);
            pstat = propcylxy.errDirProp(trv2b,sxyp2b,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( "\n  backward: " + trv2b );
            
            ETrack trv2b_my = new ETrack(trv1);
            pstat = tst.vec_propcylxy(BFIELD,trv2b_my,sxyp2b,PropDir.BACKWARD,deriv);
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
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test Zero Field Propagation." );
        {
            PropCylXY prop0 = new PropCylXY(0.0);
            if(debug) System.out.println( prop0 );
            Assert.assertTrue( prop0.bField() == 0. );
            
            double u = 10.;
            double phi2 = 0.1;
            Surface srf = new SurfCylinder(13.0);
            VTrack trv0 = new VTrack(srf);
            TrackVector vec = new TrackVector();
            vec.set(SurfCylinder.IPHI, phi2+0.1);
            vec.set(SurfCylinder.IZ, 10.);
            vec.set(SurfCylinder.IALF, 0.1);
            vec.set(SurfCylinder.ITLM, 2.);
            vec.set(SurfCylinder.IQPT, 0.);
            trv0.setVector(vec);
            trv0.setForward();
            Surface srf_to = new SurfXYPlane(u,phi2);
            
            VTrack trv = new VTrack(trv0);
            VTrack trv_der = new VTrack(trv);
            PropStat pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
            if(debug) System.out.println("trv= \n"+trv+'\n');
            Assert.assertTrue( pstat.success() );
            
            Assert.assertTrue( pstat.backward() );
            Assert.assertTrue(trv.surface().pureEqual(srf_to));
            
            check_zero_propagation(trv0,trv,pstat);
            check_derivatives(prop0,trv_der,srf_to);
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
    // Very well tested Cyl-XY propagator. Each new one can be tested against it
    PropStat
            vec_propcylxy( double B, VTrack trv, Surface srf,
            PropDir dir
            )
    {
        TrackDerivative deriv = null;
        return vec_propcylxy(B, trv, srf, dir, deriv);
    }
    
    
    PropStat
            vec_propcylxy( double B, VTrack trv, Surface srf,
            PropDir dir,
            TrackDerivative deriv  )
    {
        
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating surface and vector
        Surface  srf1 =  trv.surface();
        // TrackVector vec1 = trv.vector();
        
        // Check origin is a Cylinder.
        Assert.assertTrue( srf1.pureType().equals(SurfCylinder.staticType()) );
        if (! srf1.pureType( ).equals(SurfCylinder.staticType()) )
            return pstat;
        SurfCylinder  scy1 = ( SurfCylinder ) srf1;
        
        // Fetch the R of the cylinder and the starting track vector.
        int ir  = SurfCylinder.RADIUS;
        double Rcyl = scy1.parameter(ir);
        
        TrackVector vec = trv.vector();
        double c1 = vec.get(IPHI);                 // phi
        double c2 = vec.get(IZ);                   // z
        double c3 = vec.get(IALF);                 // alpha
        double c4 = vec.get(ITLM);                 // tan(lambda)
        double c5 = vec.get(IQPT);                 // q/pt
        
        
        // Check destination is a XYPlane.
        Assert.assertTrue( srf.pureType().equals(SurfXYPlane.staticType()) );
        if ( !srf.pureType( ).equals(SurfXYPlane.staticType()) )
            return pstat;
        SurfXYPlane  sxyp2 = (  SurfXYPlane ) srf;
        
        // Fetch the u and phi of the plane.
        int iphi  = SurfXYPlane.NORMPHI;
        int idist = SurfXYPlane.DISTNORM;
        
        double phi_n = sxyp2.parameter(iphi);
        double   u_n = sxyp2.parameter(idist);
        
        // rotate coordinate system on phi_n
        
        c1 -= phi_n;
        if(c1 < 0.) c1 += TRFMath.TWOPI;
        
        double cos_c1 = Math.cos(c1);
        
        double u = Rcyl*cos_c1;
        
        double sin_c1  = Math.sin(c1);
        double cos_dir = Math.cos(c1+c3);
        double sin_dir = Math.sin(c1+c3);
        double c4_hat2 = 1+c4*c4;
        double c4_hat  = Math.sqrt(c4_hat2);
        
        // check if du == 0 ( that is track moves parallel to the destination plane )
        // du = pt*cos_dir
        if(cos_dir/c5 == 0.) return pstat;
        
        double tan_dir = sin_dir/cos_dir;
        
        double b1 = Rcyl*sin_c1;
        double b2 = c2;
        double b3 = tan_dir;
        double b4 = c4/cos_dir;
        double b5 = c5/c4_hat;
        
        int sign_du = 0;
        if(cos_dir > 0) sign_du =  1;
        if(cos_dir < 0) sign_du = -1;
        
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
        
        // cyl-xy propagation failed : noway to the new plane
        if(Math.abs(norm)>1.) return pstat;
        
        double cat = Math.sqrt(1.-norm*norm);
        int sign_dphi = 0;
        
        if (dir.equals(PropDir.NEAREST))
        {
            if(debug) System.out.println("PropCylXY._vec_prop: Propagation in NEAREST direction is undefined");
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
            if(debug) System.out.println("PropCylXY._vec_prop: Unknown direction." );
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
        if (deriv == null ) return pstat;
        
        // du_dc
        
        double du_dc1 = -Rcyl*sin_c1;
        
        // db1_dc
        
        double db1_dc1 = Rcyl*cos_c1;
        
        // db2_dc
        
        double db2_dc2 = 1.;
        
        // db3_dc
        
        double db3_dc1 = 1./(cos_dir*cos_dir);
        double db3_dc3 = 1./(cos_dir*cos_dir);
        
        // db4_dc
        
        double db4_dc1 = b4*tan_dir;
        double db4_dc3 = b4*tan_dir;
        double db4_dc4 = 1/cos_dir;
        
        // db5_dc
        
        double db5_dc4 = -c4*c5/(c4_hat*c4_hat2);
        double db5_dc5 = 1./c4_hat;
        
        
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
        
        double ddphi_db3;
        double ddphi_db4;
        double ddphi_db5;
        double ddphi_du;
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
        
        double db2_n_db2 = 1.;
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
        
        // db1_n_dc
        
        double db1_n_dc1 = db1_n_du * du_dc1 + db1_n_db1*db1_dc1 +
                db1_n_db3*db3_dc1 + db1_n_db4*db4_dc1;
        double db1_n_dc2 = 0.;
        double db1_n_dc3 = db1_n_db3*db3_dc3 + db1_n_db4*db4_dc3;
        double db1_n_dc4 = db1_n_db4*db4_dc4 + db1_n_db5*db5_dc4;
        double db1_n_dc5 = db1_n_db5*db5_dc5;
        
        
        // db2_n_dc
        
        double db2_n_dc1 = db2_n_du * du_dc1 + db2_n_db3*db3_dc1 + db2_n_db4*db4_dc1;
        double db2_n_dc2 = db2_n_db2 * db2_dc2;
        double db2_n_dc3 = db2_n_db3*db3_dc3 + db2_n_db4*db4_dc3;
        double db2_n_dc4 = db2_n_db4*db4_dc4 + db2_n_db5*db5_dc4;
        double db2_n_dc5 = db2_n_db5*db5_dc5;
        
        // db3_n_dc
        
        double db3_n_dc1 = db3_n_du * du_dc1 + db3_n_db3*db3_dc1 + db3_n_db4*db4_dc1;
        double db3_n_dc2 = 0.;
        double db3_n_dc3 = db3_n_db3*db3_dc3 + db3_n_db4*db4_dc3;
        double db3_n_dc4 = db3_n_db4*db4_dc4 + db3_n_db5*db5_dc4;
        double db3_n_dc5 = db3_n_db5*db5_dc5;
        
        // db4_n_dc
        
        double db4_n_dc1 = db4_n_du * du_dc1 + db4_n_db3*db3_dc1 + db4_n_db4*db4_dc1;
        double db4_n_dc2 = 0.;
        double db4_n_dc3 = db4_n_db3*db3_dc3 + db4_n_db4*db4_dc3;
        double db4_n_dc4 = db4_n_db4*db4_dc4 + db4_n_db5*db5_dc4;
        double db4_n_dc5 = db4_n_db5*db5_dc5;
        
        // db5_n_dc
        
        double db5_n_dc1 = 0.;
        double db5_n_dc2 = 0.;
        double db5_n_dc3 = 0.;
        double db5_n_dc4 = db5_dc4;
        double db5_n_dc5 = db5_dc5;
        
        deriv.set(IV,IPHI       , db1_n_dc1);
        deriv.set(IV,IZ_CYL     , db1_n_dc2);
        deriv.set(IV,IALF       , db1_n_dc3);
        deriv.set(IV,ITLM       , db1_n_dc4);
        deriv.set(IV,IQPT       , db1_n_dc5);
        deriv.set(IZ,IPHI       , db2_n_dc1);
        deriv.set(IZ,IZ_CYL     , db2_n_dc2);
        deriv.set(IZ,IALF       , db2_n_dc3);
        deriv.set(IZ,ITLM       , db2_n_dc4);
        deriv.set(IZ,IQPT       , db2_n_dc5);
        deriv.set(IDVDU,IPHI    , db3_n_dc1);
        deriv.set(IDVDU,IZ_CYL  , db3_n_dc2);
        deriv.set(IDVDU,IALF    , db3_n_dc3);
        deriv.set(IDVDU,ITLM    , db3_n_dc4);
        deriv.set(IDVDU,IQPT    , db3_n_dc5);
        deriv.set(IDZDU,IPHI    , db4_n_dc1);
        deriv.set(IDZDU,IZ_CYL  , db4_n_dc2);
        deriv.set(IDZDU,IALF    , db4_n_dc3);
        deriv.set(IDZDU,ITLM    , db4_n_dc4);
        deriv.set(IDZDU,IQPT    , db4_n_dc5);
        deriv.set(IQP_XY,IPHI   , db5_n_dc1);
        deriv.set(IQP_XY,IZ_CYL , db5_n_dc2);
        deriv.set(IQP_XY,IALF   , db5_n_dc3);
        deriv.set(IQP_XY,ITLM   , db5_n_dc4);
        deriv.set(IQP_XY,IQPT   , db5_n_dc5);
        
        return pstat;
    }
    
    static void  check_zero_propagation(  VTrack trv0,  VTrack  trv,  PropStat  pstat)
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
    
    static void check_derivatives(  Propagator  prop,  VTrack  trv0,  Surface  srf)
    {
        for(int i=0;i<4;++i)
            for(int j=0;j<4;++j)
                check_derivative(prop,trv0,srf,i,j);
    }
    
    static void check_derivative(  Propagator  prop,  VTrack  trv0,  Surface  srf,int i,int j)
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
        tmp.set(j,tmp.get(j)-dx);
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
