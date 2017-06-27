package org.lcsim.material;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of detector material, mostly based on Geant4's G4Material.
 * 
 * @author jeremym
 * @version $Id: Material.java,v 1.23 2011/03/11 19:22:20 jeremy Exp $
 */
public class Material implements IMaterial
{
    public static final double DEFAULT_TEMPERATURE = 273.15; // Default temp in Kelvin.    
    public static final double DEFAULT_PRESSURE = 1.0; // Default pressure in atmospheres.
    
    private static final double MAX_X0 = java.lang.Double.MAX_VALUE; // Max X0.
    private static final double MAX_LAMBDA = java.lang.Double.MAX_VALUE; // Max lambda.

    double _density; // Density in g/cm^3.

    boolean _isElement; // Is this a chemical element?

    private double _Zeff; // Effective Z.
    private double _Aeff; // Effective A.

    MaterialState _state; // Material's state (gas, liquid, water, unknown).
    String _name; // Name of material.
    String _formula; // Formula for material.

    int _nComponents; // Number of components currently added.
    int _nComponentsMax; // Maximum number of components that can be added.
    int _nElements; // Number of elements in mass fraction list.

    private List<MaterialElement> _elements = new ArrayList<MaterialElement>(); // List of element composition.
    private List<Double> _massFractions = new ArrayList<Double>(); // List of mass fractions.
    private List<Integer> _atoms = new ArrayList<Integer>(); // List of number of atoms.

    double _radiationLength; // Radiation length in g/cm^2.
    double _radiationLengthWithDensity; // Radiation length div by density.

    double _nuclearInteractionLength; // Nuclear interaction length in g/cm^2.
    double _nuclearInteractionLengthWithDensity; // Nuclear interaction length div by density.

    /**
     * This is a constructor for materials that are chemical elements. 
     * It is not accessible outside the package, as users should not
     * be able to create their own elements. 
     * 
     * @param matName The full name of the material (e.g. "Tungsten").
     * @param formula The formula of the element (e.g. "W" for Tungsten).
     * @param z The atomic number.
     * @param a The atomic weight.
     * @param density The density in g/cm3.
     * @param X0 The radiation length in g/cm-2.
     * @param lambda The nuclear interaction length in g/cm-2.
     * @param state The state of the element (liquid, gas, solid, or unknown).
     */
    Material(String matName, 
            String formula, 
            double z, 
            double a, 
            double density, 
            double X0, 
            double lambda, 
            MaterialState state)
    {
        // System.out.println("Material " + matName + " for element " + elemName);

        // Set basic parameters.
        _name = matName;
        _density = density;
        _formula = formula;
        _state = state;
        
        // Z and A don't need compute for elements.
        _Zeff = z;
        _Aeff = a;

        // Setup component variables.
        _nComponents = 0;
        _nComponentsMax = 1;
        _isElement = true;

        // Add a new chemical element for this Material.
        this.addElement(new MaterialElement(formula, z, a, X0, lambda), 1.0);

        // Compute the radiation length.
        computeRadiationLength();
        
        // Compute the nuclear interaction length.
        computeNuclearInteractionLength();        
                
        // Add to global material store.
        MaterialManager.instance().addMaterial(this);
    }

    /**
     * Construct a material with a number of sub-components, which will be
     * either a set of mass fractions or set of number of atoms.
     */
    // TODO Add formula arg.
    public Material(String name, 
            int nComponents, 
            double density, 
            MaterialState state)
    {
        _name = name;
        _density = density;
        _state = state;
       // _formula = " ";

        if (nComponents < 1)
        {
            throw new IllegalArgumentException("nComponents must be > 0.");
        }

        _nComponentsMax = nComponents;
        _nComponents = _nElements = 0;
        _isElement = false;

        MaterialManager.instance().addMaterial(this);        
    }

    /**
     * Get the name of the Material.
     * @return The name of this Material.
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Get the density (g/cm^3).
     * @return The density in g/cm3.
     **/
    public double getDensity()
    {
        return _density;
    }

    /** @return whether this material has a 1 to 1 correspondance to a single chemical element */
    public boolean isElement()
    {
        return _isElement;
    }

    /**
     * @return The effective Z of this material.
     */
    // FIXME: Duplicates getZ().
    public double getZeff()
    {
        return _Zeff;
    }

    /**
     * @return The effective A of this material.
     */
    // FIXME: Duplicates getA().
    public double getAeff()
    {
        return _Aeff;
    }

    /** 
     * @return The maximum number of components that can be added. 
     **/
    protected int getNComponentsMax()
    {
        return _nComponentsMax;
    }

    /** 
     * @return The number of components defined so far in this material. 
     **/
    protected int getNComponents()
    {
        return _nComponents;
    }

    /** 
     * @return number of elements defined in this material 
     */
    protected int getNElements()
    {
        return _nElements;
    }

    /**
     * @return the state of this material
     */
    public MaterialState getState()
    {
        return _state;
    }

