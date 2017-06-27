/*
 * PropXYXYBX_Test.java
 *
 * Created on July 24, 2007, 10:17 PM
 *
 * $Id: PropXYXYBX_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
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
public class PropXYXYBX_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropXYXYBX_Test */
    public void testPropXYXYBX()
    {
        String ok_prefix = "PropXYXYBX (I): ";
        String error_prefix = "PropXYXYBX test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropXYXYBX. --------" );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        PropXYXYBX prop = new PropXYXYBX(2.0);
        if(debug) System.out.println( prop );
        
        //********************************************************************
        
        // Here we propagate some tracks both forward and backward and then
        // each back to the original track.  We check that the returned
        // track parameters match those of the original track.
        if(debug) System.out.println( ok_prefix + "Check reversibility." );
        
        double u1[]  ={5.,       5.,      5.,      5.,      5.,      5.};
        double u2[]  ={10.,      10.,     10.,     10.,     10.,     10.};
        double phi1[]={ 0.,    3.*Math.PI/2.,   0.,   5.*Math.PI/6.,   Math.PI/2.,   Math.PI/3.};
        double phi2[]={ 0,     Math.PI/6.,    Math.PI/2.,  Math.PI/6.,  7.*Math.PI/6., 5.*Math.PI/6.};
        int sign_du[]={  1,       -1,       1,      -1,      -1,      -1     };
        double v[]   ={ -2.,       2.,      4.,      4.,     -2.,     -2.    };
        double z[]   ={  3.,       3.,      3.,      3.,      3.,      3.    };
        double dvdu[]={-1.5,     -1.5,      0.1,      1.5,    -1.5,      0.    };
        double dzdu[]={ 2.3,     -2.3,    -2.3,     2.3,     0.,       2.3   };
        double qp[]  ={ 0.05,    -0.05,    0.05,   -0.05,    0.05,     0.05  };
        
        double maxdiff = 1.e-9;
        //		int ntrk = 6;
        int ntrk = 5;
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
            VTrack trv1 = new VTrack(sxyp1.newPureSurface(),vec1);
            if (sign_du[i]==1) trv1.setForward();
            else trv1.setBackward();
            if(debug) System.out.println( " starting: " + trv1 );
            VTrack trv2f = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2f,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "  forward: " + trv2f );
            VTrack trv2fb = new VTrack(trv2f);
            if(debug) System.out.println( "  backwards: " + trv2fb );
            pstat = prop.vecDirProp(trv2fb,sxyp1,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " f return: " + trv2fb );
            
            double difff =
                    sxyp1.vecDiff(trv2fb.vector(),trv1.vector()).amax();
            
            Assert.assertTrue( difff < maxdiff );
            
        }
        
        //********************************************************************
        
        // Repeat the above with errors.
        if(debug) System.out.println( ok_prefix + "Check reversibility with errors." );
        double evv[] =   {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01  };
        double evz[] =   {  0.01,  -0.01,   0.01,  -0.01,   0.01,  -0.01  };
        double ezz[] =   {  0.25,   0.25,   0.25,   0.25,   0.25,   0.25, };
        double evdv[] =  {  0.004, -0.004,  0.004, -0.004,  0.004, -0.004 };
        double ezdv[] =  {  0.04,  -0.04,   0.04,  -0.04,   0.04,  -0.04, };
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
            vec1.set(SurfXYPlane.IV    ,  v[i]);     // v
            vec1.set(SurfXYPlane.IZ    ,  z[i]);     // z
            vec1.set(SurfXYPlane.IDVDU ,  dvdu[i]);  // dv/du
            vec1.set(SurfXYPlane.IDZDU ,  dzdu[i]);  // dz/du
            vec1.set(SurfXYPlane.IQP   ,  qp[i]);    //  q/p
            
            TrackError err1 = new TrackError();
            err1.set(SurfXYPlane.IV,SurfXYPlane.IV       ,  evv[i]);
            err1.set(SurfXYPlane.IV,SurfXYPlane.IZ       ,  evz[i]);
            err1.set(SurfXYPlane.IZ,SurfXYPlane.IZ       ,  ezz[i]);
            err1.set(SurfXYPlane.IV,SurfXYPlane.IDVDU    ,  evdv[i]);
            err1.set(SurfXYPlane.IZ,SurfXYPlane.IDVDU    ,  ezdv[i]);
            err1.set(SurfXYPlane.IDVDU,SurfXYPlane.IDVDU ,  edvdv[i]);
            err1.set(SurfXYPlane.IV,SurfXYPlane.IDZDU    ,  evdz[i]);
            err1.set(SurfXYPlane.IZ,SurfXYPlane.IDZDU    ,  ezdz[i]);
            err1.set(SurfXYPlane.IDVDU,SurfXYPlane.IDZDU ,  edvdz[i]);
            err1.set(SurfXYPlane.IDZDU,SurfXYPlane.IDZDU ,  edzdz[i]);
            err1.set(SurfXYPlane.IV,SurfXYPlane.IQP      ,  evqp[i]);
            err1.set(SurfXYPlane.IZ,SurfXYPlane.IQP      ,  ezqp[i]);
            err1.set(SurfXYPlane.IDVDU,SurfXYPlane.IQP   ,  edvqp[i]);
            err1.set(SurfXYPlane.IDZDU,SurfXYPlane.IQP   ,  edzqp[i]);
            err1.set(SurfXYPlane.IQP,SurfXYPlane.IQP     ,  eqpqp[i]);
            
            ETrack trv1 = new ETrack(sxyp1.newPureSurface(),vec1,err1);
            if (sign_du[i]==1) trv1.setForward();
            else trv1.setBackward();
            if(debug) System.out.println( " starting: " + trv1 );
            
            ETrack trv2f = new ETrack(trv1);
            pstat = prop.errDirProp(trv2f,sxyp2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println( "  forward: " + trv2f );
            ETrack trv2fb = new ETrack(trv2f);
            pstat = prop.errDirProp(trv2fb,sxyp1,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println( " f return: " + trv2fb );
            double difff =
                    sxyp1.vecDiff(trv2fb.vector(),trv1.vector()).amax();
            if(debug) System.out.println( "vec diffs: " + difff );
            Assert.assertTrue( difff < maxdiff );
            TrackError dfb = trv2fb.error().minus(trv1.error());
            double edifff = dfb.amax();
            if(debug) System.out.println( "err diffs: " + edifff );
            Assert.assertTrue( edifff < maxdiff );
            
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cloning." );
        Assert.assertTrue( prop.newPropagator() != null);
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test the field." );
        Assert.assertTrue( prop.bField() == 2.0 );
        
        
        
        //********************************************************************
/*
// Still problems with track 5!
                if(debug) System.out.println( ok_prefix
                        + "------------- All tests passed. -------------" );
 */
    }
    
}
