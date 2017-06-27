package org.lcsim.recon.tracking.trflayer;

import java.util.*;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Propagator;

/**
 * Layers describe the geometry of the detector.  Each layer is
 * composed of surfaces.  The composition may be direct or via other
 * layers.
 *
 * The layer provides the methods propagate which propagate a track
 * to the next of its constituent surfaces.  The propagate methods are
 * passed a propagator which carries out the action and determines the
 * direction.
 *
 * Propagation returns a list of layer tracks. A layer track is a
 * kinematic track plus a layer status.  The layer status may also
 * contain a miss and a cluster finder.  It is expected that each of
 * the returned tracks will be updated with the miss and clusters
 * and then propagated through the layer again until the status indicates
 * the track has reached a layer exit.
 *
 * There are two methods of propagation: both are passed the track and
 * the propagator but in the second the track is wrapped in the layer
 * track.  The first is used when entering the layer and the second
 * for all calls within the layer.
 *
 * Both of these propagate methods are implemented here.  Subclasses
 * do not override these but must provide the method _propagate which is
 * used by these.  Its interface is described below.
 *
 * If there are a lists of clusters associated layer, the method
 * get_clusters may be used to access them.
 *
 *
 * NOTICE for subclass developers:
 *
 * The major method that subclasses must implement is
 * LTracklist  _propagate(const LTrack& trl, const Propagator& prop) const .
 * The input is a layer track and a propagator and the output is a list
 * of layer tracks.  The output layer tracks would typically be created
 * by copying from the input layer track and then modifying it two
 * components: the kinematic track and the layer status chain.
 *
 * The layer chain includes a list of layer status objects and two pointers
 * to this list: one for the current layer postion and one for the
 * status which contains the list of clusters and the miss.
 *
 * When a layer is nested (i.e. composed of other layers), the chain must
 * be advanced to that layer before calling the propagate method of a
 * sublayer.  The current status should be set to that same position when
 * layer when returning from propagation.
 *
 * The cluster pointer is not set automatically.  The layer class
 * contributing the status containing a cluster list or miss should
 * call LTrack::set_cluster_status() to set this pointer.
 *
 *
 * still needs work to implement multiple surface layers...
 **/


public abstract class Layer
{
    
    // Assertion arguments.
    // Set false to enable assertions.
    static boolean GET_CLUSTERS_HAS_NO_CLUSTERS = false;
    static boolean GET_CLUSTERS_SURFACE_HAS_NO_CLUSTERS = false;
    static boolean ADD_CLUSTER_HAS_NO_CLUSTERS = false;
    static boolean ADD_CLUSTER_SURFACE_HAS_NO_CLUSTERS = false;
    static boolean DROP_CLUSTERS_HAS_NO_CLUSTERS = false;
    
    // static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return String representation of this class type
     *Included for completeness with C++ version
     */
    public static String typeName()
    { return "Layer"; }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of this class type
     *Included for completeness with C++ version
     */
    public static String staticType()
    { return typeName(); }
    
    // methods
    
    // Subclasses call this method if they are passed an invalid
    // surface.
    protected void reportInvalidSurface(Surface srf)
    {
    }
    
    // methods implemented here
    
    //
    
    /**
     *constructor
     *
     */
    public Layer()
    {
    }
    //
    
    /**
     *Return the generic type of this class.
     * Subclasses must not override.
     *
     * @return String representation of this class type
     *Included for completeness with C++ version
     */
    public String genericType()
    { return staticType(); }
    
    //
    
    /**
     *Propagate a track to the first surface in this layer.
     * This simply constructs a status and calls the virtual method.
     *
     * @param   tre ETrack
     * @param   prop Propagator
     * @return List of LayerStats
     */
    public List propagate( ETrack tre, Propagator prop)
    {
        LayerStat lstat = new LayerStat(this);
        LTrack trl = new LTrack(tre,lstat);
        return propagate(trl, prop);
    }
    
