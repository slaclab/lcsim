/*
 * ScatterAngle.java
 *
 * Created on January 21, 2008, 10:20 PM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

/**
 *
 * @author Richard Partridge
 * @version 1.0
 */
public class ScatterAngle implements Comparable {
    private double _pathlen;
    private double _angle;
    
    /** Creates a new instance of ScatterAngle */
    public ScatterAngle(double pathlen, double angle) {
        _pathlen = pathlen;
        _angle = angle;
    }
    
    public double PathLen() {
        return _pathlen;
    }
    
    public double Angle() {
        return _angle;
    }
    
    public int compareTo(Object scatter2) {
        double s2 = ((ScatterAngle) scatter2).PathLen();
        if (_pathlen < s2) return -1;
        if (_pathlen == s2) return 0;
        return 1;
    }    
}