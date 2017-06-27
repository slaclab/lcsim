package org.lcsim.detector.material;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the @see IMaterialMixture interface. 
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: MaterialMixture.java,v 1.7 2010/04/14 18:24:53 jeremy Exp $
 *
 */
public class MaterialMixture 
extends MaterialElement
{    
    // Max radiation length.
    public static final double MAX_RADIATION_LENGTH = java.lang.Double.MAX_VALUE;
    
    // Max interaction length.
    public static final double MAX_NUCLEAR_INTERACTION_LENGTH = java.lang.Double.MAX_VALUE;
             
    // Effective, computed Z value.
    private double Zeff;
    
    // Effective, computed A value.
    private double Aeff;
    
    // Number of components added.
    private int nComponentsAdded=0;
    
    // Number of components that are allowed to be added.
    private int nComponentsMax=0;
    
    // Number of elements in the mass fraction list.
    private int nElements=0;
    
    // The list of elements making up the mixture.
    private List<MaterialElement> elementList = new ArrayList<MaterialElement>();
    
    // List of mass fractions.
    private List<Double> massFractionList = new ArrayList<Double>();
    
    // Number of atoms of each element.
    private List<Integer> atomCountList = new ArrayList<Integer>();      
    
    /** Construct a material with a number of components. */
    public MaterialMixture(
            String name,
            int nComponents,
            double density,
            State state)
    {
        this.name = name;
        this.density = density;
        this.state = state;
        
        if ( nComponents <= 0 )
        {
            throw new IllegalArgumentException("nComponents must be >= 0.");
        }         
        
        this.nComponentsMax = nComponents;
    }
    
    public double getZ()
    {
    	return Zeff;
    }

    public double getA()
    {
    	return Aeff;
    }                
            
    /** @return maximum number of 1st-level (non-recursive) components that can be defined */
    public int getNComponentsMax()
    {
        return nComponentsMax;
    }
    
    /** @return number of components defined so far in this material */
    public int getNComponents()
    {
        return nComponentsAdded;
    }
    
    /** @return number of elements defined in this material */
    public int getNElements()
    {
        return nElements;
    }
    
    /** @return lambda (nuclear interaction length) based on mass fractions of elements */
    private double computeNuclearInteractionLengthForMixture()
    {
        double NILinv = 0.0;
        
        for (int i = 0; i < nElements; i++)
        {
            MaterialElement me = elementList.get(i);
            NILinv += massFractionList.get(i) / me.getNuclearInteractionLength();
        }
        
        nuclearInteractionLength = (NILinv <= 0.0 ? MAX_NUCLEAR_INTERACTION_LENGTH : 1.0/NILinv);
        nuclearInteractionLengthWithDensity = nuclearInteractionLength / getDensity();

        return NILinv;
    }
    
    /** compute X0 (radiation length) based on mass fractions of elements */
    private double computeRadiationLengthForMixture()
    {
        double rlinv = 0.0;
        
        for (int i = 0; i < nElements; i++)
        {
            MaterialElement me = elementList.get(i);
            rlinv += massFractionList.get(i) / me.getRadiationLength();
        }
        
        radiationLength = (rlinv <= 0.0 ? MAX_RADIATION_LENGTH : 1.0/rlinv);
        radiationLengthWithDensity = radiationLength / getDensity();
        
        return radiationLength;
    }
    
    /** Caches computed quantities after all components have been added to the material. */
    private void computeDerivedQuantities()
    {                
        computeZeff();        
        computeAeff();        
        //computeEffectiveNumberOfNucleons();
        //computeMolecularWeigth();
        computeRadiationLengthForMixture();
        computeNuclearInteractionLengthForMixture();
        computeCriticalEnergy();
        computeMoliereRadius();    			    
    }    
    
    /** @return total number of (unique) elements in the element list for this material */
    public int getNumberOfElements()
    {
        return nElements;
    }
    
    /** @return a list of elements referenced by this material */
    public List<MaterialElement> getElements()
    {
        return elementList;
    }
    
    /** @return a list of atom counts for each element */
    public List<Integer> getAtomCounts()
    {
    	return atomCountList;
    }
    
    /** @return corresponding mass fractions for each element in the material */
    public List<Double> getMassFractions()
    {
        return massFractionList;
    }
    
    /**
     * Add an element to the material by atom count.
     *
     * Based on Geant4's G4Material::AddElement() .
     */
    public void addElement(MaterialElement element, int nAtoms)
    {
        if ( nElements < nComponentsMax )
        {
            elementList.add(element);
            atomCountList.add( nElements, nAtoms);
            nComponentsAdded = ++nElements;
        }
        else
        {
            throw new RuntimeException("Attempting to add more than declared number of elements for this material: " + getName());
        }
        
        if ( isFilled() )
        {
            double Amol = 0;
            
            int atomCnt = 0;
            for ( MaterialElement e : elementList )
            {
                Amol += atomCountList.get(atomCnt) * e.getA();
                ++atomCnt;
            }
            
            int massCnt = 0;
            for ( MaterialElement ee : elementList )
            {
                massFractionList.add(atomCountList.get(massCnt) * ee.getA() / Amol);
                ++massCnt;
            }
            
            computeDerivedQuantities();
        }
    }
    
    /**
     * Add element to the material by fraction of mass. (out of 1.0)
     */
    public void addElement(MaterialElement element,
            double fraction)
    {
        if ( nComponentsAdded < nComponentsMax )
        {
            int elCnt = 0;
            while (( elCnt < nElements ) && element != elementList.get(elCnt)) elCnt++;
            
            /* Element already exists.  Increase the mass fraction. */
            if (elCnt < nElements)
            {
                double currFrac = massFractionList.get(elCnt);
                currFrac += fraction;
                massFractionList.add(elCnt, currFrac);
            }
            /* Element does not exist.  Add a new mass fraction. */
            else
            {
                elementList.add(element);
                massFractionList.add(elCnt, fraction);
                ++nElements;
            }
            ++nComponentsAdded;
        }
        else
        {
            throw new RuntimeException("Attempting to add more than declared number of components to material: " + getName() );
        }
        
        if ( isFilled() )
        {
            checkMassSum();
            computeDerivedQuantities();
        }
    }
    
    /** Add material by fraction of mass. */
    public void addMaterial(
    		MaterialMixture material,
            double fraction)
    {                
        if ( atomCountList.size() > 0 )
        {
            throw new RuntimeException("Material is already defined by atoms: " + getName());
        }
        
        if ( nComponentsAdded < nComponentsMax )
        {
            /* Loop over elements in the material. */
            for ( int i = 0; i < material.getNumberOfElements(); i++)
            {
                MaterialElement element = material.getElements().get(i);
                int elCnt = 0;
                /* Find the element. */
                while ((elCnt < nElements) && (element != elementList.get(elCnt)))
                {
                    ++elCnt;
                }
                
                /* Add fraction to existing element. */
                if ( elCnt < nElements )
                {
                    double currFrac = massFractionList.get(elCnt);
                    currFrac += fraction * material.getMassFractions().get(i) ;
                    massFractionList.set(elCnt, currFrac);
                }
                /* Add a new element. */
                else
                {
                    //System.out.println("adding mass fraction: " + element.getName() + "=" + fraction * material.getMassFractions().get(i) );
                    
                    elementList.add(element);
                    
                    /**
                     * Add the mass fraction of this element, scaled by its percentage in the material's
                     * mass fraction list.
                     */
                    massFractionList.add(elCnt, fraction * material.getMassFractions().get(i));
                    ++nElements;
                }
            }
            ++nComponentsAdded;
        }
        else
        {
            throw new RuntimeException("Attempting to add more than declared number of components for material: " + getName() );
        }
        
        if ( isFilled() )
        {
            checkMassSum();
            computeDerivedQuantities();
        }
    }
    
    /** @return true if all 1st-level (non-recursive) component elements and materials have been added */
    public boolean isFilled()
    {
        return (nComponentsAdded == nComponentsMax);
    }
    
    /** Check that the massFractions list adds up to 1.0 */
    private void checkMassSum()
    {
        double weightSum = 0;
        for ( int i = 0; i < massFractionList.size(); i++)
        {
            weightSum += massFractionList.get(i);
        }
        
        if ( abs(1 - weightSum) > 0.001 )
        {
            throw new RuntimeException("Mass fractions do not sum to 1 within 0.001 tolerance for this material: " + getName() );
        }
    }
    
    /** Compute effective Z for this material using element list. */
    private double computeZeff()
    {
        double ZsumNotNorm = 0;
        double atomCntFracSum = 0;
        
        int nelem = this.getNElements();
        for ( int i = 0; i < nelem; i++ )
        {
            MaterialElement me = this.getElements().get(i);
            double massFrac = this.getMassFractions().get(i);
            double atomCntFrac = massFrac / me.getA();
            ZsumNotNorm += atomCntFrac * me.getZ();
            atomCntFracSum += atomCntFrac;
        }
        
        double ZsumNorm = ZsumNotNorm / atomCntFracSum;
        Zeff = ZsumNorm;
        return Zeff;
    }
    
    /** Compute effective A for this material using element list. */
    private double computeAeff()
    {
        double AsumNotNorm = 0;
        double atomCntFracSum = 0;
        
        int nelem = this.getNElements();
        for ( int i = 0; i < nelem; i++ )
        {
            MaterialElement me = this.getElements().get(i);
            double massFrac = this.getMassFractions().get(i);
            double atomCntFrac = massFrac / me.getA();
            AsumNotNorm += atomCntFrac * me.getA();
            atomCntFracSum += atomCntFrac;
        }
        double ZsumNorm = AsumNotNorm / atomCntFracSum;
        Aeff = ZsumNorm;
        return Aeff;
    }    
    
    /** Translate this material to human-readable string. */
    public String toString()
    {
    	StringBuffer sb = new StringBuffer();
    	sb.append(super.toString());
    	sb.append("numberOfComponents = " + nComponentsAdded + "; ");
    	sb.append("maxNumberOfComponents = " + nComponentsMax + "; ");
    	sb.append("numberOfElements = " + nElements);                                  
        
        for ( int i = 0; i < this.getNElements(); i++)
        {
        	sb.append('\n');
        	sb.append('\t');
            sb.append(this.getElements().get(i).getName() + " ");
            sb.append(this.getMassFractions().get(i) * 100);
        }
        
        return sb.toString();
    }
}