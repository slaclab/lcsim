package org.lcsim.spacegeom;
/** A differential path element vector ds in Cartesian,
 * cylindrical or spherical coordinates.  It is constructed using a
 * subclass and then elements can be fetched for any of these coordinate
 * systems.  The stored vector can also include scalar quantities.
 * Typical applications include velocity ds/dt and direction ds/d|s|.
 *<p>
 * Transformations (translations, rotations, etc) are not provided.
 * It is expected these will be carried out by external functions which
 * return new SpacePath objects.
 *
 *<p>
 * Three orthogonal coordinate systems are:
 *<ul>
 *<li>   Cartesian: (x, y, z)
 *<li>   Cylindrical: (rxy, phi, z)
 *<li>   Spherical: (rxyz, theta, phi) .
 *</ul>
 * These coordinates may be obtained via the space point interface.
 *
 *<p>
 * The corresponding path element vectors are:
 *<ul>
 *<li>   Cartesian: (dx, dy, dz)
 *<li>   Cylindrical: (drxy, rxy*dphi, dz)
 *<li>   Spherical: (drxyz, rxyz*dtheta, rxy*dphi) .
 *</ul>
 * These elements may be accessed via the usual space vector interface
 * (v_x(), v_y(), ...) or via the methods defined here.
 *<p>
 *@author Norman A. Graf
 *@version $Id: SpacePath.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 *@deprecated this class does not offer additional functionality to SpacePointVector
 *
 */
@Deprecated public class SpacePath extends SpacePointVector
{
    
    // methods
    
    /** Default constructor.
     *
     */
    public SpacePath( )
    {
        super();
    }
    
    /** Constructor from a space vector.
     * @param svec the SpacePointVector from which to contruct
     */
    public SpacePath( SpacePointVector svec )
    {
        super(svec);
    }
    
    
    /** Cartesian dx.
     * @return delta x
     */
    public double dx( )
    {
        return v_x();
    }
    
    /** Cartesian dy.
     * @return delta y
     */
    public double dy( )
    {
        return v_y();
    }
    
    /** Cartesian or cylindrical dz.
     * @return Cartesian or cylindrical dz.
     */
    public double dz( )
    {
        return v_z();
    }
    
    /** Cylindrical drxy.
     * Can be well defined at rxy=0.
     * @return Cylindrical dradius
     */
    public double drxy( )
    {
        return v_rxy();
    }
    
    /** Cylindrical or spherical rxy*dphi.
     * Can be well defined at rxy=0.
     * @return radius*dphi (2d radius)
     */
    public double rxy_dphi( )
    {
        return v_phi();
    }
    
    /** Cylindrical or spherical dphi.
     * Not well defined at rxy=0.
     * @return dphi (not well defined at r=0)
     */
    public double dphi( )
    {
        return v_phi()/v_rxy();
    }
    
    /** Spherical rxyz.
     * Can be well defined at rxyz=0.
     * @return spherical radius
     */
    public double drxyz( )
    { return v_rxyz(); }
    
    /** Spherical rxyz*dtheta.
     * Can be well defined at rxyz=0.
     * @return spherical radius*dtheta
     */
    public double rxyz_dtheta( )
    {
        return v_theta();
    }
    
    /** Spherical dtheta.
     * Not well defined at rxyz=0;
     * @return Spherical dtheta.
     */
    public double dtheta( )
    {
        return v_theta()/v_rxyz();
    }
    
    
    /** Output stream
     *
     *
     * @return String representation of the object
     */
    public String toString()
    {
        return super.toString()
        + "SpacePath: " + "\n"
                + "       dx: " + dx() + "\n"
                + "       dy: " + dy() + "\n"
                + "       dz: " + dz() + "\n"
                + "     drxy: " + drxy() + "\n"
                + "    drxyz: " + drxyz() + "\n"
                + "  rt*dphi: " + rxy_dphi() + "\n"
                + " r*dtheta: " + rxyz_dtheta() + "\n"
                + "Magnitude: " + magnitude() + "\n";
    }
    
}

