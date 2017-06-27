package org.lcsim.detector.material;

/**
 *
 * This is the primary interface to material information
 * in the {@link org.lcsim.detector} package.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: IMaterial.java,v 1.5 2010/04/14 18:24:53 jeremy Exp $
 */
public interface IMaterial 
{
    /**
     * Get the name of this material.
     * @return The name of the material.
     */
	public String getName();
    
    /**
     * Get the density of this material in g/cm3.
     * @return The density in g/cm3.
     */
	public double getDensity();
    
    /**
     * Get the atomic mass of this material.
     * @return The atomic mass.
     */
	public double getZ();
    
    /**
     * Get the atomic number of this material.
     * @return The atomic number.
     */
	public double getA();
       
    /**
     * Get the nuclear interaction length in g/cm2.
     * @return The nuclear interaction length in g/cm2.
     */
	public double getNuclearInteractionLength();
    
    /**
     * Get the nuclear interaction length in g/cm2.
     * @return The nuclear interaction length in cm.
     */
	public double getNuclearInteractionLengthWithDensity();
    
    /**
     * Get the radiation length in g/cm2.
     * @return The radiation length in g/cm2.
     */
	public double getRadiationLength();
    
    /**
     * Get the radiation length in cm.
     * @return The radiation length in cm.
     */
	public double getRadiationLengthWithDensity();
    
    /**
     * Get the Moliere radius in cm.
     * @return The moliere radius in cm.
     */
	public double getMoliereRadius();
    
    /**
     * Get the temperature of the material in Kelvin.
     * @return The temperature in Kelvin.
     */
    public double getTemperature();
    
    /**
     * Get the pressure of the material in atmospheres.
     * @return The pressure of the material in atmospheres.
     */
    public double getPressure();
    
    /**
     * Get the state of this material, either liquid, gas, solid, or unknown.
     * @return The state of this material.
     */
	public State getState();
	
    public final static State Unknown = new State("unknown");
    public final static State Gas = new State("gas");
    public final static State Liquid = new State("liquid");
    public final static State Solid = new State("solid");
	
	public class State
	{
	    private String state;
	    	    
	    private State(String state)
	    {
	        this.state = state;
	    }
	    
	    public String toString()
	    {	       
	        return state;
	    }	
	}
	
	// Defaults for non-mandatory parameters.
	static public final double defaultTemperature=273.15;
	static public final State defaultState=Unknown;
	static public final double defaultPressure=1.0;
	static public final double defaultIonizationPotential=0.0;	
}