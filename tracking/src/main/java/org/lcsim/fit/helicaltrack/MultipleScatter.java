/*
 * MultipleScatter.java
 *
 * Created on November 21, 2007, 1:12 PM
 *
 */

package org.lcsim.fit.helicaltrack;

/**
 * Encapsulate multiple scattering errors in the r-phi and z coordinates.
 * @author Richard Partridge
 * @version 1.0
 */
public class MultipleScatter {
    private double _drphi;
    private double _dz;
    
    /**
     * Creates a new instance of MultipleScatter to encapsulate
     * multiple scattering errors in the r-phi and z coordinates.
     * @param dz multiple scattering uncertainty in the z coordinate
     * @param drphi multiple scattering uncertainty in the r*phi coordinate
     */
    public MultipleScatter(double drphi, double dz) {
        _drphi = drphi;
        _dz = dz;
    }
    
    /**
     * Return the multiple scattering error in the r-phi coordinate
     * @return r*phi coordinate multiple scattering error (units are mm)
     */
    public double drphi() {
        return _drphi;
    }
    
    /**
     * Return the multiple scattering error in z coordinate.
     * @return z coordinate multiple scattering error (units are mm)
     */
    public double dz() {
        return _dz;
    }
 
    @Override
    public String toString() {
        return "[drphi: "+_drphi+" , dz: "+_dz+"]"; 
    }
}