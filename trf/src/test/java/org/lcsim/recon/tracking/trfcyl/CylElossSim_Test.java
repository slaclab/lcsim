/*
 * CylElossSim_Test.java
 *
 * Created on July 24, 2007, 8:57 PM
 *
 * $Id: CylElossSim_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfeloss.DeDx;
import org.lcsim.recon.tracking.trfeloss.DeDxFixed;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class CylElossSim_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of CylElossSim_Test */
    public void testCylElossSim()
    {
        String component = "CylElossSim";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        TrackVector trv = new TrackVector();
        trv.set(0, 1.0);
        trv.set(1, 1.0);
        trv.set(2, 0.0);
        trv.set(3, 0.0);
        trv.set(4, 1.0);
        Surface srf = new SurfCylinder( 20.0 );
        
        double density = 1.0; // g/cm^3
        double thickness = 1.0; // cm
        
        DeDx dedx = new DeDxFixed(density);
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        {
            CylElossSim passIt = new CylElossSim(thickness, dedx);
            
            Assert.assertTrue(passIt.thickness() == thickness);
            Assert.assertTrue(passIt.dEdX().equals(dedx));
            
            VTrack tmpTrack = new VTrack( srf, trv);
            tmpTrack.setTrackForward();
            
            TrackVector initialVector = tmpTrack.vector();
            if(debug) System.out.println("trackvector before interaction "+initialVector);
            passIt.interact( tmpTrack );
            TrackVector finalVector = tmpTrack.vector();
            if(debug) System.out.println("trackvector after interaction "+finalVector);
            
            //Ensure that we lose energy going forward
            Assert.assertTrue(finalVector.get(4)<initialVector.get(4));
            
            VTrack bTrack = new VTrack( srf, trv);
            bTrack.setTrackBackward();
            if(debug) System.out.println("backwards track= "+bTrack);
            
            initialVector = bTrack.vector();
            if(debug) System.out.println("trackvector before interaction "+initialVector);
            passIt.interact( bTrack );
            finalVector = bTrack.vector();
            if(debug) System.out.println("trackvector after interaction "+finalVector);
            //Ensure that we gain energy going backward
            if(debug) System.out.println(finalVector.get(4)+" "+initialVector.get(4));
            Assert.assertTrue(finalVector.get(4)>initialVector.get(4));
        }
        
        //test constructor from Interactor
        if(debug) System.out.println( ok_prefix + "Test constructor from Interactor." );
        CylEloss passIt = new CylEloss(thickness, dedx);
        CylElossSim simIt = new CylElossSim(passIt);
        VTrack track0 = new VTrack( srf, trv);
        VTrack tmpTrack = new VTrack( srf, trv);
        tmpTrack.setTrackBackward();
        if(debug) System.out.println(" \nTrack before simulation "+tmpTrack);
        simIt.interact( tmpTrack );
        if(debug) System.out.println(" Track after simulation "+tmpTrack);
        TrackError initialError = new TrackError();
        ETrack tmpETrack = new ETrack( tmpTrack, initialError );
        //Should have gained energy going backwards...
        if(debug) System.out.println(" Backward Track0: "+track0.vector(4)+" tmpTrack: "+tmpTrack.vector(4));
        
        Assert.assertTrue(track0.vector(4)<tmpTrack.vector(4));
        
        //Reverse direction and lose it back...
        if(debug) System.out.println("\n    Track should now be forward: ");
        tmpETrack.setTrackForward();
        simIt.interact( tmpETrack );
        
        if(debug) System.out.println(" Track after interaction "+tmpETrack);
        //will need to change limit if we change track or scatterer parameters
        //equality only in small energy loss limit.
        if(debug) System.out.println(" Forward Track0: "+track0.vector(4)+" tmpTrack: "+tmpETrack.vector(4));
        Assert.assertTrue( Math.abs(track0.vector(4)-tmpETrack.vector(4)) < 1e-8);
        
        Assert.assertTrue( simIt.thickness()==passIt.thickness() );
        Assert.assertTrue( simIt.dEdX().equals(passIt.dEdX()) );
        
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
    }
    
}
