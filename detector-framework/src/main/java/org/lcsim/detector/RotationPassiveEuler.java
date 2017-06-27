/*
 * RotationPassiveEuler.java
 *
 * Created on July 31, 2007, 3:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector;


/**
 * 
 * Represents a passive Euler rotation in 3D space.
 * 
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 */
public class RotationPassiveEuler extends Rotation3D
{
    
    /** Creates a new instance of RotationPassiveEuler */
    public RotationPassiveEuler(double phi, double theta, double psi)
    {
        super(makeRotation(phi,theta,psi));
    }
  
    // Static Method
    //===============
    public static IRotation3D makeRotation(double phi, double theta, double psi)
    {
        return multiply(passiveZRotation(psi),multiply(passiveXRotation(theta),passiveZRotation(phi)));
    }
    
}
