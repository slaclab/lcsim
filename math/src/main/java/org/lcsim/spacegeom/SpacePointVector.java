package org.lcsim.spacegeom;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.io.*;
import static hep.physics.vec.VecOp.sub;
import static hep.physics.vec.VecOp.add;
import static hep.physics.vec.VecOp.mult;


/** A vector at a point in space.
 * The vector can be constructed
 * in Cartesian, cylindrical or spherical coordinates and components can be
 * fetched for any of these systems.  The default constructor creates
 * a vector of zero length at the origin.  Finite vectors of nonzero length
 * at any point can be constructed using subclasses.
 *<p>
 * The point and vector stored in a space vector cannot be modified except
 * by assignment from another space vector.
 *<p>
 * Transformations (translations, rotations, etc) are not provided.
 * It is expected these will be carried out by external functions which
 * return new SpacePointVector objects.
 *<p>
 * The three orthogonal coordinate systems are defined in the usual way:
 *<ul>
 *<li>   Cartesian: (x, y, z)
 *<li>   Cylindrical: (rxy, phi, z)
 *<li>   Spherical: (rxyz, theta, phi) .
 *</ul>
 * <br>
 * The rotation from Cartesian to cylindrical:
 * <br clear="all" /><table border="0" width="100%"><tr><td>
 * <table align="center"><tr><td nowrap="nowrap" align="center">
 * </td><td align="left" class="cl"><font face="symbol">
 * <br /><br /><br /><br />
 * <br /><br />
 * </font> </td><td nowrap="nowrap" align="center">
 * <table>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td></tr>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * <font face="symbol">-</font
 * > sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td></tr>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 1 </td></tr></table></td></tr></table>
 * </td><td nowrap="nowrap" align="center">
 * </td><td align="left" class="cl"><font face="symbol">
 * <br /><br /><br /><br />
 * <br /><br />
 * </font></td><td nowrap="nowrap" align="center">
 * </td></tr></table>
 * </td></tr></table>
 *
 *
 * <br>
 * The rotation from cylindrical to Cartesian:
 * <br clear="all" /><table border="0" width="100%"><tr><td>
 * <table align="center"><tr><td nowrap="nowrap" align="center">
 * </td><td align="left" class="cl"><font face="symbol">
 * <br /><br /><br /><br />
 * <br /><br />
 * </font> </td><td nowrap="nowrap" align="center">
 * <table>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * <font face="symbol">-</font
 * > sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td></tr>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td></tr>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 1 </td></tr></table></td></tr></table>
 * </td><td nowrap="nowrap" align="center">
 * </td><td align="left" class="cl"><font face="symbol">
 * <br /><br /><br /><br />
 * <br /><br />
 * </font></td><td nowrap="nowrap" align="center">
 * </td></tr></table>
 * </td></tr></table>
 *
 *
 *
 * <br>
 * The rotation from Cartesian to spherical:
 *
 * <br clear="all" /><table border="0" width="100%"><tr><td>
 * <table align="center"><tr><td nowrap="nowrap" align="center">
 * </td><td align="left" class="cl"><font face="symbol">
 * <br /><br /><br /><br />
 * <br /><br />
 * </font> </td><td nowrap="nowrap" align="center">
 * <table>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * sin<font face="symbol">J</font
 * >cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * sin<font face="symbol">J</font
 * >sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">J</font
 * > </td></tr></table></td></tr>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">J</font
 * >cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">J</font
 * >sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * <font face="symbol">-</font
 * > sin<font face="symbol">J</font
 * > </td></tr></table></td></tr>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * <font face="symbol">-</font
 * > sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td></tr></table>
 * </td><td nowrap="nowrap" align="center">
 * </td><td align="left" class="cl"><font face="symbol">
 * <br /><br /><br /><br />
 * <br /><br />
 * </font></td><td nowrap="nowrap" align="center">
 * </td></tr></table>
 * </td></tr></table>
 *
 *
 * <br>
 * The rotation from spherical to Cartesian:
 *
 * <br clear="all" /><table border="0" width="100%"><tr><td>
 * <table align="center"><tr><td nowrap="nowrap" align="center">
 * </td><td align="left" class="cl"><font face="symbol">
 * <br /><br /><br /><br />
 * <br /><br />
 * </font> </td><td nowrap="nowrap" align="center">
 * <table>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * sin<font face="symbol">J</font
 * >cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">J</font
 * >cos<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * <font face="symbol">-</font
 * > sin<font face="symbol">j</font
 * > </td></tr></table></td></tr>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * sin<font face="symbol">J</font
 * >sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">J</font
 * >sin<font face="symbol">j</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">j</font
 * > </td></tr></table></td></tr>
 * <tr><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * cos<font face="symbol">J</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * <font face="symbol">-</font
 * > sin<font face="symbol">J</font
 * > </td></tr></table></td><td align="center"><table border="0"><tr><td nowrap="nowrap" align="center">
 * 0 </td></tr></table></td></tr></table>
 * </td><td nowrap="nowrap" align="center">
 * </td><td align="left" class="cl"><font face="symbol">
 * <br /><br /><br /><br />
 * <br /><br />
 * </font></td><td nowrap="nowrap" align="center">
 * </td></tr></table>
 * </td></tr></table>
 *
 *
 *@author Norman A. Graf
 *@version $Id: SpacePointVector.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 *
 */

