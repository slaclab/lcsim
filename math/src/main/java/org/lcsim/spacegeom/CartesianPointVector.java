package org.lcsim.spacegeom;
/** A Cartesian SpacePointVector
 *
 *@author Norman A. Graf
 *@version $Id: CartesianPointVector.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 *
 */

public class CartesianPointVector extends SpacePointVector
{
    
    /** Constructor
     * CartesianPointVector
     * @param x
     * @param y
     * @param z
     * @param vx
     * @param vy
     * @param vz
     */
    public CartesianPointVector(double x, double y, double z, double vx, double vy, double vz)
    {
        startPoint = new CartesianPoint(x, y, z);
        direction = new CartesianVector(vx, vy, vz);
    }
    
    /** Constructor
     * CartesianPointVector
     * @param spt
     * @param vx
     * @param vy
     * @param vz
     */
    public CartesianPointVector(SpacePoint spt, double vx, double vy, double vz)
    {
        startPoint = spt;
        direction = new CartesianVector(vx, vy, vz);
    }
}

