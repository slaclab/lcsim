package org.lcsim.recon.tracking.spacegeom;
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
 *@version 1.0
 *
 */
public class SpacePath extends SpacePointVector
{
    
    // methods
    
    /** Default constructor.
     *
     */
    public SpacePath( )
    {
        super();
    }
    
    /** Copy onstructor from a space point.
     * @param spt the Spacepoint to replicate
     */
    public SpacePath( SpacePoint spt )
    {
        super(spt);
    }
    
    /** Constructor from a space vector.
     * @param svec the SpacePointVector from which to contruct
     */
    public SpacePath( SpacePointVector svec )
    {
        super(svec);
    }
    
    /**
     * Sets the origin of the path
     * @param p point in space
     */
    public void setOrigin(SpacePoint p) {
        _x = p._x;
        _y = p._y;
        _z = p._z;
        _xy = p._xy;
        _xyz = p._xyz;
        _phi = p._phi;
        _theta = p._theta;
    }
    
    /**
     * Sets the end point of the path
     * @param p point in space
     */
    public void setEndPoint(SpacePoint p) {
        _vx = p._x - _x;
        _vy = p._y - _y;
        _vz = p._z - _z;
    }
    
    /**
     * Returns the origin of the path
     * @return a new SpacePoint object at the origin
     */
    public SpacePoint origin() {
        return new CartesianPoint(_x, _y, _z);
    }
    
    /**
     * Returns the end point of the path
     * @return a new SpacePoint object at the end of the path
     */
    public SpacePoint endPoint() {
        return new CartesianPoint(_x+_vx, _y+_vy, _z+_vz);
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

