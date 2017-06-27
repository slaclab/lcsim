package org.lcsim.recon.tracking.trfxyp;

import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
import org.lcsim.recon.tracking.spacegeom.CartesianPoint;
import org.lcsim.recon.tracking.spacegeom.CartesianPath;

import org.lcsim.recon.tracking.trfbase.Surface;

import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.PureStat;
import org.lcsim.recon.tracking.trfbase.TrackSurfaceDirection;


/**
 * Defines the pure suface corresponding to a plane parallel to
 * the z-axis.
 *
 * The corresponding track parameters are:
 * <li>u (cm) is fixed
 * <li>0 - v (cm)
 * <li>1 - z (cm)
 * <li>2 - dv/du
 * <li>3 - dz/du
 * <li>4 - q/p   p is momentum of a track, q is its charge
 *<p>
 * (u,v,z) forms a right-handed orthogonal coordinate system
 *<p>
 * dist is closest distance between the  z-axis and the plane
 * phi is an angle normal to the plane forms with the x-axis
 * phi is between 0 and 2*pi
 * phi  is positive counterclockwise from x to u
 * This class serves as a base class for bounded cylinders.
 *<p>
 * The forward direction for the surface is +u.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class SurfXYPlane extends Surface
{
    
    
    
    // enums identifying surface parameters
    
    public static final int NORMPHI = 0;
    public static final int DISTNORM = 1;
    // track parameter indices
    
    public static final int IV=0;
    public static final int IZ=1;
    public static final int IDVDU=2;
    public static final int IDZDU=3;
    public static final int IQP=4;
    
    
    
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "SurfXYPlane"; }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    protected double _normphi;
    protected double _distnorm;
    
    
    
    // Return true if two surfaces have the same pure surface.
    // Argument may be safely downcast.
    protected boolean safePureEqual( Surface srf)
    {
        double s_phi = ((SurfXYPlane ) srf)._normphi;
        double s_dist= ((SurfXYPlane ) srf)._distnorm;
        return ( Math.abs(s_phi - _normphi)< 1e-7 && Math.abs(s_dist-_distnorm) < 1e-7);
    }
    
    // Return true if this surface and the argument are in order.
    // See Surface::pure_less_than() for more information.
    // Argument may be safely downcast.
    protected boolean safePureLessThan( Surface srf)
    {
        double s_phi = (( SurfXYPlane ) srf)._normphi;
        double s_dist= ((SurfXYPlane )srf)._distnorm;
        return   ( _distnorm < s_dist - 1e-7 ) ||
                ( Math.abs(_distnorm - s_dist) < 1e-7  && _normphi < s_phi -1e-7 );
    }
    
    
    
    /**
     * Construct an instance from the shortest distance to the plane from the z axis
     * and the phi angle of the normal to the plane.
     * @param distnorm The shortest distance to the plane from the z axis.
     * @param normphi The angle of the normal to the plane with respect to the x axis.
     */
    public SurfXYPlane(double distnorm, double normphi)
    {
        // Check if phi is between 0 and 2pi
        Assert.assertTrue( normphi<TRFMath.TWOPI && normphi>=0. );
        Assert.assertTrue( distnorm >= 0.);
        _normphi = normphi;
        _distnorm = distnorm;
    }
    
    /**
     * Construct an instance duplicating the SurfXYPlane (copy constructor).
     * @param sxyp The SurfZPlane to replicate.
     */
    public SurfXYPlane( SurfXYPlane sxyp)
    {
        _normphi = sxyp._normphi;
        _distnorm = sxyp._distnorm;
    }
    
    /**
     *Return a String representation of the class'  type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public String pureType()
    { return  staticType(); }
    
    /**
     * Return a copy of the underlying pure Surface.
     *
     * @return The underlying SurfZPlane.
     */
    public Surface newPureSurface()
    {
        return new SurfXYPlane(_distnorm,_normphi);
    }
    
    /**
     *Find the crossing status for a track vector without error.
     *
     * @param   trv The VTrack to test.
     * @return The crossing status.
     */
    public CrossStat pureStatus(VTrack trv)
    {
        // If the track surface is the same as this, return at
        Surface srf = trv.surface();
        if ( srf.equals(this) || pureEqual(srf) ) return new CrossStat(PureStat.AT);
        // Otherwise extract the space point and set flags using u.
        double xtrk = trv.spacePoint().x();
        double ytrk = trv.spacePoint().y();
        double cphi = Math.cos(_normphi);
        double sphi = Math.sin(_normphi);
        double utrk = xtrk*cphi + ytrk*sphi;
        double usrf = _distnorm;
        double prec = CrossStat.staticPrecision();
        if ( Math.abs(utrk-usrf) < prec ) return new CrossStat(PureStat.ON);
        if ( utrk > usrf ) return new CrossStat(PureStat.OUTSIDE);
        return new CrossStat(PureStat.INSIDE);
    }
    
    /**
     *Return the surface parameter.
     *
     * @param   ipar The surface parameter index.
     * There are two surface parameters for an XYPlane:
     *          DISTNORM is the shortest distance to the plane from the z axis.
     *          NORMPHI is the angle of the normal to the plane with respect to the x axis.
     * @return  The shortest distance to the plane from the z axis if ipar==DISTNORM.
     *          The angle of the normal to the plane with respect to the x axis. if ipar==NORMPHI.
     */
    public double parameter(int ipar)
    {
        if ( ipar == NORMPHI ) return _normphi;
        if ( ipar == DISTNORM) return _distnorm;
        return 0.0;
    }
    
    /**
     * Return the vector difference of two tracks on this surface.
     *
     * @param   vec1 The first TrackVector.
     * @param   vec2 The second TrackVector.
     * @return The difference TrackVector.
     */
    public TrackVector vecDiff(TrackVector vec1,
            TrackVector vec2)
    {
        TrackVector diff = new TrackVector(vec1);
        diff = diff.minus(vec2);
        return diff;
    }
    
    /**
     *Return the space point for a track vector.
     *
     * @param   vec The TrackVector at this Surface.
     * @return The SpacePoint for the track vec on this Surface.
     */
    public SpacePoint spacePoint(TrackVector vec)
    {
        double u   = _distnorm;
        double cphi = Math.cos(_normphi);
        double sphi = Math.sin(_normphi);
        double x =  u*cphi - vec.get(IV)*sphi;
        double y =  u*sphi + vec.get(IV)*cphi;
        return new CartesianPoint( x , y, vec.get(IZ) );
        
    }
    
    // Return the space vector for a track. (v,z,dv/du,dz/du,q/p)
    // du/ds = 1/sqrt(1+dv/du**2+dz/du**2)
    // dv/ds = dv/du*du/ds
    // dz/ds = dz/du*du/ds
    // phi is positive counterclockwise from x to u
    
    /**
     *Return the space vector for a track: (v,z,dv/du,dz/du,q/p).
     *  du/ds = 1/sqrt(1+dv/du**2+dz/du**2)
     * dv/ds = dv/du*du/ds
     * dz/ds = dz/du*du/ds
     * phi is positive counterclockwise from x to u
     * @param   vec The TrackVector at this Surface.
     * @param   dir The direction for this track on this surface.
     * @return The SpacePath for this track on this surface.
     */
    public SpacePath spacePath(TrackVector vec,
            TrackSurfaceDirection dir)
    {
        double u     = _distnorm;
        double v     = vec.get(IV);
        double z     = vec.get(IZ);
        double dv_du = vec.get(IDVDU);
        double dz_du = vec.get(IDZDU);
        
        double cphi =  Math.cos(_normphi);
        double sphi = Math.sin(_normphi);
        
        double x =  u*cphi - v*sphi;
        double y =  u*sphi + v*cphi;
        
        double du_ds = 1./Math.sqrt(1.+dv_du*dv_du+dz_du*dz_du);
        if ( dir.equals(TrackSurfaceDirection.TSD_BACKWARD) ) du_ds *= -1.0;
        else Assert.assertTrue( dir.equals(TrackSurfaceDirection.TSD_FORWARD) );
        double dv_ds = dv_du*du_ds;
        
        double dz_ds = dz_du*du_ds;
        double dx_ds =  du_ds*cphi - dv_ds*sphi;
        double dy_ds =  du_ds*sphi + dv_ds*cphi;
        
        return new CartesianPath(x, y, z, dx_ds, dy_ds, dz_ds);
    }
    
    /**
     *output stream
     *
     * @return The String representation of this instance.
     */
    public String toString()
    {
        return "XY plane at phi = " + _normphi+
                " and distance = "+ _distnorm;
    }
    
}