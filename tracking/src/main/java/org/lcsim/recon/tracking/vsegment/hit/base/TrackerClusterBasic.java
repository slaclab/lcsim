package org.lcsim.recon.tracking.vsegment.hit.base;

import java.util.*;

import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;

/**
 * Basic implementation of {@link TrackerCluster} interface.
 *
 * @author D.Onoprienko
 * @version $Id: TrackerClusterBasic.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class TrackerClusterBasic implements TrackerCluster {
  
// -- Constructors :  ----------------------------------------------------------
  
  /** Default constructor */
  public TrackerClusterBasic() {
    _digiList = new ArrayList<DigiTrackerHit>();
    _hitList = new ArrayList<TrackerHit>(1);
  }
  
  /** 
   * Fast constructor, no consistency checks, no trimming to size, no sorting. 
   * The list supplied as an argument will be owned by the created <tt>TrackerClusterDataBasic</tt> object.
   */
  public TrackerClusterBasic(ArrayList<DigiTrackerHit> digiList, Sensor sensor) {
    _digiList = digiList;
    _hitList = new ArrayList<TrackerHit>(1);
    _sensor = sensor;
  }
  
  /**
   * Construct from a list of {@link DigiTrackerHit} objects.
   * Checks that all hits in the list belong to the same sensor, and makes a copy
   * of the list.
   */
  public TrackerClusterBasic(List<DigiTrackerHit> digiList) {
    _digiList = new ArrayList<DigiTrackerHit>(digiList.size());
    for (DigiTrackerHit hit : digiList) {
      addDigiHit(hit);
    }
    Collections.sort(_digiList);
    _hitList = new ArrayList<TrackerHit>(1);
  }

// -- Access to TrackerHits that have been produced from this cluster :  -------
  
  /** 
   * Returns a list of TrackerHits produced from this cluster. 
   * No new hits are created, and no existing hits are modified.
   * The list returned by this method belongs to the <tt>TrackerCluster</tt> object
   * for which it was called.
   */
  public List<TrackerHit> getTrackerHits() {
    return _hitList;
  }
  
  /** Add a hit to the list of <tt>TrackerHit</tt>s produced from this cluster. */
  public void addTrackerHit(TrackerHit hit) {
    _hitList.add(hit);
  }
  
  /** Remove a hit from the list of <tt>TrackerHit</tt>s produced from this cluster. */
  public void removeTrackerHit(TrackerHit hit) {
    _hitList.remove(hit);
  }

// -- Getters :  ---------------------------------------------------------------

  /** Get list of <tt>DigiTrackerHits</tt> that compose the cluster. */
  public List<DigiTrackerHit> getDigiHits() {
    return _digiList;
  }

  /** Returns signal-weighted average time for all <tt>DigiTrackerHits</tt> in the cluster. */
  public double getTime() {
    double time = 0.;
    double signal = 0.;
    for (DigiTrackerHit hit : _digiList) {
      time += hit.getTime() * hit.getSignal();
      signal += hit.getSignal();
    }
    return time/signal;
  }

  /** Returns combined signal of all <tt>DigiTrackerHits</tt> in the cluster. */
  public double getSignal() {
    double signal = 0.;
    for (DigiTrackerHit hit : _digiList) {
      signal += hit.getSignal();
    }
    return signal;
  }

  /** Returns the {@link Sensor} object associated with this cluster. */
  public Sensor getSensor() {
    return _sensor;
  }
  
// -- Modifiers :  -------------------------------------------------------------
  
  /** 
   * Add {@link DigiTrackerHit} to the cluster.
   * Throws <tt>IllegalArgumentException</tt> if the hit does not belong to the 
   * same {@link Sensor} object as the hits already in the cluster.
   */
  public void addDigiHit(DigiTrackerHit digiHit) {
    if (_sensor == null) {
      _sensor = digiHit.getSensor();
    } else {
      if (_sensor != digiHit.getSensor()) {
        throw new IllegalArgumentException("Sensor mismatch");
      }
    }
    _digiList.add(digiHit);
  }
  
  /** Trims the underlying <tt>DigiTrackerHit</tt> list of hits to size. */
  public void trimToSize() {
    _digiList.trimToSize();
  }

// -- Private parts :  ---------------------------------------------------------
  
  private ArrayList<DigiTrackerHit> _digiList;
  private ArrayList<TrackerHit> _hitList;
  private Sensor _sensor;
}
