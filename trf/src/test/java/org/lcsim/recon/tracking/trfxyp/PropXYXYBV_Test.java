/*
 * PropXYXYBV_Test.java
 *
 * Created on July 24, 2007, 10:21 PM
 *
 * $Id: PropXYXYBV_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class PropXYXYBV_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropXYXYBV_Test */
    public void testPropXYXYBV()
    {
        String ok_prefix = "PropXYXYBV (I): ";
        String error_prefix = "PropXYXYBV test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropXYXYBV. --------");
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        PropXYXYBV prop = new PropXYXYBV(2.0);
        if(debug) System.out.println( prop );
        
        //********************************************************************
        // Check that if planes are not parallel the propagatiopn fails
        {
            if(debug) System.out.println( ok_prefix + "Check that only tracks on right surfaces are being propagated " );
            
            PropStat pstat = new PropStat();
            
            SurfXYPlane sxyp1 = new SurfXYPlane(10.,0.);
            SurfXYPlane sxyp2 = new SurfXYPlane(12.,0.);
            SurfXYPlane sxyp1np = new SurfXYPlane(10.,0.1);
            SurfXYPlane sxyp2np = new SurfXYPlane(12.,0.1);
            
            TrackVector vec1 = new TrackVector();
            
            vec1.set(SurfXYPlane.IV    , -2.);     // v
            vec1.set(SurfXYPlane.IZ    , 3.);     // z
            vec1.set(SurfXYPlane.IDVDU , 1.5);  // dv/du
            vec1.set(SurfXYPlane.IDZDU , 2.3);  // dz/du
            vec1.set(SurfXYPlane.IQP   , 0.05);    //  q/p
            
            VTrack trv1 = new VTrack(sxyp1.newPureSurface(),vec1);
            trv1.setForward();
            
            pstat = prop.vecDirProp(trv1,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.success() );
            
            trv1.setVector(vec1);
            trv1.setSurface( sxyp1.newPureSurface());
            trv1.setForward();
            
            pstat = prop.vecDirProp(trv1,sxyp2np,PropDir.FORWARD);
            Assert.assertTrue( !pstat.success() );
            
            trv1.setVector(vec1);
            trv1.setSurface( sxyp1np.newPureSurface());
            trv1.setForward();
            
            pstat = prop.vecDirProp(trv1,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( !pstat.success() );
            
            trv1.setVector(vec1);
            trv1.setSurface( sxyp1np.newPureSurface());
            trv1.setForward();
            
            pstat = prop.vecDirProp(trv1,sxyp2np,PropDir.FORWARD);
            Assert.assertTrue( !pstat.success() );
            
            
        }
        
        //********************************************************************
        
        // Here we propagate some tracks both forward and backward and then
        // each back to the original track.  We check that the returned
        // track parameters match those of the original track.
        if(debug) System.out.println( ok_prefix + "Check reversibility." );
        
        double u1[]  ={10.,      10.,     10.,     10.,     10.,     10.};
        double u2[]  ={12.,      12.,     12.,     12.,     12.,     12.};
        double phi1[]={  0.,    0.,   0. ,   0.,   0.,   0.};
        double phi2[]={  0.,    0.,   0.,    0.,   0.,   0.};
        int sign_du[]={  1,       -1,      -1,       1,       1,       1     };
        double v[]   ={ -2.,       2.,     40.,     40.,     -2.,     -2.    };
        double z[]   ={  3.,       3.,      3.,      3.,      3.,      3.    };
        double dvdu[]={-1.5,     -1.5,     1.5,     1.5,    -1.5,      0.    };
        double dzdu[]={ 2.3,     -2.3,    -2.3,     2.3,     0.,       2.3   };
        double qp[]  ={ 0.05,    -0.05,    0.05,   -0.05,    0.05,     0.05  };
        
        double maxdiff = 1.e-9;
        int ntrk = 6;
        int i;
        for ( i=0; i<ntrk; ++i )
        {
            if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
            PropStat pstat = new PropStat();
            SurfXYPlane sxyp1 = new SurfXYPlane(u1[i],phi1[i]);
            SurfXYPlane sxyp2 = new SurfXYPlane(u2[i],phi2[i]);
            TrackVector vec1 = new TrackVector();
            vec1.set(SurfXYPlane.IV    , v[i]);     // v
            vec1.set(SurfXYPlane.IZ    , z[i]);     // z
            vec1.set(SurfXYPlane.IDVDU , dvdu[i]);  // dv/du
            vec1.set(SurfXYPlane.IDZDU , dzdu[i]);  // dz/du
            vec1.set(SurfXYPlane.IQP   , qp[i]);    //  q/p
            VTrack trv1 = new VTrack( sxyp1.newPureSurface(),vec1);
            if(sign_du[i]==1) trv1.setForward();
            else trv1.setBackward();
            if(debug) System.out.println( " starting: " + trv1 );
            
            VTrack trv2f = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2f,sxyp2,PropDir.FORWARD);
            if(debug) System.out.println( pstat );
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "  forward: " + trv2f );
            Assert.assertTrue(check_dv(trv1,trv2f)>=0.);
            
            VTrack trv2b = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2b,sxyp2,PropDir.BACKWARD);
            if(debug) System.out.println( pstat );
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " backward: " + trv2b );
            Assert.assertTrue(check_dv(trv1,trv2b)<=0.);
            
            VTrack trv2fb = new VTrack(trv2f);
            pstat = prop.vecDirProp(trv2fb,sxyp1,PropDir.BACKWARD);
            if(debug) System.out.println( pstat );
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " f return: " + trv2fb );
            Assert.assertTrue(check_dv(trv2f,trv2fb)<=0.);
            
            VTrack trv2bf = new VTrack(trv2b);
            pstat = prop.vecDirProp(trv2bf,sxyp1,PropDir.FORWARD);
            if(debug) System.out.println( pstat );
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( " b return: " + trv2bf );
            Assert.assertTrue(check_dv(trv2b,trv2bf)>=0.);
            
            double difff =
                    sxyp1.vecDiff(trv2fb.vector(),trv1.vector()).amax();
            double diffb =
                    sxyp1.vecDiff(trv2bf.vector(),trv1.vector()).amax();
            if(debug) System.out.println( "diffs: " + difff + ' ' + diffb );
            Assert.assertTrue( difff < maxdiff || diffb < maxdiff );
            //  Assert.assertTrue( diffb < maxdiff );
            
        }
        
        //********************************************************************
        
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
        
        maxdiff = 1.e-6;
        
        for ( i=0; i<ntrk; ++i )
        {
            if(debug) System.out.println( "********** Propagate track " + i + ". **********" );
            PropStat pstat = new PropStat();
            SurfXYPlane sxyp1 = new SurfXYPlane(u1[i],phi1[i]);
            SurfXYPlane sxyp2 = new SurfXYPlane(u2[i],phi2[i]);
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
            ETrack trv1 = new ETrack( sxyp1.newPureSurface(),vec1,err1);
            if(sign_du[i]==1) trv1.setForward();
            else trv1.setBackward();
            if(debug) System.out.println( " starting: " + trv1 );
            
            ETrack trv2f = new ETrack(trv1);
            pstat = prop.errDirProp(trv2f,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "  forward: " + trv2f );
            ETrack trv2b = new ETrack(trv1);
            pstat = prop.errDirProp(trv2b,sxyp2,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " backward: " + trv2b );
            ETrack trv2fb = new ETrack(trv2f);
            pstat = prop.errDirProp(trv2fb,sxyp1,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " f return: " + trv2fb );
            ETrack trv2bf = new ETrack(trv2b);
            pstat = prop.errDirProp(trv2bf,sxyp1,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( " b return: " + trv2bf );
            double difff =
                    sxyp1.vecDiff(trv2fb.vector(),trv1.vector()).amax();
            double diffb =
                    sxyp1.vecDiff(trv2bf.vector(),trv1.vector()).amax();
            
            
            TrackError dfb = trv2fb.error().minus(trv1.error());
            TrackError dbf = trv2bf.error().minus(trv1.error());
            double edifff = dfb.amax();
            double ediffb = dbf.amax();
            
            if(debug) System.out.println( "vec diffs: " + difff + ' ' + diffb );
            if(debug) System.out.println( "err diffs: " + edifff + ' ' + ediffb );
            
            Assert.assertTrue( (difff < maxdiff && edifff < maxdiff) ||
                    (diffb < maxdiff && ediffb < maxdiff)    );
            
        }
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test Nearest Propagation" );
        
        PropStat pstat = new PropStat();
        SurfXYPlane sxyp1 = new SurfXYPlane(2.,0.);
        SurfXYPlane sxyp2 = new SurfXYPlane(3.,0.);
        
        TrackVector vec1 = new TrackVector();
        
        vec1.set(SurfXYPlane.IV    , 1.);     // v
        vec1.set(SurfXYPlane.IZ    , 1.);     // z
        vec1.set(SurfXYPlane.IDVDU , 1.);     // dv/du
        vec1.set(SurfXYPlane.IDZDU , 1.);     // dz/du
        vec1.set(SurfXYPlane.IQP   , 0.01);   //  q/p
        
        VTrack trv1 = new VTrack( sxyp1.newPureSurface(),vec1);
        trv1.setForward();
        
        if(debug) System.out.println( " starting: " + trv1 );
        VTrack trv2n = new VTrack(trv1);
        pstat = prop.vecDirProp(trv2n,sxyp2,PropDir.NEAREST);
        Assert.assertTrue( pstat.forward() );
        if(debug) System.out.println( " nearest: " + trv2n );
        
        trv1.setBackward();
        
        if(debug) System.out.println( " starting: " + trv1 );
        trv2n = new VTrack(trv1);
        pstat = prop.vecDirProp(trv2n,sxyp2,PropDir.NEAREST);
        Assert.assertTrue( pstat.backward() );
        if(debug) System.out.println( " nearest: " + trv2n );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cloning." );
        Assert.assertTrue( prop.newPropagator() != null);
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test the field." );
        Assert.assertTrue( prop.bField() == 2.0 );
        
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        //********************************************************************
        
    }
    //**********************************************************************
    //Checker of correct v propagation
    
    static double check_dv(VTrack trv1, VTrack trv2)
    {
        double v1    = trv1.vector().get(SurfXYPlane.IV);
        double v2    = trv2.vector().get(SurfXYPlane.IV);
        double dvdu  = trv1.vector().get(SurfXYPlane.IDVDU);
        int sign_du = (trv1.isForward())?1:-1;
        return (v2-v1)*dvdu*sign_du;
    }
}
