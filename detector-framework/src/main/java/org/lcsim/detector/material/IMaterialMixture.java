package org.lcsim.detector.material;

import java.util.List;

/**
 * This interface is a material mixture that is defined by a list 
 * of sub-materials.  The sub-materials are added by mass fraction 
 * or atom count.  Mixing mass fractions and atom counts in a single
 * definition is not allowed and will cause a fatal error.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: IMaterialMixture.java,v 1.4 2010/04/14 18:24:53 jeremy Exp $
 */
public interface IMaterialMixture 
extends IMaterial
{
    /**
     * Get the total number of components allowed in this mixture.
     * @return The maximum number of allowed components.
     */
	public int getNComponentsMax();
    
    /**
     * The number of components added thusfar to the mixture.
     * @return The number of components added so far.
     */
	public int getNComponents();
    
    /**
     * Get the number of different elements in this mixture.
     * @return The number of different elements in this mixture.
     */
	public int getNElements();	
    
    /**
     * Get a list of elements in this mixture.
     * @return A list of elements in this mixture.
     */
	public List<MaterialElement> getElements();
    
    /**
     * Get the list of mass fractions in this element.  Same order as {@link #getElements()}. 
     * @return
     */
	public List<Double> getMassFractions();
    
    /**
     * Get the list of atom counts in this element.  Same order as {@link #getElements()}.
     * @return The list of atom counts.
     */
	public List<Integer> getAtomCounts();
    
    /**
     * 
     * @return True if {@link #getNComponents()} is equal to {@link #getNComponentsMax()}.
     */
	public boolean isFilled();
}
