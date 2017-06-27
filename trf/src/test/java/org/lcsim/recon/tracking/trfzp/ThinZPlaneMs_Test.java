/*
 * ThinZPlaneMs_Test.java
 *
 * Created on July 24, 2007, 10:57 PM
 *
 * $Id: ThinZPlaneMs_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

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
public class ThinZPlaneMs_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ThinZPlaneMs_Test */
    public void testThinZPlaneMs()
    {
        String component = "ThinZPlaneMS";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        
        ThinZPlaneMs scatterIt = new ThinZPlaneMs(1.0);
        
        if(debug) System.out.println( scatterIt);
        
        TrackError initialError = new TrackError();
        initialError.set(2,2, 0.0);
        initialError.set(3,3, 0.0);
        initialError.set(4,4, 0.0);
        initialError.set(4,3, 0.0);
        initialError.set(3,4, 0.0);
        
        TrackVector trv = new TrackVector();
        trv.set(0, 1.0);
        trv.set(1, 1.0);
        trv.set(2, 1.0);
        trv.set(3, 1.0);
        trv.set(4, 1.0);
        
        Surface srf = new SurfZPlane( 20.0 );
        
        ETrack tmpTrack = new ETrack( srf, trv, initialError );
        
        tmpTrack.setError( initialError );
        if(debug) System.out.println("Starting Track= \n"+tmpTrack);
        scatterIt.interact( tmpTrack );
        if(debug) System.out.println("Scattered Track= \n"+tmpTrack);
        TrackError finalError = tmpTrack.error();
        
        double f = trv.get(2);
        double g = trv.get(3);
        
        double pi = Math.acos(-1.0);
        double theta = Math.atan(Math.sqrt(f*f + g*g));
        double phi = Math.atan(Math.sqrt((g*g)/(f*f)));
        
        double xhat = Math.sin(theta)*Math.cos(phi);
        double yhat = Math.sin(theta)*Math.sin(phi);
        double zhat = Math.sqrt(1 - Math.sin(theta)*Math.sin(theta));
        
        double radLength = scatterIt.radLength();
        
        double trueLength = radLength/Math.cos(theta);
        
        double thetaSqr = (0.0136)*1.0*Math.sqrt(trueLength)*
                (1 + 0.038*Math.log(trueLength));
        thetaSqr *= thetaSqr;
        
        double norm = Math.sqrt(xhat*xhat + yhat*yhat);
        
        double dxzxz = (yhat/(norm*zhat))*(yhat/(norm*zhat));
        dxzxz += Math.pow((xhat/norm)*(1 + (norm*norm)/(zhat*zhat)),2.0);
        double dyzyz = (xhat/(norm*zhat))*(xhat/(norm*zhat));
        dyzyz += Math.pow((yhat/norm)*(1 + (norm*norm)/(zhat*zhat)),2.0);
        double dxzyz = -xhat*yhat/(zhat*zhat);
        dxzyz += xhat*yhat/(norm*norm)*Math.pow((1 + (norm*norm)/(zhat*zhat)),2.0);
        
        dxzxz *= thetaSqr;
        dyzyz *= thetaSqr;
        dxzyz *= thetaSqr;
        
        Assert.assertTrue(scatterIt.radLength() == 1.0);
        Assert.assertTrue(finalError.get(0,0)==0.0);
        Assert.assertTrue(finalError.get(1,1)==0.0);
        Assert.assertTrue((finalError.get(2,2)<(dxzxz+.00001))&&((finalError.get(2,2)>(dxzxz-.00001))));
        Assert.assertTrue((finalError.get(3,3)<(dyzyz+.00001))&&((finalError.get(3,3)>(dyzyz-.00001))));
        Assert.assertTrue(finalError.get(4,4)==0.0);
        Assert.assertTrue((finalError.get(2,3)<(dxzyz+.00001))&&((finalError.get(2,3)>(dxzyz-.00001))));
        Assert.assertTrue(finalError.get(2,3)==finalError.get(3,2));
        
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
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
