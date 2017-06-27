/*
 * PropZZ_Test.java
 *
 * Created on July 24, 2007, 11:02 PM
 *
 * $Id: PropZZ_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

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

/**
 *
 * @author Norman Graf
 */
public class PropZZ_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropZZ_Test */
    public void testPropZZ()
    {
        String ok_prefix = "PropZZ (I): ";
        String error_prefix = "PropZZ test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropZZ. --------" );
        
        {
            if(debug) System.out.println( ok_prefix + "Test constructor." );
            PropZZ prop = new PropZZ(2.0);
            if(debug) System.out.println( prop );
            
            //********************************************************************
            
            // Here we propagate some tracks both forward and backward and then
            // each back to the original track.  We check that the returned
            // track parameters match those of the original track.
            if(debug) System.out.println( ok_prefix + "Check reversibility." );
            
            double z1[]  ={10.,      10.,     10.,     10.,     10.,     10.};
            double z2[]  ={20.,     -20.,     20.,     20.,    -20.,    -20.};
            double z3[]  ={30.,     -30.,     30.,     30.,    -30.,    -30.};
            int sign_dz[]={  1,       -1,      1,       1,       -1,      -1     };
            double x[]   ={ -2.,       2.,     40.,     40.,     -2.,     -2.    };
            double y[]   ={  3.,       3.,      3.,      3.,      3.,      3.    };
            double dxdz[]={-1.5,     -2.3,     0.,      1.5,     0.,       0.    };
            double dydz[]={ 2.3,     -1.5,    -2.3,     0.,      2.3,      0.   };
            double qp[]  ={ 0.05,    -0.05,    0.05,   -0.05,   -0.05,     0.05  };
            
            double maxdiff = 1.e-12;
            int ntrk = 6;
            int i;
            for ( i=0; i<ntrk; ++i )
            {
                if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
                PropStat pstat = new PropStat();
                SurfZPlane szp1 = new SurfZPlane(z1[i]);
                SurfZPlane szp2 = new SurfZPlane(z2[i]);
                SurfZPlane szp3 = new SurfZPlane(z3[i]);
                TrackVector vec1 = new TrackVector();
                vec1.set(SurfZPlane.IX   ,   x[i]);     // x
                vec1.set(SurfZPlane.IY   , y[i]);     // y
                vec1.set(SurfZPlane.IDXDZ, dxdz[i]);  // dx/dz
                vec1.set(SurfZPlane.IDYDZ,  dydz[i]);  // dy/dz
                vec1.set(SurfZPlane.IQP  ,  qp[i]);    //  q/p
                
                VTrack trv1 = new VTrack(szp2.newPureSurface(),vec1);
                if (sign_dz[i]==1)
                {trv1.setForward();
                }
                else
                {trv1.setBackward();
                }
                if(debug) System.out.println( " starting: " + trv1 );
                VTrack trv2f = new VTrack(trv1);
                pstat = prop.vecDirProp(trv2f,szp3,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "  forward: " + trv2f );
                VTrack trv2b = new VTrack(trv1);
                pstat = prop.vecDirProp(trv2b,szp1,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( " backward: " + trv2b );
                VTrack trv2fb = new VTrack(trv2f);
                pstat = prop.vecDirProp(trv2fb,szp2,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( " f return: " + trv2fb );
                VTrack trv2bf = new VTrack(trv2b);
                pstat = prop.vecDirProp(trv2bf,szp2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( " b return: " + trv2bf );
                double difff =
                        szp1.vecDiff(trv2fb.vector(),trv1.vector()).amax();
                double diffb =
                        szp1.vecDiff(trv2bf.vector(),trv1.vector()).amax();
                if(debug) System.out.println( "diffs: " + difff + ' ' + diffb );
                Assert.assertTrue( difff < maxdiff );
                Assert.assertTrue( diffb < maxdiff );
            }
            
            // Repeat the above with errors.
            if(debug) System.out.println( ok_prefix + "Check reversibility with errors." );
            double exx[] =   {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
            double exy[] =   {  0.01,  -0.01,   0.01,  -0.01,   0.01,  -0.01  };
            double eyy[] =   {  0.25,   0.25,   0.25,   0.25,   0.25,   0.25, };
            double exdx[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
            double eydx[] =  {  0.04,  -0.04,   0.04,  -0.04,   0.04,  -0.04, };
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
            
            for ( i=0; i<ntrk; ++i )
            {
                if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
                PropStat pstat = new PropStat();
                SurfZPlane szp1= new SurfZPlane((z1[i]));
                SurfZPlane szp2= new SurfZPlane((z2[i]));
                SurfZPlane szp3= new SurfZPlane((z3[i]));
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
                ETrack trv1 = new ETrack(szp2.newPureSurface(),vec1,err1);
                if (sign_dz[i]==1)
                {trv1.setForward();
                }
                else
                {trv1.setBackward();
                }
                if(debug) System.out.println( " starting: " + trv1 );
                
                ETrack trv2f = new ETrack(trv1);
                pstat = prop.errDirProp(trv2f,szp3,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( "  forward: " + trv2f );
                ETrack trv2b = new ETrack(trv1);
                pstat = prop.errDirProp(trv2b,szp1,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( " backward: " + trv2b );
                ETrack trv2fb = new ETrack(trv2f);
                pstat = prop.errDirProp(trv2fb,szp2,PropDir.BACKWARD);
                Assert.assertTrue( pstat.backward() );
                if(debug) System.out.println( " f return: " + trv2fb );
                ETrack trv2bf = new ETrack(trv2b);
                pstat = prop.errDirProp(trv2bf,szp2,PropDir.FORWARD);
                Assert.assertTrue( pstat.forward() );
                if(debug) System.out.println( " b return: " + trv2bf );
                double difff =
                        szp1.vecDiff(trv2fb.vector(),trv1.vector()).amax();
                double diffb =
                        szp1.vecDiff(trv2bf.vector(),trv1.vector()).amax();
                if(debug) System.out.println( "vec diffs: " + difff + ' ' + diffb );
                Assert.assertTrue( difff < maxdiff );
                Assert.assertTrue( diffb < maxdiff );
                
                TrackError dfb = trv2fb.error().minus(trv1.error());
                TrackError dbf = trv2bf.error().minus(trv1.error());
                double edifff = dfb.amax();
                double ediffb = dbf.amax();
                if(debug) System.out.println( "err diffs: " + edifff + ' ' + ediffb );
                Assert.assertTrue( edifff < maxdiff );
                Assert.assertTrue( ediffb < maxdiff );
                
            }
            
            //********************************************************************
            
            if(debug) System.out.println( ok_prefix + "Test Nearest Propagation" );
            
            PropStat pstat = new PropStat();
            
            SurfZPlane szp1 = new SurfZPlane(5.);
            SurfZPlane szp2 = new SurfZPlane(10.);
            
            TrackVector vec1 = new TrackVector();
            vec1.set(SurfZPlane.IX    , 1.);     // x
            vec1.set(SurfZPlane.IY    , 1.);     // y
            vec1.set(SurfZPlane.IDXDZ , 1.);     // dx/dz
            vec1.set(SurfZPlane.IDYDZ , 1.);     // dy/dz
            vec1.set(SurfZPlane.IQP   , 0.01);   //  q/p
            
            VTrack trv1 = new VTrack(szp1.newPureSurface(),vec1);
            trv1.setForward();
            
            if(debug) System.out.println( " starting: " + trv1 );
            VTrack trv2n = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2n,szp2,PropDir.NEAREST_MOVE);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( " nearest: " + trv2n );
            
            trv1.setBackward();
            
            if(debug) System.out.println( " starting: " + trv1 );
            trv2n = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2n,szp2,PropDir.NEAREST);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " nearest: " + trv2n );
            
            //********************************************************************
            
            if(debug) System.out.println( ok_prefix + "Test same surface" );
            VTrack trvs0 = new VTrack(szp1.newPureSurface(),vec1);
            trvs0.setForward();
            VTrack trvs = new VTrack(trvs0);
            PropStat tst = prop.vecDirProp(trvs,szp1,PropDir.NEAREST_MOVE);
            Assert.assertTrue( !tst.success() );
            tst = prop.vecDirProp(trvs,szp1,PropDir.FORWARD_MOVE);
            Assert.assertTrue( !tst.success() );
            tst = prop.vecDirProp(trvs,szp1,PropDir.BACKWARD_MOVE);
            Assert.assertTrue( !tst.success() );
            tst = prop.vecDirProp(trvs,szp1,PropDir.NEAREST);
            Assert.assertTrue( tst.success() );
            tst = prop.vecDirProp(trvs,szp1,PropDir.FORWARD);
            Assert.assertTrue( tst.success() );
            tst = prop.vecDirProp(trvs,szp1,PropDir.BACKWARD);
            Assert.assertTrue( tst.success() );
            Assert.assertTrue( trvs0.equals(trvs) );
            
            //********************************************************************
            
            if(debug) System.out.println( ok_prefix + "Test cloning." );
            Assert.assertTrue( prop.newPropagator() != null);
            
            
            //********************************************************************
            
            if(debug) System.out.println( ok_prefix + "Test the field." );
            Assert.assertTrue( prop.bField() == 2.0 );
            
            //********************************************************************
        }
                
                if(debug) System.out.println( ok_prefix + "Test Zero B field propagation" );
                
                {
                    PropZZ prop0 = new PropZZ(0.);
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
                    z=4.;
                    Surface srf_to = new SurfZPlane(z);
                    
                    VTrack trv = new VTrack(trv0);
                    PropStat pstat = prop0.vecDirProp(trv,srf_to,PropDir.FORWARD);
                    Assert.assertTrue( !pstat.success() );
                    
                    trv = new VTrack(trv0);
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.BACKWARD);
                    Assert.assertTrue( pstat.success() );
                    
                    trv = new VTrack(trv0);
                    trv.setBackward();
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.BACKWARD);
                    Assert.assertTrue( !pstat.success() );
                    
                    trv = new VTrack(trv0);
                    trv.setBackward();
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.FORWARD);
                    Assert.assertTrue( pstat.success() );
                    
                    trv = new VTrack(trv0);
                    trv.setForward();
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    Assert.assertTrue( pstat.success() );
                    
                    Assert.assertTrue( pstat.backward() );
                    Assert.assertTrue( trv.vector(SurfZPlane.IDXDZ) == trv0.vector(SurfZPlane.IDXDZ) );
                    Assert.assertTrue( trv.vector(SurfZPlane.IDYDZ) == trv0.vector(SurfZPlane.IDYDZ) );
                    Assert.assertTrue(trv.surface().pureEqual(srf_to));
                    
                    check_zero_propagation(trv0,trv,pstat);
                    
                    srf_to = new SurfZPlane(6.);
                    trv = new VTrack(trv0);
                    trv.setForward();
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    Assert.assertTrue( pstat.success() );
                    check_zero_propagation(trv0,trv,pstat);
                    
                    srf_to = new SurfZPlane(14.);
                    trv = new VTrack(trv0);
                    trv.setForward();
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    Assert.assertTrue( pstat.success() );
                    check_zero_propagation(trv0,trv,pstat);
                    
                    srf_to = new SurfZPlane(-1.);
                    trv = new VTrack(trv0);
                    trv.setForward();
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    Assert.assertTrue( pstat.success() );
                    check_zero_propagation(trv0,trv,pstat);
                    
                    srf_to = new SurfZPlane(-14.);
                    trv = new VTrack(trv0);
                    trv.setForward();
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    Assert.assertTrue( pstat.success() );
                    check_zero_propagation(trv0,trv,pstat);
                    
                    srf_to = new SurfZPlane(14.);
                    trv = new VTrack(trv0);
                    trv.setSurface( new SurfZPlane(1.));
                    trv.setBackward();
                    VTrack tmp = new VTrack(trv);
                    VTrack der = new VTrack(trv);
                    VTrack der1 = new VTrack(trv);
                    der1.setForward();
                    pstat = prop0.vecDirProp(trv,srf_to,PropDir.NEAREST);
                    Assert.assertTrue( pstat.success() );
                    check_zero_propagation(tmp,trv,pstat);
                    check_derivatives(prop0,der,srf_to);
                    check_derivatives(prop0,der1,srf_to);
                    
                }
                
                
                if(debug) System.out.println( ok_prefix
                        + "------------- All tests passed. -------------" );
                
                //********************************************************************
    }
    
    
    private static void  check_zero_propagation(  VTrack trv0,  VTrack trv,  PropStat pstat)
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
    
    private static void check_derivatives(  Propagator prop, VTrack trv0, Surface srf)
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
