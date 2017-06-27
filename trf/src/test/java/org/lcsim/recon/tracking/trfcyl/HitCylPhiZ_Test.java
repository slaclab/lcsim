/*
 * HitCylPhiZ_Test.java
 *
 * Created on July 24, 2007, 8:38 PM
 *
 * $Id: HitCylPhiZ_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman Graf
 */
public class HitCylPhiZ_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HitCylPhiZ_Test */
    public void testHitCylPhiZ()
    {
        String component = "HitCylPhiZ";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        if(debug) System.out.println( ok_prefix + "Test cluster constructors." );
        double r1 = 10.0;
        double phiz1 = 1.0;
        double dphiz1 = 0.01;
        SurfCylinder scy1 = new SurfCylinder(r1);
        ClusCylPhiZ hcp1 = new ClusCylPhiZ(r1,phiz1,dphiz1,stereo);
        if(debug) System.out.println( hcp1 );
        double r2 = 20.0;
        double phiz2 = 2.0;
        double dphiz2 = 0.02;
        ClusCylPhiZ hcp2 = new ClusCylPhiZ(r2,phiz2,dphiz2,stereo);
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cluster type ID." );
        if(debug) System.out.println( hcp1.type() );
        if(debug) System.out.println( hcp2.type() );
        Assert.assertTrue( hcp1.type() != null );
        Assert.assertTrue( hcp1.type().equals(hcp2.type() ));
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cluster accessors." );
        if(debug) System.out.println( hcp1 );
        if(debug) System.out.println( hcp1.surface() );
        if(debug) System.out.println( hcp1.phiZ() );
        if(debug) System.out.println( hcp1.dPhiZ() );
        if(debug) System.out.println( hcp1.stereo() );
        Assert.assertTrue( hcp1.surface().equals(scy1) );
        Assert.assertTrue( hcp1.phiZ() == phiz1 );
        Assert.assertTrue( hcp1.dPhiZ() == dphiz1 );
        Assert.assertTrue( hcp1.stereo() == stereo );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a hit." );
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        HitDerivative der_expect = new HitDerivative(1);
        der_expect.set(0,0, 1.0);
        der_expect.set(0,1, stereo);
        vec.set(0, 1.1);
        vec.set(1, 40.0);
        err.set(0,0, 0.04);
        ETrack tre1 = new ETrack( scy1.newPureSurface(), vec, err );
        //  Cluster clu1 = new ClusterCylPhiZ(hcp1);
        List tclus11 = hcp1.predict(tre1,hcp1);
        Assert.assertTrue( tclus11.size() == 1 );
        Hit pre11 = (Hit) tclus11.get(0);
        double maxdiff = 1.e-12;
        if(debug) System.out.println( pre11.size() );
        Assert.assertTrue( pre11.size() == 1);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual(pre11.measuredVector().get(0),phiz1) );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredError().get(0,0), dphiz1*dphiz1 ) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedVector().get(0), calc_phiz(vec) ) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedError().get(0,0), calc_ephiz(err) ) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect ) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) -
                calc_phiz(vec) + phiz1) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different track."
                );
        vec.set(0, 1.2);
        ETrack tre2 = new ETrack( scy1.newPureSurface(), vec, err );
        List tclus12 = hcp1.predict(tre2,hcp1);
        Assert.assertTrue( tclus12.size() == 1 );
        Hit pre12 = (Hit) tclus12.get(0);
        if(debug) System.out.println( pre12.size() );
        Assert.assertTrue( pre12.size() == 1);
        if(debug) System.out.println( pre12.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre12.measuredVector().get(0), phiz1 ) );
        if(debug) System.out.println( pre12.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre12.measuredError().get(0,0), dphiz1*dphiz1 ) );
        if(debug) System.out.println( pre12.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre12.predictedVector().get(0), calc_phiz(vec) ) );
        if(debug) System.out.println( pre12.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre12.predictedError().get(0,0), calc_ephiz(err) ) );
        if(debug) System.out.println( pre12.dHitdTrack() );
        Assert.assertTrue( pre12.dHitdTrack().equals(der_expect ) );
        if(debug) System.out.println( pre12.differenceVector() );
        Assert.assertTrue( Math.abs(pre12.differenceVector().get(0) -
                calc_phiz(vec) + phiz1) < maxdiff );
        
        // same cluster ==> same hit (even though track changes)
        Assert.assertTrue( pre11.equals(pre11) );
        Assert.assertTrue( ! ( pre11.notEquals(pre11) ) );
        Assert.assertTrue( pre11.equals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate hit for a different cluster."
                );
        List tclus22 = hcp2.predict(tre2,hcp2);
        Assert.assertTrue( tclus22.size() == 1 );
        Hit pre22 = (Hit) tclus22.get(0);
        if(debug) System.out.println( pre22.size() );
        Assert.assertTrue( pre22.size() == 1 );
        if(debug) System.out.println( pre22.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre22.measuredVector().get(0), phiz2 ) );
        if(debug) System.out.println( pre22.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre22.measuredError().get(0,0), dphiz2*dphiz2 ) );
        if(debug) System.out.println( pre22.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre22.predictedVector().get(0), calc_phiz(vec) ) );
        if(debug) System.out.println( pre22.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre22.predictedError().get(0,0), calc_ephiz(err) ) );
        if(debug) System.out.println( pre22.dHitdTrack() );
        Assert.assertTrue( pre22.dHitdTrack().equals(der_expect ) );
        if(debug) System.out.println( pre22.differenceVector() );
        Assert.assertTrue( Math.abs(pre22.differenceVector().get(0) -
                calc_phiz(vec) + phiz2) < maxdiff );
        
        // different cluster ==> different hit
        Assert.assertTrue( pre22.notEquals(pre11) );
        Assert.assertTrue( ! ( pre22.equals(pre11) ) );
        Assert.assertTrue( pre22.notEquals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Update hit." );
        if(debug) System.out.println( pre11 );
        pre11.update(tre2);
        if(debug) System.out.println( pre11 );
        Assert.assertTrue( pre11.size() == 1);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredVector().get(0), phiz1 ) );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredError().get(0,0), dphiz1*dphiz1 ) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedVector().get(0), calc_phiz(vec) ) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedError().get(0,0), calc_ephiz(err) ) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) -
                calc_phiz(vec) + phiz1) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check hit type and equality." );
        if(debug) System.out.println( HitCylPhiZ.staticType() );
        if(debug) System.out.println( pre11.type() );
        if(debug) System.out.println( pre12.type() );
        Assert.assertTrue( pre11.type() != null );
        Assert.assertTrue( pre11.type().equals(HitCylPhiZ.staticType()) );
        Assert.assertTrue( pre11.type().equals(pre12.type()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check MC ID's." );
        Assert.assertTrue( hcp1.mcIds().size() == 0 );
        List ids = new ArrayList();
        ids.add( new Integer(1));
        ids.add( new Integer(22));
        ids.add( new Integer(333));
        ClusCylPhiZ hcp3 = new ClusCylPhiZ(r2,phiz2,dphiz2,stereo,ids);
        
        {
            int[] oids = hcp3.mcIdArray();
            Assert.assertTrue( oids.length == 3 );
            Assert.assertTrue( oids[0] == 1 );
            Assert.assertTrue( oids[1] == 22 );
            Assert.assertTrue( oids[2] == 333 );
        }
        
        {
            List thits31 = hcp3.predict(tre1,hcp3);
            Assert.assertTrue( thits31.size() == 1 );
            Hit hit = (Hit)thits31.get(0);
            List oids = hit.mcIds();
            
            Assert.assertTrue( ((Integer) oids.get(0)).intValue() == 1 );
            Assert.assertTrue( ((Integer) oids.get(1)).intValue() == 22 );
            Assert.assertTrue( ((Integer) oids.get(2)).intValue() == 333 );
        }
        {
            List oids = hcp3.mcIds();
            Assert.assertTrue( oids.size() == 3 );
            Assert.assertTrue( ((Integer) oids.get(0)).intValue() == 1 );
            Assert.assertTrue( ((Integer) oids.get(1)).intValue() == 22 );
            Assert.assertTrue( ((Integer) oids.get(2)).intValue() == 333 );
        }
        
        int mcid = 137;
        ClusCylPhiZ clus1 = new ClusCylPhiZ(r2,phiz2,dphiz2,stereo,mcid);
        Assert.assertTrue( clus1.mcIds().size() == 1 );
        Assert.assertTrue( clus1.mcIdArray()[0] == mcid );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    static double stereo = 0.001;
    
    // calculate phiz from a track vector
    static double calc_phiz(TrackVector vec)
    {
        return vec.get(0) + stereo*vec.get(1);
    }
    
    // calculate ephiz from error matrix
    static double calc_ephiz(TrackError err)
    {
        return err.get(0,0) + 2.0*stereo*err.get(0,1) + stereo*stereo*err.get(1,1);
    }
}
