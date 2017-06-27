package org.lcsim.recon.tracking.spacegeom;
/** A Cylindrical SpacePath
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class CylindricalPath extends SpacePath
{
    /** Construct a cylindrical path from coordinates and path components.
     * @param r
     * @param phi
     * @param z
     * @param dr
     * @param r_dphi
     * @param dz
     */
    public CylindricalPath(double r, double phi, double z,  double dr, double r_dphi, double dz)
    {
        super( new CylindricalPointVector(r,phi,z,dr,r_dphi,dz) );
    }
    
    /** Construct a cylindrical path from a space point and path components.
     * @param spt
     * @param dr
     * @param r_dphi
     * @param dz
     */
    public CylindricalPath( SpacePoint spt, double dr, double r_dphi, double dz)
    {
        super( new CylindricalPointVector(spt,dr,r_dphi,dz) );
    }
    
}
