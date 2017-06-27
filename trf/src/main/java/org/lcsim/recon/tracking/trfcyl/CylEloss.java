package org.lcsim.recon.tracking.trfcyl;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;

import org.lcsim.recon.tracking.trfeloss.DeDx;

import static org.lcsim.recon.tracking.trfcyl.SurfCylinder.IALF;
import static org.lcsim.recon.tracking.trfcyl.SurfCylinder.IPHI;
import static org.lcsim.recon.tracking.trfcyl.SurfCylinder.IQPT;
import static org.lcsim.recon.tracking.trfcyl.SurfCylinder.ITLM;
import static org.lcsim.recon.tracking.trfcyl.SurfCylinder.IZ;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.Math.cos;

/**
 * Class for modifying the covariance matrix of a track which
 * has been propagated to an InteractingLayer containing
 * a LayerCylinder. The modifications correspond to energy
 *loss in a cylindrical shell whose material is
 *represented by the thickness and energy loss model
 * is represented by a DeDx class.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class CylEloss extends Interactor
{
    
    //attributes
    
    private double _thickness;
    private DeDx _dedx;
    
    //methods
    
    //
    
    /**
     *Construct an instance from the cylindrical shell's thickness and dE/dx class.
     *
     * @param   thickness The thickness of the cylindrical shell.
     * @param   dedx The DeDx model of energy loss to use.
     */
    public CylEloss(double thickness, DeDx dedx)
    {
        _thickness = thickness;
        _dedx = dedx;
    }
    
    //
    
    /**
     *Interact the given track in this cylindrical shell,
     *using the DeDx model for energy loss.
     *Note that both the track parameters and the
     *covariance matrix are updated to reflect the uncertainty caused
     *by traversing the cylindrical shell of material.
     *
     * @param   tre The ETrack to scatter.
     */
    public void interact(ETrack tre)
    {
        // This can only be used with cylinders... check that we have one..
        
        SurfCylinder cyl = new SurfCylinder(10.0);
        Surface srf = tre.surface();
        Assert.assertTrue( srf.pureType().equals(cyl.pureType()) );
        
        TrackError cleanError = tre.error();
        TrackError newError = new TrackError(cleanError);
        
        TrackVector theVec = tre.vector();
        TrackVector newVector = new TrackVector(theVec);
        
        double pionMass = 0.13957; // GeV
        double ptmax = 10000.; // GeV
        
        double pinv = Math.abs(theVec.get(SurfCylinder.IQPT)*Math.cos(Math.atan(theVec.get(SurfCylinder.ITLM))));
        
        // make sure pinv is greater than a threshold (1/ptmax)
        // in this case assume q = 1, otherwise q = q/pt/abs(q/pt)
        
        double sign = 1;
        if(pinv < 1./ptmax)
            pinv = 1./ptmax;
        else
            sign = theVec.get(SurfCylinder.IQPT)/Math.abs(theVec.get(SurfCylinder.IQPT));
        
        // Evaluate the initial energy assuming the particle is a pion.
        
        double trackEnergy = Math.sqrt(1./pinv/pinv+pionMass*pionMass);
        
        double trueLength = _thickness/Math.abs(Math.cos(theVec.get(SurfCylinder.IALF)))/
                Math.cos(Math.atan(theVec.get(SurfCylinder.ITLM)));
        // assume the energy loss distribution to be Gaussian
        double stdEnergy = _dedx.sigmaEnergy(trackEnergy, trueLength);
        
        double stdMomentum = stdEnergy;
        // What direction are we going?
        // If forward, that means we lose energy
        // backwards means gain energy
        if(tre.isTrackBackward()) trueLength = -trueLength;
        trackEnergy = _dedx.loseEnergy(trackEnergy, trueLength);
        
        double newMomentum = trackEnergy>pionMass ?
            Math.sqrt(trackEnergy*trackEnergy-
                pionMass*pionMass): 1./pinv;
        
        // Only vec(SurfCylinder.IQPT) changes due to E loss.
        newVector.set(SurfCylinder.IQPT, 1./newMomentum/Math.cos(Math.atan(theVec.get(SurfCylinder.ITLM)))*sign);
        
        // Also only error(SurfCylinder.IQPT,IQPT) changes.
        double stdVec = theVec.get(SurfCylinder.IQPT)*theVec.get(SurfCylinder.IQPT)*stdMomentum*Math.cos(Math.atan(
                theVec.get(SurfCylinder.ITLM)));
        stdVec *= stdVec;
        newError.set(SurfCylinder.IQPT, SurfCylinder.IQPT,  newError.get(SurfCylinder.IQPT, SurfCylinder.IQPT) + stdVec);
        
        tre.setVectorAndKeepDirection(newVector);
        tre.setError( newError );
        
    }
    
    public void interact_dir(ETrack theTrack, PropDir direction )
    {
        // This can only be used with cylinders... check that we have one..
        
        Surface srf = theTrack.surface();
        Assert.assertTrue( srf instanceof SurfCylinder );
        
        // Reduced direction must be forward or backward.
        
        Propagator.reduceDirection(direction);
        Assert.assertTrue(direction == PropDir.FORWARD || direction == PropDir.BACKWARD);
        
        TrackError cleanError = theTrack.error();
        TrackError newError = new TrackError(cleanError);
        
        TrackVector theVec = theTrack.vector();
        TrackVector newVector = new TrackVector(theVec);
        
        double pionMass = 0.13957; // GeV
        double ptmax = 10000.; // GeV
        
        double tanlm = theVec.get(ITLM);
        double coslm = 1. / sqrt(1. + tanlm*tanlm);
        double pinv = abs(theVec.get(IQPT)*coslm);
        
        // make sure pinv is greater than a threshold (1/ptmax)
        // in this case assume q = 1, otherwise q = q/pt/abs(q/pt)
        
        int sign = 1;
        if ( pinv < 1./ptmax )
            pinv = 1./ptmax;
        else
            sign = (int) (theVec.get(IQPT)/abs(theVec.get(IQPT)));
        
        // Evaluate the initial energy assuming the particle is a pion.
        
        double trackEnergy = sqrt(1./pinv/pinv+pionMass*pionMass);
        
        double trueLength = _thickness/abs(cos(theVec.get(IALF)))/coslm;
        
        // assume the energy loss distribution to be Gaussian
        double stdEnergy = _dedx.sigmaEnergy(trackEnergy, trueLength);
        
        double stdMomentum = stdEnergy;
        if(direction == PropDir.BACKWARD)
            trueLength = -trueLength;
        _dedx.loseEnergy(trackEnergy, trueLength);
        
        double newMomentum = trackEnergy>pionMass ?
            sqrt(trackEnergy*trackEnergy-
                pionMass*pionMass): 1./pinv;
        
        // Only vec(IQPT) changes due to E loss.
        newVector.set(IQPT, 1./newMomentum/coslm*sign);
        
        // Also only error(IQPT,IQPT) changes.
        double stdVec = theVec.get(IQPT)*theVec.get(IQPT)*stdMomentum*coslm;
        stdVec *= stdVec;
        newError.set(IQPT, IQPT, newError.get(IQPT, IQPT) + stdVec);
        
// TODO introduce axial definition to ETrack
//  // Axial track?
//  if(theTrack.is_axial()) {
//    newError(IPHI,IZ) = 0.;
//    newError(IZ,IZ) = 0.;
//    newError(IALF,IZ) = 0.;
//    newError(ITLM,IZ) = 0.;
//    newError(IQPT,IZ) = 0.;
//    newError(IPHI,ITLM) = 0.;
//    newError(IALF,ITLM) = 0.;
//    newError(ITLM,ITLM) = 0.;
//    newError(IQPT,ITLM) = 0.;
//  }
        
        theTrack.setVector(newVector);
        theTrack.setError( newError );
        
    }
    
//
    
    /**
     *Return the thickness of material in the cylindrical shell.
     *
     * @return The thickness of the  energy loss material.
     */
    public double thickness()
    {
        return _thickness;
    }
    
//
    
    /**
     *Return the energy loss model used in this Interactor.
     *
     * @return The DeDx class representing energy loss.
     */
    public DeDx dEdX()
    {
        return _dedx; //cng shallow copy!
    }
    
//
    
    /**
     *Make a clone of this object.
     *
     * @return A Clone of this instance.
     */
    public Interactor newCopy()
    {
        return new CylEloss(_thickness, _dedx);
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "CylEloss with thickness "+_thickness+" and energy loss "+_dedx;
    }
}