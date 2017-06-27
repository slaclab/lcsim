/*
 * RotationGeant.java
 *
 * Created on July 31, 2007, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.BasicHep3Vector;

/**
 *
 * Represents a rotation in 3D space according to the Geant4 conventions.
 *
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 */
public class RotationGeant extends Rotation3D
{
    
    /** Creates a new instance of RotationGeant */
    public RotationGeant(double a, double b, double c)
    {
        super(makeRotation(a,b,c));
    }
    
    // Static Method
    //===============
    public static IRotation3D makeRotation(double phi, double theta, double psi)
    {
        Hep3Vector x_axis = new BasicHep3Vector(1,0,0);
        Hep3Vector y_axis = new BasicHep3Vector(0,1,0);
        Hep3Vector z_axis = new BasicHep3Vector(0,0,1);
        
        // Rotate around body x_axis
        IRotation3D rotation = passiveAxisRotation(phi,x_axis);
        
        // Rotate around body y_axis
        rotation = Rotation3D.multiply(passiveAxisRotation(theta,rotation.rotated(y_axis)),rotation);

        // Rotate around body z_axis
        rotation = Rotation3D.multiply(passiveAxisRotation(psi,rotation.rotated(z_axis)),rotation);
        
        return rotation;
    }
    
}
