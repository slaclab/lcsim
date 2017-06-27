package org.lcsim.recon.tracking.trfxyp;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
import org.lcsim.recon.tracking.spacegeom.CartesianPoint;
import org.lcsim.recon.tracking.spacegeom.CartesianPath;

import org.lcsim.recon.tracking.trfbase.Surface;

import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.PureStat;
import org.lcsim.recon.tracking.trfbase.BoundedStat;
import org.lcsim.recon.tracking.trfbase.TrackSurfaceDirection;


/**
 * A bounded XYPlane with rectangular boundaries in (v,z).
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class BSurfXYPlane extends SurfXYPlane
{
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "BSurfXYPlane"; }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    
    
    // lower v-limit
    private double _vmin;
    
    // upper v-limit
    private double _vmax;
    
    // lower z-limit
    private double _zmin;
    
    // upper z-limit
    private double _zmax;
    
    // bounded equality
    protected boolean safeBoundEqual( Surface srf)
    {
        BSurfXYPlane bxyp = (BSurfXYPlane) srf;
        return super.safePureEqual(srf) &&
                _vmin == bxyp._vmin  && _vmax == bxyp._vmax &&
                _zmin == bxyp._zmin  && _zmax == bxyp._zmax;
    }
    
    // calculate crossing status from VTrack and error in (v,z)_track
    // This is used by both the public status methods.
    private CrossStat status(VTrack trv, double dvtrk, double dztrk)
    {
        CrossStat xstat = super.pureStatus(trv);
        // If track is not at surface exit with bounds flags undefined.
        if ( !xstat.at() ) return xstat;
        Assert.assertTrue( ! xstat.boundsChecked() );
        // Calculate whether v track +/- nsigma is in and/or out of bounds.
        double vtrk = trv.vector().get(SurfXYPlane.IV);
        double vtrk1 = vtrk - dvtrk;
        double vtrk2 = vtrk + dvtrk;
        
        // Calculate whether z track +/- nsigma is in and/or out of bounds.
        double ztrk = trv.vector().get(SurfXYPlane.IZ);
        double ztrk1 = ztrk - dztrk;
        double ztrk2 = ztrk + dztrk;
        
        // fully out of bounds
        if( (vtrk2 < _vmin || vtrk1 > _vmax ) ||
                (ztrk2 < _zmin || ztrk1 > _zmax ) )
            return new CrossStat(BoundedStat.OUT_OF_BOUNDS);
        // fully in bounds
        if( (vtrk1 > _vmin  &&  vtrk2 < _vmax ) &&
                (ztrk1 > _zmin  &&  ztrk2 < _zmax ) )
            return new CrossStat(BoundedStat.IN_BOUNDS);
        // must be both
        return new CrossStat(BoundedStat.BOTH_BOUNDS);
    }
    
    /**
     * Construct an instace from the shortest distance to the plane from the z axis
     * and the phi angle of the normal to the plane.
     * @param dist The shortest distance to the plane from the z axis.
     * @param phi The angle of the normal to the plane with respect to the x axis.
     * @param vmin The lower bound in v.
     * @param vmax The upper bound in v.
     * @param zmin The lower bound in z.
     * @param zmax The upper bound in z.
     */
    public BSurfXYPlane(double dist, double phi,
            double vmin, double vmax,
            double zmin, double zmax)
    {
        super(dist,phi);
        _vmin =  vmin;
        _vmax =  vmax;
        _zmin =  zmin;
        _zmax =  zmax;
        Assert.assertTrue  (  _zmax > _zmin );
        Assert.assertTrue  (   _vmax > _vmin );
    }
    
    /**
     * Return the lower bound in v.
     * @return The lower bound in v.
     */
    public double vMin()
    { return _vmin; }
    
    /**
     * Return the upper bound in v.
     * @return The upper bound in v.
     */
    public double vMax()
    { return _vmax; }
    
    /**
     * Return the lower bound in z.
     * @return The lower bound in z.
     */
    public double zMin()
    { return _zmin; }
    
    /**
     * Return the upper bound in v.
     * @return The upper bound in v.
     */
    public double zMax()
    { return _zmax; }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return  staticType(); };
    
    /**
     * Clone this BSurfZPlane.
     * @return a clone of this BSurfZPlane.
     */
    public Surface newSurface()
    {
        
        double phi = parameter(SurfXYPlane.NORMPHI);
        double dist = parameter(SurfXYPlane.DISTNORM);
        return new BSurfXYPlane( dist, phi, _vmin, _vmax, _zmin, _zmax );
    }
    
    /**
     * Calculate the crossing status for a track without error.
     * @param   trv The VTrack to test.
     * @return The crossing status.
     */
    public CrossStat status(VTrack trv)
    {
        double prec = CrossStat.staticPrecision();
        double dvtrk = prec;
        double dztrk = prec;
        return status(trv,dvtrk,dztrk);
    }
    
    /**
     * Calculate the crossing status for a track with error.
     * @param   tre The ETrack to test.
     * @return The crossing status.
     */
    public CrossStat status(ETrack tre)
    {
        double nsigma = CrossStat.staticNSigma();
        double prec = CrossStat.staticPrecision();
        double dvtrk = nsigma*
                Math.sqrt(tre.error().get(SurfXYPlane.IV,SurfXYPlane.IV)) + prec;
        double dztrk = nsigma*
                Math.sqrt(tre.error().get(SurfXYPlane.IZ,SurfXYPlane.IZ)) + prec;
        return status(tre,dvtrk,dztrk);
    }
    
    /**
     *output stream
     *
     * @return The String representation of this instance.
     */
    public String toString()
    {
        return super.toString() + ", vmin " + _vmin + " and vmax " + _vmax
                + ", zmin " + _zmin + " and zmax " + _zmax;
    }
}
