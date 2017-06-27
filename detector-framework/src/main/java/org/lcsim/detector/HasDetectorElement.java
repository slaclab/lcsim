package org.lcsim.detector;

/**
 * Mixin interface for accessing an {@link IDetectorElement} associated
 * with an object.  
 * 
 * @see IDetectorElement
 * 
 * @author Jeremy McCormick
 * @version $Id: HasDetectorElement.java,v 1.2 2008/05/23 06:49:00 jeremy Exp $ 
 */
public interface HasDetectorElement
{
    /**
      * Get the {@link IDetectorElement} associated with this object.
      */
    public IDetectorElement getDetectorElement();


    /**
      * Set the {@link IDetectorElement} associated with this object.
      */
    public void setDetectorElement(IDetectorElement de);
}
