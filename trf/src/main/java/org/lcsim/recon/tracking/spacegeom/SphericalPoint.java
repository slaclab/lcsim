package org.lcsim.recon.tracking.spacegeom;
/** A Spherical SpacePoint
 *
 *@author Norman A. Graf
 *@version 1.0
 *@see SpacePoint
 */

public class SphericalPoint extends SpacePoint
{
    /**
     * Constructor.
     * Constructs a SpacePoint with Spherical coordinates
     * @param r          Spherical radius coordinate
     * @param phi        Spherical phi coordinate
     * @param theta      Spherical theta coordinate
     */
    public SphericalPoint(double r, double phi, double theta)
    {
        _xyz = r;
        _phi = phi;
        _theta = theta;
        _xy = r*Math.sin(theta);
        _x = _xy*Math.cos(phi);
        _y = _xy*Math.sin(phi);
        _z = r*Math.cos(theta);
    }
}