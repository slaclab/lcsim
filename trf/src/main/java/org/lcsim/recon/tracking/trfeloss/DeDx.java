package org.lcsim.recon.tracking.trfeloss;

/**
 * Abstract class for evaluating the energy loss.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public abstract class DeDx
{
    /**
     *Return energy loss (dE/dx) for a given energy.
     *
     * @param   energy The energy of the particle.
     * @return The average energy lost by a particle of this energy.
     */
    public abstract double dEdX(double energy);
    
    /**
     *Return the uncertainty in the energy lost sigma(E).
     *
     * @param   energy The energy of the particle.
     * @param   x The amount of material.
     * @return The uncertainty in the energy lost sigma(E).
     */
    public  abstract double sigmaEnergy(double energy, double x);
    
    /**
     *Return new energy for a given path distance.
     * Energy increases if x < 0.
     *
     * @param   energy The energy of the particle.
     * @param   x The amount of material.
     * @return New energy for a given path distance.
     */
    public abstract double loseEnergy(double energy, double x);
    
}
