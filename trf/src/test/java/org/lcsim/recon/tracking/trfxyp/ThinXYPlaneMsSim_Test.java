/*
 * ThinXYPlaneMsSim_Test.java
 *
 * Created on July 24, 2007, 10:13 PM
 *
 * $Id: ThinXYPlaneMsSim_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.SimInteractor;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ThinXYPlaneMsSim_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ThinXYPlaneMsSim_Test */
    public void testThinXYPlaneMsSim()
    {
        String component = "ThinXYPlaneMsSim";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        // Assign track parameter indices.
        
        int IV = SurfXYPlane.IV;
        int IZ = SurfXYPlane.IZ;
        int IDVDU = SurfXYPlane.IDVDU;
        int IDZDU = SurfXYPlane.IDZDU;
        int IQP = SurfXYPlane.IQP;
        
        //********************************************************************
        
        double radLength = 0.001;
        ThinXYPlaneMsSim scatterIt = new ThinXYPlaneMsSim(radLength);
        
        Assert.assertTrue(scatterIt.radLength() == radLength);
        
        SurfXYPlane xyp = new SurfXYPlane(20.,0.);
        double[] pars = {1., 2., 3., 4., 5. };
        TrackVector tv = new TrackVector(pars);
        VTrack vtrk = new VTrack(xyp, tv);
        
        // Give the track 1GeV
        TrackVector trv = vtrk.vector();
        trv.set(IQP, 1.0);
        vtrk.setVector( trv );
        scatterIt.interact( vtrk );
        TrackVector scattered = new TrackVector( vtrk.vector() );
        Assert.assertTrue( scattered.get(IV) == trv.get(IV) );
        Assert.assertTrue( scattered.get(IZ) == trv.get(IZ) );
        Assert.assertTrue( scattered.get(IDVDU) != trv.get(IDVDU) );
        Assert.assertTrue( scattered.get(IDZDU) != trv.get(IDZDU) );
        Assert.assertTrue( scattered.get(IQP) != trv.get(IQP) );
        if(debug) System.out.println( "before interaction " + trv );
        if(debug) System.out.println( "after interaction " + scattered );
        
        
        TrackVector tvec = new TrackVector();
        tvec.set(IV, 1.0);
        tvec.set(IZ, 2.0);
        tvec.set(IDVDU, 3.0);
        tvec.set(IDZDU, 4.0);
        tvec.set(IQP, 5.0);
        SurfXYPlane szp = new SurfXYPlane(6.0,1.);
        VTrack t = new VTrack(szp.newPureSurface(),tvec);
        if(debug) System.out.println( "Track before interacting... " + tvec );
        scatterIt.interact( t );
        TrackVector trv1 = new TrackVector( t.vector() );
        if(debug) System.out.println( "Track after interacting... " + trv1 );
        
        tvec.set(IV,    7.25001);
        tvec.set(IZ,    83.571);
        tvec.set(IDVDU, 0.0220818);
        tvec.set(IDZDU, 0.586898);
        tvec.set(IQP,  -0.0116488);
        
        SurfXYPlane szp2 = new SurfXYPlane(35.0,2.15);
        VTrack t2 = new VTrack(szp2.newPureSurface(),tvec);
        if(debug) System.out.println( "Track before interacting... " + tvec );
        scatterIt.interact( t2 );
        TrackVector trv2 = new TrackVector( t2.vector() );
        if(debug) System.out.println( "Track after interacting... " + trv2 );
        
        
        //********************************************************************
        if(debug) System.out.println(ok_prefix + "Testing clone");
        VTrack t3 = new VTrack(t2);
        VTrack t4 = new VTrack(t2);
        t3.setForward();
        t4.setForward();
        Assert.assertTrue(t3.equals(t4));
        // clone
        SimInteractor scatterIt2 = scatterIt.newCopy();
        // assert they are not the same object
        Assert.assertTrue( scatterIt2!=scatterIt );
        // scatter both tracks
        scatterIt2.interact(t4);
        scatterIt.interact(t3);
        // assert that the newly scattered tracks are not equal
        // since scattering is stochastic and we have different random
        // number generators
        Assert.assertTrue( t3.notEquals(t4) );
        // assert they're different from the original track
        Assert.assertTrue( t3.notEquals(t2) );
        Assert.assertTrue( t4.notEquals(t2) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
