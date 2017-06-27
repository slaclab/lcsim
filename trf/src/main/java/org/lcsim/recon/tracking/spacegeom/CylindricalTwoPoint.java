package org.lcsim.recon.tracking.spacegeom;
/** A Cylindrical TwoSpacePoint
 *
 *@author Norman A. Graf
 *@version 1.0
 */
public class CylindricalTwoPoint extends TwoSpacePoint
{
    
    /**
     *Constructor.
     * @param   r  Cylindrical radius coordinate
     * @param   phi  Cylindrical phi coordinate
     */
    public  CylindricalTwoPoint(double r, double phi)
    {
        _xy = r;
        _phi = phi;
        _x = r*Math.cos(phi);
        _y = r*Math.sin(phi);
    }
    
}
