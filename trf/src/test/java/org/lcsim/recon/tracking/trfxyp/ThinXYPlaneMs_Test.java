/*
 * ThinXYPlaneMs_Test.java
 *
 * Created on July 24, 2007, 10:12 PM
 *
 * $Id: ThinXYPlaneMs_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

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
public class ThinXYPlaneMs_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ThinXYPlaneMs_Test */
    public void testThinXYPlaneMs()
    {
        String component = "ThinXYPlaneMs";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        
        //********************************************************************
        
        ThinXYPlaneMs scatterIt = new ThinXYPlaneMs(1.0);
        if(debug) System.out.println(scatterIt);
        
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
        
        Surface srf = new SurfXYPlane( 20.0, 0.0 );
        
        ETrack tmpTrack = new ETrack( srf, trv, initialError );
        if(debug) System.out.println("initialError= "+initialError);
        
        tmpTrack.setError( initialError );
        tmpTrack.setForward();
        
        if(debug) System.out.println("tmpTrack before scattering= "+tmpTrack);
        if(debug) System.out.println("TrackError before = "+tmpTrack.error());
        
        
        scatterIt.interact( tmpTrack );
        TrackError finalError = tmpTrack.error();
        
        if(debug) System.out.println("tmpTrack after scattering= "+tmpTrack);
        if(debug) System.out.println("TrackError after = "+tmpTrack.error());
        if(debug) System.out.println("TrackError(2,3) after = "+tmpTrack.error().get(3,2)+" " +tmpTrack.error().get(2,3));
        
        double f = trv.get(2);
        double g = trv.get(3);
        
        double theta = Math.atan(Math.sqrt(f*f + g*g));
        double phi = Math.atan(Math.sqrt((g*g)/(f*f)));
        
        double vhat = Math.sin(theta)*Math.cos(phi);
        double zhat = Math.sin(theta)*Math.sin(phi);
        double uhat = Math.sqrt(1 - Math.sin(theta)*Math.sin(theta));
        
        double radLength = scatterIt.radLength();
        
        double trueLength = radLength/Math.cos(theta);
        
        double thetaSqr = (0.0136)*1.0*Math.sqrt(trueLength)*
                (1 + 0.038*Math.log(trueLength));
        thetaSqr *= thetaSqr;
        
        double norm = Math.sqrt(vhat*vhat + uhat*uhat);
        
        double dvuvu = (zhat/(norm*uhat))*(zhat/(norm*uhat));
        dvuvu += Math.pow((vhat/norm)*(1 + (norm*norm)/(uhat*uhat)),2.0);
        double dzuzu = (vhat/(norm*uhat))*(vhat/(norm*uhat));
        dzuzu += Math.pow((zhat/norm)*(1 + (norm*norm)/(uhat*uhat)),2.0);
        double dvuzu = -vhat*zhat/(uhat*uhat);
        dvuzu += vhat*zhat/(norm*norm)*Math.pow((1 + (norm*norm)/(uhat*uhat)),2.0);
        
        dvuvu *= thetaSqr;
        dzuzu *= thetaSqr;
        dvuzu *= thetaSqr;
        
        Assert.assertTrue(scatterIt.radLength() == 1.0);
        Assert.assertTrue(finalError.get(0,0)==0.0);
        Assert.assertTrue(finalError.get(1,1)==0.0);
        if(debug) System.out.println(finalError.get(2,2));
        Assert.assertTrue((finalError.get(2,2)<(dvuvu+.00001))&&((finalError.get(2,2)>(dvuvu-.00001))));
        Assert.assertTrue((finalError.get(3,3)<(dzuzu+.00001))&&((finalError.get(3,3)>(dzuzu-.00001))));
        Assert.assertTrue(finalError.get(4,4)==0.0);
        Assert.assertTrue((finalError.get(2,3)<(dvuzu+.00001))&&((finalError.get(2,3)>(dvuzu-.00001))));
        Assert.assertTrue(finalError.get(2,3)==finalError.get(3,2));
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************
        
    }
    
}
