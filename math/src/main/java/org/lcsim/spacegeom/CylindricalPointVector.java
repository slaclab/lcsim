package org.lcsim.spacegeom;
/** A Cylindrical SpacePointVector
 *@author Norman A. Graf
 *@version $Id: CylindricalPointVector.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 *
 */

public class CylindricalPointVector extends SpacePointVector
{
    
    /** Constructor
     * CylindricalPointVector
     * @param r    Cylindrical radius coordinate
     * @param phi  Cylindrical phi coordinate
     * @param z    Cylindrical z coordinate
     * @param vr   Cylindrical radius vector component
     * @param vphi Cylindrical phi vector component
     * @param vz   Cylindrical z vector component
     */
    public CylindricalPointVector(double r, double phi, double z, double vr, double vphi, double vz)
    {
        startPoint = new CylindricalPoint(r, phi, z);
        direction = new CylindricalVector(vr, vphi, vz);
    }
    
    /**Constructor
     * CylindricalPointVector
     * @param spt SpacePoint for this points position
     * @param vr   Cylindrical radius vector component
     * @param vphi Cylindrical phi vector component
     * @param vz   Cylindrical z vector component
     */
    public CylindricalPointVector(SpacePoint spt, double vr, double vphi, double vz)
    {
        startPoint = spt;
        direction = new CylindricalVector(vr, vphi, vz);
    }
}