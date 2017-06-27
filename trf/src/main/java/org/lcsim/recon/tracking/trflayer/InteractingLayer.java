package org.lcsim.recon.tracking.trflayer;
import java.util.List;
import java.util.ArrayList;

import java.util.Iterator;

import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.Cluster;

/**
 * Class which inherits from Layer, and adds some interaction to Layer's
 * track list.  The InteractingLayer contains a concrete layer
 * (a LayerCylinder, for instance), and a concrete interactor
 * (a ThinCylMS, for instance).  To add an interaction to tracks after
 * the tracks have been propagated to the layer, one simply calls
 * InteractingLayer.propagate(...).  The contained layer's propagate is
 * then called, and the interaction appropriate to the contained interactor
 * is applied.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class InteractingLayer extends Layer
{
    
    // static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public static String typeName()
    { return "InteractingLayer";
    }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public static String staticType()
    { return typeName();
    }
    
    // attributes
    
    private Layer _lyr;
    
    private Interactor _inter;
    
    // methods
    //
    
    /**
     *Propagate a track to the next surface in this layer.
     * The list of successful propagations is returned.
     * This calls _propagate of its layer
     *
     * @param   tr1 LTrack
     * @param   prop Propagator
     * @return List of successful propagations
     */
    public List _propagate(LTrack tr1, Propagator prop)
    {
        // update the status chain
        LTrack trl0 = new LTrack(tr1);
        trl0.popStatus();
        LayerStat lstat = new LayerStat(_lyr);
        trl0.pushStatus(lstat);
        
        List ltracks = _lyr.propagate(trl0, prop);
        
        Iterator ltrIter;
        for( ltrIter=ltracks.iterator(); ltrIter.hasNext();  )
        {
            ETrack etrk = ((LTrack)ltrIter.next()).track();
            _inter.interact( etrk );
            //needs work here... what's going on?
            //(*ltrIter).pop_status();
            TrackError terr = etrk.error();
        }
        
        return ltracks;
    }
    
    // Interact a track.
    private void _interact(ETrack tre)
    {
        _inter.interact(tre);
    }
    
    // methods
    
    //
    
    /**
     *constructor from a Layer and an Interactor
     *
     * @param   lyr Layer
     * @param   inter Interactor
     */
    public InteractingLayer(  Layer lyr,   Interactor inter)
    {
        _lyr = lyr;
        _inter = inter;
    }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public String type()
    { return staticType();
    }
    
    //
    
    /**
     *Return the layer.
     *
     * @return Layer
     */
    public  Layer layer()
    { return _lyr;
    }
    
    //
    
    /**
     *Return the interactor.
     *
     * @return Interactor
     */
    public  Interactor interactor()
    { return _inter;
    }
    
    //
    
    /**
     *call the contained class's has_clusters:
     *
     * @return true if layer contains clusters
     */
    public boolean hasClusters()
    { return _lyr.hasClusters();
    }
    
    //
    
    /**
     *call the containing class's get_cluster_surfaces()
     *
     * @return List of surfaces
     */
    public List clusterSurfaces()
    {
        return _lyr.clusterSurfaces();
    }
    
    //
    
    /**
     *Return all the clusters associated with the current layer
     * or its descendants.
     * Default here is to return an error.
     *
     * @return List of clusters
     */
    public   List clusters()
    {
        return _lyr.clusters();
    }
    
    //
    
    /**
     *Return all the clusters associated with a particular surface
     * in this layer.
     * This method should call report_invalid_surface(Surface)
     * if it does not recognize the argument.
     *
     * @param   srf Surface
     * @return list of clusters for Surface srf
     */
    public List clusters( Surface srf)
    {
        return _lyr.clusters(srf);
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
        return _lyr.addCluster( clu );
    }
    
    //
    
    /**
     *Add a cluster to a particular surface in the layer.
     * Default here is to return an error.
     *
     * @param   clu Cluster
     * @param   srf Surface
     * @return 0 if successful
     */
    public  int addCluster( Cluster clu, Surface srf)
    {
        return _lyr.addCluster( clu, srf );
    }
    
    //
    
    /**
     *Drop all clusters from this layer.
     * Default here is to return an error.
     *
     */
    public  void dropClusters()
    {
        _lyr.dropClusters();
        
    }
    
    
    /**
     *output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        return getClass().getName()+" "+_lyr + "\n with " + _inter;
        
    }
    
}

