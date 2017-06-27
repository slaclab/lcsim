package org.lcsim.spacegeom;
/** A Cylindrical TwoSpacePoint
 *
 *@author Norman A. Graf
 *@version $Id: CylindricalTwoPoint.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
public class CylindricalTwoPoint extends TwoSpacePoint
{
    
    /**
     *Constructor.
     * @param   r  Cylindrical radius coordinate
     * @param   phi  Cylindrical phi coordinate
     * @deprecated please use CylindricalPoint
     */
    @Deprecated public  CylindricalTwoPoint(double r, double phi)
    {
        _xy = r;
        _phi = phi;
        _x = r*Math.cos(phi);
        _y = r*Math.sin(phi);
    }
    
}
