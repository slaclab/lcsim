/*
 * MaterialCylinder.java
 *
 * Created on November 8, 2007, 2:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker;

import org.lcsim.detector.IPhysicalVolume;

/**
 *
 * @author partridge
 */
public class MaterialCylinder {
    private IPhysicalVolume _pv;
    private double _radius;
    private double _zmin;
    private double _zmax;
    private double _t_RL;
    
    /** Creates a new instance of MaterialCylinder */
    public MaterialCylinder(IPhysicalVolume pv, double radius, double zmin, double zmax, double t_RL) {
        _pv = pv;
        _radius = radius;
        _zmin = zmin;
        _zmax = zmax;
        _t_RL = t_RL;
   }
    
    public double radius() {
        return _radius;
    }
    
    public double zmin() {
        return _zmin;
    }
    
    public double zmax() {
        return _zmax;
    }
    
    public double ThicknessInRL() {
        return _t_RL;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("Tracker MaterialCylinder for "+_pv.getName()+"\n");
        sb.append("radius =  "+_radius+"\n");
        sb.append("Z min = "+_zmin+"\n");
        sb.append("Z max = "+_zmax+"\n");
        sb.append("Thickness (in RL) = "+_t_RL+"\n");
        return sb.toString();
    }    
}