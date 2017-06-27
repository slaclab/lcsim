/*
 * TrackDirection.java
 *
 * Created on May 8, 2008, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.fit.helicaltrack;

import hep.physics.matrix.Matrix;
import hep.physics.vec.Hep3Vector;

/**
 * Encapsulate the track direction and direction derivatives.
 * @author Richard Partridge
 */
public class TrackDirection {
    private Hep3Vector _dir;
    private Matrix _deriv;
    
    /**
     * Creates a new instance of TrackDirection.
     * @param dir track direction
     * @param deriv derivative of the track direction wrt helix parameters
     */
    public TrackDirection(Hep3Vector dir, Matrix deriv) {
        for (double d : dir.v()){
            if (Double.isNaN(d)){
                throw new RuntimeException("NaN in track direction"); 
            }
        }
        _dir = dir;
        _deriv = deriv;
    }
    
    /**
     * Return the track direction.
     * @return track direction
     */
    public Hep3Vector Direction() {
        return _dir;
    }
    
    /**
     * Return the direction derivatives.  In the direction derivative matrix,
     * the row identifies the cartesian direction coordinate and the
     * column identifies the helix parameter.
     * @return direction derivatives
     */
    public Matrix Derivatives() {
        return _deriv;
    }
}
