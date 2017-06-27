package org.lcsim.spacegeom;
/** A Cartesian SpacePoint
 *
 *@author Norman A. Graf
 *@version $Id: CartesianPoint.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 *
 */
import static org.lcsim.spacegeom.Representation.Cartesian;

public class CartesianPoint extends SpacePoint
{
	protected CartesianPoint() {
		
	}
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
        _representation = Cartesian;
        _xy  = Double.NaN;
        _xyz = Math.sqrt(_x*_x + _y*_y + _z*_z);
        _phi = Double.NaN;
        _theta = Double.NaN;
    }
    
    /**
     * Constructs a CartesianSpacePoint from a legacy vector
     * @param x
     */
    public CartesianPoint(double[] x)
    {
        this(x[0], x[1], x[2]);
    }
}