public class SpacePointVector implements Serializable, Cloneable
{
    SpacePoint startPoint;
    SpaceVector direction;
    
    public SpacePointVector()
    {
        startPoint = new SpacePoint();
        direction = new SpaceVector();
    }
    
    /**
     * Copy Constructor
     */
    public SpacePointVector(SpacePointVector spv)
    {
        startPoint = spv.startPoint;
        direction = spv.direction;
    }
    
    public SpacePointVector(SpacePoint start, SpaceVector dir)
    {
        startPoint = start;
        direction = dir;
    }
    
    public SpacePointVector(SpacePoint start, SpacePoint end)
    {
        startPoint = start;
        // This is expensive
        direction = new SpaceVector(sub(end, start));
    }
    
    public Object clone()
    {
        SpacePoint newStart = (SpacePoint) startPoint.clone();
        SpaceVector newDirection = new SpaceVector(direction);
        return new SpacePointVector(newStart, newDirection);
    }
    
    /**
     * v_x
     * @return double
     */
    public double v_x()
    {
        return direction.x();
    }
    
    /**
     * v_y
     * @return double
     */
    public double v_y()
    {
        return direction.y();
    }
    
    /**
     * v_z
     * @return double
     */
    public double v_z()
    {
        return direction.z();
    }
    
    /**
     * v_rxy
     * @return double
     */
    public double v_rxy()
    {
        return direction.rxy();
    }
    
    /**
     * v_phi
     * @return double
     */
    public double v_phi()
    {
        return direction.phi();
    }
    
    /**
     * v_rxyz
     * @return double
     */
    public double v_rxyz()
    {
        return direction.rxyz();
    }
    
    /**
     * v_theta
     * @return double
     */
    public double v_theta()
    {
        return direction.theta();
    }
    
    /**
     * magnitude
     * @return double
     */
    public double magnitude()
    {
        return direction.magnitude();
    }
    
    public boolean equals(SpacePointVector spv)
    {
        return startPoint.equals(spv.startPoint) &&
                direction.equals(spv.direction);
    }
    /**
     * @return !
     * @param spv
     */
    public boolean notEquals(SpacePointVector spv)
    {
        return ! equals(spv);
    }
    
    public SpacePoint getStartPoint()
    {
        return startPoint;
    }
    
    public SpacePoint getEndPoint()
    {
        return new SpacePoint(add(startPoint, direction));
    }
    
    public SpaceVector getDirection()
    {
        return direction;
    }
    
    /**
     * The SpacePath can be parametrized by its length.
     * Determines the point in space at a distance alpha
     * from the Origin.
     * @param alpha The length parameters. All real values are valid.
     * @return A SpacePoint at Origin + alpha* (Endpoint - Origin)
     */
    public SpacePoint getPointAtLength(double alpha)
    {
        return new SpacePoint(add(startPoint, mult(alpha, direction)));
    }
    
    public String toString()
    {
//        return super.toString()
        return startPoint.toString()
        + "SpacePointVector:" + "\n"
                + "      V_x: " + v_x() + "\n"
                + "      V_y: " + v_y() + "\n"
                + "      V_z: " + v_z()  + "\n"
                + "    V_rxy: " + v_rxy()  + "\n"
                + "   V_rxyz: " + v_rxyz()  + "\n"
                + "   V_dphi: " + v_phi() + "\n"
                + "  V_theta: " + v_theta() + "\n"
                + "Magnitude: " + magnitude() + "\n";
    }
}








