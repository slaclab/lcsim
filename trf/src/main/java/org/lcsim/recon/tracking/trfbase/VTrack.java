package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePath;

/**
 * A Track vector without error.
 *<p>
 * Data includes a surface _srf, a 5-vector _vec and a direction.
 *<p>
 * The direction attribute is initially set undefined and may be defined
 * with methods set_backward() or set_forward().  However, the actual
 * direction of the track as returned by is_valid(), is_forward() and
 * is_backward() is obtained by asking the surface.  The direction
 * attribute is used only if the surface returns undefined.
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public class VTrack
{
    
    // set surface from clone
    // direction attribute is not set undefined
    //private void set_surface_from_clone(Surface srf);
    
    // Surface
    // null indicates track in invalid state of default constructor.
    Surface _srf;
    
    // Track 5-vector.
    TrackVector _vec;
    
    // Direction of the track relative to the surface.
    // This is needed because track parameters may be ambiguous.
    TrackSurfaceDirection _dir;
    
    // Direction of the track in time
    // Default is forward in time
    //used for interactions such as energy loss
    boolean _forward;
    
    // methods
    
    //
    
    /**
     *Default constructor.
     *Leaves track in invalid state.
     *
     */
    public VTrack()
    {
        _dir = TrackSurfaceDirection.TSD_UNDEFINED;
        _forward = true;
    }
    
    //
    
    /**
     *construct a track vector from a surface
     *
     * @param srf  Surface
     */
    public VTrack( Surface srf)
    {
        _srf = srf;
        _dir = TrackSurfaceDirection.TSD_UNDEFINED;
        _vec = new TrackVector();
        _forward = true;
    }
    
    //
    
    /**
     *constructor from a surface and a vector
     *
     * @param srf  Surface  at which track is defined
     * @param  vec TrackVector  of track parameters
     */
    public VTrack( Surface srf,  TrackVector vec)
    {
        _srf = srf;
        _vec = new TrackVector(vec);
        _dir = TrackSurfaceDirection.TSD_UNDEFINED;
        _forward = true;
    }
    
    //
    
    /**
     *constructor from a surface, vector and direction
     *
     * @param  srf Surface  at which track is defined
     * @param  vec TrackVector  of track parameters
     * @param  dir TrackSurfaceDirection  of direction
     */
    public VTrack( Surface srf,  TrackVector vec,
            TrackSurfaceDirection dir)
    {
        _srf = srf;
        _vec = new TrackVector(vec);
        _dir = dir;
        _forward = true;
    }
    
    //
    
    /**
     *copy constructor
     *
     * @param  trv VTrack  to copy
     */
    public VTrack( VTrack trv)
    {
        _srf = trv._srf;
        _vec = new TrackVector(trv._vec);
        _dir = trv._dir;
        _forward = trv._forward;
    }
    
    //
    
    /**
     *Return true if track is valid.
     * To be valid, the surface and direction must be defined.
     *
     * @return  true if track surface and direction are defined
     */
    public boolean isValid()
    {
        // check surface is defined
        if ( _srf == null ) return false;
        // check direction is defined
        if ( _srf.direction(_vec) == TrackSurfaceDirection.TSD_UNDEFINED  &&
                _dir ==  TrackSurfaceDirection.TSD_UNDEFINED ) return false;
        return true;
    }
    
    //
    
    /**
     *set surface
     * direction attribute is set undefined
     *
     * @param  srf Surface  at which track will be defined
     */
    public void setSurface( Surface srf)
    {
        _srf = srf;
        _dir = TrackSurfaceDirection.TSD_UNDEFINED;
    }
    
    //
    
    /**
     *get surface
     *
     * @return Surface at which track is defined
     */
    public  Surface surface( )
    {
        return _srf;
    }
    
    //
    
    /**
     * set track vector
     * direction attribute is set undefined
     *
     * @param  vec TrackVector  containing track parameters
     */
    public void setVector( TrackVector vec)
    {
        _vec = new TrackVector(vec);
        _dir = TrackSurfaceDirection.TSD_UNDEFINED;
    }
    
    //
    
    /**
     *set vector and keep the current direction
     *
     * @param vec  TrackVector   containing track parameters
     */
    public void setVectorAndKeepDirection( TrackVector vec)
    {
        _vec = new TrackVector(vec);
    }
    
    //
    
    /**
     * get track vector
     *
     * @return   TrackVector  containing track parameters
     */
    public  TrackVector vector()
    {
        return new TrackVector(_vec);
    }
    
    //
    
    /**
     *return a component of the vector
     *
     * @param   i track component to return
     * @return  the value of the track vector component i
     */
    public double vector(int i)
    {
        if( !(i>=0 && i<5) )
        {
            throw new IllegalArgumentException("VTrack index must be within [0,4]!");
        }
        return _vec.vector()[i];
    }
    
    //
    
    /**
     *set direction forward
     *
     */
    public void setForward()
    {
        _dir = TrackSurfaceDirection.TSD_FORWARD;
    }
    
    //
    
    /**
     *set direction backward
     *
     */
    public void setBackward()
    {
        _dir = TrackSurfaceDirection.TSD_BACKWARD;
    }
    
    //
    
    
    /**
     *is track direction same as surface
     * surface direction takes priority over local value
     *
     * @return true if track direction is forward
     */
    public boolean isForward()
    {
        TrackSurfaceDirection dir =  _srf.direction(_vec);
        if( dir.equals(TrackSurfaceDirection.TSD_FORWARD) ) return true;
        if( dir.equals(TrackSurfaceDirection.TSD_BACKWARD) )return false;
        if ( dir.equals(TrackSurfaceDirection.TSD_UNDEFINED) )  return _dir.equals(TrackSurfaceDirection.TSD_FORWARD);
        return false;
    }
    
    //cng
    // Method to
    
    /**
     *set the pure track direction forwards
     *
     */
    public void setTrackForward()
    {
        _forward = true;
    }
    
    //
    
    /**
     *return the pure track direction
     *
     * @return true if the pure track direction is forward
     */
    public boolean isTrackForward()
    {
        return _forward;
    }
    
    //
    
    
    /**
     *is track direction opposite that of surface?
     * surface direction takes priority over local value
     *
     * @return true if track direction is opposite that of Surface
     */
    public boolean isBackward()
    {
        TrackSurfaceDirection dir =  _srf.direction(_vec);
        if(dir == TrackSurfaceDirection.TSD_FORWARD) return false;
        if(dir == TrackSurfaceDirection.TSD_BACKWARD)return true;
        if (dir == TrackSurfaceDirection.TSD_UNDEFINED)  return _dir == TrackSurfaceDirection.TSD_BACKWARD;
        return false;
    }
    
    //cng
    //
    
    /**
     *set the pure track direction backwards
     *
     */
    public void setTrackBackward()
    {
        _forward = false;
    }
    //
    
    /**
     *return the pure track direction
     *
     * @return  true if pure track direction is backward
     */
    public boolean isTrackBackward()
    {
        return !_forward;
    }
    
    
    //
    
    /**
     * return the track position
     *
     * @return the SpacePoint position of the track on the Surface
     */
    public SpacePoint spacePoint()
    {
        return surface().spacePoint(vector());
    }
    
    //
    
    /**
     * the track position and direction
     *
     * @return get the SpaceVector position and direction of the track on the Surface
     */
    public SpacePath spacePath()
    {
        return surface().spacePath(vector(),_dir);
    }
    
    //
    
    /**
     *Return q/p in 1/GeV/c.
     *
     * @return the track charge over momentum
     */
    public double qOverP()
    {
        return surface().qOverP(vector());
    }
    
    
    /**
     * String representation of the VTrack
     *
     * @return String representation of the VTrack
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+" at "+_srf+" with track parameters: \n"+_vec+" travelling in direction "+_dir+" forward: "+_forward+"\n";
    }
    
    //
    
    /**
     *Equality
     *
     * @param  vtrk VTrack  to compare with
     * @return  true if this equals vtrk
     */
    public boolean equals(VTrack vtrk)
    {
        if( !surface().equals(vtrk.surface()) ) return false;
        if( !vector().equals(vtrk.vector()) ) return false;
        if( isForward() && vtrk.isForward() ) return true;
        if(isBackward() && vtrk.isBackward() ) return true;
        return false;
    }
    
    //
    
    /** Inequality
     *
     * @return true if this does not equal vtrk
     * @param vtrk VTrack to compare
     */
    public boolean notEquals(VTrack vtrk)
    {
        return !equals(vtrk);
    }
}

