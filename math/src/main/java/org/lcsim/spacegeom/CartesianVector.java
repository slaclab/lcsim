package org.lcsim.spacegeom;

import static java.lang.Math.sqrt;
import static org.lcsim.spacegeom.Representation.Cartesian;
/**
 *
 *@version $Id: CartesianVector.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
public class CartesianVector extends SpaceVector
{
	/**
	 * Constructs a new Vector from the first three dimensions of an array
	 * @param x The coordinate array
	 */
    public CartesianVector(double[] x) {
    	this(x[0], x[1], x[2]);
    }
    /**
     * Constructs a new Vector from 3 Cartesian co-ordinates
     * @param x
     * @param y
     * @param z
     */
    public CartesianVector(double x, double y, double z)
    {
        _x = x;
        _y = y;
        _z = z;
        _representation = Cartesian;
        _phi = Double.NaN;
        _theta = Double.NaN;
        _xy = Double.NaN;
        _xyz = sqrt(_x*_x + _y*_y + _z*_z);
    }
}
