package org.lcsim.recon.tracking.trfcyl;


import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;

/**
 * This class modifies the covariance matrix of a track
 *corresponding to multiple
 *scattering in a thin cylindrical shell whose material is
 *represented by the number of radiation lengths.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class ThinCylMs extends Interactor
{
    
    private double _radLength;
    
    //
    
    /**
     * Construct an instance from the number of radiation
     * lengths of the cylindrical shell material.
     * The Interactor is constructed with the
     * appropriate number of radiation lengths.
     *
     * @param   radLength The thickness of the material in radiation lengths.
     */
    public ThinCylMs(double radLength)
    {
        _radLength = radLength;
    }
    
    //
    
    /**
     *Interact the given track in this cylindrical shell,
     *using the thin material approximation for multiple scattering.
     *Note that the track parameters are not modified. Only the
     *covariance matrix is updated to reflect the uncertainty caused
     *by traversing the thin cylindrical shell of material.
     *
     * @param   tre The ETrack to scatter.
     */
    public void interact(ETrack tre)
    {
        // This can only be used with cylinders... check that we have one..
        
        TrackError cleanError = tre.error();
        TrackError newError = cleanError;
        
        // set the rms scattering appropriate to this momentum
        
        // Theta = (0.0136 GeV)*(z/p)*(sqrt(rad_length))*(1+0.038*log(rad_length))
        
        
        
        TrackVector theVec = tre.vector();
        double trackMomentum = Math.abs(theVec.get(SurfCylinder.IQPT)*Math.cos(Math.atan(theVec.get(SurfCylinder.ITLM))));
        
        double trueLength = _radLength/Math.abs(Math.cos(theVec.get(SurfCylinder.IALF)))*Math.sqrt(1.0 +
                theVec.get(SurfCylinder.ITLM)*theVec.get(SurfCylinder.ITLM));
        
        double stdTheta = (0.0136)*trackMomentum*Math.sqrt(trueLength)*
                (1 + 0.038*Math.log(trueLength));
        
        // The MS covariance matrix can now be set.
        
        // **************** code for matrix ***********************//
        
        
        // Insert values for upper triangle... use symmetry to set lower.
        
        double stdThetaSqr = stdTheta*stdTheta;
        double tlamSqr = theVec.get(SurfCylinder.ITLM)*theVec.get(SurfCylinder.ITLM);
        //This gets a bit messy here...
        // alpha term...
        double val = newError.get(SurfCylinder.IALF,SurfCylinder.IALF)+stdThetaSqr*(1 + tlamSqr);
        newError.set(SurfCylinder.IALF,SurfCylinder.IALF,val);
        // tan(lambda) term...
        val = newError.get(SurfCylinder.ITLM,SurfCylinder.ITLM) + stdThetaSqr*(1 + tlamSqr)*(1 + tlamSqr);
        newError.set(SurfCylinder.ITLM,SurfCylinder.ITLM,val);
        // q/pt term...
        val = newError.get(SurfCylinder.IQPT,SurfCylinder.IQPT)+stdThetaSqr*theVec.get(SurfCylinder.IQPT)*theVec.get(SurfCylinder.IQPT)*tlamSqr;
        newError.set(SurfCylinder.IQPT,SurfCylinder.IQPT,val);
        val =  newError.get(SurfCylinder.ITLM,SurfCylinder.IQPT) + stdThetaSqr*theVec.get(SurfCylinder.ITLM)*theVec.get(SurfCylinder.IQPT) * (1.0 + tlamSqr);
        // covariance term between tan(lambda) and q/pt...
        newError.set(SurfCylinder.ITLM,SurfCylinder.IQPT,val);
        newError.set(SurfCylinder.IQPT,SurfCylinder.ITLM,val);
        
        // ********************************************************//
        
        tre.setError( newError );
        
    }
    
    
    public void interact_dir(ETrack theTrack, PropDir direction )
    {
        interact(theTrack);
    }
    
    //
    
    /**
     *Return the number of radiation lengths.
     *
     * @return The thickness of the scattering material in radiation lengths.
     */
    public double radLength()
    {
        return _radLength;
    }
    
    //
    
    /**
     *Make a clone of this object.
     *
     * @return A Clone of this instance.
     */
    public Interactor newCopy()
    {
        return new ThinCylMs(_radLength);
    }
    
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "ThinCylMs with "+_radLength+" radiation lengths.";
    }
    
}

