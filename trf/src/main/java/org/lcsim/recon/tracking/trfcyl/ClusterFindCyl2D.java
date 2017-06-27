package org.lcsim.recon.tracking.trfcyl;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.SortedMap;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.ETrack;

import org.lcsim.recon.tracking.trflayer.ClusterFindManager;

/**
 * Cluster finder for a cylindrical layer.  It maintains a map of
 * clusters of type HitCylPhiZ2D indexed by position.
 * The clusters near a track can be retrieved quickly.
 *<p>
 * A track is defined to be near a cluster if the chi-square difference
 * between the corresponding hit measurement and prediction is less
 * than _max_chsq_diff.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class ClusterFindCyl2D extends ClusterFindManager
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
    { return "ClusterFindCyl2D";
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
    private SurfCylinder _scy;
    
    // maximum allowed chi-square difference
    private double _max_chsq_diff;
    
    // clusters
    private TreeMap _clusters;
    
    
    // methods
    
    //
    
    /**
     *Construct an instance from the cylindrical radius, the stereo angle, and the maximum chi-square
     *difference which defines the nearness of a hit to a track.
     *
     * @param   radius The radius of the cylinder.
     * @param   max_chsq_diff The maximum chi-square for a track-hit association.
     */
    public ClusterFindCyl2D(double radius, double max_chsq_diff)
    {
        //	_clusters = new HashMap(); // may need to use a HashMap later for speed
        _clusters = new TreeMap(); //use a TreeMap to automatically sort in phi
        _scy = new SurfCylinder(radius);
        _max_chsq_diff = max_chsq_diff;
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType();
    }
    
    //
    
    /**
     *Return the radius of the cylinder.
     *
     * @return The radius of this cylinder.
     */
    public double radius()
    { return _scy.radius();
    }
    
    //
    
    /**
     *  Return the maximum chi-square difference which defines a hit to be near a track.
     *
     * @return  The maximum chi-square for a track-hit association.
     */
    public double maxChsqDiff()
    { return _max_chsq_diff;
    }
    
    //
    
    /**
     *Add a cluster to this ClusterFindManager.
     * The cluster must be of type ClusCylPhi or ClusCylPhiZ.
     *
     * @param   clus The Cluster to add.
     * @return  0 if successful.
     */
    public int addCluster( Cluster clus )
    {
        // declare the key value
        double keyval = 0.;
        
        // Extract the cluster position
        ClusCylPhiZ2D clu = ( ClusCylPhiZ2D) clus;
        Assert.assertTrue( clu.surface().equals(_scy) );
        keyval = clu.phi();
        
        // Put value in range (0,2pi)
        keyval = TRFMath.fmod1( keyval, TRFMath.TWOPI );
        Double key = new Double(keyval);
        // Check there is no other cluster at this position.
        if ( _clusters.containsKey(key))
        {
            Assert.assertTrue(false);
            System.exit(2);
        }
        
        // Store the cluster.
        _clusters.put(key, clus);
        return 0;
        
    }
    
    //
    
    /**
     *Drop all clusters from this manager.
     *
     */
    public void dropClusters()
    {
        _clusters.clear();
    }
    
    //
    
    /**
     *Return the surface.
     *
     * @return The Surface associated to this manager.
     */
    public   Surface surface()
    { return _scy;
    }
    
    //
    
    /**
     *Return all the clusters.
     *
     * @return The list of clusters managed by this manager.
     */
    public  List clusters()
    {
        // Create the list of clusters.
        List clusters = new ArrayList();
        
        // Loop over clusters and add to output list.
        
        for ( Iterator iclu=_clusters.values().iterator();  iclu.hasNext(); )
            clusters.add( iclu.next() );
        
        return clusters;
        
    }
    
    //
    
    
    /**
     *Return all the clusters near a track at the surface. Nearness is delimited by the
     *maximum chi-squared between the track prediction and the cluster measurement.
     *
     * Clusters have been ordered by phi or phiz in range (0,2pi). need to check!
     * The nearby subset is returned in the same order.
     *
     * @param   tre The ETrack for which to return clusters.
     * @return  A list of clusters near the track tre.
     */
    public  List clusters(  ETrack tre)
    {
        
        // Create the list of clusters.
        List clusters = new ArrayList();
        
        // Check the track surface.
        if ( !tre.surface().equals(_scy) )
        {
            Assert.assertTrue(false);
            return clusters;
        }
        
        // If there are no clusters, return the empty list.
        if ( _clusters.size() == 0 )
        {
            return clusters;
        }
        
        // Fetch the predicted phi position.
        double value = tre.vector(0);
        value = TRFMath.fmod1( value, TRFMath.TWOPI );
        
        //for time being loop over all the clusters.
        // need to be smarter later
        
        for(Iterator it = _clusters.values().iterator(); it.hasNext(); )
        {
            //get the cluster
            Cluster clu = (Cluster) it.next();
            // is cluster within range?
            if (chsqDiff(clu,tre) < _max_chsq_diff )
            {
                // if so add it to the list
                clusters.add( clu );
            }
        }
        
                /*
                // Fetch first cluster after the prediction.
                // If there is none, wrap around to the first.
                SortedMap sm = _clusters.tailMap( new Double(value) );
                 
                // Loop over clusters from this and above.
                // If cluster is close, add it to the list; otherwise exit loop.
                 
                Iterator it = sm.values().iterator();
                 
                while(true)
                {
                        //get the cluster
                        Cluster clu = (Cluster) it.next();
                // is cluster within range?
                        if(chsq_diff(clu,tre) > _max_chsq_diff ) break;
                // if so add it to the list
                clusters.add( clu );
                // if we get to end of tailMap (and therefore end of cluster map)
                // continue at beginning of cluster map.
                // needed because of phi-wrap in cylindrical detector
                if(!it.hasNext()) it = _clusters.values().iterator();
                //have we gotten all the way back to the beginning of the tailMap?
                //if so break out of the loop. (shouldn't happen, but check anyway)
                if(it == sm.values().iterator()) break;
                }
                 */
                /*
                ClusterMap::const_iterator iclu = iclu1;
                ClusterMap::const_iterator iclu2;
                while ( true ) {
                ClusterPtr pclu = (*iclu).second;
                iclu2 = iclu;
                if ( chsq_diff(pclu,tre) > _max_chsq_diff ) break;
                clusters.push_back( pclu );
                // increment iterator; loop from end back to beginning
                if ( ++iclu == _clusters.end() ) iclu = _clusters.begin();
                // Exit if we are back at the beginning.
                if ( iclu == iclu1 ) break;
                }
                 */
                /*
                // We have checked all clusters in the (circular) range greater than
                // the prediction.
                // Now loop back, again accounting for the phi-wrap problem
                // Need to find out how to get a generic collection reverse iterator.
                // Until then copy the sorted map into a LinkedList.
                SortedMap sm2 = _clusters.headMap( new Double(value) );
                LinkedList ll = new LinkedList(sm2.values());
                ListIterator it2 = ll.listIterator(ll.size());
                 */
                /*
                iclu = iclu1;
                if ( iclu == _clusters.begin() ) iclu = _clusters.end();
                while ( --iclu != iclu2 ) {
                ClusterPtr pclu = (*iclu).second;
                if ( chsq_diff(pclu,tre) > _max_chsq_diff ) break;
                clusters.push_front( pclu );
                // decrement iterator; loop from beginning back to end
                if ( iclu == _clusters.begin() ) iclu = _clusters.end();
                }
                 
                // Return the nearby clusters.
                 
                 */
        return clusters;
        
    }
    
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        
        StringBuffer sb = new StringBuffer("Cluster finder for " + _scy + ".\n");
        sb.append( "Maximum chi-square difference is "
                + _max_chsq_diff + "." + ".\n");
        int count = _clusters.size();
        if ( count>0 )
        {
            sb.append( "Finder has " + count + " cluster");
            if ( count > 1 ) sb.append( "s");
            sb.append( ": \n");
            for (Iterator i=_clusters.entrySet().iterator(); i.hasNext(); )
            {
                Map.Entry e = (Map.Entry) i.next();
                sb.append(e.getKey() + ": " + e.getValue() + "\n");
            }
        }
        else
        {
            sb.append( "Finder has no clusters.");
        }
        return sb.toString();
    }
    
    //**********************************************************************
    // Helper functions.
    //**********************************************************************
    
    // Calculate the chi-square difference between the cluster and a track.
    // It is assumed that the cluster generates exactly one prediction and that
    // this prediction has two dimensions.
    
    private static double chsqDiff( Cluster clu,   ETrack tre)
    {
        
        // Fetch the hit predictions.
        List hits = clu.predict(tre,clu);
        
        // Check there is one hit.
        Assert.assertTrue( hits.size() == 1 );
        
        // Fetch the hit.
        Hit hit = (Hit) hits.get(0);
        
        // Check the hit dimension.
        Assert.assertTrue( hit.size() == 2 );
        
        // Fetch the difference between prediction and measurement.
        double diff0 = hit.differenceVector().get(SurfCylinder.IPHI); // phi
        double diff1 = hit.differenceVector().get(SurfCylinder.IZ);   // z
        
        if( diff0 == 0.0 && diff1 == 0.0 ) return 0.0;
        
        // Fetch the measurement errors and the prediction errors
        // and create the covariance matrix.
        double a = hit.measuredError().get(0,0) - hit.predictedError().get(0,0);
        double b = hit.measuredError().get(0,1) - hit.predictedError().get(0,1);
        double c = hit.measuredError().get(1,1) - hit.predictedError().get(1,1);
        
        // Calculate and return chi-square difference.
        // Recall that the covariance matrix needs to be inverted first before being sandwiched
        // between the difference vectors
        double det = a*c - b*b;
        if (det == 0. ) return 1e15;
        double chisq = ( c*diff0*diff0 + a*diff1*diff1 - 2*b*diff0*diff1)/det;
        Assert.assertTrue(chisq > 0. );
        
        return chisq;
        
    }
}

