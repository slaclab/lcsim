package org.lcsim.recon.tracking.trfeloss;
/**
 * Class for evaluating a fixed energy loss.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class DeDxFixed extends DeDx
{
    
    private double _density;
    
    /**
     *Construct an instance given a material density.
     *
     * @param   density The density of the material.
     */
    public DeDxFixed(double density)
    {
        _density = density;
    }
    
    /**
     *Return energy loss (dE/dx) for a given energy.
     *
     * @param   energy The energy of the particle.
     * @return The average energy lost by a particle of this energy.
     */
    public double dEdX(double energy)
    {
        double mip = 1.665; // this is an average mip
        double de_dx = mip*_density;   // MeV/cm
        de_dx /= 1000.; // GeV/cm
        return de_dx;
    }
    
    /**
     *Return the uncertainty in the energy lost sigma(E).
     *
     * @param   energy The energy of the particle.
     * @param   x The amount of material.
     * @return The uncertainty in the energy lost sigma(E).
     */
    public double sigmaEnergy(double energy, double x)
    {
        final double pionMass = 0.13957;
        double ZoverA = 0.5; // assume Z/A = 0.5
        double sigma_e = 0.1569*ZoverA*_density*x;
        
        // add relativistic correction
        if(energy>pionMass)
        {
            double gamma = energy/pionMass;
            double beta = Math.sqrt(gamma*gamma - 1)/gamma;
            sigma_e *= (1-0.5*beta*beta)*gamma*gamma; // MeV^2
            sigma_e = Math.sqrt(sigma_e)/1000; // GeV
        }
        return sigma_e;
    }
    
    /**
     *Return new energy for a given path distance.
     * Energy increases if x < 0.
     *
     * @param   energy The energy of the particle.
     * @param   x The amount of material.
     * @return New energy for a given path distance.
     */
    public double loseEnergy(double energy, double x)
    {
        double deltaE = dEdX(energy)*x;
        return energy-deltaE;
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "DeDxFixed with density "+_density;
    }
}
