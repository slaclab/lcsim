package org.lcsim.recon.tracking.vsegment.clustering.clusterers;

import java.util.*;

import org.lcsim.recon.tracking.vsegment.clustering.Clusterer;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.base.TrackerClusterBasic;

/**
 * Simple digitized tracker hit clusterer using nearest neighbor algorithm.
 *
 * @author D. Onoprienko
 * @version $Id: NearestNeighborClusterer.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class NearestNeighborClusterer implements Clusterer {
  
// -- Constructors :  ----------------------------------------------------------
  
  public NearestNeighborClusterer() {
  }
  
// -- Implementing Clusterer :  ------------------------------------------------

  /**
   * Returns a list of found clusters, given a list of digitized hits that belong
   * to the same <tt>Sensor</tt> object.
   */
  public ArrayList<TrackerCluster> findClusters(Sensor sensor, List<DigiTrackerHit> hits) {
    SensorType senType = sensor.getType();
    LinkedList<DigiTrackerHit> hitList = (hits instanceof LinkedList) ? 
           (LinkedList<DigiTrackerHit>) hits : new LinkedList<DigiTrackerHit>(hits);
    ArrayList<TrackerCluster> clusterList = new ArrayList<TrackerCluster>();
    while (! hitList.isEmpty()) {
      DigiTrackerHit seed = hitList.poll();
      ArrayList<DigiTrackerHit> hitsInCluster = new ArrayList<DigiTrackerHit>();
      hitsInCluster.add(seed);
      List<Integer> newChannels = senType.getNeighbors(seed.getChannel());
      while (!newChannels.isEmpty()) {
        List<Integer> oldChannels = newChannels;
        newChannels = new ArrayList<Integer>(30);
        ListIterator<DigiTrackerHit> it = hitList.listIterator();
        while (it.hasNext()) {
          DigiTrackerHit hit = it.next();
          int channel = hit.getChannel();
          if ( oldChannels.contains(channel) ) {
            hitsInCluster.add(hit);
            newChannels.addAll(senType.getNeighbors(channel));
            it.remove();
          }
        }
      }
      hitsInCluster.trimToSize();
      clusterList.add(new TrackerClusterBasic(hitsInCluster,sensor));
    }
    clusterList.trimToSize();
    return clusterList;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
}
