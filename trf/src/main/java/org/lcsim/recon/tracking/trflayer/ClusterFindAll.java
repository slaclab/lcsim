package org.lcsim.recon.tracking.trflayer;
import java.util.*;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Cluster;
/**
 * This a cluster finder which simply returns all the clusters
 * for either request.  The clusters are stored in the same
 * format in which they are returned.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/

public class ClusterFindAll extends ClusterFindManager
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
    { return "ClusterFindAll"; }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    // surface
    private Surface _srf;
    
    // list of clusters
    private  List _clusters;
    
    
    // methods
    
    //
    
    /**
     *constructor from a surface
     *
     * @param   srf Surface for this finder
     */
    public ClusterFindAll( Surface srf)
    {
        _srf = srf;
        _clusters = new ArrayList();
    }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of class type
     *Included for completeness with C++ version
     */
    public String type()
    { return staticType(); }
    
    
    //
    
    /**
     *add a cluster
     *
     * @param   clu Cluster to add to this finder
     * @return 0 if successful
     */
    public int addCluster( Cluster clu)
    {
        _clusters.add(clu);
        return 0;
    }
    
    //
    
    /**
     *drop the clusters
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
     * @return Surface for this Finder
     */
    public  Surface surface()
    { return _srf; }
    
    //
    
    /**
     *Return all the clusters associated with this surface.
     *
     * @return a List of all Clusters
     */
    public List clusters()
    {
        return _clusters;
    }
    
    //
    
    /**
     *Return all the clusters near the specified track at the surface.
     * Here we return all.
     *
     * @param   tre Etrack
     * @return List of all Clusters close to this track
     */
    public List clusters(  ETrack  tre)
    {
        return _clusters;
    }
    
    
    /**
     * output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(getClass().getName()+" Finder for all clusters. \n Finder surface: " + _srf
                +"\n");
        int size = _clusters.size();
        if ( size == 1 ) sb.append("There is " + size + " cluster");
        else             sb.append("Finder has " + size + " clusters");
        if ( size == 0 ) sb.append(".");
        else
        {
            sb.append(":");
            for ( Iterator iclu=_clusters.iterator(); iclu.hasNext(); )
                sb.append("\n "+iclu.next());
        }
        return sb.toString();
    }
    
}