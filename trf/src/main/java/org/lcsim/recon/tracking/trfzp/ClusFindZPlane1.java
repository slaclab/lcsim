package org.lcsim.recon.tracking.trfzp;

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
 * Cluster finder for a z plane. It maintains a map of clusters of type ZPlane1
 * indexed by position so the clusters near a track can be retrieved quickly.
 * <p>
 * A track is defined to be near a cluster if the chi-square difference between
 * the corresponding hit measurement and prediction is less than _max_chsq_diff.
 *
 * @author Norman A. Graf
 * @version 1.0
 *
 */
public class ClusFindZPlane1 extends ClusterFindManager
{

    // static methods
    //
    /**
     * Return a String representation of the class' type name. Included for
     * completeness with the C++ version.
     *
     * @return A String representation of the class' type name.
     */
    public static String typeName()
    {
        return "ClusFindZPlane1";
    }

    //
    /**
     * Return a String representation of the class' type name. Included for
     * completeness with the C++ version.
     *
     * @return A String representation of the class' type name.
     */
    public static String staticType()
    {
        return typeName();
    }

    // attributes
    // surface
    private SurfZPlane _szp;

    // stereo angle
    private double _wx;
    private double _wy;

    // maximum allowed chi-square difference
    private double _max_chsq_diff;

    // clusters
    private TreeMap _clusters;

    // methods
    //
    /**
     * Construct an instance from the z location of the plane, the stereo angles
     * for the x-y measurement, and the maximum chi-square difference which
     * defines the nearness of a hit to a track.
     *
     * @param zpos The z position of the z plane.
     * @param wx The stereo angle for the x measurement.
     * @param wy The stereo angle for the y measurement.
     * @param max_chsq_diff The maximum chi-square for a track-hit association.
     */
    public ClusFindZPlane1(double zpos, double wx, double wy, double max_chsq_diff)
    {
        _clusters = new TreeMap(); //use a TreeMap to automatically sort
        _szp = new SurfZPlane(zpos);
        _wx = wx;
        _wy = wy;
        _max_chsq_diff = max_chsq_diff;
    }

    //
    /**
     * Add a cluster to this ClusterFindManager. The cluster must be of type
     * ClusCylPhi or ClusCylPhiZ.
     *
     * @param clus The Cluster to add.
     * @return 0 if successful.
     */
    public int addCluster(Cluster clus)
    {
        // declare the key value
        double keyval = 0.;

        // Extract the cluster position depending on type
        if (clus.type().equals(ClusZPlane1.staticType())) {
            ClusZPlane1 clu = (ClusZPlane1) clus;
            Assert.assertTrue(clu.surface().equals(_szp));
            Assert.assertTrue(clu.wX() == _wx);
            Assert.assertTrue(clu.wY() == _wy);
            keyval = clu.aXY();
        } else {
            Assert.assertTrue(false);
            System.exit(1);
        }

        // Check there is no other cluster at this position.
        Double key = new Double(keyval);
        if (_clusters.containsKey(key)) {
            Assert.assertTrue(false);
            System.exit(2);
        }
        // Store the cluster.
        _clusters.put(key, clus);
        return 0;

    }

    //
    /**
     * Drop all clusters from this manager.
     *
     */
    public void dropClusters()
    {
        _clusters.clear();
    }

    //
    /**
     * Return the surface.
     *
     * @return The Surface associated to this manager.
     */
    public Surface surface()
    {
        return _szp;
    }

    //
    /**
     * Return all the clusters.
     *
     * @return The list of clusters managed by this manager.
     */
    public List clusters()
    {
        // Create the list of clusters.
        List clusters = new ArrayList();

        // Loop over clusters and add to output list.
        for (Iterator iclu = _clusters.values().iterator(); iclu.hasNext();) {
            clusters.add(iclu.next());
        }

        return clusters;

    }

