package org.lcsim.recon.tracking.trfbase;
/**
 *
 * This class describes the crossing of a track with a surface.
 * It is characterized by seven boolean variables and provide methods
 * to fetch the state of each.
 *
 * The variables include:
 *<pre>
 *   Surface:
 *     at - the track and surface have the same pure surface
 *
 *   Normal:
 *     on - the track vector is on the surface
 *     inside - the track vector is inside the surface
 *     outside - the track vector is outside the surface
 *
 *   Boundary:
 *     bounds_checked - boundaries have been checked
 *     in_bounds - at surface and within boundaries
 *     out_of_bounds - at surface and outside boundaries
 *</pre>
 * The vector state on is true if the track is with a distance
 * PRECISION of the surface.  This value is nonzero to allow
 * for roundoff errors.  If the track is not on, then it is either
 * inside or outside--the surface defines the meaning of those.
 * Exactly one of these states can be true.
 *
 * The state bounds_checked is true and the states in_bounds and
 * out_of_bounds are defined only if the track is at the surface
 * and the surface is bounded.  By definition, a bounded surface
 * assigns each point in track parameter space as either in bounds
 * or out of bounds.  The state in_bounds (out_of_bounds) is true
 * if any in bounds (out of bounds) points are within distance
 * PRECISION of the track volume.  The track volume is defined by
 * expanding the error ellipsoid by NSIGMA.  Note that it is
 * possible for both states to be true.
 *
 * Here are the allowed pure states and their names:
 *<pre>
 *  at  on in out
 *   0   1  0  0      ON
 *   0   0  1  0      INSIDE
 *   0   0  0  1      OUTSIDE
 *   1   1  0  0      AT
 *</pre>
 * Here are the allowed bound states (pure state must be AT):
 *<pre>
 *   0   0     UNDEFINED_BOUNDS
 *   1   0     IN_BOUNDS
 *   0   1     OUT_OF_BOUNDS
 *   1   1     BOTH_BOUNDS
 *</pre>
 * The crossing status is constructed from
 * <ol>
 * <li> a pure state alone (leaving its bound state UNDEFINED_BOUNDS),
 * <li> a bound state alone (pure state is set to AT), or
 * <li> another crossing status (copy constructor).
 *</ol>
 * The state of the class is set by the constructor and cannot
 * be modified.
 *
 * The methods get_precision() and get_nsigma() return the values
 * to used for PRECISION and NSIGMA.  Programmers can inherit from
 * this class to change these values.  If the base class is used,
 * the values can also be obtained through the static methods
 * get_static_precision() and get_static_nsigma().
 *
 *@author Norman A. Graf
 *@version 1.0
 */
public class CrossStat
{
    
    
    // static attributes
    
    // precision
    private static double _precision = 1.0e-14;
    
    // Magnification factor for error ellipsoid.
    private static double _nsigma = 5.0;
    
    // static methods
    
    //
    
    /**
     * Return the precision parameter for crossing.
     *
     * @return the precision parameter for crossing.
     */
    public static double staticPrecision()
    { return _precision; }
    
    //
    
    /**
     * Return sigma for error ellipsoid.
     *
     * @return  number of sigma with which to expand the track error ellipsoid.
     */
    public static double staticNSigma()
    { return _nsigma; }
    
    // attributes
    
    // Pure state
    private PureStat _pure;
    
    // Bound state
    private BoundedStat _bound;
    
    // methods
    
    
    
    //
    
    /**
     * Constructor from a pure state.
     *
     * @param  pure PureStat with which to construct.
     */
    public CrossStat(PureStat pure)
    {
        _pure = pure;
        _bound = BoundedStat.UNDEFINED_BOUNDS;
    }
    
    //
    
    /** Constructor from a bound state.
     *
     * @param bound BoundedStat from which to construct this CrossStat
     */
    public CrossStat(BoundedStat bound)
    {
        _pure = PureStat.AT;
        _bound = bound;
    }
    
    //
    
    /**
     * Copy constructor.
     *
     * @param   xstat  CrossStat to copy.
     */
    public CrossStat(CrossStat xstat)
    {
        _pure = xstat._pure;
        _bound = xstat._bound;
    }
    
    //
    
    /**
     * Is track at the surface?
     *
     * @return true if track is at surface.
     */
    public boolean at()
    { return _pure==PureStat.AT; }
    
    //
    
    /**
     * Is the the track position on the the surface?
     *
     * @return true if  track position is on the the surface.
     */
    public boolean on()
    { return _pure==PureStat.ON || _pure==PureStat.AT; }
    
    //
    
    /**
     * Is the track off the surface on the inside?
     *
     * @return  true if track is off the surface on the inside.
     */
    public boolean inside()
    { return _pure==PureStat.INSIDE; }
    
    //
    
    /**
     * Is the track off the surface on the outside?
     *
     * @return     true if track is off the surface on the outside.
     */
    public boolean outside()
    { return _pure==PureStat.OUTSIDE; }
    
    // Have the bounds been checked?
    
    /**
     * Have the bounds been checked?
     *
     * @return true if   the bounds have been checked.
     */
    public boolean boundsChecked()
    { return _bound!=BoundedStat.UNDEFINED_BOUNDS; }
    
    //
    
    /**
     * Is the track in bounds?
     *
     * @return true if the track is in bounds.
     */
    public boolean inBounds()
    {
        return _bound==BoundedStat.IN_BOUNDS || _bound==BoundedStat.BOTH_BOUNDS;
    }
    
    //
    
    /**
     * Is the track out of bounds?
     *
     * @return  true if the track is out of bounds.
     */
    public boolean outOfBounds()
    {
        return _bound==BoundedStat.OUT_OF_BOUNDS || _bound==BoundedStat.BOTH_BOUNDS;
    }
    
    //
    
    /**
     * Return the precision.
     *
     * @return precision of matching for track crossing.
     */
    public double precision()
    { return staticPrecision(); }
    
    /**
     * Return sigma for error ellipsoid.
     *
     * @return  number of sigma with which to expand the track error ellipsoid.
     */
    public double nSigma()
    { return staticNSigma(); }
    
    // Output stream.
    
    /**
     * String representation of CrossStat.
     *
     * @return     String representation of CrossStat.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+" \n"
                +"\n at=            "+at()
                +"\n on=            "+on()
                +"\n inside=        "+inside()
                +"\n outside=       "+outside()
                +"\n in_bounds=     "+inBounds()
                +"\n out_of_bounds= "+outOfBounds();
    }
    
}
