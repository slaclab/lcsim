package org.lcsim.recon.tracking.trfcyl;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.CylindricalPoint;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
import org.lcsim.recon.tracking.spacegeom.CylindricalPath;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.PureStat;
import org.lcsim.recon.tracking.trfbase.TrackSurfaceDirection;
import org.lcsim.recon.tracking.trfbase.TrackVector;

/**
 * Defines the pure suface corresponding to a cylinder with axis
 * along the z-axis.
 *It inherits directly from the trfbase class Surface and is a pure surface.
 *<p>
 * The corresponding track parameters are:
 * <li> r (cm) is fixed
 * <li> 0 - phi
 * <li> 1 - z (cm)
 * <li> 2 - alpha = phi_dir - phi; tan(alpha) = r*dphi/dr
 * <li> 3 - tan(lambda) = dz/dsT
 * <li> 4 - q/pT (1/GeV/c) (pT is component of p parallel to cylinder)
 *<p>
 * The curvature is signed pointing along +z (i.e. phi_dir
 * is increasing if curvature is positive).
 * sin(lambda) = dz/ds;
 *<p>
 * This class serves as a base class for bounded cylinders.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class SurfCylinder extends Surface
{
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "SurfCylinder"; }
    
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    // I think I need to handle the enums this way here. It's not typesafe, but
    // the argument checker in Java will return a run-time Exception
    // if the values are out of range.
    
    // Surface parameters.
    public static final int RADIUS = 0;
    
    // Track parameters.
    public static final int IPHI=0;
    public static final int IZ = 1;
    public static final int IALF=2;
    public static final int ITLM=3;
    public static final int IQPT=4;
    
    // protected (package) attributes
    
    protected double _radius;
    
    // Return true if two surfaces have the same pure surface.
    // Argument may be safely downcast.
    protected boolean safePureEqual( Surface srf)
    {
        return _radius == ((SurfCylinder) srf)._radius;
    }
    
    // Return true if two pure surfaces are ordered.
    // We order in r.
    // Argument may be safely downcast.
    protected boolean safePureLessThan( Surface srf)
    {
        return _radius < ((SurfCylinder) srf)._radius;
    }
    
    //
    
    /**
     *Construct an instance specifying the cylinder radius.
     *
     * @param   radius The radius of the cylindrical surface.
     */
    public SurfCylinder(double radius)
    {
        _radius = radius;
    }
    
    //
    
    /**
     * Construct an instance duplicating the SurfCylinder (copy constructor).
     * @param srf The SurfCylinder to replicate.
     */
    public SurfCylinder( SurfCylinder srf)
    {
        _radius = srf._radius;
    }
    
    //
    
    /**
     *Return a String representation of the class'  type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public String pureType()
    {
        return staticType();
    }
    
    //  public SurfCylinder new_pure_surface()
    
    /**
     * Return a copy of the underlying pure Surface.
     *
     * @return The underlying SurfCylinder.
     */
    public Surface newPureSurface()
    {
        return new SurfCylinder(_radius);
    }
    
    
    /**
     * Return a copy of the underlying Surface.
     *
     * @return The underlying SurfCylinder.
     */
    public Surface newSurface()
    {
        return new SurfCylinder(_radius);
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
        // If the track surface is the same as this, return at.
        Surface srf = trv.surface();
        // Still need some infrastructure here...
        if ( srf.equals(this) || pureEqual(srf) ) return new CrossStat(PureStat.AT);
        // Otherwise extract the space point and set flags using r.
        double rtrk = trv.spacePoint().rxy();
        double rsrf = _radius;
        double prec = CrossStat.staticPrecision();
        if ( Math.abs(rtrk-rsrf) < prec ) return new CrossStat(PureStat.ON);
        if ( rtrk > rsrf ) return new CrossStat(PureStat.OUTSIDE);
        return new CrossStat(PureStat.INSIDE);
    }
    
    //
    
    /**
     *Return the surface parameter.
     *
     * @param   ipar The surface parameter index. There is only one for a SurfCylinder, which is the radius.
     * @return The radius for this SurfCylinder.
     */
    public double parameter(int ipar)
    {
        if( ipar != RADIUS)
        {
            throw new IllegalArgumentException("Wrong SurfCylinder surface parameter!");
        }
        if ( ipar == RADIUS ) return _radius;
        return 0.0;
    }
    
    //
    
    /**
     *Return the radius.
     *
     * @return The radius of this SurfCylinder.
     */
    public double radius()
    { return _radius; }
    
    
    //
    
    /**
     *Return the space point for a track vector.
     *
     * @param   vec The TrackVector at this Surface.
     * @return The SpacePoint for the track vec on this Surface.
     */
    public SpacePoint spacePoint( TrackVector vec)
    {
        return new CylindricalPoint( _radius, vec.vector()[0], vec.vector()[1] );
    }
    
    //
    
    
    /**
     *Return the space vector for a track vector.
     *     dr/ds = cos(lambda)*cos(alpha)
     * r*dphi/ds = cos(lambda)*sin(alpha)
     *     dz/ds = sin(lambda)
     *
     * @param   vec The TrackVector at this Surface.
     * @param   dir The direction for this track on this surface.
     * @return The SpacePath for this track on this surface.
     */
    public SpacePath spacePath( TrackVector vec, TrackSurfaceDirection dir)
    {
        double r = _radius;
        double phi = vec.vector()[0];
        double z = vec.vector()[1];
        double alf = vec.vector()[2];
        double tlam = vec.vector()[3];
        double salf = Math.sin(alf);
        double calf = Math.cos(alf);
        double slam = tlam/Math.sqrt(1.0+tlam*tlam);
        double clam = 1.0;
        if ( tlam != 0.0 ) clam = slam/tlam;
        double dr_ds = clam*calf;
        double r_dphi_ds = clam*salf;
        double dz_ds = slam;
        return new CylindricalPath(r, phi, z, dr_ds, r_dphi_ds, dz_ds);
    }
    
    //
    
    /**
     *Return the signed inverse momentum, q/p.
     *
     * @param   vec The TrackVector on this Surface.
     * @return The signed inverse momentum for track vec.
     */
    public double qOverP( TrackVector vec)
    {
        double tlam = vec.vector()[ITLM];
        double clam = 1.0/Math.sqrt(1.0+tlam*tlam);
        //assert( clam != 0.0 );
        return vec.vector()[IQPT]*clam;
    }
    
    //
    
    /**
     *Return the direction.
     * Forward is radially outward.
     *
     * @param   vec The TrackVector on this Surface.
     * @return The direction of trackvec on this surface.
     */
    public TrackSurfaceDirection direction( TrackVector vec)
    {
        double aalf = Math.abs( TRFMath.fmod2( vec.get(IALF), TRFMath.TWOPI ) );
        Assert.assertTrue( aalf <= Math.PI );
        if ( aalf <= TRFMath.PI2 ) return TrackSurfaceDirection.TSD_FORWARD;
        else return TrackSurfaceDirection.TSD_BACKWARD;
    }
    
    
    /**
     *output stream
     *
     * @return The String representation of this instance.
     */
    public String toString()
    {
        return super.toString() + ": radius= "+_radius;
    }
    
    
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
        double tmp = TRFMath.fmod2( diff.get(SurfCylinder.IPHI), TRFMath.TWOPI );
        diff.set(SurfCylinder.IPHI, tmp);
        return diff;
    }
    
    
}