    //
    /**
     * Return all the clusters near a track at the surface. Nearness is
     * delimited by the maximum chi-squared between the track prediction and the
     * cluster measurement.
     *
     * Clusters have been ordered by axy. The nearby subset is returned in the
     * same order.
     *
     * @param tre The ETrack for which to return clusters.
     * @return A list of clusters near the track tre.
     */
    public List clusters(ETrack tre)
    {
        // Create the list of clusters.
        List clusters = new ArrayList();
        // Check the track surface.
        if (!tre.surface().equals(_szp)) {
            Assert.assertTrue(false);
            return clusters;
        }

        // If there are no clusters, return the empty list.
        if (_clusters.size() == 0) {
            return clusters;
        }

        // Fetch the predicted position.
        double value = _wx * tre.vector(SurfZPlane.IX)
                + _wy * tre.vector(SurfZPlane.IY);

        //for time being loop over all the clusters.
        // need to be smarter later
        for (Iterator it = _clusters.values().iterator(); it.hasNext();) {
            //get the cluster
            Cluster clu = (Cluster) it.next();
            // is cluster within range?
            if (chsq_diffzp1(clu, tre) < _max_chsq_diff) {
                // if so add it to the list
                clusters.add(clu);
            }
        }

        /*
         // Fetch first cluster after the prediction.
 
         ClusterMap::const_iterator iclu1 = _clusters.lower_bound(value);
 
         // Loop over clusters from this and above.
         // If cluster is close, add it to the list; otherwise exit loop.
 
         ClusterMap::const_iterator iclu = iclu1;
         for(iclu = iclu1 ; iclu != _clusters.end(); ++iclu ) {
         ClusterPtr pclu = (*iclu).second;
         if ( chsq_diffzp1(pclu,tre) > _max_chsq_diff ) break;
         clusters.push_back( pclu );
         }
 
         // We have checked all clusters in the range (iclu1, +).
         // Next go backwards from iclu1.
         iclu = iclu1;
         if( iclu == _clusters.begin() ) return clusters;
         iclu--;
         while ( true ) {
         ClusterPtr pclu = (*iclu).second;
         if ( chsq_diffzp1(pclu,tre) > _max_chsq_diff ) break;
         clusters.push_front( pclu );
         // decrement iterator; check if the begining is reached
         if ( iclu == _clusters.begin() ) break;
         iclu--;
         }
         */
        // Return the nearby clusters.
        return clusters;
    }

    /**
     * Return the maximum chi-square difference which defines a hit to be near a
     * track.
     *
     * @return The maximum chi-square for a track-hit association.
     */
    public double maxChsqDiff()
    {
        return _max_chsq_diff;
    }

    /**
     * Return the x component of the measurement
     * @return wx
     */
    public double wx()
    {
        return _wx;
    }

    /**
     * Return the y component of the measurement
     * @return
     */
    public double wy()
    {
        return _wy;
    }

    //
    /**
     * Return a String representation of the class' type name. Included for
     * completeness with the C++ version.
     *
     * @return A String representation of the class' type name.
     */
    public String type()
    {
        return staticType();
    }

    //
    /**
     * output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("1D Cluster finder for \n" + _szp + "\n");
        sb.append("wx weight is " + _wx + ".\n");
        sb.append("wy weight is " + _wy + ".\n");
        sb.append("Maximum chi-square difference is "
                + _max_chsq_diff + ".\n");
        int count = _clusters.size();
        if (count > 0) {
            sb.append("Finder has " + count + " cluster");
            if (count > 1) {
                sb.append("s");
            }
            sb.append(": \n");
            for (Iterator i = _clusters.entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Map.Entry) i.next();
                sb.append(e.getKey() + ": " + e.getValue() + "\n");
            }
        } else {
            sb.append("Finder has no clusters.");
        }
        return sb.toString();
    }

    //**********************************************************************
    // Helper functions.
    //**********************************************************************
    // Calculate the chi-square difference between a cluster and a track.
    // It is assumed the cluster generates exactly one prediction and that
    // this prediction has one dimension.
    private static double chsq_diffzp1(Cluster clu, ETrack tre)
    {

        // Fetch the hit predictions.
        List hits = clu.predict(tre, clu);

        // Check there is one hit.
        Assert.assertTrue(hits.size() == 1);

        // Fetch the hit.
        Hit hit = (Hit) hits.get(0);

        // Check the hit dimension.
        Assert.assertTrue(hit.size() == 1);

        // Fetch the difference between prediction and measurement.
        double diff = hit.differenceVector().get(0);

        // Fetch the measurement error and the prediction error.
        double emeas = hit.measuredError().get(0, 0);
        double epred = hit.predictedError().get(0, 0);

        // Calculate and return chi-square difference.
        return diff * diff / (emeas + epred);

    }

}
