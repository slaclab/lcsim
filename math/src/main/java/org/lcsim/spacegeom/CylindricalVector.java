package org.lcsim.spacegeom;

import static java.lang.Math.sqrt;
import static org.lcsim.spacegeom.Representation.Cylindrical;
/**
 *
 *@version $Id: CylindricalVector.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
public class CylindricalVector extends SpaceVector
{
    
    /**
     * Constructs a vector in cylindrical co-cordinates
     * @param r
     * @param phi
     * @param z
     */
    public CylindricalVector(double r, double phi, double z)
    {
        _xy = r;
        _phi = phi;
        _z = z;
        _xyz = sqrt(r*r+z*z);
        _representation = Cylindrical;
        _x = _y = _theta = Double.NaN;
    }
    
}
