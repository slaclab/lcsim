/*
 * PropZCyl_Test.java
 *
 * Created on July 24, 2007, 10:42 PM
 *
 * $Id: PropZCyl_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
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
public class PropZCyl_Test extends TestCase
{
    private boolean debug;
    
    // Assign track parameter indices.
    private static final int   IX = SurfZPlane.IX;
    private static final int   IY   = SurfZPlane.IY;
    private static final int   IDXDZ = SurfZPlane.IDXDZ;
    private static final int   IDYDZ = SurfZPlane.IDYDZ;
    private static final int   IQP_Z  = SurfZPlane.IQP;
    
    private static final int   IPHI = SurfCylinder.IPHI;
    private static final int   IZ   = SurfCylinder.IZ;
    private static final int   IALF = SurfCylinder.IALF;
    private static final int   ITLM = SurfCylinder.ITLM;
    private static final int   IQPT  = SurfCylinder.IQPT;
    
    
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
    /** Creates a new instance of PropZCyl_Test */
    public void testPropZCyl()
    {
        String ok_prefix = "PropZCyl (I): ";
        String error_prefix = "PropZCyl test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropZCyl. --------" );
        {
            if(debug) System.out.println( ok_prefix + "Test constructor." );
            double BFIELD = 2.0;
            PropZCyl prop = new PropZCyl(BFIELD);
            if(debug) System.out.println( prop );
            PropZCyl_Test tst = new PropZCyl_Test();
            //********************************************************************
            
            // Here we propagate some tracks both forward and backward and then
            // the same track forward and backward but using method
            // that we checked very thoroughly before.
            
            if(debug) System.out.println( ok_prefix + "Check against correct propagation." );
            
            PropZCyl propzcyl = new PropZCyl(BFIELD/TRFMath.BFAC);
            
            double z1[]  ={10.,      10.,     10.,     10.,     10.,     10.     };
            
            int sign_dz[]   ={ -1,        1,      -1,       1,       1,      -1     };
            double x[]      ={ -2.,       2.,      5.,      5.,     -2.,     -2.    };
            double y[]      ={  3.,       3.,      3.,      3.,      3.,      3.    };
            double dxdz[]   ={ 1.5,     -2.3,     0.,      1.5,    -1.5,      1.5   };
            double dydz[]   ={ 2.3,     -1.5,    -2.3,     0.,     -2.3,      0.    };
            double qp[]     ={ 0.01,    -0.01,    0.01,   -0.01,   -0.01,     0.01  };
            
            double rcyl2[]  ={ 20.,      20.,     20.,     20.,     20.,     20.    };
            double rcyl2b[] ={ 20.,      20.,     20.,     20.,     20.,     20.    };
            
            double maxdiff = 1.e-10;
            double diff;
            int ntrk = 6;
            int i;
            
            for ( i=0; i<ntrk; ++i )
            {
                if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
                
                PropStat pstat = new PropStat();
                
                SurfZPlane   szp1 = new SurfZPlane(z1[i]);
                SurfCylinder scy2 = new SurfCylinder(rcyl2[i]);
                SurfCylinder scy2b = new SurfCylinder(rcyl2b[i]);
                
                TrackVector vec1 = new TrackVector();
                
                vec1.set(IX    , x[i]);     // x
                vec1.set(IY    , y[i]);     // y
                vec1.set(IDXDZ , dxdz[i]);  // dx/dz
                vec1.set(IDYDZ , dydz[i]);  // dy/dz
                vec1.set(IQP_Z , qp[i]);    //  q/p
                
                VTrack trv1 = new VTrack(szp1.newPureSurface(),vec1);
                if (sign_dz[i]==1) trv1.setForward();
                else trv1.setBackward();
                
                if(debug) System.out.println( "\n starting: " + trv1 );
                
                VTrack trv2f = new VTrack(trv1);
                pstat = propzcyl.vecDirProp(trv2f,scy2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "\n  forward: " + trv2f );
                
                VTrack trv2f_my = new VTrack(trv1);
                pstat = tst.vec_propzcyl(BFIELD,trv2f_my,scy2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "\n  forward my: " + trv2f_my );
                diff = tst.compare(trv2f_my,trv2f);
                
                if(debug) System.out.println( "\n diff: " + diff );
                Assert.assertTrue( diff < maxdiff );
                
                VTrack trv2b = new VTrack(trv1);
                pstat = propzcyl.vecDirProp(trv2b,scy2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( "\n  backward: " + trv2b );
                
                VTrack trv2b_my = new VTrack(trv1);
                pstat = tst.vec_propzcyl(BFIELD,trv2b_my,scy2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( "\n  backward my: " + trv2b_my );
                diff = tst.compare(trv2b_my,trv2b);
                
                if(debug) System.out.println( "\n diff: " + diff );
                Assert.assertTrue( diff < maxdiff );
                
            }
            //********************************************************************
            
            // Repeat the above with errors.
            if(debug) System.out.println( ok_prefix + "Check against correct propagation with errors." );
            
            double exx[] =   {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
            double exy[] =   {  0.01,  -0.01,   0.01,  -0.01,   0.01,  -0.01  };
            double eyy[] =   {  0.25,   0.25,   0.25,   0.25,   0.25,   0.25  };
            double exdx[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
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
            
            maxdiff = 1.e-8;
            double ediff;
            ntrk = 6;
            
            for ( i=0; i<ntrk; ++i )
            {
                if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
                
                PropStat pstat = new PropStat();
                
                SurfZPlane   szp1 = new SurfZPlane(z1[i]);
                SurfCylinder scy2 = new SurfCylinder(rcyl2[i]);
                SurfCylinder scy2b = new SurfCylinder(rcyl2b[i]);
                
                TrackVector vec1 = new TrackVector();
                
                vec1.set(IX    , x[i]);     // x
                vec1.set(IY    , y[i]);     // y
                vec1.set(IDXDZ , dxdz[i]);  // dx/dz
                vec1.set(IDYDZ , dydz[i]);  // dy/dz
                vec1.set(IQP_Z , qp[i]);    //  q/p
                
                TrackError err1 = new TrackError();
                
                err1.set(IX,IX       , exx[i]);
                err1.set(IX,IY       , exy[i]);
                err1.set(IY,IY       , eyy[i]);
                err1.set(IX,IDXDZ    ,  exdx[i]);
                err1.set(IY,IDXDZ    ,  eydx[i]);
                err1.set(IDXDZ,IDXDZ ,  edxdx[i]);
                err1.set(IX,IDYDZ    ,  exdy[i]);
                err1.set(IY,IDYDZ    ,  eydy[i]);
                err1.set(IDXDZ,IDYDZ ,  edxdy[i]);
                err1.set(IDYDZ,IDYDZ ,  edydy[i]);
                err1.set(IX,IQP_Z    ,  exqp[i]);
                err1.set(IY,IQP_Z    ,  eyqp[i]);
                err1.set(IDXDZ,IQP_Z ,  edxqp[i]);
                err1.set(IDYDZ,IQP_Z ,  edyqp[i]);
                err1.set(IQP_Z,IQP_Z ,  eqpqp[i]);
                
                ETrack trv1 = new ETrack(szp1.newPureSurface(),vec1,err1);
                if (sign_dz[i]==1)trv1.setForward();
                else trv1.setBackward();
                
                if(debug) System.out.println( "\n starting: " + trv1 );
                
                ETrack trv2f = new ETrack(trv1);
                pstat = propzcyl.errDirProp(trv2f,scy2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "\n  forward: " + trv2f );
                
                ETrack trv2f_my = new ETrack(trv1);
                TrackDerivative deriv = new TrackDerivative();
                pstat = tst.vec_propzcyl(BFIELD,trv2f_my,scy2,PropDir.FORWARD,deriv);
                Assert.assertTrue( pstat.forward() );
                TrackError err = trv2f_my.error();
                trv2f_my.setError( err.Xform(deriv) );
                if(debug) System.out.println( "\n  forward my: " + trv2f_my );
                double[] diffs = tst.compare(trv2f_my,trv2f);
                
                if(debug) System.out.println( "\n diff: " + diffs[0] + ' ' + "ediff: "+ diffs[1] );
                Assert.assertTrue( diffs[0] < maxdiff );
                Assert.assertTrue( diffs[1] < maxdiff );
                
                
                ETrack trv2b = new ETrack(trv1);
                pstat = propzcyl.errDirProp(trv2b,scy2b,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( "\n  backward: " + trv2b );
                
                
                ETrack trv2b_my = new ETrack(trv1);
                pstat = tst.vec_propzcyl(BFIELD,trv2b_my,scy2b,PropDir.BACKWARD,deriv);
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
                    PropZCyl prop0 = new PropZCyl(0.0);
                    if(debug) System.out.println( prop0 );
                    Assert.assertTrue( prop0.bField() == 0. );
                    
                    double z=10.;
                    Surface srf = new SurfZPlane(z);
                    VTrack trv0 = new VTrack(srf);
                    TrackVector vec = new TrackVector();
                    vec.set(SurfZPlane.IX, 2.);
                    vec.set(SurfZPlane.IY, 10.);
                    vec.set(SurfZPlane.IDXDZ, 4.);
                    vec.set(SurfZPlane.IDYDZ, 2.);
                    trv0.setVector(vec);
                    trv0.setForward();
                    Surface srf_to = new SurfCylinder(13.0);
                    
                    VTrack trv = new VTrack(trv0);
                    VTrack trv_der = new VTrack(trv);
                    PropStat pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    Assert.assertTrue( pstat.success() );
                    
                    Assert.assertTrue( pstat.forward() );
                    Assert.assertTrue(trv.surface().pureEqual(srf_to));
                    
                    check_zero_propagation(trv0,trv,pstat);
                    check_derivatives(prop0,trv_der,srf_to);
                    if(debug) System.out.println("\n**trv= \n"+trv+"\n trv_der= \n"+trv_der);
                    
                    srf_to = new SurfCylinder(trv0.spacePoint().rxy());
                    if(debug) System.out.println("\n**trv0.spacePoint()= "+trv0.spacePoint() );
                    if(debug) System.out.println("\n**trv0.spacePoint().rxy()= "+trv0.spacePoint().rxy());
                    
                    trv =  new VTrack(trv0);
                    trv_der = new VTrack(trv);
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    Assert.assertTrue( pstat.success() );
                    
                    Assert.assertTrue( pstat.same() );
                    Assert.assertTrue(trv.surface().pureEqual(srf_to));
                    if(debug) System.out.println("\n**trv= \n"+trv+"\n trv_der= \n"+trv_der);
                    
                    check_zero_propagation(trv0,trv,pstat);
                    check_derivatives(prop0,trv_der,srf_to);
                    
                    trv0.setBackward();
                    trv = new VTrack(trv0);
                    trv_der = new VTrack(trv);
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
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
    // Very well tested Z-Cyl propagator. Each new one can be tested against it
    
    //**********************************************************************
    // helpers
    //**********************************************************************
    
    //**********************************************************************
    // The track parameters for a cylinder are:
    // phi z alpha tan(lambda) curvature
    // (NOT [. . . lambda .] as in TRF and earlier version of TRF++.)
    //
    // If pderiv is nonzero, return the derivative matrix there.
    // On Cylinder:
    // r (cm) is fixed
    // 0 - phi
    // 1 - z (cm)
    // 2 - alp
    // 3 - tlam
    // 4 - q/pt   pt is transverse momentum of a track, q is its charge
    // On ZPlane:
    // 0 - x (cm)
    // 1 - y (cm)
    // 2 - dx/dz
    // 3 - dy/dz
    // 4 - q/p   p is momentum of a track, q is its charge
    // If pderiv is nonzero, return the derivative matrix there.
    
    PropStat
            vec_propzcyl( double B, VTrack trv, Surface srf,
            PropDir dir)
    {
        TrackDerivative deriv = null;
        return vec_propzcyl(B, trv, srf, dir, deriv);
    }
    
    
    
    PropStat
            vec_propzcyl( double B, VTrack trv, Surface srf,
            PropDir dir,
            TrackDerivative deriv )
    {
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        //TrackVector vec1 = trv.vector();
        
        // Check origin is a Zplane.
        Assert.assertTrue( srf1.pureType().equals(SurfZPlane.staticType()) );
        if ( !srf1.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        SurfZPlane szp1 = ( SurfZPlane) srf1;
        
        // Check destination is a cylinder.
        Assert.assertTrue( srf.pureType().equals(SurfCylinder.staticType()) );
        if ( ! srf.pureType( ).equals(SurfCylinder.staticType()) )
            return pstat;
        SurfCylinder scy2 = ( SurfCylinder) srf;
        
        
        // Fetch the z of the plane and the starting track vector.
        int iz  = SurfZPlane.ZPOS;
        double z = szp1.parameter(iz);
        
        TrackVector vec = trv.vector();
        double x = vec.get(IX);                  // v
        double y = vec.get(IY);                  // z
        double b = vec.get(IDXDZ);               // dv/du
        double a = vec.get(IDYDZ);               // dz/du
        double e = vec.get(IQP_Z);              // q/p
        
        // Fetch the radii and the starting track vector.
        int irad = SurfCylinder.RADIUS;
        double r2 = scy2.parameter(irad);
        
        int sign_dz = 0;
        if(trv.isForward())  sign_dz =  1;
        if(trv.isBackward()) sign_dz = -1;
        if(sign_dz == 0)
        {
            if(debug) System.out.println("PropZCyl._vec_propagate: Unknown direction of a track ");
            System.exit(1);
        }
        
        // Calculate cylindrical coordinates
        
        double cnst1=x>0?0.:Math.PI;
        double cnst2=Math.PI;
        if(a>0.&&b>0.&&sign_dz>0.) cnst2=0.;
        if(a<0.&&b>0.&&sign_dz>0.) cnst2=0.;
        if(a>0.&&b<0.&&sign_dz<0.) cnst2=0.;
        if(a<0.&&b<0.&&sign_dz<0.) cnst2=0.;
        
        double sign_y= y>0 ? 1.:-1;
        double sign_a= a>0 ? 1.:-1;
        if(y==0) sign_y=0.;
        if(a==0) sign_a=0.;
        double atnxy=(x!=0.?Math.atan(y/x):sign_y*Math.PI/2.);
        double atnab=(b!=0.?Math.atan(a/b):sign_a*Math.PI/2.);
        
        double phi1= TRFMath.fmod2(atnxy+cnst1,TRFMath.TWOPI);
        double r1=Math.sqrt(x*x+y*y);
        double z1=z;
        double alp1= TRFMath.fmod2(atnab-phi1+cnst2,TRFMath.TWOPI);
        double tlm1= sign_dz/Math.sqrt(a*a+b*b);
        double qpt1=e*Math.sqrt((1+a*a+b*b)/(a*a+b*b));
        
        
        
        // Check alpha range.
        alp1 = TRFMath.fmod2( alp1, TRFMath.TWOPI );
        Assert.assertTrue( Math.abs(alp1) <= Math.PI );
        
        //if ( trv.is_forward() ) Assert.assertTrue( Math.abs(alp1) <= TRFMath.PI2 );
        //else Assert.assertTrue( Math.abs(alp1) > TRFMath.PI2 );
        
        // Calculate the cosine of lambda.
        //double clam1 = 1.0/Math.sqrt(1+tlm1*tlm1);
        
        // Calculate curvature: C = _bfac*(q/p)/cos(lambda)
        // and its derivatives
        // Assert.assertTrue( clam1 != 0.0 );
        // double dcrv1_dqp1 = B/clam1;
        // double crv1 = dcrv1_dqp1*qp1;
        // double dcrv1_dtlm1 = crv1*clam1*clam1*tlm1;
        
        // Calculate the curvature = _bfac*(q/pt)
        double dcrv1_dqpt1 = B;
        double crv1 = dcrv1_dqpt1*qpt1;
        //double dcrv1_dtlm1 = 0.0;
        
        // Evaluate the new track vector.
        // See dla log I-044
        
        // lambda and curvature do not change
        double tlm2 = tlm1;
        double crv2 = crv1;
        double qpt2 = qpt1;
        
        // We can evaluate sin(alp2), leaving two possibilities for alp2
        // 1st solution: alp21, phi21, phid21, tht21
        // 2nd solution: alp22, phi22, phid22, tht22
        // evaluate phi2 to choose
        double salp1 = Math.sin( alp1 );
        double calp1 = Math.cos( alp1 );
        double salp2 = r1/r2*salp1 + 0.5*crv1/r2*(r2*r2-r1*r1);
        // if salp2 > 1, track does not cross cylinder
        if ( Math.abs(salp2) > 1.0 ) return pstat;
        double alp21 = Math.asin( salp2 );
        double alp22 = alp21>0 ? Math.PI-alp21 : -Math.PI-alp21;
        double calp21 = Math.cos( alp21 );
        double calp22 = Math.cos( alp22 );
        double phi20 = phi1 + Math.atan2( salp1-r1*crv1, calp1 );
        double phi21 = phi20 - Math.atan2( salp2-r2*crv2, calp21 );   // phi position
        double phi22 = phi20 - Math.atan2( salp2-r2*crv2, calp22 );
        // Construct an sT object for each solution.
        STCalcZ_CHECK sto1 = new STCalcZ_CHECK(r1,phi1,alp1,crv1,r2,phi21,alp21);
        STCalcZ_CHECK sto2 = new STCalcZ_CHECK(r1,phi1,alp1,crv1,r2,phi22,alp22);
        // Check the two solutions are nonzero and have opposite sign
        // or at least one is nonzero.
        
        // Choose the correct solution
        boolean use_first_solution = false;
        
        if (dir.equals(PropDir.NEAREST))
        {
            use_first_solution = Math.abs(sto2.st()) > Math.abs(sto1.st());
        }
        else if (dir.equals(PropDir.FORWARD))
        {
            use_first_solution = sto1.st() > 0.0;
        }
        else if (dir.equals(PropDir.BACKWARD))
        {
            use_first_solution = sto1.st() < 0.0;
        }
        else
        {
            if(debug) System.out.println("PropCyl._vec_propagate: Unknown direction." );
            System.exit(1);
        }
        
        // Assign phi2, alp2 and sto2 for the chosen solution.
        double phi2, alp2;
        STCalcZ_CHECK sto;
        double calp2;
        if ( use_first_solution )
        {
            sto = sto1;
            phi2 = phi21;
            alp2 = alp21;
            calp2 = calp21;
        }
        else
        {
            sto = sto2;
            phi2 = phi22;
            alp2 = alp22;
            calp2 = calp22;
        }
        
        // fetch sT.
        double st = sto.st();
        
        // use sT to evaluate z2
        double z2 = z1 + tlm1*st;
        
        // Check alpha range.
        Assert.assertTrue( Math.abs(alp2) <= Math.PI );
        
        // put new values in vec
        vec.set(IPHI , phi2);
        vec.set(IZ   , z2);
        vec.set(IALF , alp2);
        vec.set(ITLM , tlm2);
        vec.set(IQPT , qpt2);
        
        // Update trv
        trv.setSurface(srf.newPureSurface());
        trv.setVector(vec);
        if ( Math.abs(alp2) <= TRFMath.PI2 ) trv.setForward();
        else trv.setBackward();
        
        // Set the return status.
        if (st > 0)  pstat.setForward() ;
        else pstat.setBackward();
        
        // exit now if user did not ask for error matrix.
        if ( deriv == null) return pstat;
        
        // Calculate derivatives.
        // dphi1
        
        double dphi1_dx= -y/(x*x+y*y);
        double dphi1_dy= x/(x*x+y*y);
        
        // dz1
        
        //double dz1_dz=0.;
        
        // dalf1
        
        double dalp1_da= b/(a*a+b*b);
        double dalp1_db= -a/(a*a+b*b);
        double dalp1_dy= -dphi1_dy;
        double dalp1_dx= -dphi1_dx;
        
        // dr1
        
        double dr1_dx= x/Math.sqrt(x*x+y*y);
        double dr1_dy= y/Math.sqrt(x*x+y*y);
        
        // dtlm1
        
        double dtlm1_da= -sign_dz*a/Math.sqrt(a*a+b*b)/(a*a+b*b);
        double dtlm1_db= -sign_dz*b/(a*a+b*b)/Math.sqrt(a*a+b*b);
        
        // dcrv1
        
        double dcrv1_de= Math.sqrt((1+a*a+b*b)/(a*a+b*b))*B;
        double dcrv1_da= -a*e/Math.sqrt((a*a+b*b)*(1+a*a+b*b))/(a*a+b*b)*B;
        double dcrv1_db= -e*b/Math.sqrt(1+a*a+b*b)/Math.sqrt(a*a+b*b)/(a*a+b*b)*B;
        
        // alpha_2
        double da2da1 = r1*calp1/r2/calp2;
        double da2dc1 = (r2*r2-r1*r1)*0.5/r2/calp2;
        double da2dr1 = (salp1-crv2*r1)/r2/calp2;
        
        // phi2
        double rcsal1 = r1*crv1*salp1;
        double rcsal2 = r2*crv2*salp2;
        double den1 = 1.0 + r1*r1*crv1*crv1 - 2.0*rcsal1;
        double den2 = 1.0 + r2*r2*crv2*crv2 - 2.0*rcsal2;
        double dp2dp1 = 1.0;
        double dp2da1 = (1.0-rcsal1)/den1 - (1.0-rcsal2)/den2*da2da1;
        double dp2dc1 = -r1*calp1/den1 + r2*calp2/den2
                - (1.0-rcsal2)/den2*da2dc1;
        double dp2dr1= -crv1*calp1/den1-(1.0-rcsal2)*da2dr1/den2;
        
        // z2
        //double dz2dz1 = 1.0;
        double dz2dl1 = st;
        double dz2da1 = tlm1*sto.d_st_dalp1(dp2da1, da2da1);
        double dz2dc1 = tlm1*sto.d_st_dcrv1(dp2dc1, da2dc1);
        double dz2dr1 = tlm1*sto.d_st_dr1(  dp2dr1, da2dr1);
        
        
        // final derivatives
        
        // phi2
        double dphi2_dx=dp2dp1*dphi1_dx+dp2da1*dalp1_dx+dp2dr1*dr1_dx;
        double dphi2_dy=dp2dp1*dphi1_dy+dp2da1*dalp1_dy+dp2dr1*dr1_dy;
        double dphi2_db=dp2da1*dalp1_db+dp2dc1*dcrv1_db;
        double dphi2_da=dp2da1*dalp1_da+dp2dc1*dcrv1_da;
        double dphi2_de=dp2dc1*dcrv1_de;
        
        // alp2
        double dalp2_dx= da2da1*dalp1_dx+da2dr1*dr1_dx;
        double dalp2_dy= da2da1*dalp1_dy+da2dr1*dr1_dy;
        double dalp2_db= da2da1*dalp1_db+da2dc1*dcrv1_db;
        double dalp2_da= da2da1*dalp1_da+da2dc1*dcrv1_da;
        double dalp2_de= da2dc1*dcrv1_de;
        
        // crv2
        double dcrv2_da=dcrv1_da;
        double dcrv2_db=dcrv1_db;
        double dcrv2_de=dcrv1_de;
        
        // tlm2
        double dtlm2_da= dtlm1_da;
        double dtlm2_db= dtlm1_db;
        
        // z2
        double dz2_dx= dz2dr1*dr1_dx+dz2da1*dalp1_dx;
        double dz2_dy= dz2dr1*dr1_dy+dz2da1*dalp1_dy;
        double dz2_db= dz2da1*dalp1_db+dz2dl1*dtlm1_db+dz2dc1*dcrv1_db;
        double dz2_da= dz2da1*dalp1_da+dz2dl1*dtlm1_da+dz2dc1*dcrv1_da;
        double dz2_de= dz2dc1*dcrv1_de;
        
        
        // Build derivative matrix.
        
        deriv.set(IPHI,IX , dphi2_dx);
        deriv.set(IPHI,IY , dphi2_dy);
        deriv.set(IPHI,IDXDZ,  dphi2_db);
        deriv.set(IPHI,IDYDZ,  dphi2_da);
        deriv.set(IPHI,IQP_Z,  dphi2_de);
        deriv.set(IZ,IX   ,  dz2_dx);
        deriv.set(IZ,IY  ,  dz2_dy);
        deriv.set(IZ,IDXDZ , dz2_db);
        deriv.set(IZ,IDYDZ , dz2_da);
        deriv.set(IZ,IQP_Z, dz2_de);
        deriv.set(IALF,IX ,  dalp2_dx);
        deriv.set(IALF,IY ,  dalp2_dy);
        deriv.set(IALF,IDXDZ ,  dalp2_db);
        deriv.set(IALF,IDYDZ ,  dalp2_da);
        deriv.set(IALF,IQP_Z ,  dalp2_de);
        deriv.set(ITLM,IDXDZ ,  dtlm2_db);
        deriv.set(ITLM,IDYDZ ,  dtlm2_da);
        deriv.set(IQPT,IDXDZ ,  dcrv2_db/B);
        deriv.set(IQPT,IDYDZ ,  dcrv2_da/B);
        deriv.set(IQPT,IQP_Z ,  dcrv2_de/B);
        
        return pstat;
        
    }
    
    
    // Private class STCalcZ_CHECK.
    //
    // An STCalcZ_CHECK_ object calculates sT (the signed transverse path length)
    // and its derivatives w.r.t. alf1 and crv1.  It is constructed from
    // the starting (r1, phi1, alf1, crv1) and final track parameters
    // (r2, phi2, alf2) assuming these are consistent.  Methods are
    // provided to retrieve sT and the two derivatives.
    
    private class STCalcZ_CHECK
    {
        
        private boolean _big_crv;
        private double _st;
        private double _dst_dphi21;
        private double _dst_dcrv1;
        private double _dst_dr1;
        private double _cnst1,_cnst2;
        public double _crv1;
        
        // constructor
        public STCalcZ_CHECK()
        {
        }
        public STCalcZ_CHECK(double r1, double phi1, double alf1, double crv1,
                double r2, double phi2, double alf2)
        {
            _crv1 = crv1;
            Assert.assertTrue( r1 > 0.0 );
            Assert.assertTrue( r2 > 0.0 );
            double rmax = r1+r2;
            
            // Calculate the change in xy direction
            double phi_dir_diff = TRFMath.fmod2(phi2+alf2-phi1-alf1,TRFMath.TWOPI);
            Assert.assertTrue( Math.abs(phi_dir_diff) <= Math.PI );
            
            // Evaluate whether |C| is" big"
            _big_crv = rmax*Math.abs(crv1) > 0.001;
            
            // If the curvature is big we can use
            // sT = (phi_dir2 - phi_dir1)/crv1
            if ( _big_crv )
            {
                Assert.assertTrue( crv1 != 0.0 );
                _st = phi_dir_diff/crv1;
            }
            
            // Otherwise, we calculate the straight-line distance
            // between the points and use an approximate correction
            // for the (small) curvature.
            else
            {
                
                // evaluate the distance
                double d = Math.sqrt( r1*r1 + r2*r2 - 2.0*r1*r2*Math.cos(phi2-phi1) );
                double arg = 0.5*d*crv1;
                double arg2 = arg*arg;
                double st_minus_d = d*arg2*( 1.0/6.0 + 3.0/40.0*arg2 );
                _st = d + st_minus_d;
                
                // evaluate the sign
                // We define a metric xsign = abs( (dphid-d*C)/(d*C) ).
                // Because sT*C = dphid and d = abs(sT):
                // xsign = 0 for sT > 0
                // xsign = 2 for sT < 0
                // Numerical roundoff will smear these predictions.
                double xsign = Math.abs( (phi_dir_diff - _st*crv1) / (_st*crv1) );
                double sign = 0.0;
                if ( crv1 != 0 )
                {
                    if ( xsign < 0.5 ) sign = 1.0;
                    if ( xsign > 1.5  &&  xsign < 3.0 ) sign = -1.0;
                }
                // If the above is indeterminate, assume zero curvature.
                // In this case abs(alpha) decreases monotonically
                // with sT.  Track passing through origin has alpha = 0 on one
                // side and alpha = +/-pi on the other.  If both points are on
                // the same side, we use dr/ds > 0 for |alpha|<pi/2.
                if ( sign == 0)
                {
                    sign = 1.0;
                    if ( Math.abs(alf2) > Math.abs(alf1) ) sign = -1.0;
                    if ( Math.abs(alf2) == Math.abs(alf1) )
                    {
                        if ( Math.abs(alf2) < TRFMath.PI2 )
                        {
                            if ( r2 < r1 ) sign = -1.0;
                        }
                        else
                        {
                            if ( r2 > r1 ) sign = -1.0;
                        }
                    }
                }
                
                // Correct _st using the above sign.
                Assert.assertTrue( Math.abs(sign) == 1.0 );
                _st = sign*_st;
                
                // save derivatives
                _dst_dcrv1 = sign*d*d*arg*( 1.0/6.0 + 3.0/20.0*arg2);
                double root = (1.0 + 0.5*arg*arg + 3.0/8.0*arg*arg*arg*arg );
                _dst_dphi21 = sign*(r1*r2*Math.sin(phi2-phi1))*root/d;
                _dst_dr1= (1.+arg2/2.*(1+3./4.*arg2))/d*sign;
                _cnst1=r1-r2*Math.cos(phi2-phi1);
                _cnst2=r1*r2*Math.sin(phi2-phi1);
            }
            
        }
        public double st()
        { return _st; };
        public double d_st_dalp1(double d_phi2_dalf1, double d_alf2_dalf1 )
        {
            if ( _big_crv ) return ( d_phi2_dalf1 + d_alf2_dalf1 - 1.0 ) / _crv1;
            else return _dst_dphi21 * d_phi2_dalf1;
        }
        public double d_st_dcrv1(double d_phi2_dcrv1, double d_alf2_dcrv1 )
        {
            if ( _big_crv ) return ( d_phi2_dcrv1 + d_alf2_dcrv1 - _st ) / _crv1;
            else return _dst_dcrv1 + _dst_dphi21*d_phi2_dcrv1;
            
        }
        public double d_st_dr1(  double d_phi2_dr1,   double d_alf2_dr1   )
        {
            if ( _big_crv ) return ( d_phi2_dr1 + d_alf2_dr1 ) / _crv1;
            else return _dst_dr1*(_cnst1+_cnst2*d_phi2_dr1);
            
        }
        
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
        {
            if( Math.abs((dydx - didj)/didj) >= 1e-4 )
                 System.out.println("i="+ i +" j="+ j + " "+dydx + " "+ didj+'\n');
            Assert.assertTrue( Math.abs((dydx - didj)/didj) < 1e-4 );
        }
        else
        {
            if( Math.abs((dydx - didj)/didj) >= 1e-4 )
                 System.out.println("i="+ i +" j="+ j + " "+dydx + " "+ didj+'\n');
            
            Assert.assertTrue( Math.abs(dydx) < 1e-4 ) ;
        }
    }
    
}
