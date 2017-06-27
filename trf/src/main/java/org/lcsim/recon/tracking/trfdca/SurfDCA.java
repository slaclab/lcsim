package org.lcsim.recon.tracking.trfdca;


import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

import org.lcsim.recon.tracking.spacegeom.CylindricalPointVector;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
import org.lcsim.recon.tracking.spacegeom.CartesianPoint;
import org.lcsim.recon.tracking.spacegeom.CartesianPath;
import org.lcsim.recon.tracking.spacegeom.CylindricalPath;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;

import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.PureStat;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackSurfaceDirection;
import org.lcsim.recon.tracking.trfbase.VTrack;

/**
 * Defines the pure surface corresponding to the point (Distance)
 * of Closest Approach (DCA) of a track from a given origin.
 *<p>
 * The surface parameter is the angle alpha from the radial direction
 * to the direction of the track:
 * <li>  alpha = phi_direction-phi_position .
 * By definition, alpha = +/- pi/2 for the DCA surface.
 * <p>
 * A track on a DCA surface would normally be parametrized by:
 * (r, phi_position, z, tan(lamda), q/pT).
 * In order to make the parameters continous when a track crosses
 * the origin, however, a modified set of parameters has been chosen:
 *<p>
 * <li>alpha_positive = abs(alpha) = pi/2        for the DCA surface,
 * <li> r
 * <li>z
 * <li>phi_direction
 * <li>tan(lamda)
 * <li>q/pT)
 *<p>
 * where:
 * <li>  r_signed = sign(alpha) * r
 * <li>  phi_direction = alpha + phi_position
 *<p>
 * The method get_parameter is designed to always return +pi/2
 * and the method safe_pure_equal is designed to always return true.
 *
 * The crossing status of a track and the DCA surface is defined
 * as following:
 *<pre>
 *  at      - the pure surface of the track is the DCA surface
 *  on      - the track vector is on the DCA surface,
 *            i.e. the angle from the radial direction to
 *            the direction of the track is equal to pi/2
 *            (in absolute value)
 *  inside  - the track vector points towards the origin,
 *            i.e. the angle from the radial direction to
 *            the direction of the track is greater than pi/2
 *            (in absolute value)
 *  outside - the track vector points away from the origin,
 *            i.e. the angle from the radial direction to
 *            the direction of the track is less than pi/2
 *            (in absolute value)
 *</pre>
 * Note that all angles are defined from -pi to pi.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class SurfDCA extends Surface
{
    
    
    // Parameters   ******************************************
    
    // Track parameters.
    public static final int IRSIGNED=0;
    public static final int IZ = 1;
    public static final int IPHID=2;
    public static final int ITLM=3;
    public static final int IQPT=4;
    
    // Surface Parameters
    public static final int IX =0;
    public static final int IY=1;
    public static final int IDXDZ=2;
    public static final int IDYDZ=3;
    
    private double _x;
    private double _y;
    private double _dxdz;
    private double _dydz;
    
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class'  type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public static String typeName()
    { return "SurfDCA";
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName();
    }
    
    // Methods   *********************************************
    
    
    
    //
    
    /**
     *Construct a default instance .
     *
     */
    
    public SurfDCA()
    {
        this(0., 0., 0., 0.);
    }
    
    /**
     *Construct a default instance .
     *
     */
    
    public SurfDCA(double x, double y)
    {
        this(x, y, 0., 0.);
    }
    
    
    /**
     *Construct a fully qualified instance .
     *
     */
    
    public SurfDCA(double x, double y, double dxdz, double dydz)
    {
        _x = x;
        _y = y;
        _dxdz = dxdz;
        _dydz = dydz;
    }
    
    
    /**
     * Return a copy of the underlying pure Surface.
     *
     * @return The underlying SurfCylinder.
     */
    public Surface newPureSurface()
    {
        return new SurfDCA();
        
    }
    
    //
    
    /**
     *Return a String representation of the class'  type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public String pureType()
    { return staticType();
    }
    
    //
    
    /**
     *Return the surface parameter.
     *
     * @param   ipar The surface parameter index. There is none for a DCA.
     * @return 0.
     */
    public double parameter(int ipar)
    {
        Assert.assertTrue( ipar == IX || ipar == IY || ipar == IDXDZ || ipar == IDYDZ );
        if( ipar == IX ) return _x;
        else if ( ipar == IY ) return _y;
        else if ( ipar == IDXDZ ) return _dxdz;
        else return _dydz;
    }
    
    
    /**
     *output stream
     *
     * @return The String representation of this instance.
     */
    public String toString()
    {
        return "DCA Surface to ("+_x+","+_y+") with slope ("+_dxdz+","+_dydz+")";
    }
    
    //
    
    /**
     *Find the crossing status for a track vector without error.
     *
     * @param   trv The VTrack to test.
     * @return The crossing status.
     */
    public CrossStat pureStatus(VTrack trv )
    {
        // If the track surface is the same as this, return at.
        Surface srf = trv.surface();
        if ( equals(srf)  || pureEqual(srf) ) return new CrossStat(PureStat.AT);
        // Otherwise extract the space vector and set flags using alpha.
        double   vrtrk = trv.spacePath().v_rxy();
        double vphitrk = trv.spacePath().v_phi();
        double tan_a = vphitrk/vrtrk;
        double atrk = Math.atan(vphitrk/vrtrk);
        if ( (tan_a > 0.0) && (vrtrk < 0.0) )
        {
            atrk = Math.atan(vphitrk/vrtrk) - Math.PI;
        }
        if ( (tan_a < 0.0) && (vrtrk < 0.0) )
        {
            atrk = Math.atan(vphitrk/vrtrk) + Math.PI;
        }
        double asrf = TRFMath.PI2;
        double prec = CrossStat.staticPrecision();
        if ( Math.abs(atrk-asrf) < prec ) return new CrossStat(PureStat.ON);
        if ( Math.abs(atrk) < asrf ) return new CrossStat(PureStat.OUTSIDE);
        return new CrossStat(PureStat.INSIDE);
    }
    
    //
    
    /**
     * Return the vector difference of two tracks on this surface.
     *
     * @param   vec1 The first TrackVector.
     * @param   vec2 The second TrackVector.
     * @return The difference TrackVector.
     */
    public TrackVector vecDiff(TrackVector vec1,TrackVector vec2 )
    {
        TrackVector diff = new TrackVector(vec1);
        diff = diff.minus(vec2);
        diff.set(IPHID, TRFMath.fmod2( diff.get(IPHID), TRFMath.TWOPI ));
        return diff;
        
    }
    
    //
    
    /**
     *Return the space point for a track vector.
     *
     * @param   vec The TrackVector at this Surface.
     * @return The SpacePoint for the track vec on this Surface.
     */
    public SpacePoint spacePoint(TrackVector vec )
    {
        double r    = Math.abs(vec.get(0));
        double z    = vec.get(1);
        double sign = 0.0;
        double phi  = 0.0;
        if ( vec.get(0) != 0.0 )
        {
            sign = vec.get(0)/Math.abs(vec.get(0));
            phi  = vec.get(2)-(sign*TRFMath.PI2);
            phi  = TRFMath.fmod2( phi, TRFMath.TWOPI );
        }
        double x = r*Math.cos(phi) + _x + _dxdz*z;
        double y = r*Math.sin(phi) + _y + _dydz*z;
        return new CartesianPoint( x, y, z );
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
    public SpacePath spacePath(TrackVector vec,
            TrackSurfaceDirection dir )
    {
        /*
        double r    = Math.abs(vec.get(0));
        double z    = vec.get(1);
        double sign = 0.0;
        double phi  = 0.0;
        if ( vec.get(0) != 0.0 )
        {
            sign = vec.get(0)/Math.abs(vec.get(0));
            phi  = vec.get(2)-(sign*TRFMath.PI2);
            phi  = TRFMath.fmod2( phi, TRFMath.TWOPI );
        }
        double tlam = vec.get(3);
        double salf = sign;
        double calf = 0.0;
        double slam = tlam/Math.sqrt(1.0+tlam*tlam);
        double clam = 1.0/Math.sqrt(1.0+tlam*tlam);
        double dr_ds     = clam*calf;
        double r_dphi_ds = clam*salf;
        double dz_ds     = slam;
        return new CylindricalPath(r, phi, z, dr_ds, r_dphi_ds, dz_ds);
         */
        double r    = Math.abs(vec.get(0));
        double z    = vec.get(1);
        double sign = 1.0;
        if(vec.get(0) < 0.0) sign = -1.;
        double phi  =  vec.get(2)-(sign*TRFMath.PI2);
        phi  = TRFMath.fmod2( phi, TRFMath.TWOPI );
        
        double tlam = vec.get(3);
        double salf = sign;
        double calf = 0.0;
        double slam = tlam/Math.sqrt(1.0+tlam*tlam);
        double clam = 1.0/Math.sqrt(1.0+tlam*tlam);
        double dr_ds     = clam*calf;
        double r_dphi_ds = clam*salf;
        double dz_ds     = slam;
        SpacePointVector sp = new CylindricalPointVector(r, phi, z, dr_ds, r_dphi_ds, dz_ds);
        double x = sp.x() + _x + _dxdz*z;
        double y = sp.y() + _y + _dydz*z;
        return new CartesianPath(x, y, sp.z(), sp.x(), sp.y(), sp.z());
        
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
        double tlam = vec.get(ITLM);
        double clam = 1.0/Math.sqrt(1.0+tlam*tlam);
        Assert.assertTrue( clam != 0.0 );
        return vec.get(IQPT)*clam;
        
    }
    
    
    
    // Return true if two surfaces have the same pure surface.
    // Argument may be safely downcast.
    protected boolean safePureEqual( Surface srf)
    {
        return true;
    }
    
    // Return true if two surfaces are ordered.
    // Argument may be safely downcast.
    protected boolean safePureLessThan( Surface srf)
    {
        return false;
    }
    
    //
    
    /**
     *Return the direction.
     * Forward is radially outward.
     *
     * @param   vec The TrackVector on this Surface.
     * @return The direction of trackvec on this surface.
     */
    public  TrackSurfaceDirection direction( TrackVector vec)
    {
        return TrackSurfaceDirection.TSD_FORWARD;
    }
    
    
    // Accessors.
    public double x()
    {
        return x(0.);
    }
    public double y()
    {
        return y(0.);
    }
    public double x(double z)
    {
        return _x + _dxdz*z;
    }
    public double y(double z)
    {
        return _y + _dydz*z;
    }
    public double dXdZ()
    {
        return _dxdz;
    }
    public double dYdZ()
    {
        return _dydz;
    }
    
    
}
