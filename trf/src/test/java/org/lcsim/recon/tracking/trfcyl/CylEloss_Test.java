/*
 * CylEloss_Test.java
 *
 * Created on July 24, 2007, 8:55 PM
 *
 * $Id: CylEloss_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfeloss.DeDx;
import org.lcsim.recon.tracking.trfeloss.DeDxFixed;
import org.lcsim.recon.tracking.trfutil.Assert;

import static java.lang.Math.cos;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.sqrt;

/**
 *
 * @author Norman Graf
 */
public class CylEloss_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of CylEloss_Test */
    public void testCylEloss()
    {
        String component = "CylEloss";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        {
            TrackVector trv = new TrackVector();
            trv.set(0, 1.0);
            trv.set(1, 1.0);
            trv.set(2, 0.0);
            trv.set(3, 0.0);
            trv.set(4, 2.0);
            
            double density = 1.0; // g/cm^3
            double thickness = 2.; // cm
            
            DeDx dedx = new DeDxFixed(density);
            if(debug) System.out.println( ok_prefix + "Test constructor." );
            CylEloss passIt = new CylEloss(thickness, dedx);
            if(debug) System.out.println(passIt);
            Assert.assertTrue(passIt.thickness() == thickness);
            Assert.assertTrue(passIt.dEdX().equals(dedx));
            
            TrackError initialError = new TrackError();
            
            Surface srf = new SurfCylinder( 20.0 );
            
            ETrack tmpTrack = new ETrack( srf, trv, initialError );
            
            tmpTrack.setError( initialError );
            
            TrackVector initialVector = tmpTrack.vector();
            passIt.interact( tmpTrack );
            
            TrackError finalError = tmpTrack.error();
            TrackVector finalVector = tmpTrack.vector();
            
            double particleMass = 0.13957;
            double ptmax = 10000.; // GeV
            
            double pinv = Math.abs(trv.get(4))*Math.cos(Math.atan(trv.get(3)));
            
            // make sure pinv is greater than a threshold (1/ptmax)
            // in this case assume q = 1, otherwise q = q/pt/abs(q/pt)
            
            double sign = 1;
            if(pinv < 1./ptmax)
                pinv = 1./ptmax;
            else
                sign = trv.get(4)/Math.abs(trv.get(4));
            
            double initialEnergy = Math.sqrt(1./pinv/pinv
                    +particleMass*particleMass);
            double finalEnergy = initialEnergy;
            
            double d = thickness/Math.abs(Math.cos(trv.get(2)))/Math.cos(Math.atan(trv.get(3)));
            
            finalEnergy = dedx.loseEnergy(finalEnergy, d);
            double sigmaEnergy = dedx.sigmaEnergy(initialEnergy, d);
            double sigmaMomentum = sigmaEnergy;
            
            if(finalEnergy<particleMass)finalEnergy=initialEnergy;
            
            // now evaluate the final q/p and error(4,4)
            double finalQoverP = sign/Math.sqrt(finalEnergy*finalEnergy-
                    particleMass*particleMass)/Math.cos(Math.atan(trv.get(3)));
            double finalEr = sigmaMomentum*trv.get(4)*trv.get(4)*Math.cos(Math.atan(trv.get(3)));
            finalEr *= finalEr;
            
            if(debug) System.out.println("initial E= "+1/initialVector.get(4));
            if(debug) System.out.println("interacted E= "+1/finalVector.get(4)+ " calculated E= "+1/finalQoverP);
            Assert.assertTrue(Math.abs(finalVector.get(4)-finalQoverP)<0.00001);
            Assert.assertTrue(Math.abs(finalError.get(4,4)-finalEr)<0.00001);
            
            //********************************************************************
            {
                if(debug) System.out.println(ok_prefix + "Testing directionality");
                ETrack tre1 = new ETrack(tmpTrack);
                ETrack tre2 = new ETrack(tmpTrack);
                if(debug) System.out.println("Track before interaction "+tmpTrack);
                tre1.setForward();
                tre1.setTrackForward();
                passIt.interact(tre1);
                if(debug) System.out.println("Track after forward interaction "+tre1);
                //Should lose energy going forward
                Assert.assertTrue(tre1.vector(4)>tmpTrack.vector(4));
                tre2.setBackward();
                tre2.setTrackBackward();
                passIt.interact(tre2);
                //Should gain energy going backward
                Assert.assertTrue(tre2.vector(4)<tmpTrack.vector(4));
                if(debug) System.out.println("Track after backward interaction "+tre2);
            }
            //********************************************************************
            {
                if(debug) System.out.println(ok_prefix + "Testing clone");
                ETrack tre1 = new ETrack(tmpTrack);
                ETrack tre2 = new ETrack(tmpTrack);
                Assert.assertTrue(tre1.equals(tre2));
                // clone
                Interactor passIt2 = passIt.newCopy();
                // assert they are not the same object
                Assert.assertTrue( passIt2!=passIt );
                // scatter both tracks
                passIt2.interact(tre2);
                passIt.interact(tre1);
                // assert they're now equal
                Assert.assertTrue( tre1.equals(tre2) );
                // assert they're different from the original track
                Assert.assertTrue( tre1.notEquals(tmpTrack) );
            }
        }
                //********************************************************************
                
                {
                    if(debug) System.out.println(ok_prefix + "Testing directionality");
                    TrackVector trv = new TrackVector();
                    trv.set(0, 1.0);
                    trv.set(1, 1.0);
                    trv.set(2, 0.0);
                    trv.set(3, 0.0);
                    trv.set(4, 0.0);
                    
                    double density = 1.0; // g/cm^3
                    double thickness = 1.0; // cm
                    
                    DeDx dedx = new DeDxFixed(density);
                    CylEloss passIt = new CylEloss(thickness, dedx);
                    
                    TrackError initialError = new TrackError();
                    
                    Surface srf = new SurfCylinder( 20.0 );
                    
                    ETrack tmpTrack = new ETrack( srf, trv, initialError );
                    
                    tmpTrack.setError( initialError );
                    
                    TrackVector initialVector = tmpTrack.vector();
                    passIt.interact_dir( tmpTrack, PropDir.FORWARD );
                    
                    TrackError finalError = tmpTrack.error();
                    TrackVector finalVector = tmpTrack.vector();
                    
                    double particleMass = 0.13957;
                    double ptmax = 10000.; // GeV
                    
                    double pinv = abs(trv.get(4))*cos(atan(trv.get(3)));
                    
                    // make sure pinv is greater than a threshold (1/ptmax)
                    // in this case assume q = 1, otherwise q = q/pt/abs(q/pt)
                    
                    int sign = 1;
                    if(pinv < 1./ptmax)
                        pinv = 1./ptmax;
                    else
                        sign = (int) (trv.get(4)/abs(trv.get(4)));
                    
                    double initialEnergy = sqrt(1./pinv/pinv
                            +particleMass*particleMass);
                    double finalEnergy = initialEnergy;
                    
                    double d = thickness/abs(cos(trv.get(2)))/cos(atan(trv.get(3)));
                    
                    dedx.loseEnergy(finalEnergy, d);
                    double sigmaEnergy = dedx.sigmaEnergy(initialEnergy, d);
                    double sigmaMomentum = sigmaEnergy;
                    
                    if(finalEnergy<particleMass)finalEnergy=initialEnergy;
                    
                    // now evaluate the final q/p and error(4,4)
                    double finalQoverP = sign/sqrt(finalEnergy*finalEnergy-
                            particleMass*particleMass)/cos(atan(trv.get(3)));
                    double finalEr = sigmaMomentum*trv.get(4)*trv.get(4)*cos(atan(trv.get(3)));
                    finalEr *= finalEr;
                    
                    Assert.assertTrue(abs(finalVector.get(4)-finalQoverP)<0.00001);
                    Assert.assertTrue(abs(finalError.get(4,4)-finalEr)<0.00001);
                    
                }
        
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
    }
    
}
