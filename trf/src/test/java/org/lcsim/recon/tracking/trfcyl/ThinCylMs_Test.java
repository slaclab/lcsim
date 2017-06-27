/*
 * ThinCylMs_Test.java
 *
 * Created on July 24, 2007, 7:54 PM
 *
 * $Id: ThinCylMs_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ThinCylMs_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ThinCylMs_Test */
    public void testThinCylMs()
    {
         String component = "ThinCylMs";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        ThinCylMs scatterIt = new ThinCylMs(1.0);
        
        TrackError initialError = new TrackError();
        initialError.set(2,2 , 0.0);
        initialError.set(3,3 , 0.0);
        initialError.set(4,4 , 0.0);
        initialError.set(4,3 , 0.0);
        initialError.set(3,4 , 0.0);
        
        TrackVector trv = new TrackVector();
        trv.set(0 , 1.0);
        trv.set(1 , 1.0);
        trv.set(2 , 1.0);
        trv.set(3 , 1.0);
        trv.set(4 , 1.0);
        
        Surface srf = new SurfCylinder( 20.0 );
        
        ETrack tmpTrack = new ETrack( srf, trv, initialError );
        
        tmpTrack.setError( initialError );
        scatterIt.interact( tmpTrack );
        
        TrackError finalError = tmpTrack.error();
        double trueLength = 1.0/Math.cos(1.0)*Math.sqrt(1.0 + 1.0);
        double thetaSqr = (0.0136)*(1.0/Math.cos(Math.atan(1.0)))*Math.sqrt(trueLength)*
                (1 + 0.038*Math.log(trueLength));
        thetaSqr *= thetaSqr;
        
        double alfalf = (1.0 + 1.0);
        double tlamtlam = (alfalf*alfalf);
        double cc = (1.0);
        double ctlam = ((1.0)*(1.0 + 1.0));
        double tlamc = (ctlam);
        
        alfalf *= thetaSqr;
        tlamtlam *= thetaSqr;
        cc *= thetaSqr;
        ctlam *= thetaSqr;
        tlamc *= thetaSqr;
        
        Assert.assertTrue(scatterIt.radLength() == 1.0);
        Assert.assertTrue((finalError.get(2,2)-alfalf)<0.000001);
        Assert.assertTrue((finalError.get(3,3)-tlamtlam)<0.000001);
        Assert.assertTrue((finalError.get(4,4)-cc)<0.000001);
        Assert.assertTrue((finalError.get(3,4)-ctlam)<0.000001);
        Assert.assertTrue((finalError.get(4,3)-tlamc)<0.000001);
        
        //********************************************************************
        if(debug) System.out.println(ok_prefix + "Testing clone");
        ETrack tre1 = new ETrack(tmpTrack);
        ETrack tre2 = new ETrack(tmpTrack);
        Assert.assertTrue(tre1.equals(tre2));
        // clone
        Interactor scatterIt2 = scatterIt.newCopy();
        // assert they are not the same object
        Assert.assertTrue( scatterIt2!=scatterIt );
        // scatter both tracks
        scatterIt2.interact(tre2);
        scatterIt.interact(tre1);
        // assert they're now equal
        Assert.assertTrue( tre1.equals(tre2) );
        // assert they're different from the original track
        Assert.assertTrue( tre1.notEquals(tmpTrack) );
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************      
    }
    
}
