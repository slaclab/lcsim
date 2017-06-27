package org.lcsim.recon.tracking.trflayer;

import java.util.*;

import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Miss;
import org.lcsim.recon.tracking.trfbase.ETrack;

/**
 * Layer status contains the results of propagation to a layer surface.
 * It includes:
 *
 * <li> the layer
 * <li> a flag indicating whether this is the last surface in the layer
 * <li> an optional miss which provides the likelihood that the track
 *    would not have produced any clusters
 * <li> an optional cluster finder which provides access to the clusters
 *    associated with the layer surface and
 * <li> an integer to record the internal state of the layer
 *<p>
 * This is a concrete class and is not intended to be used for inheritance.
 * Layers can save and then retrieve their state in the layer status.
 *<p>
 * Layer status objects form a linked list which is intended to be managed
 * from the top; when an object is deleted, children (and not parents)
 * are deleted recursively.
 *<p>
 * The miss object is managed here, i.e. it is deleted when the layer
 * status is deleted.  The layer and finder are not managed here.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/
public class LayerStat
{
    
    
    private boolean debug;
    // the layer that produced this status
    private  Layer _layer;
    
    // flag indicating this is the last surface in this layer
    private boolean _at_exit;
    
    // crossing of track with layer surface
    private Miss _miss;
    
    // The object managing the list of clusters.
    // The object is not managed here; it is managed by the layer.
    // We are in trouble if the layer is deleted.
    // Null => this layer surface does not hold clusters (default).
    private ClusterFinder  _finder;
    
    // layer internal state
    private int _layer_state;
    
    
    // constructors
    
    //
    
    /**
     * default constructor
     *
     */
    public LayerStat()
    {
        reset();
    }
    
    //
    
    /**
     *constructor from layer
     *
     * @param   layer  Layer for which to create a status
     */
    public LayerStat(Layer layer)
    {
        // need clone method here...
        _layer = layer;
        reset();
    }
    
    //
    /**
     *copy constructor
     *
     * @param   lstat LayerStat to replicate
     */
    public LayerStat(LayerStat lstat)
    {
        _layer = lstat._layer;
        _at_exit = lstat._at_exit;
        _miss = lstat._miss ;
        _finder = lstat._finder ;
        _layer_state = lstat._layer_state;
        if(debug) System.out.println(this);
    }
    
    
    /**
     *output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer( getClass().getName()+" is ");
        if ( ! _at_exit ) sb.append("not ");
        sb.append( "at exit.\n");
        // Display the miss, finder and state.
        if ( _miss != null ) sb.append( _miss + "\n");
        else sb.append( "No miss. \n");
        if ( _finder != null ) sb.append( _finder +"\n");
        else sb.append( "No cluster finder.\n");
        sb.append( "Layer state: " + _layer_state);
        return sb.toString();
    }
    
    // modifiers
    
    //
    
    /**
     *reset flag, miss, finder and state
     *
     */
    public void reset()
    {
        _at_exit = false;
        _miss = null;
        _finder = null;
        _layer_state = 0;
    }
    
    //
    
    /**
     *set exit
     *
     */
    public void setAtExit()
    {
        setAtExit(true);
    }
    
    
    /**
     *set at exit
     *
     * @param   value to set at exit
     */
    public void setAtExit(boolean value)
    {
        _at_exit = value;
    }
    
    //
    
    /**
     *set the miss
     *
     * @param   miss Miss to set for this status
     */
    public void setMiss(Miss miss)
    {
        _miss = miss.newCopy();
    }
    
    //
    
    /**
     *drop the miss
     *
     */
    public void unsetMiss()
    {
        _miss = null;
    }
    
    //
    
    /**
     *set the cluster finder
     *
     * @param   finder ClusterFinder for this Layer's surface
     */
    public void setFinder(ClusterFinder finder)
    {
        _finder = finder;
    }
    
    //
    
    /**
     *set the layer state
     *
     * @param   layer_state for this layer
     */
    public void setState(int layer_state)
    {
        _layer_state = layer_state;
    }
    
    // accessors
    
    //
    
    /**
     *return the layer
     *
     * @return Layer
     */
    public  Layer layer()
    {
        return _layer;
    }
    
    //
    
    /**
     *return true if the track is at the exit of this layer
     *
     * @return true if track is at exit
     */
    public boolean atExit()
    {
        return _at_exit;
    }
    
    //
    
    /**
     *Return whether this surface has clusters.
     *
     * @return true if this layer contains clusters
     */
    public boolean hasClusters()
    {
        if (_finder == null) return false;
        return true;
    }
    
    //
    
    /**
     *Return all the clusters associated with this surface
     * in the layer.
     * Default here is to return all clusters from the last layer in the nest.
     *
     * @return a List of clusters on this layer
     */
    public List clusters()
    {
        if ( _finder == null ) return new ArrayList();
        return _finder.clusters();
        
    }
    
    //
    
    
    /**
     *Return all the clusters near the specified track associated with
     * the track's surface in the current layer surface.
     * Default here is to return all clusters from the last layer in the nest.
     *
     * @param   tre ETrack
     * @return List of clusters near ETrack tre
     */
    public List clusters(ETrack tre)
    {
        if ( _finder == null ) return new ArrayList();
        return _finder.clusters(tre);
    }
    
    //
    
    /**
     *Return the miss.
     *
     * @return the Miss for this layer
     */
    public  Miss miss()
    {
        return _miss;
    }
    
    //
    
    /**
     *Fetch the layer state
     *
     * @return the state of this layer
     */
    public int state()
    {
        return _layer_state;
    }
    
    //  //cng
    
    /**
     *Return the finder
     *
     * @return the finder associated with this layer
     */
    public ClusterFinder finder()
    {
        return _finder;
    }
    
}

