package  org.lcsim.recon.tracking.trfzp;
// ClusFindZPlane2

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;

import org.lcsim.recon.tracking.trflayer.ClusterFindManager;

/**
 * Cluster finder for a z plane.  It maintains a map of
 * clusters of type ZPlane2 indexed by x position so
 * the clusters near a track can be retrieved quickly.
 *<p>
 * A track is defined to be near a cluster if the chi-square difference
 * between the corresponding hit measurement and prediction is less
 * than _max_chsq_diff.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class ClusFindZPlane2 extends ClusterFindManager
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
    { return "ClusFindZPlane2"; }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    // surface
    private SurfZPlane _szp;
    
    // maximum allowed chi-square difference
    private double _max_chsq_diff;
    
    // clusters
    private List _clusters;
    
    
    // methods
    
    //
    
    /**
     *Construct an instance from the z location of the plane and the maximum chi-square
     *difference which defines the nearness of a hit to a track.
     *
     * @param   zpos The z position of the z plane.
     * @param   max_chsq_diff The maximum chi-square for a track-hit association.
     */
    public ClusFindZPlane2(double zpos, double max_chsq_diff)
    {
        _clusters = new ArrayList(); //use a TreeMap to automatically sort
        _szp = new SurfZPlane(zpos);
        _max_chsq_diff = max_chsq_diff;
    }
    
    //
    
    /**
     *Add a cluster to this ClusterFindManager.
     * The cluster must be of type ClusCylPhi or ClusCylPhiZ.
     *
     * @param   clus The Cluster to add.
     * @return  0 if successful.
     */
    public int addCluster( Cluster clus)
    {
        // Extract the cluster position depending on type
        if ( clus.type().equals(ClusZPlane2.staticType()) )
        {
            ClusZPlane2 clu = ( ClusZPlane2) clus;
            Assert.assertTrue( clu.surface().equals(_szp) );
        }  else
        {
            Assert.assertTrue(false);
            return 1;
        }
        
        // Check there is no other cluster at this position.
        if ( _clusters.contains(clus))
        {
            Assert.assertTrue(false);
            System.exit(2);
        }
        // Store the cluster.
        _clusters.add(clus);
        
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
    public  Surface surface()
    { return _szp; }
    
    //
    
     /**
     *Return all the clusters.
     *
     * @return The list of clusters managed by this manager.
     */
    public List clusters()
    {
        // Create the list of clusters.
        List clusters = new ArrayList();
        
        // Loop over clusters and add to output list.
        
        for ( Iterator iclu=_clusters.iterator();  iclu.hasNext(); )
            clusters.add( iclu.next() );
        
        return clusters;
        
    }
    
    //
    
    /**
     *Return all the clusters near a track at the surface. Nearness is delimited by the
     *maximum chi-squared between the track prediction and the cluster measurement.
     *
     * Clusters have been ordered by axy.
     * The nearby subset is returned in the same order.
     *
     * @param   tre The ETrack for which to return clusters.
     * @return  A list of clusters near the track tre.
     */
    public List clusters( ETrack tre)
    {
        // Create the list of clusters.
        List clusters = new ArrayList();
        
        // Check the track surface.
        if ( !tre.surface().equals(_szp) )
        {
            Assert.assertTrue(false);
            return clusters;
        }
        
        // If there are no clusters, return the empty list.
        if ( _clusters.size()==0 ) return clusters;
        
        //for time being loop over all the clusters.
        // need to be smarter later
        
        for(Iterator it = _clusters.iterator(); it.hasNext(); )
        {
            //get the cluster
            Cluster clu = (Cluster) it.next();
            // is cluster within range?
            if (chsq_diffzp2(clu,tre) < _max_chsq_diff )
            {
                // if so add it to the list
                clusters.add( clu );
            }
        }
        
        // Return the nearby clusters.
        return clusters;
    }
    
    
     /**
     * Return the maximum chi-square difference which defines a hit to be near a track.
     *
     * @return  The maximum chi-square for a track-hit association.
     */
    public double maxChsqDiff()
    { return _max_chsq_diff ;}
    
    //
    
    /**
         *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType(); }
    
    //
    
    /**
     *output stream
     *
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("2D Cluster finder for \n" + _szp +"\n");
        sb.append("Maximum chi-square difference is "
        + _max_chsq_diff + ".\n");
        return sb.toString();
    }
    
    
    //**********************************************************************
    // Helper functions.
    //**********************************************************************
    
    // Calculate the chi-square difference between and cluster and a track.
    // Only x components are taken into account for calculated returned chisq_diff
    // Real chisquare are returned in double *chisq variable
    // It is assumed the cluster generates exactly one prediction and that
    // this prediction has two dimension.
    
    private static double chsq_diffzp2( Cluster clus,  ETrack tre)
    {
        
        ClusZPlane2 clu = ( ClusZPlane2)(clus);
        TrackVector vec = tre.vector();
        TrackError err = tre.error();
        
        double diff0 = clu.x() - vec.get(SurfZPlane.IX );
        double diff1 = clu.y() - vec.get(SurfZPlane.IY );
        
        if ( diff0 == 0. && diff1 == 0. ) return 0.;
        
        double e11 = err.get(SurfZPlane.IX,SurfZPlane.IX);
        double e22 = err.get(SurfZPlane.IY,SurfZPlane.IY);
        double e12 = err.get(SurfZPlane.IY,SurfZPlane.IX);
        double a = e11 + clu.dX2();
        double b = e12 + clu.dXdY();
        double c = e22 + clu.dY2();
        double det = a*c-b*b;
        
        if ( det  == 0. ) return 1.e15;
        
        double chisq = (c*diff0*diff0 + a*diff1*diff1 - 2*b*diff0*diff1)/det;
        
        if ( chisq < 0 ) return 1.e15;
        
        return chisq;
        
    }
    
    
}
