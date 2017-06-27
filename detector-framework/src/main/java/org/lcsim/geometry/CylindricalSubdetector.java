package org.lcsim.geometry;

/**
 * 
 * @author tonyj
 * 
 *         FIXME: Subdetectors should have a "has a" rather than "is a" relationship to
 *         shapes. FIXME: Need Cylinder shape extending Solid.
 * 
 */
public interface CylindricalSubdetector
{
    public double getInnerRadius();

    public double getOuterRadius();

    public double getZMin();

    public double getZMax();
}
