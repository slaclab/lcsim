package org.lcsim.spacegeom;
import static org.lcsim.spacegeom.Representation.Spherical;
/** A Spherical SpacePoint
 *
 *@author Norman A. Graf
 *@version $Id: SphericalPoint.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
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
        _representation = Spherical;
        _xy = Double.NaN;
        _x = Double.NaN;
        _y = Double.NaN;
        _z = Double.NaN;
    }
}