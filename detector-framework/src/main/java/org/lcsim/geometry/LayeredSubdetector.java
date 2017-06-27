/*
 * LayeredSubdetector.java
 * 
 * Created on July 17, 2005, 4:32 PM
 */

package org.lcsim.geometry;

import org.lcsim.geometry.layer.Layering;

/**
 * @deprecated Use {@link org.lcsim.detector.IDetectorElement}.
 * @author jeremym
 */
@Deprecated
public interface LayeredSubdetector extends Subdetector
{
    Layering getLayering();
}