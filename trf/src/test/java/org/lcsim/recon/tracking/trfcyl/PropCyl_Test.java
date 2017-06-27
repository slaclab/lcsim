/*
 * PropCyl_Test.java
 *
 * Created on July 24, 2007, 8:33 PM
 *
 * $Id: PropCyl_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

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
public class PropCyl_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropCyl_Test */
    public void testPropCyl()
    {
        String ok_prefix = "PropCyl (I): ";
        String error_prefix = "PropCyl test (E): ";
        
        if(debug) System.out.println(ok_prefix + "-------- Testing component PropCyl. --------" );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor." );
        double bfield = 2.0;
        PropCyl prop = new PropCyl(bfield);
        if(debug) System.out.println( prop );
        
        //********************************************************************
        
        // Here we propagate some tracks both forward and backward and then
        // each back to the original track.  We check that the returned
        // track parameters match those of the original track.
        if(debug) System.out.println(ok_prefix + "Check reversibility." );
        double r1[] =   { 10.0,  10.0,  10.0,  10.0, 10.0,  10.0, 10.0, 10.0 };
        double r2[] =   { 20.0,  20.0,  20.0,  20.0, 20.0,  20.0, 20.0, 20.0 };
        double phi1[] = {  0.0,   1.0,  -1.0,   2.0,  2.0,  -2.0, 3.0,   0.0 };
        double alf1[] = {  0.0,  .0001,-.0001,  0.9, -0.9,   0.9, 0.9,   3.0 };
        double crv[]  = {  0.0,   0.0,   0.0,   0.0,  0.0, -0.05, 0.01,  0.0 };
        double z[]  =   {  0.0,   0.0,   0.0,   0.0,  0.0,   0.0, 15.0,  0.0 };
        double tlm[]  = {  0.0,   0.0,   0.0,   0.0,  0.0,   0.0, 0.35,  0.0 };
        double fbdf[] = {  0.0,   0.0,   0.0,   0.0,  0.0,   0.0,   0.0, 0.0 };
        double bfdf[] = {  0.0,   0.0,   0.0,   0.0,  0.0,   0.0,   0.0, 0.0 };
        double maxdiff = 1.e-10;
        int ntrk = 8;
        int i;
        for ( i=0; i<ntrk; ++i )
        {
            if(debug) System.out.println("********** Propagate track " + i + ". **********" );
            PropStat pstat;
            SurfCylinder scy1 = new SurfCylinder(r1[i]);
            SurfCylinder scy2 = new SurfCylinder(r2[i]);
            TrackVector vec1 = new TrackVector();
            
            vec1.set(0,phi1[i]); // phi
            vec1.set(1,z[i]);    // z
            vec1.set(2,alf1[i]); // alpha
            vec1.set(3,tlm[i]);  // tlam
            vec1.set(4,crv[i]);  // curv
            VTrack trv1 = new VTrack(scy1,vec1);
            if(debug) System.out.println(" starting: " + trv1 );
            //
            // Propagate forward.
            VTrack trv2f = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2f,scy2,PropDir.FORWARD);
            if(debug) System.out.println("  forward: " + trv2f );
            if(debug) System.out.println(pstat );
            Assert.assertTrue( pstat.forward() );
            //
            // Propagate backward.
            VTrack trv2b = new VTrack(trv1);
            pstat = prop.vecDirProp(trv2b,scy2,PropDir.BACKWARD);
            if(debug) System.out.println(" backward: " + trv2b );
            if(debug) System.out.println(pstat );
            Assert.assertTrue( pstat.backward() );
            //
            // Propagate forward track backward.
            VTrack trv2fb = new VTrack(trv2f);
            pstat = prop.vecDirProp(trv2fb,scy1,PropDir.BACKWARD);
            if(debug) System.out.println(" f return: " + trv2fb );
            if(debug) System.out.println(pstat );
            Assert.assertTrue( pstat.backward() );
            //
            // Propagate backward track forward.
            VTrack trv2bf = new VTrack(trv2b);
            pstat = prop.vecDirProp(trv2bf,scy1,PropDir.FORWARD);
            if(debug) System.out.println(" b return: " + trv2bf );
            if(debug) System.out.println(pstat );
            Assert.assertTrue( pstat.forward() );
            // Check the return differences.
            // At least one of these should be zero.
            // Not both because we may cross the original surface on one of
            // the paths out.
            double difff =
                    scy1.vecDiff(trv2fb.vector(),trv1.vector()).amax();
            double diffb =
                    scy1.vecDiff(trv2bf.vector(),trv1.vector()).amax();
            if(debug) System.out.println("diffs: " + difff + ' ' + diffb );
            Assert.assertTrue( difff<maxdiff || diffb<maxdiff );
            //
            // Check no-move forward propagation to the same surface.
            VTrack trv1s = new VTrack(trv1);
            pstat = prop.vecDirProp(trv1s,scy1,PropDir.FORWARD);
            if(debug) System.out.println(" same f forward: " + trv1s );
            if(debug) System.out.println(pstat );
            Assert.assertTrue( pstat.same() );
            Assert.assertTrue( pstat.pathDistance() == 0 );
            //
            // Check no-move backward propagation to the same surface.
            trv1s = new VTrack(trv1);
            pstat = prop.vecDirProp(trv1s,scy1,PropDir.BACKWARD);
            if(debug) System.out.println(" same f backward: " + trv1s );
            if(debug) System.out.println(pstat );
            Assert.assertTrue( pstat.same() );
            Assert.assertTrue( pstat.pathDistance() == 0 );
            //
            // Check move propagation to the same surface.
            //
            // forward
            int successes = 0;
            trv1s = new VTrack(trv1);
            pstat = prop.vecDirProp(trv1s,scy1,PropDir.FORWARD_MOVE);
            if(debug) System.out.println(" forward move: " + trv1s );
            if(debug) System.out.println(pstat );
            if ( pstat.forward() ) ++successes;
            // backward
            trv1s = trv1;
            pstat = prop.vecDirProp(trv1s,scy1,PropDir.BACKWARD_MOVE);
            if(debug) System.out.println(" backward move: " + trv1s );
            if(debug) System.out.println(pstat );
            if ( pstat.backward() ) ++successes;
            // One of these should have succeeded.
            Assert.assertTrue( successes == 1 );
            //
            // nearest
            trv1s = trv1;
            pstat = prop.vecDirProp(trv1s,scy1,PropDir.NEAREST_MOVE);
            if(debug) System.out.println(" nearest move: " + trv1s );
            if(debug) System.out.println(pstat );
            Assert.assertTrue( pstat.success() );
            
        }
        //********************************************************************
        
        // Repeat the above with errors.
        if(debug) System.out.println(ok_prefix + "Check reversibility with errors." );
        double epp[] = {  0.01,   0.01,   0.01,   0.01,   0.01,   0.01,  0.01, 0.01  };
        double epz[] = { 0.01,  -0.01,  0.01,  -0.01,  0.01,  -0.01,  0.01,  0.01  };
        double ezz[] = { 0.25,   0.25,  0.25,   0.25,  0.25,   0.25,  0.25,  0.25  };
        double epa[] = { 0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004 };
        double eza[] = { 0.04,  -0.04,  0.04,  -0.04,  0.04,  -0.04,  0.04,  0.04  };
        double eaa[] = { 0.01,   0.01,  0.01,   0.01,  0.01,   0.01,  0.01,  0.01  };
        double epl[] = { 0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004 };
        double eal[] = { 0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004 };
        double ezl[] = { 0.04,  -0.04,  0.04,  -0.04,  0.04,  -0.04,  0.04,  0.04  };
        double ell[] = { 0.02,   0.02,  0.02,   0.02,  0.02,   0.02,  0.02,  0.02  };
        double epc[] = { 0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004 };
        double ezc[] = { 0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004 };
        double eac[] = { 0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004 };
        double elc[] = { 0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004 };
        double ecc[] = { 0.01,   0.01,  0.01,   0.01,  0.01,   0.01,  0.01,  0.01  };
        for ( i=0; i<ntrk; ++i )
        {
            if(debug) System.out.println("********** Propagate track " + i + ". **********" );
            PropStat pstat;
            SurfCylinder scy1 = new SurfCylinder(r1[i]);
            SurfCylinder scy2 = new SurfCylinder(r2[i]);
            TrackVector vec1 = new TrackVector();
            vec1.set(0,phi1[i]); // phi
            vec1.set(1,z[i]);    // z
            vec1.set(2,alf1[i]); // alpha
            vec1.set(3,tlm[i]);  // tlam
            vec1.set(4,crv[i]);  // curv
            TrackError err1 = new TrackError();
            err1.set(0,0,epp[i]);
            err1.set(0,1,epz[i]);
            err1.set(1,1,ezz[i]);
            err1.set(0,2,epa[i]);
            err1.set(1,2,eza[i]);
            err1.set(2,2,eaa[i]);
            err1.set(0,3,epl[i]);
            err1.set(1,3,ezl[i]);
            err1.set(2,3,eal[i]);
            err1.set(3,3,ell[i]);
            err1.set(0,4,epc[i]);
            err1.set(1,4,ezc[i]);
            err1.set(2,4,eac[i]);
            err1.set(3,4,elc[i]);
            err1.set(4,4,ecc[i]);
            ETrack trv1 = new ETrack(scy1.newPureSurface(), vec1, err1 );
            if(debug) System.out.println(" starting: " + trv1 );
            ETrack trv2f = new ETrack(trv1);
            if(debug) System.out.println("trv2f= "+trv2f);
            pstat = prop.errDirProp(trv2f,scy2,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println("  forward: " + trv2f );
            ETrack trv2b = new ETrack(trv1);
            pstat = prop.errDirProp(trv2b,scy2,PropDir.BACKWARD);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println(" backward: " + trv2b );
            ETrack trv2fb = new ETrack(trv2f);
            if(debug) System.out.println("trv2fb= "+trv2fb+", scy1= "+scy1);
            pstat = prop.errDirProp(trv2fb,scy1,PropDir.BACKWARD);
            if(debug) System.out.println("pstat= "+pstat);
            Assert.assertTrue( pstat.backward() );
            if(debug) System.out.println(" f return: " + trv2fb );
            ETrack trv2bf = new ETrack(trv2b);
            pstat = prop.errDirProp(trv2bf,scy1,PropDir.FORWARD);
            Assert.assertTrue( pstat.forward() );
            if(debug) System.out.println(" b return: " + trv2bf );
            double difff =
                    scy1.vecDiff(trv2fb.vector(),trv1.vector()).amax();
            double diffb =
                    scy1.vecDiff(trv2bf.vector(),trv1.vector()).amax();
            if(debug) System.out.println("vec diffs: " + difff + ' ' + diffb );
            Assert.assertTrue( difff<maxdiff || diffb<maxdiff );
            TrackError dfb = trv2fb.error().minus(trv1.error());
            TrackError dbf = trv2bf.error().minus(trv1.error());
            double edifff = dfb.amax();
            double ediffb = dbf.amax();
            if(debug) System.out.println("err diffs: " + edifff + ' ' + ediffb );
            Assert.assertTrue( edifff<maxdiff || ediffb<maxdiff );
            
        }
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test cloning." );
        Assert.assertTrue( prop.newPropagator() != null );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test the field." );
        Assert.assertTrue( prop.bField() == bfield );
        
        
        if(debug) System.out.println(ok_prefix + "------------- All tests passed. -------------" );
        //********************************************************************
        
    }
    
}
