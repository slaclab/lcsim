package org.lcsim.recon.tracking.spacegeom;
/** A Cartesian SpacePointVector
 *
 *@author Norman A. Graf
 *@version 1.0
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
        super(new CartesianPoint(x,y,z));
        _vx = vx;
        _vy = vy;
        _vz = vz;
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
        super(spt);
        _vx = vx;
        _vy = vy;
        _vz = vz;
    }
}