    /** 
     * @return X0 
     */
    public double getRadiationLength()
    {
        return _radiationLength;
    }

    /** 
     * @return The nuclear interaction length; also called lambda. 
     */
    public double getNuclearInteractionLength()
    {
        return _nuclearInteractionLength;
    }

    /** 
     * @return lambda / density 
     */
    public double getNuclearInteractionLengthWithDensity()
    {
        return _nuclearInteractionLengthWithDensity;
    }

    /** 
     * @return X0 in cm = (X0/density) 
     */
    public double getRadiationLengthWithDensity()
    {
        return _radiationLengthWithDensity;
    }

    /** 
     * @return total number of (unique) elements in the element list for this material 
     **/
    protected int getNumberOfElements()
    {
        return _nElements;
    }

    /** 
     * Get the list of component MaterialElements referenced by this material.
     * @return A list of elements in this material. 
     **/
    public List<MaterialElement> getElements()
    {
        return _elements;
    }

    /**
     * @return The mass fractions for each element in the material.
     */
    public List<Double> getMassFractions()
    {
        return _massFractions;
    }

    /**
     * Add an element to the material by atom count.
     * 
     * Based on Geant4's G4Material::AddElement() method.
     */
    protected void addElement(MaterialElement element, int nAtoms)
    {
        if (_nElements < _nComponentsMax)
        {
            _elements.add(element);
            _atoms.add(_nElements, nAtoms);
            _nComponents = ++_nElements;
        }
        else
        {
            throw new RuntimeException("Attempting to add more than declared number of elements for this material: " + _name);
        }

        if (isFilled())
        {
            // double Zmol = 0;
            double Amol = 0;

            int atomCnt = 0;
            for (MaterialElement e : _elements)
            {
                Amol += _atoms.get(atomCnt) * e.getA();
                ++atomCnt;
            }

            int massCnt = 0;
            for (MaterialElement ee : _elements)
            {
                _massFractions.add(_atoms.get(massCnt) * ee.getA() / Amol);
                ++massCnt;
            }

            computeDerivedQuantities();
        }
    }

    /**
     * Add chemical element to the material by fraction of mass.  When
     * all components are added, the fractions must add up to 1.0 .
     */
    protected void addElement(MaterialElement element, double fraction)
    {
        if (_nComponents < _nComponentsMax)
        {
            int elCnt = 0;
            while ((elCnt < _nElements) && element != _elements.get(elCnt))
                elCnt++ ;

            // / Element already exists. Increase its mass fraction.
            if (elCnt < _nElements)
            {
                double currFrac = _massFractions.get(elCnt);
                currFrac += fraction;
                _massFractions.add(elCnt, currFrac);
            }
            // Element does not exist in list. Add a new mass fraction.
            else
            {
                _elements.add(element);
                _massFractions.add(elCnt, fraction);
                ++_nElements;
            }
            ++_nComponents;
        }
        else
        {
            throw new RuntimeException("Attempting to add more than declared number of components to material: " + getName());
        }

        if (isFilled())
        {
            checkMassSum();
            computeDerivedQuantities();
        }
    }

    /**
     * Add a component material by fraction of mass.  When done, must add up to 1.0.    
     * @param The material to add.
     * @param The fraction of total which must be > 0 and <= 1.
     */
    public void addMaterial(Material material, double fraction)
    {
        if (_atoms.size() > 0)
        {
            throw new RuntimeException("Material is already defined by atoms: " + getName());
        }

        if (_nComponents < _nComponentsMax)
        {
            // Loop over elements.
            for (int i = 0, n = material.getNumberOfElements(); i < n; i++ )
            {
                MaterialElement element = material.getElements().get(i);
                int elCnt = 0;
                // Find the element.
                while ((elCnt < _nElements) && (element != _elements.get(elCnt)))
                {
                    ++elCnt;
                }

                // Add fraction to existing element.
                if (elCnt < _nElements)
                {
                    double currFrac = _massFractions.get(elCnt);
                    currFrac += fraction * material.getMassFractions().get(i);
                    _massFractions.set(elCnt, currFrac);
                }
                // Add a new element.
                else
                {
                    // System.out.println("adding mass fraction: " + element.getName() + "=" + fraction *
                    // material.getMassFractions().get(i) );

                    _elements.add(element);

                    // Add the mass fraction of this element, scaled by its percentage in the material's
                    _massFractions.add(elCnt, fraction * material.getMassFractions().get(i));
                    ++_nElements;
                }
            }
            ++_nComponents;
        }
        else
        {
            throw new RuntimeException("Attempting to add more than allowed umber of components to material: " + getName());
        }

        if (isFilled())
        {
            checkMassSum();
            computeDerivedQuantities();
        }
    }
    
    // FIXME Remove as just uses default?
    public double getPressure()
    {
        return DEFAULT_PRESSURE;
    }
    
