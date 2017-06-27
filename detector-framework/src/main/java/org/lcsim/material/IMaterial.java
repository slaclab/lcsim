package org.lcsim.material;

/**
 *
 * This is the primary interface to information on a single material.
 *
 * @author jeremym
 * @version $Id: IMaterial.java,v 1.1 2011/03/11 19:22:20 jeremy Exp $
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
	public MaterialState getState();		
}