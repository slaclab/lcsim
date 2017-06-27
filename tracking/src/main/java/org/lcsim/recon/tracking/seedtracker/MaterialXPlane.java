/*
 * MaterialXPlane.java
 *
 * Created on March 14, 2011
 *
 */

package org.lcsim.recon.tracking.seedtracker;

/**
 *
 * @author R. Partridge and M. Graham
 */
public class MaterialXPlane {
    private double _ymin;
    private double _ymax;
    private double _zmin;
    private double _zmax;
    private double _x;
    private double _t_RL;
    
    /** Creates a new instance of MaterialCylinder */
    public MaterialXPlane(double ymin, double ymax, double zmin, double zmax, double x, double t_RL) {
        _ymin = ymin;
        _ymax = ymax;
        _zmin = zmin;
        _zmax = zmax;
        _x = x;
        _t_RL = t_RL;
    }
    
    public double ymin() {
        return _ymin;
    }

    public double ymax() {
        return _ymax;
    }

    public double zmin() {
        return _zmin;
    }
    
    public double zmax() {
        return _zmax;
    }
    
    public double x() {
        return _x;
    }

    public double ThicknessInRL() {
        return _t_RL;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("Tracker MaterialXPlane\n");
        sb.append("Y min =  "+_ymin+"\n");
        sb.append("Y max = "+_ymax+"\n");
        sb.append("Z min = "+_zmin+"\n");
        sb.append("Z max = "+_zmax+"\n");
        sb.append("Thickness (in RL) = "+_t_RL+"\n");
        return sb.toString();
    }    
}