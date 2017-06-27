/*
 * MaterialDisk.java
 *
 * Created on November 8, 2007, 2:19 PM
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
public class MaterialDisk {
    private IPhysicalVolume _pv;
    private double _rmin;
    private double _rmax;
    private double _z;
    private double _t_RL;
    
    /** Creates a new instance of MaterialCylinder */
    public MaterialDisk(IPhysicalVolume pv, double rmin, double rmax, double z, double t_RL) {
        _pv = pv;
        _rmin = rmin;
        _rmax = rmax;
        _z = z;
        _t_RL = t_RL;
   }
    
    public double rmin() {
        return _rmin;
    }
    
    public double rmax() {
        return _rmax;
    }
    
    public double z() {
        return _z;
    }
    
    public double ThicknessInRL() {
        return _t_RL;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("Tracker MaterialDisk "+_pv.getName()+"\n");
        sb.append("Inner radius =  "+_rmin+"\n");
        sb.append("Outer radius = "+_rmax+"\n");
        sb.append("Z = "+_z+"\n");
        sb.append("Thickness (in RL) = "+_t_RL+"\n");
        return sb.toString();
    }    

}
