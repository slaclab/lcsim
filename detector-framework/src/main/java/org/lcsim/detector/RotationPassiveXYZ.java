/*
 * RotationPassiveXYZ.java
 *
 * Created on July 31, 2007, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector;


/**
 * Represents a passive rotation in 3D space.
 *
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 */
public class RotationPassiveXYZ extends Rotation3D
{
    
    /** Creates a new instance of RotationPassiveXYZ */
    public RotationPassiveXYZ(double alpha, double beta, double gamma)
    {
        super(makeRotation(alpha,beta,gamma));
    }
   
    // Static Method
    //===============
    public static IRotation3D makeRotation(double alpha, double beta, double gamma)
    {
        return multiply(passiveZRotation(gamma),multiply(passiveYRotation(beta),passiveXRotation(alpha)));
    }
    
}
