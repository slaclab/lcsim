/*
 * BiasSurface.java
 *
 * Created on November 29, 2007, 11:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector.tracker.silicon;

import org.lcsim.detector.solids.Polygon3D;

/**
 *
 * @author tknelson
 */
public class BiasSurface
{
    
    Polygon3D _surface;
    double _voltage;
    
    /** Creates a new instance of BiasSurface */
    public BiasSurface(Polygon3D surface, double voltage)
    {
        _surface = surface;
        _voltage = voltage;
    }
    
    public Polygon3D getSurface()
    {
        return _surface;
    }
    
    public double getVoltage()
    {
        return _voltage;
    }
    
}