    // FIXME Remove as just uses default?
    public double getTemperature()
    {
        return DEFAULT_TEMPERATURE;
    }
    
    /** 
     * Translate this material to a human-readable string.
     * @return String rep of this object. 
     */
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        buff.append(getName() + "; state=" + _state.toString() + "; Z=" + _Zeff + "; A=" + _Aeff + "; dens=" + _density + "; X0=" + _radiationLength + "; lambda=" + _nuclearInteractionLength);

        for (int i = 0; i < this.getNElements(); i++ )
        {
            buff.append("\n\t" + getElements().get(i).getName() + " " + this.getMassFractions().get(i) * 100);
        }

        return buff.toString();
    }
        
    /**
     * True if all components have been added; false if not.
     * @return True if all component elements and materials have been added.
     */
    protected boolean isFilled()
    {
        return (_nComponents == _nComponentsMax);
    }

    /** 
     * Check that the massFractions list adds up to 1.0.
     * @throw RuntimeException If mass fractions do not add to 1.0. 
     */
    private void checkMassSum()
    {
        double weightSum = 0;
        for (int i = 0; i < _massFractions.size(); i++ )
        {
            weightSum += _massFractions.get(i);
        }

        if (abs(1 - weightSum) > 0.001)
        {
            throw new RuntimeException("Mass fractions do not sum to 1 within 0.001 tolerance for this material: " + getName());
        }
    }

    /** 
     * Compute the effective Z for this material using its element list. 
     * @return The effective Z.
     */
    private double computeZeff()
    {
        double ZsumNotNorm = 0;
        double atomCntFracSum = 0;

        int nelem = this.getNElements();
        for (int i = 0; i < nelem; i++ )
        {
            MaterialElement me = this.getElements().get(i);
            double massFrac = this.getMassFractions().get(i);
            double atomCntFrac = massFrac / me.getA();
            ZsumNotNorm += atomCntFrac * me.getZ();
            atomCntFracSum += atomCntFrac;
        }

        double ZsumNorm = ZsumNotNorm / atomCntFracSum;
        _Zeff = ZsumNorm;
        return _Zeff;
    }

    /** 
     * Compute the effective A for this material using its element list.
     * @return The effective A. 
     */
    private double computeAeff()
    {
        double AsumNotNorm = 0;
        double atomCntFracSum = 0;

        int nelem = this.getNElements();
        for (int i = 0; i < nelem; i++ )
        {
            MaterialElement me = this.getElements().get(i);
            double massFrac = this.getMassFractions().get(i);
            double atomCntFrac = massFrac / me.getA();
            AsumNotNorm += atomCntFrac * me.getA();
            atomCntFracSum += atomCntFrac;
        }
        double ZsumNorm = AsumNotNorm / atomCntFracSum;
        _Aeff = ZsumNorm;
        return _Aeff;
    }
      
    /**
     * Compute the nuclear interaction length (lambda).
     */
    private void computeNuclearInteractionLength()
    {
        double NILinv = 0.0;

        for (int i = 0; i < _nElements; i++ )
        {
            MaterialElement me = _elements.get(i);
            NILinv += _massFractions.get(i) / me.getNuclearInteractionLength();
        }

        _nuclearInteractionLength = (NILinv <= 0.0 ? MAX_LAMBDA : 1.0 / NILinv);
        _nuclearInteractionLengthWithDensity = _nuclearInteractionLength * _density;
    }

    /** 
     * Compute the radiation length (x0) based on mass fractions of elements. 
     */
    private void computeRadiationLength()
    {
        double rlinv = 0.0;

        for (int i = 0; i < _nElements; i++ )
        {
            MaterialElement me = _elements.get(i);
            rlinv += _massFractions.get(i) / me.getRadiationLength();
        }

        _radiationLength = (rlinv <= 0.0 ? MAX_X0 : 1.0 / rlinv);
        _radiationLengthWithDensity = _radiationLength * _density;
        
        // NOTE Critical energy and moliere radius calcs left here for reference.
        
        // Set the critical energy
        //double criticalEnergy = 2.66 * pow(_radiationLength * _Zeff / _Aeff, 1.1);

        // Set Moliere radius. What is magic number?
        // _moliereRadius = _radiationLengthWithDensity * 21.2052 / criticalEnergy;
        // _moliereRadius = 0.01 * rMoliere / _density;
    }

    /**
     * Computes and caches derived computed quantities after all components have been added to the material.
     * This method is called in Material ctors.  For Materials with sub-components, it is called after
     * all components are added.
     */
    private void computeDerivedQuantities()
    {
        computeZeff();
        computeAeff();
        computeRadiationLength();
        computeNuclearInteractionLength();
    }

    /**
     * Get the A of this Material, which is a computed, effective A.
     */
    public double getA()
    {
        return _Aeff;
    }
        
    /**
     * Get the Z of this Material, which is a computed, effective Z.
     */
    public double getZ()
    {
        return _Zeff;
    }
}