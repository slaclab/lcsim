package org.lcsim.recon.tracking.spacegeom;
import java.io.*;
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
 *@version 1.0
 *
 */

public class SpacePointVector extends SpacePoint implements Serializable, Cloneable
{
    // The vector
    double _vx;
    double _vy;
    double _vz;
    
    /**
     * //Default Constructor
     */
    public SpacePointVector()
    {
        _vx = _vy = _vz = 0.0;
    }
    
    /**
     * SpacePointVector
     * Constructor from a space point.
     * // Creates a vector of zero length.
     * @param spt
     */
    public SpacePointVector(SpacePoint spt)
    {
        super(spt);
        _vx = _vy = _vz = 0.0;
    }
    
    //Copy Constructor
    public SpacePointVector(SpacePointVector spv)
    {
        _x = spv.x();
        _y = spv.y();
        _z = spv.z();
        _xy = spv.rxy();
        _xyz = spv.rxyz();
        _phi = spv.phi();
        _theta = spv.theta();
        _vx = spv.v_x();
        _vy = spv.v_y();
        _vz = spv.v_z();
    }
    
    public Object clone()
    {
        Object o = null;
        o = super.clone();
        return o;
    }
    
    /**
     * v_x
     * @return double
     */
    public double v_x()
    { 
        return _vx;
    }
    
    /**
     * v_y
     * @return double
     */
    public double v_y()
    {
        return _vy;
    }
    
    /**
     * v_z
     * @return double
     */
    public double v_z()
    { 
        return _vz;
    }
    
    /**
     * v_rxy
     * @return double
     */
    public double v_rxy()
    {
        return cosPhi()*_vx + sinPhi()*_vy;
    }
    
    /**
     * v_phi
     * @return double
     */
    public double v_phi()
    {
        return -sinPhi()*_vx + cosPhi()*_vy;
    }
    
    /**
     * v_rxyz
     * @return double
     */
    public double v_rxyz()
    {
        return sinTheta()*cosPhi()*_vx
                + sinTheta()*sinPhi()*_vy + cosTheta()*_vz;
    }
    
    /**
     * v_theta
     * @return double
     */
    public double v_theta()
    {
        return cosTheta()*cosPhi()*_vx
                + cosTheta()*sinPhi()*_vy - sinTheta()*_vz;
    }
    
    /**
     * magnitude
     * @return double
     */
    public double magnitude()
    {
        return Math.sqrt( _vx*_vx + _vy*_vy + _vz*_vz );
    }
    
    public boolean equals(SpacePointVector spv)
    {
        return (super.equals(spv) &&
                ( v_x()==spv.v_x() ) &&
                ( v_y()==spv.v_y() ) &&
                ( v_z()==spv.v_z() ) );
    }
    /**
     * @return !
     * @param spv
     */
    public boolean notEquals(SpacePointVector spv)
    {
        return ! equals(spv);
    }
    
    public String toString()
    {
        return super.toString()
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








