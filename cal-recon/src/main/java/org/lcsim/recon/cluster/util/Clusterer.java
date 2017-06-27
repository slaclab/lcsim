/*
 * Clusterer.java
 *
 * Created on April 5, 2006, 9:32 AM
 *
 * Interface for creating cluster lists from CalorimeterHit lists
 *
 * Ron Cassell
 */

package org.lcsim.recon.cluster.util;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import java.util.*; 

/**
 *
 * @author cassell
 */
public interface Clusterer
{
   public List<Cluster> createClusters(List<CalorimeterHit> hits);
   public List<Cluster> createClusters(Map<Long,CalorimeterHit> map);
}
