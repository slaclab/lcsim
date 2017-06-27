package org.lcsim.recon.tracking.spacegeom;
/** A Cartesian SpacePath
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class CartesianPath extends SpacePath
{
    
    /** Constructor from coordinates and direction.
     * @param x Cartesian x coordinate
     * @param y Cartesian y coordinate
     * @param z Cartesian z coordinate
     * @param dx Cartesian path element dx coordinate
     * @param dy Cartesian path element dy coordinate
     * @param dz Cartesian path element dz coordinate
     */
    public CartesianPath(double x, double y, double z,double dx, double dy, double dz)
    {
        super( new CartesianPointVector(x,y,z,dx,dy,dz) );
    }
    
    
    /** Constructor from space point and direction.
     * @param spt The SpacePoint for the position coordinates
     * @param dx Cartesian path element dx coordinate
     * @param dy Cartesian path element dy coordinate
     * @param dz Cartesian path element dz coordinate
     */
    public CartesianPath(SpacePoint spt, double dx, double dy, double dz)
    {
        super( new CartesianPointVector(spt,dx,dy,dz) );
    }
    
}