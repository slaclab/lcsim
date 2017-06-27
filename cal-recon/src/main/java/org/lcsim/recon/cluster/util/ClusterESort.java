package org.lcsim.recon.cluster.util;

import java.util.Comparator;
import org.lcsim.event.Cluster;
/** A Comparator for sorting Clusters
 * @author Norman A. Graf
 * @version 1.0
 */
public class ClusterESort implements Comparator
{
    
    /** The compare function used for sorting.
     * Comparison is done on cluster energy.
     * @param   obj1 Cluster1
     * @param   obj2 Cluster2
     * @return
     * <ol>
     * <li> -1 if Cluster1 > Cluster2
     * <li>  0 if Cluster1 = Cluster2
     * <li>  1 if Cluster1 < Cluster2
     * </ol>
     */
    public int compare(Object obj1, Object obj2)
    {
        if(obj1==obj2) return 0;
        Cluster v1 = (Cluster) obj1;
        Cluster v2 = (Cluster) obj2;
        if(v1.getEnergy()-v2.getEnergy()>0.) return -1;
        return 1;
    }
}
