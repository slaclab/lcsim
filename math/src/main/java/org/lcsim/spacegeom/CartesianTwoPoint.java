package org.lcsim.spacegeom;
/** A Cartesian TwoSpacePoint
 *
 *@author Norman A. Graf
 *@version $Id: CartesianTwoPoint.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
public class CartesianTwoPoint extends TwoSpacePoint
{
    
    /**
     *Constructor.
     * @param x Cartesian x coordinate
     * @param y Cartesian y coordinate
     */
    public  CartesianTwoPoint(double x, double y)
    {
        _x = x;
        _y = y;
        _xy = Math.sqrt(_x*_x+_y*_y);
        _phi = Math.atan2(_y,_x);
    }
    
}

