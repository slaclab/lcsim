package org.lcsim.recon.tracking.trfbase;
import java.util.*;
import org.lcsim.recon.tracking.trfutil.Assert;
/** Abstract interface for clusters.
 *
 * The abstract base class Cluster represents a detector observation
 * that is independent of other such observations.  I.e., the track
 * crossing contributing to a particular cluster does not contribute
 * to any others.  Due to finite detector resolution, more than one track
 * crossing may contribute to a single cluster.
 *<p>
 * The abstract base class Hit represents a measurement derived from
 * a cluster which is assigned to a single track.  If the hit only makes
 * use of a portion of the cluster, then it is assumed that the remainder
 * of the cluster is independent of the track crossing which produced the
 * portion of interest.
 *<p>
 * The only requirement on a cluster is that it is able to generate a list
 * hits from a track. The track must first be propagated to the cluster
 * surface.  The returned list contains reference-counting pointers so
 * that each hit is deleted when its list element
 * is deleted.
 *<p>
 *@author Norman A. Graf
 *@version 1.0
 */
public abstract class Cluster
{
    
    /**
     *Return the type name.
     *
     * @return  String name of this class.
     * Included for consistency with C++ version
     */
    public static String typeName()
    { return "Cluster"; }
    //
    
    /**
     *Return the type.
     *
     * @return  String name of this class
     * Included for consistency with C++ versio
     */
    public static String staticType()
    { return typeName(); }
    
    
    //
    
    /**
     *equality
     * Return true if and only if the two clusters are identical.
     *
     * @param   clus Cluster to compare with
     * @return  true if Clusters are equal
     */
    public abstract boolean equal(  Cluster clus )  ;
    
    //
    
    /**
     *Generate and return the predictions for a track.
     *
     * @param   tre ETrack generating the prediction
     * @return  Cluster for this ETrack
     */
    public abstract List predict( ETrack tre)  ;
    
    //
    
    /**
     *constructor
     *
     */
    public Cluster()
    {
    }
    
    //
    
    /**
     *copy constructor
     *
     * @param   clus Cluster to replicate
     */
    public Cluster( Cluster clus)
    {
    }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of generic type
     * Included for consistency with C++ versio
     */
    public String genericType()
    { return staticType(); }
    
    /**
     *Return the type.
     *
     * @return String representation of the type
     * Included for consistency with C++ version
     */
    public String type()
    { return staticType(); }
    
    //
    
    /**
     *Return the cluster surface.
     *
     * @return The Surface at which this Cluster is defined
     */
    public abstract  Surface surface()  ;
    
    //
    
    
    /**
     *Return the ID's of MC tracks contributing to this cluster.
     * The default implementation returns an empty list
     *
     * @return a list of Monte Carlo ID's  contributing to this Cluster
     */
    public List mcIds()
    {
        return new ArrayList();
    }
    
    
    /**
     *Return the ID's of MC tracks contributing to this cluster.
     * The default implementation returns an empty list
     *
     * @return n array of Monte Carlo ID's  contributing to this Cluster
     */
    public int[] mcIdArray()
    {
        return new int[0];
        
    }
    
    //
    
    /**
     *Generate and return the predictions for a track.
     * This is a list of Hits.
     * The specified parent is assigned to each prediction.
     *
     * @param   tre The ETrack for which to generate a prediction
     * @param   clus The predicted Cluster
     * @return  a list of Hits for this prediction
     */
    public List predict( ETrack tre,  Cluster clus)
    {
        // check clus points to this cluster
        Assert.assertTrue( this == clus );
        // use virtual method to fetch list
        List hits = predict(tre);
        // Iterate through the list and
        // assign the parent cluster for each prediction
        for (Iterator it = hits.iterator(); it.hasNext(); )
        {
            ((Hit)it.next()).setParentPointer(clus);
        }
        return hits;
    }
    
    //
    
    /**
     *equality
     * false if the cluster predictions are of diffent type
     * otherwise compare with public abstract function equal
     *
     * @param   clus Cluster to compare with
     * @return true if Clusters are equal
     */
    public boolean equals(  Cluster clus )
    {
        if( !type().equals(clus.type()) ) return false;
        return equal(clus);
    }
    
    //
    
    /** inequality
     *
     * @param   clus Cluster to compare with
     * @return true if Clusters are not equal
     */
    public boolean notEquals(  Cluster clus )
    {
        return ! equals(clus);
    }
    
}
