package org.lcsim.recon.tracking.trfcyl;

import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfcyl.SurfCylinder;

/**
 * This class modifies the covariance matrix of a track which
 * has been propagated to an InteractingLayer containing
 * a LayerCylinder. The modifications correspond to multiple
 *scattering in a thick cylindrical shell whose material is
 *represented by the number of radiation lengths.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class ThickCylMs extends Interactor
{
 
    private double _pathLength; // Actual path length in cm
    private double _radLength;  // Radiation length of this cylinder

    /**
     * 
     * Construct an instance from the number of radiation
     * lengths of the cylindrical shell material.
     * The Interactor is constructed with the
     * appropriate number of radiation lengths.
     *
     * @param   pathLength The physical thickness of this cylindrical shell (in cm).
     * @param   radLength The thickness of the material in radiation lengths.
     */
    
    public ThickCylMs(double pathLength, double radLength)
    {
        _pathLength = pathLength;
        _radLength = radLength;
    }
    
    
    /**
     *Make a clone of this object.
     *
     * @return A Clone of this instance.
     */
    public Interactor newCopy()
    {
        return new ThickCylMs(_pathLength, _radLength);
    }
    

    /**
     *Interact the given track in this cylindrical shell,
     *using the thick material approximation for multiple scattering.
     *Note that both the track parameters and the
     *covariance matrix are updated to reflect the uncertainty caused
     *by traversing the cylindrical shell of material.
     *
     * @param   tre The ETrack to scatter.
     */
    public void interact(ETrack tre)
    {
        
        //if called with pl = 0, get nasty error matrix (log 0)!
        if(_pathLength==0 || _radLength==0) return;
        
        
        // This can only be used with cylinders... check that we have one..
        //  SurfCylinder cyl(10.0);  //cylinder with radius 10 cm
        //  SurfacePtr srf = tre.get_surface();
        //  assert( srf->get_pure_type() == cyl.get_pure_type() );
        
        
        TrackError cleanError = tre.error();
        TrackError newError = cleanError;
        
        TrackVector theVec = tre.vector();
        
        //calculate q/p from q/pt
        double trackMomentum = Math.abs(theVec.get(SurfCylinder.IQPT)*Math.cos(Math.atan(theVec.get(SurfCylinder.ITLM))));
        
        
        //calculate dE/dx = a * p * s / X (a from fit to mc):
        double dE = 0.01*(_pathLength/_radLength)/(trackMomentum);
        
        
        //set the rms scattering appropriate to this momentum
        //Theta = (0.0136 GeV)*(z/p)*(sqrt(thickness/rad_length))*(1+0.038*log(thickness/rad_length))
        
        //calculate rms scattering angle
        double stdTheta = (0.0136)*trackMomentum*Math.sqrt(_pathLength/_radLength)*
                (1 + 0.038*Math.log(_pathLength/_radLength));
        
        //the rms scattering displacement, y^2:
        // double stdY = (1/(sqrt(3.0)))*_pathLength*stdTheta;
        
        //there is a correlation between y and theta
        double correl = Math.sqrt(3.0)/2;
        
        
        // The MS covariance matrix can now be set.
        // **************** code for matrix ***********************//
        
        double ThetaSqr = stdTheta*stdTheta;
        double tlamSqr = theVec.get(SurfCylinder.ITLM)*theVec.get(SurfCylinder.ITLM);
        //get the final radius from the track
        
        double radius = tre.spacePoint().rxy();
        
        double sSqr = _pathLength*_pathLength;
        double rSqr = radius * radius;
        
        double val = newError.get(SurfCylinder.IZ,SurfCylinder.IZ) + sSqr*ThetaSqr*(1 + tlamSqr)/3;
        newError.set(SurfCylinder.IZ,SurfCylinder.IZ, val);
        val = newError.get(SurfCylinder.IALF,SurfCylinder.IALF) + ThetaSqr*( (1 + tlamSqr) + sSqr/(3*rSqr) );;
        newError.set(SurfCylinder.IALF,SurfCylinder.IALF,val);
        //   - 2*correl*sqrt((1 + tlamSqr)*sSqr/(3*rSqr)) );
        val = newError.get(SurfCylinder.IPHI,SurfCylinder.IPHI) + sSqr*ThetaSqr/(3*rSqr);
        newError.set(SurfCylinder.IPHI,SurfCylinder.IPHI,val);
        
        //same as for thin cylinder MS
        val = newError.get(SurfCylinder.ITLM,SurfCylinder.ITLM) + ThetaSqr*(1 + tlamSqr)*(1 + tlamSqr);
        newError.set(SurfCylinder.ITLM,SurfCylinder.ITLM,val);
        val = newError.get(SurfCylinder.IQPT,SurfCylinder.IQPT) + ThetaSqr*theVec.get(SurfCylinder.IQPT)*theVec.get(SurfCylinder.IQPT)*tlamSqr;
        newError.set(SurfCylinder.IQPT,SurfCylinder.IQPT,val);
        
        
        //Sort out the correlations
        //Insert values for off diagonals & use symmetry to set lower.
        val = newError.get(SurfCylinder.IZ,SurfCylinder.ITLM) + correl*Math.sqrt(ThetaSqr)*(1 + tlamSqr) * Math.sqrt( sSqr*ThetaSqr*(1 + tlamSqr)/3 );
        newError.set(SurfCylinder.IZ,SurfCylinder.ITLM,val);
        newError.set(SurfCylinder.ITLM,SurfCylinder.IZ,val);
        
        val = newError.get(SurfCylinder.ITLM,SurfCylinder.IQPT) + ThetaSqr*theVec.get(SurfCylinder.ITLM)*theVec.get(SurfCylinder.IQPT)*(1.0+tlamSqr);
        newError.set(SurfCylinder.ITLM,SurfCylinder.IQPT,val);
        newError.set(SurfCylinder.IQPT,SurfCylinder.ITLM, val);
        
        val = newError.get(SurfCylinder.IALF,SurfCylinder.IPHI) + correl * ThetaSqr * Math.sqrt(sSqr/(3*rSqr) * ( (1 + tlamSqr) + sSqr/(3*rSqr)) );
        newError.set(SurfCylinder.IALF,SurfCylinder.IPHI,val);
        newError.set(SurfCylinder.IPHI,SurfCylinder.IALF,val);
        
        
        // ********************************************************//
        
        
        //correct for dE/dx:
        // System.out.println("qoverpt: "+theVec.get(SurfCylinder.IQPT) +"; dE: "+dE);
        // if ( theVec.get(SurfCylinder.IQPT)<0)  theVec.set(SurfCylinder.IQPT, 1/( 1/(theVec.get(SurfCylinder.IQPT)) + dE ) );
        // else theVec.set(SurfCylinder.IQPT, 1/( 1/(theVec.get(SurfCylinder.IQPT)) - dE ) );
        // val = newError.get(SurfCylinder.IQPT,SurfCylinder.IQPT) + (theVec.get(SurfCylinder.IQPT)*theVec.get(SurfCylinder.IQPT))*0.5*( dE*dE );
        // newError.set(SurfCylinder.IQPT,SurfCylinder.IQPT,val);
        
        // tre.set_vector(theVec);
        
        tre.setError( newError );
    }
 
    /**
     *Return the number of radiation lengths.
     *
     * @return The thickness of the scattering material in radiation lengths.
     */
    public double radLength()
    {
        return _radLength;
    }

    /**
     *Return the thickness of this cylindrical shell in cm.
     *
     * @return The thickness of the cylindrical shell in cm.
     */
    public double pathLength()
    {
        return _pathLength;
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "ThickCylMs with "+_radLength+" radiation lengths and "+_pathLength+" thickness.";
    }
    
}