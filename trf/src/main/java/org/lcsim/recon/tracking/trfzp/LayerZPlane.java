package  org.lcsim.recon.tracking.trfzp;
// LayerZPlane

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

import  org.lcsim.recon.tracking.spacegeom.SpacePath;

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
public class LayerZPlane extends Layer
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
    { return "LayerZPlane"; }
    
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    
    //attributes
    
    // ZPlane surface
    private SurfZPlane _srf;
    
    // cluster finder
    private ClusterFindManager _find;
    
    // Miss.
    // If defined, a copy of this miss is added to the status after
    // updating with track.
    private Miss _miss;
    
    //methods
    // propagate to this layer
    protected List
    _propagate(  LTrack trl0,  Propagator prop)
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
            PropDir dir = PropDir.BACKWARD;
            
            double dz= tre.spacePath().dz();
            double tz= tre.spacePoint().z();
            double sz= _srf.z();
            double dz_p=sz-tz;
            if( dz_p*dz > 0. ) dir = PropDir.FORWARD;
            // Propagate.
            // needs review here..
            PropStat pstat = prop.errDirProp(tre,_srf,dir);
            // If propagation fails, exit.
            if ( ! pstat.success() ) return ltracks;
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
        
    }
    
    // constructors
    
    //
    
    
    /**
     *Construct an instance given the layer's z position and a manager to
     * find the clusters on this layer.
     *Set find = null if no clusters are associated with layer.
     *
     * @param   zpos The layer's z position
     * @param   find The ClusterFindManager for this layer. Set find = null if no clusters are associated with layer.
     */
    public LayerZPlane(  double zpos,
    ClusterFindManager find )
    {
        _srf = new SurfZPlane(zpos);
        _find = find;
    }
    
    
    /**
     *Construct an instance given a ZPlane surface and a manager to
     * find the clusters on this layer.
     *Set find = null if no clusters are associated with layer.
     *
     * @param   zplane The ZPlane surface.
     * @param   find The ClusterFindManager for this layer. Set find = null if no clusters are associated with layer.
     *
     */
    public LayerZPlane(  SurfZPlane zplane,
    ClusterFindManager find )
    {
        _srf = new SurfZPlane(zplane);
        _find = find;
    }
    
    //
    
    /**
     *Construct an instance replicating the LayerZPlane ( copy constructor ).
     *
     * @param   lzp The LayerZPlane to replicate.
     */
    public LayerZPlane(LayerZPlane lzp)
    {
        _srf = new SurfZPlane(lzp._srf);
        _find = lzp._find;
        _miss = lzp._miss;
    }
    
    // implement Layer methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String get_type()
    { return staticType(); }
    
    
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
    
    // We do have clusters.
    
    /**
     *Check whether layer has clusters.
     *
     * @return true if this layer has clusters.
     */
    public boolean hasClusters()
    { return true; }
    
    
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
    
    // methods specific to this subclass
    
    //
    
   /**
     *Drop clusters from this layer.
     *
     */
    public void dropClusters()
    {
        if ( _find!=null ) _find.dropClusters();
    }
    
    //
    
     /**
     *Add a cluster to this layer.
     * The cluster surface is required to match this ZPlane
     *
     * @param   clu The Cluster to add.
     * @return 0 if successful. 
     *          1 if the cluster's surface is not a cylinder.
     *          2 if unsuccessful.
     */
    public int addCluster( Cluster clu)
    {
        boolean ok = _srf.pureEqual( clu.surface() );
        Assert.assertTrue( ok );
        if ( ! ok ) return 1;
        if ( _find!=null ) return _find.addCluster(clu);
        else return 2;   }
    
    //
    
     /**
     *Add a cluster associated with a specified surface.
     * The cluster surface and surface are required to match
     * this z plane.
     *
     * @param   clu The Cluster to add.
     * @param   srf The Surface to which to add the cluster.
     * @return 0 if successful. 
     *          1 if the cluster's surface is not a cylinder.
     *          2 if unsuccessful.
     *          3 if surface is wrong.
     */
    public int addCluster( Cluster clu, Surface srf)
    {
        // If the surface is wrong, return an error
        if ( !srf.equals(_srf) )
        {
            reportInvalidSurface(srf);
            return 3;
        }
        return addCluster(clu);
    }
    
    
    /**
     *Return the surface associated to this layer.
     *
     * @return The surface associated to this layer.
     */
    public  SurfZPlane surface()
    {return _srf;}
    
     /**
     *Return the Cluster finder.
     *
     * @return The ClusterFindManager associated to this layer.
     */
    public   ClusterFindManager finder()
    {return _find;}
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("ZPlane layer.\n Surface: " + _srf);
        if ( _find!=null ) sb.append("\n Finder: " +_find);
        else sb.append("No finder.");
        if ( _miss!=null ) sb.append("\n Miss: " +_miss + "\n");
        else sb.append("\n No miss defined.\n");
        
        return sb.toString();
    }
    
}
