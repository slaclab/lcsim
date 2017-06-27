package org.lcsim.recon.tracking.trfbase;
/**This class provides the interface for propagating a track to
 * a surface.  In general, there may be any number of points
 * (0, 1, 2, 3, ...) where the track intersects the surface.
 * This interface is intended to propagate to either the nearest
 * point in forward direction or the nearest point in the backward
 * direction.  There are three options: the track may be propagated
 * only in the forward direction, only the backward direction or
 * to the nearest of the two crossings.
 *<p>
 * For each of the options there are two possibilities when the
 * destination surface is the same as the original surface.  The
 * original specification was to stay at the same position.  Now
 * there are options (XXX_MOVE) to move to the next crossing.
 *<p>
 * Two methods of propagation are provided.  One specifies this option.
 * The other does not and it is the reponsibility of the subclass to
 * make a clear and unambiguous choice.
 *<p>
 * There are also separate propagation methods for tracks with and
 * without error matrices.  These should behave in exactly the same
 * manner except for changes to the error matrix.  The new vector
 * may not depend on the error matrix.
 *<p>
 * The methods to propagate a track with error are implemented here
 * using the methods without error.  Typically there should be no
 * need for subclasses to override the former.
 *<p>
 * The original track is overwritten with the new track parameters
 * if and only if the propagation is successful.  A propagation
 * status is returned to indicate whether the propagation was
 * was successful and, if so, the direction of propagation relative
 * to the track's direction.
 *<p>
 * The propagation may be called repeatedly to iterate over all the
 * crossings in either the forward or backward direction.  In order
 * to maintain this capability, a track that is already at the
 * surface (i.e. has the same pure surface as the destination) must
 * be propagated to a new point or the propagation is not successful.
 * However, if the point is on but not at the surface (i.e. it is on
 * a different type of pure surface but has coordinates lying on the
 * destination surface, then the transformed track should be returned
 * as a successful propagation in the requested direction.
 *<p>
 * When labeling or choosing the direction of propagation, forward
 * should be chosen if there is an ambiguity.
 *<p>
 * This clase is abstract: derived classes provide the algorithms
 * for propagation.
 *<p>
 * A higher level class might make use of this interface to provide
 * an iterator over crossings.  It could carry out the propagation
 * as needed and cache the results.
 *<p>
 * The propagate methods all include an optional argument which is a
 * pointer to a track derivative.  If this pointer is not null, then
 * its location should be filled with the derivative of the new track
 * w.r.t. the original.
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public abstract class Propagator
{
    
    // static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return  String representation of class type
     *Included for completeness with C++ version
     */
    public static String typeName()
    { return "Propagator";
    }
    
    //
    
    /**
     * return a reduced PropDir
     *
     * @param   dir  direction in which to propagate
     * @return  new propagation direction
     */
    public static PropDir reduce(PropDir dir)
    {
        if (dir.equals(PropDir.NEAREST_MOVE)) return PropDir.NEAREST;
        if (dir.equals(PropDir.FORWARD_MOVE)) return PropDir.FORWARD;
        if (dir.equals(PropDir.BACKWARD_MOVE)) return PropDir.BACKWARD;
        return dir;
    }
    
    /**
     * Reduce a propagation direction.
     * This drops the _MOVE from a direction and returns true
     * if the suffix was present.
     * It is used in subclasses to separate the true direction from
     * the move status in a direction.
     * @param   dir  propagation direction
     * @return  true if dir was a _MOVE propagation direction
     */
    public static boolean reduceDirection(PropDir dir)
    {
        if(dir.equals(PropDir.NEAREST))
            return false;
        if(dir.equals(PropDir.FORWARD))
            return false;
        if(dir.equals(PropDir.BACKWARD))
            return false;
        if (dir.equals(PropDir.NEAREST_MOVE))
        {
            dir = PropDir.NEAREST;
            return true;
        }
        if (dir.equals(PropDir.FORWARD_MOVE))
        {
            dir = PropDir.FORWARD;
            return true;
        }
        if (dir.equals(PropDir.BACKWARD_MOVE))
        {
            dir = PropDir.BACKWARD;
            return true;
        }
        
        return false;
    }
    
    
    //
    
    /**
     *constructor
     *
     */
    public Propagator()
    {
    }
    
    //
    
    /**
     *Clone, i.e. create a copy.
     * Must be overriden in subclasses.
     *
     * @return new copy of this Propagator
     */
    public abstract Propagator newPropagator();
    
    /**
     *Propagate a track without error.
     * Must be overriden in subclasses.
     * This is implemented in PropDirected--typically subclasses
     * will inherit from that and then not override this method.
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @return  propagation status
     */
    public  PropStat vecProp(VTrack trv,  Surface srf)
    {
        TrackDerivative tder = null;
        return vecProp(trv, srf, tder);
    }
    
    
    /**
     *propagate a track without error
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @param   tder TrackDerivative to update at srf
     * @return  propagation status
     */
    public abstract PropStat vecProp(VTrack trv,  Surface srf,
            TrackDerivative tder);
    
    // Propagate a track without error in the specified direction.
    // Must be overriden in subclasses.
    /**
     *propagate a track without error in the specified direction
     *Must be overriden in subclasses.
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @param   dir direction in which to propagate
     * @return propagation status
     */
    public PropStat  vecDirProp(VTrack trv,  Surface srf, PropDir dir)
    {
        TrackDerivative tder = null;
        return vecDirProp(trv, srf, dir, tder);
    }
    
    /**
     *propagate a track without error in the specified direction
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @param   dir direction in which to propagate
     * @param   tder TrackDerivative to update at srf
     * @return propagation status
     */
    public abstract PropStat  vecDirProp(VTrack trv,  Surface srf, PropDir dir,
            TrackDerivative tder);
    
    // Propagate a track with error.
    // Typically does not need to be overridden in subclasses.
    /**
     *propagate a track with error
     *
     * @param   tre0 ETrack to propagate
     * @param   srf Surface to which to propagate
     * @param   tder TrackDerivative to update at srf
     * @return  propagation status
     */
    public PropStat errProp(ETrack tre0,  Surface srf,
            TrackDerivative tder)
    {
        // Default implementation of propagation with error.  We ask
        // for the derivative matrix and use it to update the error
        // matrix.
        ETrack tre = tre0;
        PropStat pstat = vecProp(tre,srf,tder);
        if ( ! pstat.success() ) return pstat;
        TrackError err = tre.error();
        tre.setError( err.Xform(tder) );
        if ( tre.checkError() != 0 )
        {
            return new PropStat();
        }
        //cng        tre0 = tre;
        tre0 = new ETrack(tre);
        return pstat;
    }
    
    
    // Propagate a track with error.
    // Typically does not need to be overridden in subclasses.
    /**
     *propagate a track with error
     *Typically does not need to be overridden in subclasses.
     *
     * @param   tre0 ETrack to propagate
     * @param   srf Surface to which to propagate
     * @return  propagation status
     */
    public PropStat  errProp(ETrack tre0,  Surface srf)
    {
        TrackDerivative tder = null;
        return errProp(tre0, srf, tder);
    }
    
    // Same as above with direction specified.
    
    // Propagate a track with error in the specified direction.
    // Typically does not need to be overridden in subclasses.
    /**
     *propagate a track with error in the specified direction
     *Typically does not need to be overridden in subclasses.
     *
     * @param   tre0 ETrack to propagate
     * @param   srf Surface to which to propagate
     * @param   dir direction in which to propagate
     * @return  propagation status
     */
    public PropStat errDirProp(ETrack tre0,  Surface srf, PropDir dir)
    {
        TrackDerivative tder = null;
        return errDirProp( tre0, srf, dir, tder);
    }
    
    /**
     *propagate a track with error in the specified direction
     *
     * @param   tre0 ETrack to propagate
     * @param   srf Surface to which to propagate
     * @param   dir direction in which to propagate
     * @param   tder TrackDerivative to update at srf
     * @return  propagation status
     */
    public PropStat errDirProp(ETrack tre0,  Surface srf, PropDir dir,
            TrackDerivative tder)
    {
        // want to change incoming ETrack!
        ETrack tre = tre0;
        TrackDerivative deriv;
        if (tder != null)
        {
            deriv = tder;
        }
        else
        {
            deriv  = new TrackDerivative();
        }
        //System.out.println("\n deriv= "+deriv);
        //System.out.println("\n srf= "+srf);
        PropStat pstat = vecDirProp(tre,srf,dir,deriv);
        if ( ! pstat.success() ) return pstat;
        //System.out.println("\n pstat= "+pstat+"\n tre= "+tre);
        TrackError err = tre.error();
        TrackError tmp = err.Xform(deriv);
        //tre.set_error( err.Xform(deriv) );
        tre.setError( tmp );
        if ( tre.checkError() != 0 )
        {
            return new PropStat();
        }
        tre0 = new ETrack(tre);
        return pstat;
    }
}