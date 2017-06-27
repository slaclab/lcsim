/*
 * HelicalTrack2DHit.java
 *
 * Created on November 13, 2007, 1:10 PM
 *
 */

package org.lcsim.fit.helicaltrack;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

import java.util.List;

import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 * Encapsulate 2D hit info needed by HelicalTrackFitter.  This class is
 * applicable to axial barrel strip hits that measure the r-phi coordinate
 * and place limits on the z coordinate.
 * @author Richard Partridge
 * @version 1.0
 */
public class HelicalTrack2DHit  extends HelicalTrackHit {
    private double _zmin;
    private double _zmax;
    private static int _type = 2;
    
    /**
     * Create a HelicalTrack2DHit from the associated TrackerHit and HelicalTrackStrip.
     * @param pos location of the strip center
     * @param cov covariance matrix for the hit
     * @param dEdx deposited energy
     * @param time hit time
     * @param rawhits list of raw hits
     * @param zmin minimum z for the strip
     * @param zmax maximum z for the strip
     * @param detname detector name
     * @param layer layer number
     * @param beflag
     */
    public HelicalTrack2DHit(Hep3Vector pos, SymmetricMatrix cov, double dEdx, double time,
            List rawhits, String detname, int layer, BarrelEndcapFlag beflag, double zmin, double zmax) {
        super(pos, cov, dEdx, time, _type, rawhits, detname, layer, beflag);
        _zmin = zmin;
        _zmax = zmax;        
    }
    
    /**
     * Return the minimum z coordinate.
     * @return minimum z coordinate
     */
    public double zmin() {
        return _zmin;
    }
    
    /**
     * Return the maximum z coordinate.
     * @return maximum z coordinate
     */
    public double zmax() {
        return _zmax;
    }

    /**
     * Return the length of the strip along the z direction.
     * @return strip length
     */
    public double zlen() {
        return _zmax - _zmin;
    }
}