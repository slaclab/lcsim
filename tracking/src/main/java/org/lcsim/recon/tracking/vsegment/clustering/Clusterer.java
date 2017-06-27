package org.lcsim.recon.tracking.vsegment.clustering;

import java.util.*;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;

/**
 * Interface to be implemented by classes providing algorithms for clustering
 * digitized tracker hits.
 *
 * @author D. Onoprienko
 * @version $Id: Clusterer.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public interface Clusterer {

  /**
   * Returns a list of found clusters, given a list of digitized hits that belong
   * to the same <tt>Sensor</tt> object.
   */
  public ArrayList<TrackerCluster> findClusters(Sensor sensor, List<DigiTrackerHit> hits);

}
