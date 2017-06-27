package org.lcsim.recon.tracking.spacegeom;
/** A Cartesian SpacePoint
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class CartesianPoint extends SpacePoint
{
    /**
     * Constructor.
     * Constructs a SpacePoint with Cartesian coordinates
     * @param x Cartesian x coordinate
     * @param y Cartesian y coordinate
     * @param z Cartesian z coordinate
     */
    public CartesianPoint(double x, double y, double z)
    {
        _x   = x;
        _y   = y;
        _z   = z;
        _xy  = Math.sqrt(_x*_x+_y*_y);
        _xyz = Math.sqrt(_xy*_xy+_z*_z);
        _phi = Math.atan2(_y,_x);
        _theta = Math.atan2(_xy,_z);
    }
    
    @Deprecated public CartesianPoint(double[] x) {
        this(x[0], x[1], x[2]);
    }
}