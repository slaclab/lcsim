package org.lcsim.recon.tracking.trfeloss;
/**
 *Class for evaluating the energy loss
 * according to the Bethe-Bloch equation.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class DeDxBethe extends DeDx
{
    
    
    private double _density;
    private double _particleMass;
    
    // use notations from Sternheimer ...
    private double _a;
    private double _ionizationPot;
    private double _cA;
    private double  _X0;
    private double _X1;
    private double _m;
    private double _C;
    
    /**
     *Construct an instance given a material density.
     *
     * @param   density The density of the material.
     */
    public DeDxBethe(double density)
    {
        _density = density;
        // assume that the ionizing particle is a pion and
        // all parameters of the absorber (except density)
        // are the average of Si, Be, Al and polyvinyl Cl.
        _particleMass = 0.13957;
        _a = 0.074315;
        _ionizationPot = 127.726;
        _cA = 0.3123;
        _X0 = 0.0966;
        _X1 = 2.45;
        _C = -3.878;
        _m = 2.878;
    }
    
    /**
     *Return energy loss (dE/dx) for a given energy.
     *
     * @param   energy The energy of the particle.
     * @return The average energy lost by a particle of this energy.
     */
    public double dEdX(double energy)
    {
        double de_dx;
        de_dx = _a*_density;
        // need a protection ...
        double gamma = energy/_particleMass;
        double beta = Math.sqrt(gamma*gamma-1)/gamma;
        double weakDep = Math.log(1.044e12*Math.pow(beta,4)*Math.pow(gamma,4)
        /Math.pow(_ionizationPot,2));
        weakDep -= 2*beta*beta;
        
        // include density effect correction
        double X = Math.log(beta*gamma)/2.3026; // recall log10(x) = log(x)/log(10)
        double delta;
        if(X<_X1 && X>_X0)
        {delta = 4.6052*X + _cA*Math.pow((_X1-X),_m) + _C;}
        else if(X>_X1)
        {delta = 4.6052*X + _C;}
        else
        {delta = 0;}
        weakDep -= delta;
        
        de_dx *= weakDep; // MeV/cm
        de_dx /= beta*beta;
        
        de_dx /= 1000.0; // GeV/cm
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
        double factor = 1.022;
        double sigma_e = factor*_density*_a*x;
        
        // add relativistic correction
        double gamma = energy/_particleMass;
        double beta = Math.sqrt(gamma*gamma - 1)/gamma;
        sigma_e *= (1-0.5*beta*beta)/(1-beta*beta); // MeV^2
        sigma_e = Math.sqrt(sigma_e)/1000; // GeV
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
        return "DeDxBethe";
    }
    
}
