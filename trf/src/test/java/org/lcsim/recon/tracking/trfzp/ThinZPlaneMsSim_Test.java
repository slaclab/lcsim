/*
 * ThinZPlaneMsSim_Test.java
 *
 * Created on July 24, 2007, 10:58 PM
 *
 * $Id: ThinZPlaneMsSim_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.SimInteractor;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ThinZPlaneMsSim_Test extends TestCase
{
    private boolean debug;
    private static final int IX = SurfZPlane.IX;
    private static final int IY   = SurfZPlane.IY;
    private static final int IDXDZ = SurfZPlane.IDXDZ;
    private static final int IDYDZ = SurfZPlane.IDYDZ;
    private static final int IQP  = SurfZPlane.IQP;
    //**********************************************************************
    
    /** Creates a new instance of ThinZPlaneMsSim_Test */
    public void testThinZPlaneMsSim()
    {
        String component = "ThinZPlaneMsSim";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        
        //********************************************************************
        
        ThinZPlaneMsSim scatterIt = new ThinZPlaneMsSim(0.001);
        if(debug) System.out.println( scatterIt);
        
        Assert.assertTrue(scatterIt.radLength() == 0.001);
        
        VTrack vtrk = new VTrack();
        // Give the track 1GeV
        TrackVector trv = new TrackVector( );
        trv.set(IDXDZ, 0.001);
        trv.set(IQP,  1.0);
        vtrk.setVector( trv );
        scatterIt.interact( vtrk );
        TrackVector scattered = new TrackVector( vtrk.vector() );
        Assert.assertTrue( scattered.get(IX) == trv.get(IX) );
        Assert.assertTrue( scattered.get(IY) == trv.get(IY) );
        Assert.assertTrue( scattered.get(IDXDZ) != trv.get(IDXDZ) );
        Assert.assertTrue( scattered.get(IDYDZ) != trv.get(IDYDZ) );
        Assert.assertTrue( scattered.get(IQP) == trv.get(IQP) ); // q/p NOT q/pT
        if(debug) System.out.println( "before interaction " + trv );
        if(debug) System.out.println( "after interaction " + scattered );
        
        //cng
        TrackVector tvec = new TrackVector();
        tvec.set(IX, 1.0);
        tvec.set(IY, 2.0);
        tvec.set(IDXDZ, 3.0);
        tvec.set(IDYDZ, 4.0);
        tvec.set(IQP, 5.0);
        SurfZPlane szp = new SurfZPlane(6.0);
        VTrack t = new VTrack(szp.newPureSurface(),tvec);
        if(debug) System.out.println( "Track before interacting... " + tvec );
        scatterIt.interact( t );
        TrackVector trv1 = t.vector() ;
        if(debug) System.out.println( "Track after interacting... " + trv1 );
        //cng
        
        
        tvec.set(IX, 7.25001);
        tvec.set(IY, 83.571);
        tvec.set(IDXDZ, 0.0220818);
        tvec.set(IDYDZ, 0.586898);
        tvec.set(IQP, -0.0116488);
        
        
        SurfZPlane szp2 = new SurfZPlane(35.0);
        VTrack t2 = new VTrack(szp2.newPureSurface(),tvec);
        if(debug) System.out.println( "Track before interacting... " + tvec );
        scatterIt.interact( t2 );
        TrackVector trv2 = t2.vector();
        if(debug) System.out.println( "Track after interacting... " + trv2 );
        
        //********************************************************************
        if(debug) System.out.println(ok_prefix + "Testing clone");
        VTrack t3 = new VTrack(t2);
        t3.setForward();
        VTrack t4 = new VTrack(t2);
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
