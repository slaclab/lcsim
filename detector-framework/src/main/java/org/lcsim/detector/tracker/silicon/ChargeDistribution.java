/*
 * ChargeDistribution.java
 *
 * Created on October 10, 2007, 10:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector.tracker.silicon;

import org.lcsim.detector.ITransform3D;
import hep.physics.vec.Hep3Vector;
import org.lcsim.detector.solids.Transformable;

/**
 *
 * @author tknelson
 */
public interface ChargeDistribution extends Transformable
{
    // Transform this charge distribution into new coordinates in place
    public void transform(ITransform3D transform);
    
    // Charge distribution transformed into new coordinates
    public ChargeDistribution transformed(ITransform3D transform);
    
    // Normalization of distribution
    public double getNormalization();
    
    // Mean of distribution
    public Hep3Vector getMean();
    
    // One standard deviation along given axis
    public double sigma1D(Hep3Vector axis);
    
    // One dimensional upper integral of charge distribution along a given axis 
    public double upperIntegral1D(Hep3Vector axis, double integration_limit);            
}
