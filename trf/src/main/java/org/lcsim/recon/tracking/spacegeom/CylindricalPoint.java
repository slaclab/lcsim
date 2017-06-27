package org.lcsim.recon.tracking.spacegeom;
/** A Cylindrical SpacePoint
 *
 *@author Norman A. Graf
 *@version 1.0
 *@see SpacePoint
 */
public class CylindricalPoint extends SpacePoint
{
    
    /** Constructor.
     * Constructs a SpacePoint with Cylindrical coordinates
     * @param r    Cylindrical radius coordinate
     * @param phi  Cylindrical phi coordinate
     * @param z    Cylindrical z coordinate
     */
    public CylindricalPoint(double r, double phi, double z)
    {
        _xy = r;
        _phi = phi;
        _z = z;
        _x = r*Math.cos(phi);
        _y = r*Math.sin(phi);
        _xyz = Math.sqrt(_xy*_xy+_z*_z);
        _theta = Math.atan2(_xy,_z);
    }
}