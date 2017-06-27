/*
 * Transformable.java
 *
 * Created on November 23, 2007, 4:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector.solids;

import org.lcsim.detector.ITransform3D;

/**
 *
 * @author tknelson
 */
public interface Transformable
{
    public void transform(ITransform3D transform);
    
    public Transformable transformed(ITransform3D transform);
}
