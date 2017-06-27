package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;
import org.lcsim.recon.tracking.spacegeom.SpacePath;

/** Base class to describe a surface.
 *<p>
 * There are two conceptual levels of concrete surfaces:
 *  1. Pure surfaces are unbounded and are used to specify the set
 *     of track parameters used in VTrack.  They derive directly
 *     from Surface.  Typically these are named SurfXXX.
 *  2. Bounded surfaces are derived from pure surfaces.
 *     Typically these are named BSurfXXX.
 *<p>
 * Typically the first two track parameters specify the position on the
 * surface, the next two the direction of the track and the last is
 * q/p where q is the charge (in natural units where the electron is -1)
 * and p is the total momentum in GeV/c.  Note that the radius of
 * curvature (in cm) is then Rc = cos(tht) / [ 0.003 * (q/p) * B ]
 * where B is the magnetic field in Tesla and tht is the angle between the
 * field and the direction.  Also 0.003 is actually 0.0029979....
 *<p>
 * This is an abstract class.  Classes VTrack and Hit maintain pointers
 * of this type.
 *
 *
 * <p>
 * Add attributes Interactor and SimInteractor to Surface to allow tracks made up of Hits
 * to be refit, allowing for multiple scattering and energy loss. Will not need
 * Layer to do this for us.
 *<p>
 * add methods interact() and simInteract() to smear and simulate, respectively.
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public abstract class Surface
{
    // attributes...
    private Interactor _interactor;
    private SimInteractor _siminteractor;
    
    // Statics here...
    
    /**
     * The type name for the class
     *
     * @return  String name
     */
    public static String typeName()
    {
        return "Surface";
    }
    
    /**
     *The type name for the class
     *
     * @return String name
     */
    public static String staticType()
    {
        return typeName();
    }
    // Methods here...
    //
    
    /**
     * default constructor
     *
     */
    public Surface()
    {
    }
    
    
    
    /**
     *  Return the full type.
     * This implementation returns the pure type ==>
     * Pure surfaces need not override.
     * Bound surfaces should override.
     *
     * @return  String type name
     */
    public String type()
    {
        return pureType();
    }
    
    //
    
    /**
     * Return the generic type of this class.
     * Subclasses must not override.
     *
     * @return String type name
     */
    public String genericType()
    {
        return staticType();
    }
    
    //
    
    /**
     * Return whether this surface is pure.
     *
     * @return true if Surface is pure
     */
    public boolean isPure( )
    {
        return type().equals(pureType());
    }
    
    
    //
    
    /**
     * Return true if two surfaces are exactly the same
     * including bounds.
     *
     * @param   srf Surface  to be compared to
     * @return  true if both surfaces are exactly the same, including bounds
     */
    public boolean boundEqual(Surface srf)
    {
        if ( type().equals(srf.type()) ) return safeBoundEqual(srf);
        return false;
    }
    
    //
    
    /** Return true if two surfaces have the same pure surface.
     *
     * @return true if two surfaces have the same pure surface.
     * @param srf Surface to compare
     */
    public boolean pureEqual( Surface srf)
    {
        if( pureType().equals(srf.pureType()) ) return safePureEqual(srf);
        return false;
    }
    
    //
    
    /**
     * Ordering operator.
     * Returns true if this surface comes before the
     * argument surface.  Comparison is based on the pure parameters only.
     * As usual exactly one of a<b, b<a and a==b is true and all comparisons
     * are transitive.
     *
     * @param  srf Surface  to be compared to.
     * @return  true if this surface comes before srf.
     */
    public boolean pureLessThan(Surface srf)
    {
        if( pureType().equals(srf.pureType()) ) return safePureLessThan(srf);
        // not sure what to do here... return get_pure_type() < srf.get_pure_type();
        return false;
    }
    
    //
    
    /**
     * Return q/p in 1/GeV/c.  This is proportional to curvature.
     * Override for non-standard fifth track parameter.
     *
     * @param  vec TrackVector
     * @return  Track parameter q/p.
     */
    public double qOverP(TrackVector vec)
    {
        return vec.get(4);
    }
    
    //
    //
    //
    
    
    /**
     *Return the direction consistent with the specified track vector.
     *Subclasses for which the track vector does specify the direction
     * should override this method.
     * If the vector is ambiguous, then VTrack carries an attribute
     * which is used to specify the direction.
     * @param  vec TrackVector
     * @return The default defined here returns TSD_UNDEFINED.
     */
    public TrackSurfaceDirection direction(TrackVector vec)
    {
        return TrackSurfaceDirection.TSD_UNDEFINED;
    }
    
    //cng
    
    //
    
    /**
     * Interact the track parameters.
     * If Surface contains an Interactor, modify tre accordingly.
     *
     * @param tre  ETrack
     */
    public void interact(ETrack tre)
    {
        if(_interactor!=null) _interactor.interact(tre);
    }
    
    //
    
    /**
     *Set the Interactor.
     *
     * @param   interactor TrackInteractor
     */
    public void setInteractor(Interactor interactor)
    {
        _interactor = interactor.newCopy();
    }
    
    //
    
    /**
     *Return the Interactor.
     *
     * @return the Interactor
     */
    public  Interactor getInteractor()
    {
        return _interactor.newCopy();
    }
    
    //
    
    /**
     *smear the track parameters
     *If the Surface contains a SimInteractor, smear the track accordingly.
     * @param  trv VTrack  to be smeared
     */
    public void simInteract(VTrack trv)
    {
        if(_siminteractor!=null) _siminteractor.interact(trv);
    }
    
    //
    
    /**
     *Set the SimInteractor.
     *
     * @param   siminteractor  Interactor to set for this surface
     */
    public void setSimInteractor(SimInteractor siminteractor)
    {
        _siminteractor = siminteractor.newCopy();
    }
    
    
    //
    
    /**
     *Return the SimInteractor.
     *
     * @return  Interactor for this surface
     */
    public  SimInteractor simInteractor()
    {
        return _siminteractor.newCopy();
    }
    //cng
    
    /**
     * String representation of the Surface.
     *
     * @return  String representation of the Surface.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className;
    }
    
    // Methods to implement in all surfaces.  *****************************
    
    
    
    // Return true if two surfaces have the same pure surface.
    // Argument may be safely downcast.
    protected abstract boolean safePureEqual(Surface srf);
    
    // Return true if this surface and the argument are in order.
    // See pure_less_than() for more information.
    // Argument may be safely downcast.
    protected abstract boolean safePureLessThan(Surface srf);
    
    // Return the pure type.
    
    /**
     * pure type of object
     *
     * @return String representation of object's pure type
     */
    public abstract String pureType();
    
    //
    
    /**
     *Return a new surface corresponding to the underlying pure surface.
     *
     * @return  copy of pure surface
     */
    public abstract Surface newPureSurface();
    
    //
    
    /**
     *Return the crossing status for a track without error.
     * Subclasses should override the return type with their own
     * crossing status.
     *
     * @param trv  VTrack
     * @return crossing status for trv.
     */
    public abstract CrossStat pureStatus( VTrack trv);
    
    //
    
    
    /**
     * Return the surface parameter with specified index (0, 1, 2, 3, 4).
     *
     * @param   i  index
     * @return  value of surface parameter i.
     */
    public abstract double parameter(int i);
    
    //
    
    /**
     *Return the difference between two vectors on the surface.
     *
     * @param vec1  TrackVector
     * @param  vec2 TrackVector
     * @return  new TrackVector representing difference of vec1 and vec2
     */
    public abstract TrackVector vecDiff(TrackVector vec1,
            TrackVector vec2);
    //
    
    /**
     *Return the position of a track vector.
     *
     * @param  vec TrackVector
     * @return  the SpacePoint position of the TrackVector on the Surface
     */
    public abstract SpacePoint spacePoint(TrackVector vec);
    
    //
    
    
    /**
     *Return the position and direction of a track vector.
     *
     * @param vec  TrackVector
     * @param  dir TrackSurfaceDirection
     * @return  Spacepath position and direction of vec.
     */
    public abstract SpacePath spacePath(TrackVector vec,
            TrackSurfaceDirection dir);
    
    // Methods to implement only in bounded surfaces.  *******************
    
    // The default implementations for these methods are only appropriate
    // for pure surfaces.  Bounded surfaces must override.  This is checked
    // with an assertion.
    // Return true if two surfaces are exactly the same
    // including bounds.  Argument may be safely downcast.
    // This method should be implemented by all bounded surfaces.
    protected boolean safeBoundEqual(Surface srf)
    {
        if(!isPure()) throw new IllegalArgumentException("Surface is not pure!");
        return safePureEqual(srf);
    }
    
    //
    
    /**
     *Return a new Surface of the full type (clone).
     * The default implementation is appropriate only for a pure Surface.
     *
     * @return clone Surface
     */
    public Surface newSurface()
    {
        if(!isPure()) throw new IllegalArgumentException("Surface is not pure!");
        return newPureSurface();
    }
    
    //
    
    
    /**
     *Return the crossing status for a track without or with error.
     * Without error should produce the same result as with error
     * with zero errors.
     * Default for status is pure status.
     *
     * @param  trv VTrack
     * @return  CrossStat status for track without error.
     */
    public CrossStat status(VTrack trv)
    {
        if(!isPure()) throw new IllegalArgumentException("Surface is not pure!");
        return pureStatus(trv);
    }
    
    
    /**
     *Return the crossing status for a track with error.
     * Without error should produce the same result as with error
     * with zero errors.
     * Default for status is pure status.
     *
     * @param  tre ETrack
     * @return  CrossStat status for track with error.
     */
    public CrossStat status(ETrack tre)
    {
        if(!isPure()) throw new IllegalArgumentException("Surface is not pure!");
        return pureStatus(tre);
    }
    
    //
    /**
     *Equality operator.
     * This compares the full surface types.
     *
     * @param  srf Surface  to be compared to
     * @return true if Surfaces are equal, including bounds.
     */
    public boolean equals( Surface srf)
    {
        return boundEqual(srf);
    }
    
}
