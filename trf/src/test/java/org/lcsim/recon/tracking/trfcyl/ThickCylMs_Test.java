/*
 * ThickCylMs_Test.java
 *
 * Created on July 24, 2007, 7:56 PM
 *
 * $Id: ThickCylMs_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ThickCylMs_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ThickCylMs_Test */
    public void testThickCylMs()
    {
        String component = "ThickCylMS";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        
        //********************************************************************
        
        ThickCylMs scatterIt = new ThickCylMs(1.0, 1.0);
        
        TrackError initialError = new TrackError();
        initialError.set(0,0, 0.0);
        initialError.set(1,1, 0.0);
        initialError.set(2,2, 0.0);
        initialError.set(3,3, 0.0);
        initialError.set(4,4, 0.0);
        
        initialError.set(0,2, 0.0);
        initialError.set(1,3, 0.0);
        initialError.set(3,1, 0.0);
        initialError.set(4,3, 0.0);
        initialError.set(3,4, 0.0);
        
        TrackVector trv = new TrackVector();
        trv.set(0, 1.0);  // phi
        trv.set(1, 1.0);  // z
        trv.set(2, 1.0);  // alpha
        trv.set(3, 1.0);  // tan lambda
        trv.set(4, 0.01); // q/pt
        
        Surface srf = new SurfCylinder( 100.0 );
        
        ETrack tmpTrack = new ETrack( srf, trv, initialError );
        
        tmpTrack.setError( initialError );
        scatterIt.interact( tmpTrack );
        
        //if(debug) System.out.println(endl+"Returned track: "+tmpTrack+endl+endl;
        
        
        TrackError finalError = tmpTrack.error();
        
        double path_length = 1.0;
        double rad_length = 1.0;
        //actually 1 over track momentum:
        double trackMomentum =  Math.abs(trv.get(4)*Math.cos(Math.atan( trv.get(3) )));
        double tlamSqr =  trv.get(3)*trv.get(3);
        double ThetaSqr = (0.0136)*trackMomentum*Math.sqrt(path_length/rad_length)*
                (1 + 0.038*Math.log(path_length/rad_length));
        
        ThetaSqr *= ThetaSqr;
        
        double correl = Math.sqrt(3.0)/2;
        
        double phiphi = ThetaSqr/(3*10000);
        double zz = ThetaSqr*(1 + tlamSqr)/3;
        double alfalf = ThetaSqr*( (1 + tlamSqr) + 1/(3*10000)
        - 2*correl*((1 + tlamSqr)*1/(3*10000)) );
        double tlamtlam = ThetaSqr*(1 + tlamSqr)*(1 + tlamSqr);
        double ptpt = ThetaSqr*trv.get(4)*trv.get(4)*tlamSqr;
        double pttlam = ThetaSqr*trv.get(3)*trv.get(3)*(1.0+tlamSqr);
        
        if(debug) System.out.println("Calulated Errors:"+" \n phiphi: "+phiphi+"\n zz: "+zz+"\n alfalf: "+alfalf+"\n tlamtlam: "+tlamtlam+"\n ptpt: "+ptpt);
        
        if(debug) System.out.println("ThetaSqr: "+ThetaSqr+ " trackMomentum: "+trackMomentum);
        
        if(debug) System.out.println("Track Errors \nphiphi: "+finalError.get(0,0)+"\n zz: "+finalError.get(1,1)+"\n alfalf: "+finalError.get(2,2)+"\n tlamtlam: "+finalError.get(3,3)+"\n ptpt: "+finalError.get(4,4));
        
        
        
        Assert.assertTrue(scatterIt.radLength() == 1.0);
        Assert.assertTrue(scatterIt.pathLength() == 1.0);
        Assert.assertTrue(Math.abs((finalError.get(0,0)-phiphi))<0.000001);
        Assert.assertTrue(Math.abs((finalError.get(1,1)-zz))<0.000001);
        Assert.assertTrue(Math.abs((finalError.get(2,2)-alfalf))<0.000001);
        Assert.assertTrue(Math.abs((finalError.get(3,3)-tlamtlam))<0.000001);
        Assert.assertTrue(Math.abs((finalError.get(4,4)-ptpt))<0.000001);
        
        //  Assert.assertTrue(Math.abs((finalError.get(0,2)-correl))<0.000001);
        //  Assert.assertTrue(Math.abs((finalError.get(2,0)-correl))<0.000001);
        //  Assert.assertTrue(Math.abs((finalError.get(1,3)-correl))<0.000001);
        //  Assert.assertTrue(Math.abs((finalError.get(3,1)-correl))<0.000001);
        //  Assert.assertTrue(Math.abs((finalError.get(3,4)-ptpt))<0.000001);
        //  Assert.assertTrue(Math.abs((finalError.get(4,3)-pttlam))<0.000001);
        
        //********************************************************************
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************
        
    }
    
}
