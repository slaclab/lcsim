package org.lcsim.recon.tracking.spacegeom;

/** Spherical SpacePointVector
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class SphericalPointVector extends SpacePointVector
{
    
    /** Constructor
     * SphericalPointVector
     * @param r          Spherical radius coordinate
     * @param phi        Spherical phi coordinate
     * @param theta      Spherical theta coordinate
     * @param vr         Spherical delta radius coordinate
     * @param vtheta     Spherical delta theta coordinate
     * @param vphi       Spherical rdelta phi coordinate
     */
    public SphericalPointVector(double r, double phi, double theta, double vr, double vtheta, double vphi)
    {
        super(new SphericalPoint(r,phi,theta));
        _vx = sinTheta()*cosPhi()*vr
                + cosTheta()*cosPhi()*vtheta - sinPhi()*vphi;
        _vy = sinTheta()*sinPhi()*vr
                + cosTheta()*sinPhi()*vtheta + cosPhi()*vphi;
        _vz = cosTheta()*vr - sinTheta()*vtheta;
    }
    
    /**Constructor
     * SphericalPointVector
     * @param spt    SpacePoint for this point's location
     * @param vr         Spherical delta radius coordinate
     * @param vtheta     Spherical delta theta coordinate
     * @param vphi       Spherical rdelta phi coordinate
     */
    public SphericalPointVector(SpacePoint spt, double vr, double vtheta, double vphi)
    {
        super(spt);
        _vx = sinTheta()*cosPhi()*vr
                + cosTheta()*cosPhi()*vtheta - sinPhi()*vphi;
        _vy = sinTheta()*sinPhi()*vr
                + cosTheta()*sinPhi()*vtheta + cosPhi()*vphi;
        _vz = cosTheta()*vr - sinTheta()*vtheta;
    }
}