package org.lcsim.spacegeom;

import static org.lcsim.spacegeom.Representation.Spherical;
/**
 *
 *@version $Id: SphericalVector.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
public class SphericalVector extends SpaceVector
{
    
    /**
     * Constructs a Vector from spherical co-ordinates
     * @param r
     * @param phi
     * @param theta
     */
    public SphericalVector(double r, double phi, double theta)
    {
        _representation = Spherical;
        _xyz = r;
        _phi = phi;
        _theta = theta;
        _x = _y = _z = _xy = Double.NaN;
    }
    
}
