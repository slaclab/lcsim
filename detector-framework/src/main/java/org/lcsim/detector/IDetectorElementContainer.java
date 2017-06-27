package org.lcsim.detector;

import java.util.List;

import org.lcsim.detector.identifier.IIdentifier;

/**
 * DetectorElementContainer extends 
 * {@link java.util.List} and contains 0 
 * or more DetectorElements.  It can be used 
 * to provide special behavior for collections 
 * of DetectorElements.
 * 
 * @see DetectorElement
 * @see IDetectorElement
 * @see java.util.List
 * @see java.util.ArrayList
 * 
 * @author Jeremy McCormick
 * @version $Id: IDetectorElementContainer.java,v 1.8 2007/05/25 20:16:26 jeremy Exp $
 */
public interface IDetectorElementContainer 
extends List<IDetectorElement>
{      
    /**
     * Find an {@link IDetectorElement} by class.
     * @param  klass The class.
     * @return A <code>List</code> of <code>DetectorElement</code> objects with matching class.
     */
    public IDetectorElementContainer find(Class<? extends IDetectorElement> klass);    
    
    /**
     * Find an {@link IDetectorElement} by name.
     * 
     * @param  name The name of the DetectorElement.
     * @return      The DetectorElement matching name or
     *              <code>null</code> if none exists.
     */
    public IDetectorElementContainer find(String name);
    
    /**
     * Find an {@link IDetectorElement} by {@link IIdentifier}.
     * 
     * @param  id The id of the DetectorElement.
     * @return    The DetectorElement matching id or
     *            <code>null</code> if none exists.
     */
    public IDetectorElementContainer find(IIdentifier id);   
}