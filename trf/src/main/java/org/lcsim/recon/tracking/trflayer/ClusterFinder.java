package org.lcsim.recon.tracking.trflayer;

//import trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import java.util.*;

/**
 * A finder provides access to a list of clusters associated with
 * a surface.
 * It provides a method to return the complete list and a
 * method to return clusters near a specified track at the surface.
 *<p>
 * This class is abstract; subclasses provide concrete implementations
 * for specific surfaces and clusters.
 *<p>
 * Layers construct these cluster finders and include them in the layer
 * status objects returned after layer propagation.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public abstract class ClusterFinder
{
    
    //
    
    /**
     *Return the type name.
     *
     * @return   String representation of class type
     *Included for completeness with C++ version
     */
    public static String typeName()
    { return "ClusterFinder"; }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public static String staticType()
    { return typeName(); }
    
    // methods
    
    //
    
    /**
     *constructor
     *
     */
    public ClusterFinder()
    {
    }
    
    //
    
    /**
     *Return the generic type of this class.
     * Subclasses must not override.
     *
     * @return  String representation of class type
     *Included for completeness with C++ version
     */
    public String genericType()
    { return staticType(); }
    
    //
    
    /**
     *Return the surface.
     *
     * @return  Surface for this cluster finder
     */
    public abstract Surface surface();
    
    //
    
    /**
     *Return all the clusters associated with this surface.
     *
     * @return   List of clusters this finder manages
     */
    public abstract List clusters();
    
    //
    
    /**
     *Return all the clusters near the specified track at the surface.
     *
     * @param   tre  ETrack
     * @return  List of clusters this finder finds close to this track
     */
    public abstract List clusters( ETrack tre);
    
    
}