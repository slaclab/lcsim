/*
 * XYPlaneEloss.java
 *
 * Created on September 10, 2007, 11:17 AM
 *
 * $Id: XYPlaneEloss.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfeloss.DeDx;
import org.lcsim.recon.tracking.trfutil.Assert;

import static org.lcsim.recon.tracking.trfxyp.SurfXYPlane.IV;
import static org.lcsim.recon.tracking.trfxyp.SurfXYPlane.IZ;
import static org.lcsim.recon.tracking.trfxyp.SurfXYPlane.IDVDU;
import static org.lcsim.recon.tracking.trfxyp.SurfXYPlane.IDZDU;
import static org.lcsim.recon.tracking.trfxyp.SurfXYPlane.IQP;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 *
 * @author Norman Graf
 */
public class XYPlaneEloss extends Interactor
{
    //attributes
    
    private double _thickness;
    private DeDx _dedx;
    
    /** Creates a new instance of XYPlaneEloss */
    public XYPlaneEloss(double thickness,   DeDx dedx)
    {
        _thickness = thickness;
        _dedx = dedx;
    }
    
    
    public void interact(ETrack tre)
    {
    }
    
    
    public void interact_dir(ETrack theTrack, PropDir direction )
    {
        // This can only be used with xy-planes... check that we have one..
        
        Surface srf = theTrack.surface();
        Assert.assertTrue( srf instanceof SurfXYPlane);
        
        // Reduced direction must be forward or backward.
        
        // Reduced direction must be forward or backward.
        Propagator.reduceDirection(direction);
        Assert.assertTrue(direction == PropDir.FORWARD || direction == PropDir.BACKWARD);
        
        
        TrackError cleanError = theTrack.error();
        
        
        TrackError newError = new TrackError(cleanError);
        
        TrackVector theVec = theTrack.vector();
        TrackVector newVector = new TrackVector(theVec);
        
        double pionMass = 0.13957; // GeV
        double ptmax = 10000.; // GeV
        
        
        double pinv = abs(theVec.get(IQP));
        double dvdu = theVec.get(IDVDU);
        double dzdu = theVec.get(IDZDU);
        double lfac = sqrt(1. + dvdu*dvdu + dzdu*dzdu);
        
        // make sure pinv is greater than a threshold (1/ptmax)
        // in this case assume q = 1, otherwise q = q/p/abs(q/p)
        
        double sign = 1.;
        if ( pinv < 1./ptmax )
            pinv = 1./ptmax;
        else
            sign = theVec.get(IQP) > 0. ? 1. : -1.;
        
        // Evaluate the initial energy assuming the particle is a pion.
        
        double trackEnergy = sqrt(1./(pinv*pinv) + pionMass*pionMass);
        
        double trueLength = lfac * _thickness;
        
        // assume the energy loss distribution to be Gaussian
        double stdEnergy = _dedx.sigmaEnergy(trackEnergy, trueLength);
        
        if(direction == PropDir.BACKWARD) trueLength = -trueLength;
        _dedx.loseEnergy(trackEnergy, trueLength);
        
        double newMomentum = trackEnergy>pionMass ?
            sqrt(trackEnergy*trackEnergy - pionMass*pionMass) :
            1./pinv;
        
        // Only vec(IQP) changes due to E loss.
        newVector.set(IQP, sign/newMomentum);
        
        // Also only error(IQP,IQP) changes.
        double stdVec = theVec.get(IQP)*theVec.get(IQP)*stdEnergy;
        stdVec *= stdVec;
        newError.set(IQP, IQP, newError.get(IQP, IQP) + stdVec);
        
        // Store modified track parameters.
        theTrack.setVectorAndKeepDirection(newVector);
        theTrack.setError( newError );
        
    }
    
    
    /**
     * Return the thickness of material in the z plane.
     *
     * @return The thickness of the energy loss material.
     */
    public double thickness()
    {
        return _thickness;
    }
    
    /**
     *Return the energy loss model used in this Interactor.
     *
     * @return The DeDx class representing energy loss.
     */
    public DeDx dEdX()
    {
        return _dedx; //cng shallow copy!
    }
    
    /**
     *Make a clone of this object.
     *
     * @return A Clone of this instance.
     */
    public Interactor newCopy()
    {
        return new XYPlaneEloss(_thickness, _dedx);
    }
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "XYPlaneEloss with thickness "+_thickness+" and energy loss "+_dedx;
    }
    
    
    
}
