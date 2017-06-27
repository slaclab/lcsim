/*
 * HitCylPhi_Test.java
 *
 * Created on July 24, 2007, 8:37 PM
 *
 * $Id: HitCylPhi_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
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
public class HitCylPhi_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HitCylPhi_Test */
    public void testHitCylPhi()
    {
        String component = "HitCylPhi";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        if(debug) System.out.println( ok_prefix + "Test hit constructors." );
        double r1 = 10.0;
        double phi1 = 1.0;
        double dphi1 = 0.1;
        SurfCylinder scy1 = new SurfCylinder(r1);
        ClusCylPhi hcp1 = new ClusCylPhi(r1,phi1,dphi1);
        if(debug) System.out.println( hcp1 );
        
        double r2 = 20.0;
        double phi2 = 2.0;
        double dphi2 = 0.2;
        ClusCylPhi hcp2 = new ClusCylPhi(r2,phi2,dphi2);
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test cluster type ID." );
        if(debug) System.out.println( hcp1.type() );
        if(debug) System.out.println( hcp2.type() );
        Assert.assertTrue( hcp1.type() != null );
        Assert.assertTrue( hcp1.type().equals(hcp2.type()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test hit accessors." );
        if(debug) System.out.println( hcp1 );
        if(debug) System.out.println( hcp1.surface() );
        if(debug) System.out.println( hcp1.phi() );
        if(debug) System.out.println( hcp1.dPhi() );
        Assert.assertTrue( hcp1.surface().equals(scy1) );
        Assert.assertTrue( hcp1.phi() == phi1  );
        Assert.assertTrue( hcp1.dPhi() == dphi1  );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate a prediction." );
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        HitDerivative der_expect = new HitDerivative(1);
        der_expect.set(0,0, 1.0);
        vec.set(0, 1.1);
        err.set(0,0, 0.02);
        ETrack tre1 = new ETrack( scy1.newPureSurface(), vec, err );
        //  const ClusterPtr hcp1ptr(&hcp1);
        List thits11 = hcp1.predict(tre1,hcp1);
        Assert.assertTrue( thits11.size() == 1 );
        Hit pre11 = (Hit)thits11.get(0);
        double maxdiff = 1.e-12;
        if(debug) System.out.println( pre11.size() );
        Assert.assertTrue( pre11.size() == 1);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredVector().get(0), phi1 ) );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.measuredError().get(0,0), dphi1*dphi1 ) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedVector().get(0), vec.get(0) ) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre11.predictedError().get(0,0), err.get(0,0) ) );
        if(debug) System.out.println( der_expect );
        if(debug) System.out.println( pre11.dHitdTrack() );
        if(debug) System.out.println( pre11.dHitdTrack().minus(der_expect) );
        //  Assert.assertTrue( TRFMath.isEqual( pre11.dhit_dtrack(), der_expect ) );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect ) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) - vec.get(0) + phi1) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate prediction for a different track."
                );
        vec.set(0, 1.2);
        ETrack tre2 = new ETrack( scy1.newPureSurface(), vec, err );
        List thits12 = hcp1.predict(tre2,hcp1);
        Assert.assertTrue( thits12.size() == 1 );
        Hit pre12 = (Hit) thits12.get(0);
        if(debug) System.out.println( pre12.size() );
        Assert.assertTrue( pre12.size() == 1);
        if(debug) System.out.println( pre12.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre12.measuredVector().get(0), phi1 ) );
        if(debug) System.out.println( pre12.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre12.measuredError().get(0,0), dphi1*dphi1 ) );
        if(debug) System.out.println( pre12.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre12.predictedVector().get(0), vec.get(0) ) );
        if(debug) System.out.println( pre12.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre12.predictedError().get(0,0), err.get(0,0) ) );
        if(debug) System.out.println( pre12.dHitdTrack() );
        Assert.assertTrue( pre12.dHitdTrack().equals(der_expect ) );
        if(debug) System.out.println( pre12.differenceVector() );
        Assert.assertTrue( Math.abs(pre12.differenceVector().get(0) - vec.get(0) + phi1) < maxdiff );
        // same hit ==> same prediction (even though track changes)
        Assert.assertTrue( pre11.equals(pre11) );
        Assert.assertTrue( ! ( pre11.notEquals(pre11) ) );
        Assert.assertTrue( pre11.equals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Generate prediction for a different hit."
                );
        
        List thits22 = hcp2.predict(tre2,hcp2);
        Assert.assertTrue( thits22.size() == 1 );
        Hit pre22 = (Hit)thits22.get(0);
        if(debug) System.out.println( pre22.size() );
        Assert.assertTrue( pre22.size() == 1);
        if(debug) System.out.println( pre22.measuredVector() );
        Assert.assertTrue( TRFMath.isEqual( pre22.measuredVector().get(0), phi2 ) );
        if(debug) System.out.println( pre22.measuredError() );
        Assert.assertTrue( TRFMath.isEqual( pre22.measuredError().get(0,0), dphi2*dphi2 ) );
        if(debug) System.out.println( pre22.predictedVector() );
        Assert.assertTrue( TRFMath.isEqual( pre22.predictedVector().get(0), vec.get(0) ) );
        if(debug) System.out.println( pre22.predictedError() );
        Assert.assertTrue( TRFMath.isEqual( pre22.predictedError().get(0,0), err.get(0,0) ) );
        
        if(debug) System.out.println( pre22.dHitdTrack() );
        Assert.assertTrue( pre22.dHitdTrack().equals(der_expect ) );
        if(debug) System.out.println( pre22.differenceVector() );
        
        Assert.assertTrue( Math.abs(pre22.differenceVector().get(0) - vec.get(0) + phi2) < maxdiff );
        // different hit ==> different prediction
        Assert.assertTrue( pre22.notEquals(pre11) );
        Assert.assertTrue( ! ( pre22.equals(pre11) ) );
        Assert.assertTrue( pre22.notEquals(pre12) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Update prediction." );
        if(debug) System.out.println( pre11 );
        pre11.update(tre2);
        if(debug) System.out.println( pre11 );
        Assert.assertTrue( pre11.size() == 1);
        if(debug) System.out.println( pre11.measuredVector() );
        Assert.assertTrue( pre11.measuredVector().get(0) == phi1 );
        if(debug) System.out.println( pre11.measuredError() );
        Assert.assertTrue( TRFMath.isEqual(pre11.measuredError().get(0,0), dphi1*dphi1) );
        if(debug) System.out.println( pre11.predictedVector() );
        Assert.assertTrue( pre11.predictedVector().get(0) == vec.get(0) );
        if(debug) System.out.println( pre11.predictedError() );
        Assert.assertTrue( pre11.predictedError().get(0,0) == err.get(0,0) );
        if(debug) System.out.println( pre11.dHitdTrack() );
        Assert.assertTrue( pre11.dHitdTrack().equals(der_expect) );
        if(debug) System.out.println( pre11.differenceVector() );
        Assert.assertTrue( Math.abs(pre11.differenceVector().get(0) - vec.get(0) + phi1) < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check prediction type and equality." );
        if(debug) System.out.println( HitCylPhi.staticType() );
        if(debug) System.out.println( pre11.type() );
        if(debug) System.out.println( pre12.type() );
        Assert.assertTrue( pre11.type() != null );
        Assert.assertTrue( pre11.type().equals(HitCylPhi.staticType()) );
        Assert.assertTrue( pre11.type().equals(pre12.type()) );
        Assert.assertTrue(! pre11.type().equals(hcp1.type()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check MC ID's." );
        Assert.assertTrue( hcp1.mcIds().size() == 0 );
        List ids = new ArrayList();
        ids.add(new Integer(1));
        ids.add(new Integer(22));
        ids.add(new Integer(333));
        ClusCylPhi hcp3 = new ClusCylPhi(r2,phi2,dphi2,ids);
        if(debug) System.out.println( "hcp3= "+ hcp3 );
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
            Assert.assertTrue( oids.size() == 3 );
            Assert.assertTrue( ((Integer) oids.get(0)).intValue() == 1 );
            Assert.assertTrue( ((Integer) oids.get(1)).intValue() == 22 );
            Assert.assertTrue( ((Integer) oids.get(2)).intValue() == 333 );
        }
        
        {
            ClusCylPhi hcp4 = new ClusCylPhi(hcp3);
            List oids = hcp4.mcIds();
            Assert.assertTrue( oids.size() == 3 );
            Assert.assertTrue( ((Integer) oids.get(0)).intValue() == 1 );
            Assert.assertTrue( ((Integer) oids.get(1)).intValue() == 22 );
            Assert.assertTrue( ((Integer) oids.get(2)).intValue() == 333 );
        }
        
        int mcid = 137;
        ClusCylPhi clus1 = new ClusCylPhi(r2,phi2,dphi2,mcid);
        Assert.assertTrue( clus1.mcIds().size() == 1 );
        Assert.assertTrue( clus1.mcIdArray()[0] == mcid );
        
        //********************************************************************
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
