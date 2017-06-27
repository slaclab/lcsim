package org.lcsim.spacegeom;

/** Spherical SpacePointVector
 * @author Norman A. Graf
 * @version $Id: SphericalPointVector.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
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
        startPoint = new SphericalPoint(r, phi, theta);
        direction = new SphericalVector(vr, vphi, vtheta);
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
        startPoint = spt;
        direction = new SphericalVector(vr, vphi, vtheta);
    }
}