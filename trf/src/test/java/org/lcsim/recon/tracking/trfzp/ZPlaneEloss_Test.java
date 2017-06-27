/*
 * ZPlaneEloss_Test.java
 *
 * Created on September 10, 2007, 11:05 AM
 *
 * $Id: ZPlaneEloss_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfeloss.DeDx;
import org.lcsim.recon.tracking.trfeloss.DeDxFixed;


import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
/**
 *
 * @author Norman Graf
 */
public class ZPlaneEloss_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ZPlaneEloss_Test */
    public void testZPlaneEloss()
    {
        String component = "ZPlaneEloss";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        TrackVector trv = new TrackVector();
        trv.set(0,1.0);
        trv.set(1, 1.0);
        trv.set(2, 0.1);
        trv.set(3, 0.2);
        trv.set(4, 0.0);
        
        double density = 1.0; // g/cm^3
        double thickness = 1.0; // cm
        
        DeDx dedx = new DeDxFixed(density);
        ZPlaneEloss passIt = new ZPlaneEloss(thickness, dedx);
        
        TrackError initialError = new TrackError();
        
        Surface srf = new SurfZPlane( 20.0 );
        
        ETrack tmpTrack = new ETrack( srf, trv, initialError );
        
        tmpTrack.setError( initialError );
        
        TrackVector initialVector = tmpTrack.vector();
        passIt.interact_dir( tmpTrack, PropDir.FORWARD );
        
        TrackError finalError = tmpTrack.error();
        TrackVector finalVector = tmpTrack.vector();
        
        double particleMass = 0.13957;
        double ptmax = 10000.; // GeV
        
        double pinv = abs(trv.get(4));
        
        // make sure pinv is greater than a threshold (1/ptmax)
        // in this case assume q = 1, otherwise q = q/pt/abs(q/pt)
        
        int sign = 1;
        if(pinv < 1./ptmax)
            pinv = 1./ptmax;
        else
            sign = (int) (trv.get(4)/abs(trv.get(4)));
        
        double initialEnergy = sqrt(1./pinv/pinv + particleMass*particleMass);
        double finalEnergy = initialEnergy;
        
        double d = thickness * sqrt(1. + trv.get(2)*trv.get(2) + trv.get(3)*trv.get(3));
        
        dedx.loseEnergy(finalEnergy, d);
        double sigmaEnergy = dedx.sigmaEnergy(initialEnergy, d);
        
        if(finalEnergy<particleMass) finalEnergy=initialEnergy;
        
        // now evaluate the final q/p and error(4,4)
        double finalQoverP = sign/sqrt(finalEnergy*finalEnergy - particleMass*particleMass);
        double finalEr = sigmaEnergy*trv.get(4)*trv.get(4);
        finalEr *= finalEr;
        
        assertTrue(abs(finalVector.get(4)-finalQoverP)<0.00001);
        assertTrue(abs(finalError.get(4,4)-finalEr)<0.00001);
        
    }
    
}
