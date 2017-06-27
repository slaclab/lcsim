package org.lcsim.material;

/**
 * A class to represent chemical elements.
 * @author jeremym
 * @version $Id: MaterialElement.java,v 1.12 2011/03/11 19:22:20 jeremy Exp $
 */
public class MaterialElement
{
    private String _name;                     // formula
    private double _Z;                        // atomic number
    private double _A;                        // atomic weight 
    private double _nuclearInteractionLength; // lambda
    private double _radiationLength;          // X0 
     
    //public static final double AVOGADRO = 6.24151e+21;   
    
    /**
     * The ctor for the element class.
     * @param name The name of the element; same as formula.
     * @param Z The atomic number.
     * @param A The atomic mass.
     * @param X0 The radiation length.
     * @param lambda The nuclear interaction length.
     */
    MaterialElement(String name,                    
            double Z,
            double A,
            double X0,
            double lambda)
    {
        // Set parameters.
        _name = name;
        _Z = Z;
        _A = A;
        _radiationLength = X0;
        _nuclearInteractionLength = lambda;
        
        // Add the element to the static MaterialManager instance.
        MaterialManager.instance().addElement(this);
    }            
   
    /**
     * Get the atomic number of the element.
     * @return The atomic number.
     */
    public double getZ()
    {
        return _Z;
    }
    
    /**
     * Get the atomic weight of the element.
     * @return The atomic weight.
     */
    public double getA()
    {
        return _A;
    }
            
    /**
     * Get the name of the element.
     * @return The name of the element.
     */
    public String getName()
    {
        return _name;
    }
        
    /**
     * Get the nuclear interaction length of the element (cm).
     * @return The nuclear interaction length in cm.
     */
    public double getNuclearInteractionLength()
    {
        return _nuclearInteractionLength;
    }   
    
    /**
     * Get the radiation length of the element (cm).
     * @return The nuclear interaction length in cm.
     */
    public double getRadiationLength()
    {
        return _radiationLength;
    }
    
    /**
     * Convert to String.
     * @return String representation.
     */
    public String toString()
    {
        return getName()  + "; Z=" + _Z + "; A=" + _A +  "; X0=" + _radiationLength + "; lambda=" + _nuclearInteractionLength;
    }    
}