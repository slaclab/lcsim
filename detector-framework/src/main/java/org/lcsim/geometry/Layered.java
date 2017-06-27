package org.lcsim.geometry;

import org.lcsim.geometry.layer.Layering;

/**
 * 
 * @deprecated Use {@link org.lcsim.detector.IDetectorElement}.
 * 
 * @author jeremym
 */
@Deprecated
public interface Layered
{
    Layering getLayering();
}
