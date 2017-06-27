package org.lcsim.recon.tracking.trflayer;
import org.lcsim.recon.tracking.trfbase.Cluster;
/**
 * This abstract class inherits from ClusterFinder and extends that
 * interface to include methods for adding and dropping clusters
 * in an internally managed container.  The type of container is
 * not specified here.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/
public abstract class ClusterFindManager extends ClusterFinder
{
    
    // methods
    
    //
    
    /**
     *constructor
     *
     */
    public ClusterFindManager()
    {
    }
    
    //
    /**
     *Add a cluster.
     * Return nonzero for error.
     *
     * @param   pclu Cluster to add
     * @return 0 if success
     */
    public  abstract int addCluster( Cluster pclu);
    
    //
    
    /**
     *Drop all clusters.
     *
     */
    public abstract void dropClusters();
    
}