    //
    
    /**
     *Propagate a track to the next surface in this layer.
     * The status is that from the previous surface in the layer.
     *
     * @param   trl LTrack
     * @param   prop Propagator
     * @return List of LayerStats
     */
    public List propagate(LTrack trl, Propagator prop)
    {
        
        // Check that the status corresponds to this layer.
        // If not, crash or return an empty list.
        Layer lyr = trl.status().layer();
        Assert.assertTrue( lyr.equals(this) );
        if ( !lyr.equals(this) ) return new ArrayList();
        
        // propagate
        List ltracks = _propagate(trl, prop);
        
        // Check status still points to this layer.
        // If not, crash or return an empty list.
        lyr = trl.status().layer();
        Assert.assertTrue( lyr.equals(this) );
        if ( !lyr.equals(this) ) return new ArrayList();
        
        return ltracks;
    }
    
    //
    
    /**
     *Return whether this layer or its descendants contains clusters.
     * Returns false if get_cluster_surfaces() returns an empty list.
     *
     * @return true if surface has clusters
     */
    public  boolean hasClusters()
    { return !(clusterSurfaces().size()==0); }
    
    // methods to be implemented in subclasses
    
    
    // Propagate a track to the next surface in this layer.
    // The list of succesful propagations is returned.
    // When layers are nested, this method in the parent will typically
    // invoke the same method for each of its children.
    protected abstract  List
            _propagate( LTrack  trl,  Propagator  prop);
    
/*  //This needs to be replaced with the above!
protected abstract  List
  _propagate( ETrack  trl,  Propagator  prop);
 */
    // methods to be implemented in subclasses
    
    //
    
    /**
     *Return the list of active surfaces -- i.e. surfaces associated
     * with clusters.  These should include surfaces in sublayers.
     * Default method reports no surfaces.
     *
     * @return List of surfaces
     */
    public List clusterSurfaces()
    {
        return new ArrayList();
    }
    
    //
    
    /**
     *Return all the clusters associated with the current layer
     * or its descendants.
     * Default here is to return an empty list.
     *
     * @return list of clusters
     */
    public  List clusters()
    {
        Assert.assertTrue( GET_CLUSTERS_HAS_NO_CLUSTERS );
        return new ArrayList();
    }
    
    //
    
    /**
     *Return all the clusters associated with a particular surface
     * in this layer.
     * This method should call report_invalid_surface(SurfacePtr)
     * if it does not recognize the argument.
     * Default is to return an empty list.
     *
     * @param   srf Surface
     * @return List of clusters associated with Surface srf
     */
    public  List clusters(Surface srf)
    {
        reportInvalidSurface(srf);
        Assert.assertTrue( GET_CLUSTERS_SURFACE_HAS_NO_CLUSTERS );
        return new ArrayList();
    }
    
    //
    
    /**
     *Add a cluster to the layer.
     * Default here is to return an error.
     *
     * @param   clu Cluster to add
     * @return 0 if successful
     */
    public  int addCluster( Cluster clu)
    {
        Assert.assertTrue( ADD_CLUSTER_HAS_NO_CLUSTERS );
        return -1;
    }
    
    //
    /**
     *Add a cluster to a particular surface in the layer.
     * Default here is to return an error.
     *
     * @param   clu Cluster to add
     * @param   srf Surface to which to add
     * @return 0 if successful
     */
    
    public  int addCluster( Cluster clu, Surface srf)
    {
        reportInvalidSurface(srf);
        Assert.assertTrue( ADD_CLUSTER_SURFACE_HAS_NO_CLUSTERS );
        return -1;
    }
    
    //
    
    /**
     *Drop all clusters from this layer.
     * // Default here is to return an error.
     *
     */
    public  void dropClusters()
    {
        if ( hasClusters() ) Assert.assertTrue( DROP_CLUSTERS_HAS_NO_CLUSTERS );
    }
}
