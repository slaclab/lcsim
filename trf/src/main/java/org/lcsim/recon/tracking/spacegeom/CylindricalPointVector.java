package org.lcsim.recon.tracking.spacegeom;
/** A Cylindrical SpacePointVector
 *@author Norman A. Graf
 *@version 1.0
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
        super(new CylindricalPoint(r,phi,z));
        _vx = cosPhi()*vr - sinPhi()*vphi;
        _vy = sinPhi()*vr + cosPhi()*vphi;
        _vz = vz;
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
        super(spt);
        _vx = cosPhi()*vr - sinPhi()*vphi;
        _vy = sinPhi()*vr + cosPhi()*vphi;
        _vz = vz;
    }
}