package org.lcsim.recon.tracking.trfcyl;
import java.util.*;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Miss;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropStat;

import org.lcsim.recon.tracking.spacegeom.SpacePath;

import org.lcsim.recon.tracking.trflayer.Layer;
import org.lcsim.recon.tracking.trflayer.ClusterFindManager;
import org.lcsim.recon.tracking.trflayer.LTrack;

/**
 * This is a simple layer consisting of one cylindrical surface.
 * Propagation assumes tracks originate from inside the cylinder;
 * i.e. forward for tracks inside and backward for tracks outside.
 * Clusters may be associated with the layer via a cluster finder.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class LayerCylinder extends Layer
{
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "LayerCylinder";
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName();
    }
    
    // attributes
    
    // surface
    private Surface _srf;
    
    // cluster finder
    private ClusterFindManager _find;
    
    // Miss.
    // If defined, a copy of this miss is added to the status after
    // updating with track.
    private Miss _miss;
    
    // implement Layer methods
    
    // propagate to this layer
    protected List
            _propagate(LTrack trl0, Propagator prop)
    {
        
        // Create the output track list.
        // This is empty for early return.
        List ltracks = new ArrayList();
        
        // The input state should be the default one.
        if ( trl0.status().state() != 0 ) return ltracks;
        
        // Copy the track.
        LTrack trl = new LTrack(trl0);
        
        // Access the kinematic track.
        ETrack tre = trl.track();
        
        // Fetch the crossing status.
        CrossStat xstat = _srf.status(tre);
        
        // If track is not on surface, propagate it.
        if ( ! xstat.at() )
        {
            // Find the direction for propagation.
            PropDir dir = PropDir.FORWARD;
            SpacePath svec = tre.spacePath();
            if ( xstat.inside() )
            {
                Assert.assertTrue( ! xstat.outside() );
                if ( svec.drxy() < 0.0 ) dir = PropDir.BACKWARD;
            }
            else
            {
                Assert.assertTrue( xstat.outside() );
                if ( svec.drxy() > 0.0 ) dir = PropDir.BACKWARD;
            }
            // Propagate.
            // needs review here..
            //		trl.get_prop_stat() = prop.err_dir_prop(tre,*_psrf,dir);
            PropStat pstat = prop.errDirProp(tre,_srf,dir);
            // If propagation fails, exit.
            if ( ! trl.propStat().success() ) return ltracks;
        }
        else
        {
            trl.propStat().setPathDistance(0.0);
        }
        
        // Check the crossing status.
        // We could but do not check bounds.
        xstat = _srf.status(tre);
        Assert.assertTrue( xstat.at() );
        
        // Modify the return status.
        // Set the state to a non-default value and set at_exit.
        trl.status().reset();
        trl.status().setState(1);
        trl.status().setAtExit();
        if ( _find != null)
        {
            trl.status().setFinder(_find);
            trl.setClusterStatus();
        }
        if ( _miss != null )
        {
            Miss miss = _miss.newCopy();
            miss.update(tre);
            trl.status().setMiss(miss);
        }
        // Update the propagtor return status.
        
        // Put the track on the list and return.
        ltracks.add(trl);
        
        return ltracks;
        
                /*
                //This needs to be replaced by above...
                 
                // Create the output track list.
                // This is empty for early return.
                List ltracks = new ArrayList();
                 
                // Access the kinematic track.
                ETrack tre = new ETrack(trl);
                 
                // Fetch the crossing status.
                CrossStat xstat = _srf.status(tre);
                 
                // If track is not on surface, propagate it.
                if ( ! xstat.at() )
                {
                // Find the direction for propagation.
                PropDir dir = PropDir.FORWARD;
                SpacePath svec = tre.space_vector();
                if ( xstat.inside() )
                {
                Assert.assertTrue( ! xstat.outside() );
                if ( svec.drxy() < 0.0 ) dir = PropDir.BACKWARD;
                }
                else
                {
                Assert.assertTrue( xstat.outside() );
                if ( svec.drxy() > 0.0 ) dir = PropDir.BACKWARD;
                }
                // Propagate.
                //    trl.get_prop_stat() = prop.err_dir_prop(tre,_srf,dir);
                PropStat pstat = prop.err_dir_prop(tre,_srf,dir);
                // If propagation fails, exit.
                //    if ( ! trl.get_prop_stat().success() ) return ltracks;
                if(!pstat.success()) return ltracks;
                }
                else
                {
                //    trl.get_prop_stat().set_path_distance(0.0); //needs work here
                }
                 
                // Check the crossing status.
                // We could but do not check bounds.
                xstat = _srf.status(tre);
                Assert.assertTrue( xstat.at() );
                 */		/*
                // Modify the return status.
                // Set the state to a non-default value and set at_exit.
                trl.get_status().reset();
                trl.get_status().set_state(1);
                trl.get_status().set_at_exit();
                if ( _pfind ) {
                trl.get_status().set_finder(*_pfind);
                trl.set_cluster_status();
                }
                if ( _pmiss ) {
                Miss* pmiss = _pmiss->new_copy();
                pmiss->update(tre);
                trl.get_status().set_miss(*pmiss);
                delete pmiss;
                }
                // Update the propagtor return status.
                                 
                // Put the track on the list and return.
                  */
                /*
                ltracks.add(trl);
                return ltracks;
                 */
        
        
    }
    
    //
    
    /**
     *Construct an instance given the layer's radius and z extent.
     *
     * @param   r      The radius of the cylindrical layer (cm).
     * @param   zmin   The minimum z extent of the cylindrical layer (cm).
     * @param   zmax   The maximum z extent of the cylindrical layer (cm).
     */
    public LayerCylinder(double r, double zmin, double zmax)
    {
        _srf =  new BSurfCylinder(r,zmin,zmax);
    }
    
    
    /**
     *Construct an instance given the layer's radius, z extent and a manager to
     * find the clusters on this layer.
     *
     * @param   r      The radius of the cylindrical layer (cm).
     * @param   zmin   The minimum z extent of the cylindrical layer (cm).
     * @param   zmax   The maximum z extent of the cylindrical layer (cm).
     * @param   find  The ClusterFindManager for this layer. Set find = null if no clusters are associated with layer.
     */
    public LayerCylinder(double r, double zmin, double zmax, ClusterFindManager find)
    {
        _srf =  new BSurfCylinder(r,zmin,zmax);
        _find = find;
    }
    
    //
    
    /**
     *Construct an instance given the layer's radius, z extent, a manager to
     * find the clusters on this layer and a Miss.
     *
     * @param   r      The radius of the cylindrical layer (cm).
     * @param   zmin   The minimum z extent of the cylindrical layer (cm).
     * @param   zmax   The maximum z extent of the cylindrical layer (cm).
     * @param   find   The ClusterFindManager for this layer. Set find = null if no clusters are associated with layer.
     * @param   miss   The Miss for this layer.
     */
    public LayerCylinder(double r, double zmin, double zmax,
            ClusterFindManager find, Miss miss )
    {
        _srf =  new BSurfCylinder(r,zmin,zmax);
        _find = find;
        _miss = miss;
    }
    
    //
    
    /**
     *Construct an instance given a bounded cylindrical surface, a manager to
     * find the clusters on this layer and a Miss.
     *
     * @param   srf    The BSurfCylinder which represents this cylindrical layer.
     * @param   find   The ClusterFindManager for this layer. Set find = null if no clusters are associated with layer.
     * @param   miss   The Miss for this layer.
     */
    public LayerCylinder( BSurfCylinder srf,
            ClusterFindManager find, Miss  miss  )
    {
        _srf =  srf;
        _find = find;
        _miss = miss;
    }
    
    //
    
    /**
     *Construct an instance replicating the LayerCylinder ( copy constructor ).
     *
     * @param   lc The LayerCylinder to replicate.
     */
    public LayerCylinder( LayerCylinder lc)
    {
        _srf =  lc._srf;
        _find = lc._find;
        _miss = lc._miss;
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Cylinder layer.\n Surface: " + _srf);
        if ( _find!=null ) sb.append("\n Finder: " +_find);
        else sb.append("No finder.");
        if ( _miss!=null ) sb.append("\n Miss: " +_miss + "\n");
        else sb.append("\n No miss defined.\n");
        
        return sb.toString();
    }
    
    // implement Layer methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    {
        return staticType();
    }
    
    //
    
    /**
     *Return the list of surfaces.
     *
     * @return The list of surfaces associated with this cylindrical layer.
     */
    public List clusterSurfaces()
    {
        List tmp = new ArrayList();
        tmp.add(_srf);
        return tmp;
    }
    
    //
    
    /**
     *Return the list of clusters.
     *
     * @return The list of clusters associated with this layer.
     */
    public List clusters()
    {
        if ( _find != null ) return _find.clusters();
        else return  new ArrayList();
    }
    
    //
    
    /**
     *Return the clusters associated with a specified surface.
     *
     * @param   srf The Surface within this layer for which to return clusters.
     * @return A list of clusters for the surface srf. If the surface is not associated with this layer, return an empty list.
     */
    public List clusters(Surface srf)
    {
        // If the surfaces is wrong, return an empty list.
        if ( !srf.equals(_srf) )
        {
            reportInvalidSurface(srf);
            return new ArrayList();
        }
        return clusters();
        
    }
    
    //
    
    /**
     *Add a cluster to this layer.
     * The cluster surface is required to match this cylinder.
     *
     * @param   clu The Cluster to add.
     * @return 0 if successful.
     *          1 if the cluster's surface is not a cylinder.
     *          2 if unsuccessful.
     */
    public int addCluster(Cluster clu)
    {
        boolean ok = _srf.pureEqual( clu.surface() );
        Assert.assertTrue( ok );
        if ( ! ok ) return 1;
        if ( _find!=null ) return _find.addCluster(clu);
        else return 2;
    }
    
    //
    
    /**
     *Add a cluster associated with a specified surface.
     * The cluster surface and surface are required to match
     * this cylinder.
     *
     * @param   clu The Cluster to add.
     * @param   srf The Surface to which to add the cluster.
     * @return 0 if successful.
     *          1 if the cluster's surface is not a cylinder.
     *          2 if unsuccessful.
     *          3 if surface is wrong.
     */
    public int addCluster(Cluster clu, Surface srf)
    {
        // If the surface is wrong, return an error
        if ( !srf.equals(_srf) )
        {
            reportInvalidSurface(srf);
            return 3;
        }
        return addCluster(clu);
    }
    
    //
    
    /**
     *Drop clusters from this layer.
     *
     */
    public void dropClusters()
    {
        if ( _find!=null ) _find.dropClusters();
    }
    
    // methods specific to this subclass
    
    //
    
    /**
     *Return the surface associated to this layer.
     *
     * @return The surface associated to this layer.
     */
    public  Surface surface()
    { return _srf;
    }
    
    //
    
    /**
     *Return the Cluster finder.
     *
     * @return The ClusterFindManager associated to this layer.
     */
    public  ClusterFindManager finder()
    { return _find;
    }
    
}

