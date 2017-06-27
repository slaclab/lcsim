package org.lcsim.recon.tracking.trfbase;
/** A miss represents the crossing of a track with a layer surface.
 * It returns the likelihood that the track would cross the layer
 * without producing a cluster.
 *
 * This likelihood should reflect overlap with inactive parts of the
 * detector (beyond boundaries, gaps, dead channels, ...) as well
 * as the intrinsic inefficiency of active regions.
 *
 * This is an abstract base class.  Concrete subclasses must provide
 * methods to return the surface and likelihood, update the likelihood,
 * clone and fill the output stream.
 *
 * These subclasses will typically be defined along with concrete
 * layer classes.
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public abstract class Miss
{
    
    // methods
    
    //
    
    /**
     *constructor
     *
     */
    public Miss()
    {
    }
    
    //
    
    /**
     *return a unique address specifying the type
     * Default retuns zero--override for subclasses which are able to
     * identify themselves.
     *
     * @return String representation of this class
     * Included only for completeness with C++ code
     */
    public String type()
    {return "Miss";
    }
    
    //
    
    /**
     *Clone the miss.
     *
     * @return new copy of this Miss
     */
    public abstract Miss  newCopy();
    
    //
    
    /**
     *update the likelihood with a new track
     *
     * @param   tre ETrack to update the likelihood
     */
    public abstract void update( ETrack tre);
    
    //
    
    /**
     *return the surface
     *
     * @return the Surface for this Miss
     */
    public abstract  Surface surface()  ;
    
    //
    
    /**
     *return the likelihood
     *
     * @return the likelihood for this Miss
     */
    public abstract  double likelihood()  ;
    
    
    /**
     * String representation
     *
     * @return String representation of this Class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className;
    }
}