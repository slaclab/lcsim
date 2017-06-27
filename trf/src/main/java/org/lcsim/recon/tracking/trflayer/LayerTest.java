package org.lcsim.recon.tracking.trflayer;
// Concrete class for testing the abstract class Layer.
//
// This also serves an an example of implementing a layer.
//
import java.util.*;

import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.MissTest;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfutil.Assert;

public class LayerTest extends Layer
{
    private boolean debug;
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "LayerTest"; }
    
    // Return the type.
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    // surface contained in this layer
    private Surface _srf;
    
    // Cluster finder for this layer.
    private ClusterFinderTest _finder;
    
    // Number of times to propagate to each surface before exiting.
    private int _count;
    
    // List of clusters.
    private List _clusters;
    
    // methods
    
    public String toString()
    {
        return
                "Sub Test layer with surface " + _srf;
    }
    
    // methods
    
    // constructor
    // arguments are the position bounds of the surface
    public LayerTest(double x)
    {
        _srf = new SurfTest(x);
        _finder = new ClusterFinderTest(x);
        _count = 1;
        _clusters = new ArrayList();
    }
    
    // constructor
    // arguments are the position bounds of the surface
    // Index sepcifies the surface count.
    public LayerTest(double x, int count)
    {
        _srf = new SurfTest(x);
        _finder  = new ClusterFinderTest(x);
        _count = count;
        Assert.assertTrue( _count > 0 );
        _clusters = new ArrayList();
    }
    
    // Return the type.
    public String get_type()
    { return staticType(); }
    
    // Return the count.
    public int get_count()
    { return _count; }
    
    // Return the surface.
    public Surface get_surface()
    { return _srf; }
    
    // original code for LTrack
    // propagation
    // propagation is successful from any surface but this one.
    public List
            _propagate(LTrack trl0, Propagator prop)
    {
        
        // Copy the input track.
        List ltracks = new ArrayList();
        ltracks.add(new LTrack(trl0));
        LTrack trl = (LTrack) ltracks.get(0);
        
        // Fetch the input ETrack
        ETrack tre = trl.track();
        if(debug) System.out.println("tre= "+tre);
        // Fetch the input status.
        if(debug) System.out.println("trl= "+trl);
        LayerStat lstat = trl.status();
        
        // Check the state.
        // Something is wrong if it is greater than _count.
        int count = lstat.state();
        Assert.assertTrue( count >= 0 );
        Assert.assertTrue( count < _count );
        
        // Set the new count.
        lstat.setState( ++count );
        
        // Propagate the track.
        // In a more realistic layer, we would use the propagator prop
        // and would have to deal with the possibility of failure.
        tre.setSurface(_srf);
        
        // If this is the last propagation, set at_exit flag.
        if ( count == _count ) lstat.setAtExit();
        
        // Define a miss with likelihood 0.5.
        // A more realistic miss would be constructed from the track.
        MissTest miss = new MissTest(_srf.parameter(0),0.5);
        lstat.setMiss(miss);
        
        // Set the cluster finder.
        lstat.setFinder(_finder);
        
        // Make this the the cluster status.
        trl.setClusterStatus();
        
        return ltracks;
        
    }
    
/*
// this needs to be replaced by above call with LTrack argument.
  // propagation
  // propagation is successful from any surface but this one.
  public List
   _propagate(ETrack trl0, Propagator prop) {
 
 
    List ltracks = new ArrayList();
 
    // copy the input ETrack
    ETrack tre = new ETrack(trl0);
 
    // Propagate the track.
    // In a more realistic layer, we would use the propagator prop
    // and would have to deal with the possibility of failure.
        // Here we simply reset its surface.
    tre.set_surface(_srf);
 
    //add the track to the list
        ltracks.add(tre);
 
        //return the list of tracks
    return ltracks;
 
  }
 */
    //remove to here
    public List clusterSurfaces()
    {
        ArrayList cl = new ArrayList();
        cl.add(_srf);
        return cl;
    }
    
    public List clusters()
    {
        return new ArrayList();
    }
    
    public List clusters(Surface srf)
    {
        return new ArrayList();
    }
    
    public void dropClusters()
    {
        _clusters.clear();
    }
    
    public int addCluster(Cluster clu)
    {
        _clusters.add(clu);
        return 0;
    }
    
    public int addCluster(Cluster clu, Surface srf)
    {
        if ( ! _srf.pureEqual(srf) ) return 1;
        return addCluster(clu);
    }
    
    public boolean equals(LayerTest lt)
    {
        int stat = 0;
        if ( !get_surface().equals(lt.get_surface()) ) stat += 1;
        if ( get_count() != lt.get_count() ) stat += 2;
        return stat == 0;
    }
    
}

