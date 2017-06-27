/*
 * ThinCylMsSim_Test.java
 *
 * Created on July 24, 2007, 7:55 PM
 *
 * $Id: ThinCylMsSim_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.SimInteractor;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ThinCylMsSim_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ThinCylMsSim_Test */
    public void testThinCylMsSim()
    {
        String component = "ThinCylMsSim";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        ThinCylMsSim scatterIt = new ThinCylMsSim(0.001);
        if(debug) System.out.println(scatterIt);
        
        Assert.assertTrue(scatterIt.radLength() == 0.001);
        
        SurfCylinder cyl = new SurfCylinder(20.);
        double[] pars = {1., 2., 3., 4., 5. };
        TrackVector tv = new TrackVector(pars);
        VTrack vtrk = new VTrack(cyl, tv);
        // Give the track 1GeV
        if(debug) System.out.println("\n Original VTrack: "+vtrk);
        TrackVector trv = new TrackVector( vtrk.vector() );
        trv.set(SurfCylinder.IQPT,1.0);
        vtrk.setVector( trv );
        if(debug) System.out.println("\n Modified VTrack: "+vtrk);
        scatterIt.interact( vtrk );
        if(debug) System.out.println("\n Interacted VTrack: "+vtrk);
        if(debug) System.out.println("\n Original TrackVector trv: "+trv);
        TrackVector scattered = new TrackVector( vtrk.vector() );
        Assert.assertTrue( scattered.get(SurfCylinder.IPHI) == trv.get(SurfCylinder.IPHI) );
        Assert.assertTrue( scattered.get(SurfCylinder.IZ) == trv.get(SurfCylinder.IZ) );
        Assert.assertTrue( scattered.get(SurfCylinder.IALF) != trv.get(SurfCylinder.IALF) );
        Assert.assertTrue( scattered.get(SurfCylinder.ITLM) != trv.get(SurfCylinder.ITLM) );
        Assert.assertTrue( scattered.get(SurfCylinder.IQPT) != trv.get(SurfCylinder.IQPT) );
        if(debug) System.out.println( "before interaction " + trv );
        if(debug) System.out.println( "after interaction " + scattered );
        
        //cng
        TrackVector tvec = new TrackVector();
        tvec.set(SurfCylinder.IPHI,1.0);
        tvec.set(SurfCylinder.IZ, 2.0);
        tvec.set(SurfCylinder.IALF,3.0);
        tvec.set(SurfCylinder.ITLM,4.0);
        tvec.set(SurfCylinder.IQPT,5.0);
        SurfCylinder scyl = new SurfCylinder(6.0);
        VTrack t = new VTrack(scyl.newPureSurface(),tvec);
        if(debug) System.out.println( "Track before interacting... " + t );
        scatterIt.interact( t );
        if(debug) System.out.println( "Track after interacting... " + t );
        //cng
        
        
        tvec.set(SurfCylinder.IPHI,7.25001);
        tvec.set(SurfCylinder.IZ,83.571);
        tvec.set(SurfCylinder.IALF,0.0220818);
        tvec.set(SurfCylinder.ITLM,0.586898);
        tvec.set(SurfCylinder.IQPT,-0.0116488);
        
        
        SurfCylinder scyl2 = new SurfCylinder(35.0);
        VTrack t2 = new VTrack(scyl2.newPureSurface(),tvec);
        if(debug) System.out.println( "Track before interacting... " + t2 );
        scatterIt.interact( t2 );
        if(debug) System.out.println( "Track after interacting... " + t2 );
        
        //********************************************************************
        if(debug) System.out.println(ok_prefix + "Testing clone");
        VTrack trv1 = new VTrack(t2);
        VTrack trv2 = new VTrack(t2);
        Assert.assertTrue(trv1.equals(trv2));
        // clone
        SimInteractor scatterIt2 = scatterIt.newCopy();
        // assert they are not the same object
        Assert.assertTrue( scatterIt2!=scatterIt );
        // scatter both tracks
        scatterIt2.interact(trv2);
        scatterIt.interact(trv1);
        // assert that the newly scattered tracks are not equal
        // since scattering is stochastic and we have different random
        // number generators
        Assert.assertTrue( trv1.notEquals(trv2) );
        // assert they're different from the original track
        Assert.assertTrue( trv1.notEquals(t2) );
        Assert.assertTrue( trv2.notEquals(t2) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor from ThinCylMs." );
        ThinCylMs scatter = new ThinCylMs(1.0);
        ThinCylMsSim simscatter = new ThinCylMsSim(scatter);
        Assert.assertTrue(scatter.radLength()==simscatter.radLength());
        
        
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
                
    }
    
}
