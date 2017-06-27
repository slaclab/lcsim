package org.lcsim.recon.tracking.trfcyl;
import java.util.Random;

import org.lcsim.recon.tracking.trfbase.SimInteractor;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;

/**
 * Class for adding Multiple Scattering to track vectors defined at
 * SurfCylinders.  Single point interaction is assumed.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class ThinCylMsSim extends SimInteractor
{
    
    // radiation lengths in material
    private double _radLength;
    
    // random number generator
    private Random _r;
    
    //
    
    /**
     * Construct an instance from the number of radiation
     * lengths of the thin cylindrical shell material.
     * The Interactor is constructed with the
     * appropriate number of radiation lengths.
     *
     * @param   radLength The thickness of the material in radiation lengths.
     */
    public ThinCylMsSim( double radLength )
    {
        _radLength = radLength;
        _r = new Random();
    }
    
    //
    
    /**
     *Construct an instance from  a ThinCylMs Interactor.
     *
     * @param   inter The ThinCylMs Interactor for this cylindrical shell.
     */
    public ThinCylMsSim( ThinCylMs inter )
    {
        _radLength = inter.radLength();
        _r = new Random();
    }
    
    //
    
    /**
     *Interact the given track in this cylindrical shell,
     *using the thin material approximation for multiple scattering.
     *Note that the track parameters are modified to simulate
     *the effects of multiple scattering in traversing the cylindrical
     *shell of material.
     *
     * @param   vtrk  The Vrack to scatter.
     */
    public void interact( VTrack vtrk )
    {
        TrackVector trv = vtrk.vector();
        // first, how much should the track be scattered:
        
        double trueLength = _radLength/Math.abs(Math.cos(trv.get(SurfCylinder.IALF)))*Math.sqrt(1+trv.get(SurfCylinder.ITLM)*trv.get(SurfCylinder.ITLM));
        double zq = trv.get(SurfCylinder.IQPT);
        double trackMomentum = Math.abs(zq*Math.cos(Math.atan(trv.get(SurfCylinder.ITLM))));
        double scatRMS = (0.0136)*trackMomentum*Math.sqrt(trueLength)*
                (1+0.038*Math.log(trueLength));
        
        double phiDir,offset,lam,a1,a2;
        double dirprime,alprime;
        double theta1,theta2;
        phiDir = trv.get(SurfCylinder.IALF) + trv.get(SurfCylinder.IPHI);
        offset=0.0;
        if(phiDir > Math.PI)
        {
            phiDir += -2*Math.PI;
            offset = -2*Math.PI;
        }
        if( phiDir < -Math.PI )
        {
            phiDir += 2*Math.PI;
            offset = 2*Math.PI;
        }
        lam = Math.atan(trv.get(SurfCylinder.ITLM));
        
        //now, get a couple of angles:
        //cng  20010223 use intrinsic generator, need to set seed for this
        double r1 = gauss();
        double r2 = gauss();
        theta1 = scatRMS*r1;
        theta2 = scatRMS*r2;
        //cng
        // If using Java Random, be careful, since seed is set based on time.
        // two Randoms created too quickly will give same sequence.
        //  theta1 = scatRMS*_r.nextGaussian();
        //  theta2 = scatRMS*_r.nextGaussian();
        //  System.out.println("ThinCylMsSim at "+vtrk.get_surface() +" theta1= "+theta1+" theta2= "+theta2);
        
        // scatter the track as described in d0note xxxx.
        
        a1 = -theta1*Math.cos(phiDir) + Math.sin(phiDir)*(Math.cos(lam) +
                theta2*Math.sin(lam));
        a2 = theta1*Math.sin(phiDir) + Math.cos(phiDir)*(Math.cos(lam) +
                theta2*Math.sin(lam));
        
        dirprime = Math.atan(a1/a2);
        if((a2<0)&&(dirprime<0))
        {
            dirprime += Math.PI;
        }
        else
        {
            if((a2<0)&&(dirprime>0))
                dirprime -= Math.PI;
        }
        alprime = dirprime - trv.get(SurfCylinder.IPHI) - offset;
        
        double tlprime = (Math.sin(lam) - theta2*Math.cos(lam))/
                (Math.sqrt( 1 + theta1*theta1 + theta2*theta2 )*
                Math.sqrt(1- ((Math.sin(lam) - theta2*Math.cos(lam))*
                (Math.sin(lam) - theta2*Math.cos(lam)))/
                ( 1 + theta1*theta1 + theta2*theta2)));
        
        double zqprime = zq*Math.cos(lam)/Math.sqrt( 1 - ((Math.sin(lam) - theta2*Math.cos(lam))*
                (Math.sin(lam) - theta2*Math.cos(lam)))/
                ( 1 + theta1*theta1 + theta2*theta2));
        trv.set(SurfCylinder.IALF, alprime);
        trv.set(SurfCylinder.ITLM, tlprime);
        trv.set(SurfCylinder.IQPT, zqprime);
        
        // assume that we don't encounter back-scattering... which is
        // assumed above anyway.
        vtrk.setVectorAndKeepDirection( trv );
        
    }
    
    
    /**
     *Return the number of radiation lengths of material in this cylindrical shell.
     *
     * @return The number of radiation lengths.
     */
    public double radLength()
    { return _radLength; }
    
    //
    
    /**
     * Make a clone of this object.
     * Note that new copy will have a different random number generator.
     *
     * @return A Clone of this instance.
     */
    public SimInteractor newCopy()
    {
        return new ThinCylMsSim(_radLength);
    }
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "ThinCylMsSim with "+_radLength+" radiation lengths";
    }
}
