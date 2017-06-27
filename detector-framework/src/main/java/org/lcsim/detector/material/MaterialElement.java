package org.lcsim.detector.material;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static org.lcsim.units.clhep.PhysicalConstants.fine_structure_const;

/**
 * This class is a Material that represents a chemical element.
 * 
 * It uses the CLHEP values for Avogadro, fine structure constant, and electron
 * radius. Radiation lengths are computed using the Tsai formula, ported from
 * Lelaps. Nuclear interaction length is computed with a simple fit, ported from
 * Lelaps.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: MaterialElement.java,v 1.5 2010/04/14 18:24:53 jeremy Exp $
 */
public class MaterialElement implements IMaterial
{
    // From constructor.
    protected String name;
    protected double Z;
    protected double A;
    protected double density;
    protected double temperature;
    protected double pressure;

    // Set by the detailed constructor.  Otherwise, use defaults.
    protected State state = defaultState;

    // Derived quantities.
    protected double N;
    protected double nuclearInteractionLength;
    protected double nuclearInteractionLengthWithDensity;
    protected double radiationLength;
    protected double radiationLengthWithDensity;
    protected double criticalEnergy;
    protected double moliereRadius;

    /**
     * Constructor for sub-classes.
     */
    protected MaterialElement()
    {
        MaterialStore.getInstance().add(this);
    }

    /**
     * Construct a MaterialElement.
     * 
     * @param name The name of the material, which must be globally unique.
     * @param Z The atomic mass.
     * @param A The atomic number.
     * @param density The density in g/cm3.
     * @param state The material's state.
     * @param temperature The temperature in Kelvin.
     * @param pressure The pressure in atmospheres.
     */
    public MaterialElement(String name, double Z, double A, double density, State state, double temperature, double pressure)
    {
        // Set parameters.
        this.name = name;
        this.Z = Z;
        this.A = A;
        this.density = density;
        this.density = density;
        this.state = state;
        this.temperature = temperature;
        this.pressure = pressure;

        // Compute derived quantities.
        computeDerivedQuantities();

        // Add to global static materials store.
        MaterialStore.getInstance().add(this);
    }

    /**
     * Construct a MaterialElement.
     * 
     * @param name The name of the material, which must be globally unique.
     * @param Z The atomic mass.
     * @param A The atomic number.
     * @param density The density in g/cm3.
     */
    public MaterialElement(String name, double Z, double A, double density)
    {
        // Set parameters.
        this.name = name;
        this.Z = Z;
        this.A = A;
        this.density = density;
        this.density = density;
        this.state = Unknown;
        this.temperature = defaultTemperature;
        this.pressure = defaultPressure;

        // Compute derived quantities.
        computeDerivedQuantities();

        // Add to global static materials store.
        MaterialStore.getInstance().add(this);
    }

    private void computeDerivedQuantities()
    {
        computeRadiationLength();
        computeNuclearInteractionLength();
        computeCriticalEnergy();
        computeMoliereRadius();
    }

    protected void computeCriticalEnergy()
    {
        criticalEnergy = 2.66 * pow(getRadiationLength() * getZ() / getA(), 1.1);
    }

    protected void computeMoliereRadius()
    {
        moliereRadius = getRadiationLengthWithDensity() * 21.2052 / criticalEnergy;
    }

    protected void computeRadiationLength()
    {
        radiationLength = computeRadiationLengthTsai(A, Z);
        radiationLengthWithDensity = radiationLength / getDensity();
    }

    protected void computeNuclearInteractionLength()
    {
        nuclearInteractionLength = computeNuclearInteractionLength(A, Z);
        nuclearInteractionLengthWithDensity = nuclearInteractionLength / getDensity();
    }

    public double getA()
    {
        return A;
    }

    public double getDensity()
    {
        return density;
    }

    public String getName()
    {
        return name;
    }

    public double getNuclearInteractionLength()
    {
        return nuclearInteractionLength;
    }

    public double getRadiationLength()
    {
        return radiationLength;
    }

    public State getState()
    {
        return state;
    }

    public double getZ()
    {
        return Z;
    }

    public double getEffectiveNumberOfNucleons()
    {
        return N;
    }

    public double getMoliereRadius()
    {
        return moliereRadius;
    }

    public double getNuclearInteractionLengthWithDensity()
    {
        return nuclearInteractionLengthWithDensity;
    }

    public double getRadiationLengthWithDensity()
    {
        return radiationLengthWithDensity;
    }

    public static double computeRadiationLengthTsai(double A, double Z)
    {
        double azsq = fine_structure_const * Z;
        azsq *= azsq;
        double f = azsq * (1.0 / (1.0 + azsq) + 0.20206 - 0.0369 * azsq + 0.0083 * azsq * azsq - 0.002 * azsq * azsq * azsq);

        double Lrad, LradP;
        if (Z == 1)
        {
            Lrad = 5.31;
            LradP = 6.144;
        }
        else if (Z == 2)
        {
            Lrad = 4.79;
            LradP = 5.621;
        }
        else if (Z == 3)
        {
            Lrad = 4.74;
            LradP = 5.805;
        }
        else if (Z == 4)
        {
            Lrad = 4.71;
            LradP = 5.924;
        }
        else
        {
            Lrad = log(184.15 / pow(Z, 0.333333333));
            LradP = log(1194.0 / pow(Z, 0.666666667));
        }
        double rlen = 716.408 * A / ((Z * Z * (Lrad - f) + Z * LradP));
        return rlen;
    }

    public static double computeNuclearInteractionLength(double A, double Z)
    {
        double NIL = 0;
        if (Z == 1)
        {
            if (A < 1.5)
            {
                NIL = 50.8;
            }
            else
            {
                NIL = 54.7;
            }
        }
        else if (Z == 2)
        {
            NIL = 65.2;
        }
        else
        {
            NIL = 40.8 * pow(A, 0.289);
        }

        return NIL;
    }

    public double getTemperature()
    {
        return temperature;
    }

    public double getPressure()
    {
        return pressure;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getName() + '\n');
        sb.append("D = " + getDensity() + " {g/cm3} ; ");
        sb.append("A = " + getA() + "; ");
        sb.append("Z = " + getZ());
        sb.append('\n');
        sb.append("LamdaT = " + getNuclearInteractionLength() + " {g/cm2}; ");
        sb.append("LamdaT = " + getNuclearInteractionLengthWithDensity() + " {cm}; ");
        sb.append("X0 = " + getRadiationLength() + " {g/cm2}; ");
        sb.append("X0 = " + getRadiationLengthWithDensity() + " {cm}; ");
        sb.append("Rm = " + getMoliereRadius() + " {cm} ");
        sb.append('\n');

        return sb.toString();
    }
}