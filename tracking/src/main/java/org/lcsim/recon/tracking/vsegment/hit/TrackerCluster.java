package org.lcsim.recon.tracking.vsegment.hit;

import java.util.List;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;

/**
 * Collection of <tt>DigiTrackerHits</tt> that cannot be unambigously separated.
 * Clusters are independent in a sense that a track crossing the sensor only
 * contributes to one cluster. But it is possible for several tracks to 
 * contribute to one cluster.
 * 
 * 
 * @author D.Onoprienko
 * @version $Id: TrackerCluster.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public interface TrackerCluster {
  
// -- Access to DigiTrackerHits constituting the cluster :  --------------------

  /** Get list of <tt>DigiTrackerHits</tt> that compose the cluster, sorted by channel ID. */  
  public List<DigiTrackerHit> getDigiHits();

// -- Access to TrackerHits that have been produced from this cluster :  -------
  
  /** 
   * Returns a list of TrackerHits produced from this cluster. 
   * No new hits are created, and no existing hits are modified by a call to this method,
   * it simply returns the list of hits that have been associated with this cluster through
   * prior calls to {@link #addTrackerHit}.
   */
  public List<TrackerHit> getTrackerHits();
  
  /** Associate <tt>TrackerHit</tt> object with this cluster. */
  public void addTrackerHit(TrackerHit hit);
  
  /** Remove <tt>TrackerHit</tt> object from the list of hits associated with this cluster. */
  public void removeTrackerHit(TrackerHit hit);
  
// -- Access to Sensor :  ------------------------------------------------------
  
  /** Returns the <tt>Sensor</tt> object associated with this cluster. */
  public Sensor getSensor();
  
// -- Convenience methods :  ---------------------------------------------------
  
  /** Returns combined signal of all <tt>DigiTrackerHits</tt> in the cluster. */
  public double getSignal();
  
  /** Returns time associated with the cluster. */
  public double getTime();

}
