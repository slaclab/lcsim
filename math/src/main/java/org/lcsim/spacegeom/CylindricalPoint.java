package org.lcsim.spacegeom;
import static org.lcsim.spacegeom.Representation.Cylindrical;
/** A Cylindrical SpacePoint
 *
 *@author Norman A. Graf
 *@version $Id: CylindricalPoint.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 *@see SpacePoint
 */
import static java.lang.Math.sqrt;

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
        _representation = Cylindrical;
        _x = Double.NaN;
        _y = Double.NaN;
        _xyz = sqrt(_xy*_xy+_z*_z);
        _theta = Double.NaN;
    }
}