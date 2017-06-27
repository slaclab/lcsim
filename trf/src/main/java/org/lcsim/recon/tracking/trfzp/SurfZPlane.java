package  org.lcsim.recon.tracking.trfzp;


import org.lcsim.recon.tracking.trfutil.Assert;

import  org.lcsim.recon.tracking.spacegeom.SpacePoint;
import  org.lcsim.recon.tracking.spacegeom.CartesianPoint;
import  org.lcsim.recon.tracking.spacegeom.SpacePath;
import  org.lcsim.recon.tracking.spacegeom.CartesianPath;

import org.lcsim.recon.tracking.trfbase.Surface;

import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.PureStat;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackSurfaceDirection;
/**
 * Defines the pure suface correponding to a plane perpendicular to
 * the z-axis.
 *<p>
 * The corresponding track parameters are:
 * <li>z (cm) is fixed
 * <li>0 - x (cm)
 * <li>1 - y (cm)
 * <li>2 - dx/dz
 * <li>3 - dy/dz
 * <li>4 - q/p   p is momentum of a track, q is its charge
 *<p>
 * This class serves as a base class for bounded cylinders.
 *<p>
 * The forward direction for the surface is +z.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class SurfZPlane extends Surface
{
    
    
    
    // enums identifying surface parameters
    
    public static final int ZPOS = 0;
    
    // track parameter indices
    
    public static final int IX = 0;
    public static final int IY = 1;
    public static final int IDXDZ = 2;
    public static final int IDYDZ = 3;
    public static final int IQP = 4;
    
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "SurfZPlane"; }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    protected double _z;
    
    
    // Return true if two surfaces have the same pure surface.
    // Argument may be safely downcast.
    protected boolean safePureEqual(Surface srf)
    {
        return ( ((SurfZPlane) srf)._z == _z );
    }
    
    // Return true if this surface and the argument are in order.
    // See Surface::pure_less_than() for more information.
    // Argument may be safely downcast.
    protected boolean safePureLessThan(Surface srf)
    {
        double z_n = ((SurfZPlane) srf)._z ;
        return _z < z_n   ;
    }
    
    
    
    //
    
    /**
     *Construct an instance specifying the  z location of the plane.
     *
     * @param   z The z location of the plane surface.
     */
    public SurfZPlane(double z)
    {
        _z = z;
    }
    
    //
    
    /**
     * Construct an instance duplicating the SurfZPlane (copy constructor).
     * @param surf The SurfZPlane to replicate.
     */
    public SurfZPlane(SurfZPlane surf)
    {
        _z = surf._z;
    }
    
    //
    
    /**
     *Return a String representation of the class'  type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public String pureType()
    { return  staticType(); }
    
    //
    
    /**
     * Return a copy of the underlying pure Surface.
     *
     * @return The underlying SurfZPlane.
     */
    public Surface newPureSurface()
    {
        return new SurfZPlane(_z);
        
    }
    
    //
    
    /**
     *Find the crossing status for a track vector without error.
     *
     * @param   trv The VTrack to test.
     * @return The crossing status.
     */
    public CrossStat pureStatus( VTrack trv)
    {
        
        // If the track surface is the same as this, return at
        Surface srf = trv.surface();
        if ( srf==this || pureEqual(srf) ) return new CrossStat(PureStat.AT);
        // Otherwise extract the space point and set flags using z.
        double ztrk = trv.spacePoint().z();
        double zsrf = _z;
        double prec = CrossStat.staticPrecision();
        if ( Math.abs(ztrk-zsrf) < prec ) return new CrossStat(PureStat.ON);
        if ( ztrk > zsrf ) return new CrossStat(PureStat.OUTSIDE);
        return new CrossStat(PureStat.INSIDE);
    }
    
    //
    
    /**
     *Return the surface parameter.
     *
     * @param   ipar The surface parameter index.
     * There is only one for a SurfZPlane, which is the z location.
     * @return The z location for this SurfZPlane.
     */
    public double parameter(int ipar)
    {
        if ( ipar == ZPOS ) return _z;
        return 0.0;
    }
    
    //
    
    /**
     *Return the z position of the plane.
     *
     * @return The z location of this plane.
     */
    public double z()
    { return _z; };
    
    //
    
    /**
     * Return the vector difference of two tracks on this surface.
     *
     * @param   vec1 The first TrackVector.
     * @param   vec2 The second TrackVector.
     * @return The difference TrackVector.
     */
    public TrackVector vecDiff( TrackVector vec1,
            TrackVector vec2)
    {
        TrackVector diff = new TrackVector(vec1);
        diff = diff.minus(vec2);
        return diff;
    }
    
    //
    
    /**
     *Return the space point for a track vector.
     *
     * @param   vec The TrackVector at this Surface.
     * @return The SpacePoint for the track vec on this Surface.
     */
    public SpacePoint spacePoint( TrackVector vec)
    {
        return new CartesianPoint( vec.get(IX) , vec.get(IY), _z );
    }
    
    //
    
    /**
     *Return the space vector for a track. (x,y,dx/dz,dy/dz,q/p)
     * dz/ds = 1/sqrt(1+dx/dz**2+dy/dz**2)
     * dy/ds = dy/dz*dz/ds
     * dx/ds = dx/dz*dz/ds
     *
     * @param   vec The TrackVector at this Surface.
     * @param   dir The direction for this track on this surface.
     * @return The SpacePath for this track on this surface.
     */
    public SpacePath spacePath( TrackVector vec,
            TrackSurfaceDirection dir)
    {
        double x     = vec.get(IX);
        double y     = vec.get(IY);
        double z     = _z;
        double dx_dz = vec.get(IDXDZ);
        double dy_dz = vec.get(IDYDZ);
        
        double dz_ds = 1./Math.sqrt(1.+dx_dz*dx_dz+dy_dz*dy_dz);
        if ( dir.equals(TrackSurfaceDirection.TSD_BACKWARD) ) dz_ds *= -1.0;
        else Assert.assertTrue( dir.equals(TrackSurfaceDirection.TSD_FORWARD) );
        double dy_ds = dy_dz*dz_ds;
        double dx_ds = dx_dz*dz_ds;
        
        return new CartesianPath(x, y, z, dx_ds, dy_ds, dz_ds);
    }
    
    
    /**
     *Test equality.
     *
     * @param   srf The surface to test against.
     * @return true if the surfaces are equal.
     */
    public boolean equals(SurfZPlane srf)
    {
        if(_z != srf._z) return false;
        
        return true;
        
    }
    
    
    /**
     *output stream
     *
     * @return The String representation of this instance.
     */
    public String toString()
    {
        return "Z-plane at z = " + _z;
        
    }
    
}