package org.lcsim.recon.tracking.spacegeom;
/** A Spherical SpacePath
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class SphericalPath extends SpacePath
{
    /** Construct a spherical path from coordinates and path components.
     * Arguments are (dr, r*dtheta, r*sin(theta)*dphi).
     * @param r          Spherical radius coordinate
     * @param theta      Spherical theta coordinate
     * @param phi        Spherical phi coordinate
     * @param dr         Spherical delta radius coordinate
     * @param r_dtheta   Spherical delta theta coordinate
     * @param rt_dphi    Spherical rdelta phi coordinate
     */
    public SphericalPath(double r, double theta, double phi, double dr, double r_dtheta, double rt_dphi)
    {
        super( new SphericalPointVector(r,theta,phi,dr,r_dtheta,rt_dphi) );
    }
    
    /** Constructor from space point and direction.
     * @param spt       Spacepoint for this point's location
     * @param dr         Spherical delta radius coordinate
     * @param r_dtheta   Spherical delta theta coordinate
     * @param rt_dphi    Spherical rdelta phi coordinate
     */
    public SphericalPath( SpacePoint spt, double dr, double r_dtheta, double rt_dphi)
    {
        super( new SphericalPointVector(spt,dr,r_dtheta,rt_dphi) );
    }
    
}
