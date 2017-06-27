package org.lcsim.recon.tracking.vsegment.hit.base;

import java.util.List;
import java.util.ArrayList;

import org.lcsim.event.MCParticle;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;

/**
 * Composite DigiTrackerHit.
 * Represents a sum of several digitized hits in a single channel. Typically used
 * for combining simulated events, or overlaying events and electronic noise.
 *
 * @author D.Onoprienko
 * @version $Id: DigiTrackerHitComposite.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class DigiTrackerHitComposite extends DigiTrackerHitAdapter {

// -- Constructors :  ----------------------------------------------------------
  
  /** 
   * Default constructor. 
   * Signal, time, and cell ID will be set to zero.
   */
  public DigiTrackerHitComposite() {
    _signal = 0.;
    _time = 0.;
  }
  
  /** 
   * Fast constructor from data. 
   * No check is performed to see if all hits in the list have the same cell ID.
   * The list supplied as an argument will be owned by this composite hit.
   */
  public DigiTrackerHitComposite(double signal, double time, Sensor sensor, int channel, ArrayList<DigiTrackerHit> hitList) {
    _signal = signal;
    _time = time;
    _sensor = sensor;
    _channel = channel;
    _hitList = hitList;
  }

  /** 
   * Creates a composite hit from a list of hits. 
   * Signal is the sum of signals of constituent hits, time is signal-weighted average.
   * Throws <tt>IllegalArgumentException</tt> if not all of the hits in the list 
   * have the same channel ID. The list supplied as an argument will be copied.
   */
  public DigiTrackerHitComposite(List<DigiTrackerHit> hitList) {
    if (hitList.isEmpty()) throw new IllegalArgumentException("Empty list");
    _signal = 0.;
    _time = 0.;
    _channel = 0;
    _hitList = new ArrayList<DigiTrackerHit>(hitList.size());
    _hitList.addAll(hitList);
    for (DigiTrackerHit hit : hitList) {
      if (_sensor == null) {
        _sensor = hit.getSensor();
        _channel = hit.getChannel();
      } else {
        if ( ! ( _sensor == hit.getSensor() && _channel == hit.getChannel() ) ) {
          throw new IllegalArgumentException("Mismatched sensor or channel ID");
        }
      }
      _time += hit.getTime() * hit.getSignal();
      _signal += hit.getSignal();
    }
    _time /= _signal;
  }

  /** Copy constructor. */
  public DigiTrackerHitComposite(DigiTrackerHitComposite digiHit) {
    _signal = digiHit._signal;
    _time = digiHit._time;
    _sensor = digiHit._sensor;
    _channel = digiHit._channel;
    _hitList = new ArrayList<DigiTrackerHit>(digiHit._hitList);
    _hitList.trimToSize();
  }
  
// -- Getters :  ---------------------------------------------------------------

  /**  
   * Returns <tt>true</tt> if the hit is a superposition of more than one elemental hit. 
   * Objects of this class always return <tt>true</tt>.
   */
  public boolean isComposite() {return true;}

  /** Returns <tt>null</tt> since the hit is composite. */
  public MCParticle getMCParticle() {return null;}

  /** Returns a list of underlying elemental hits. */
  public List<DigiTrackerHit> getElementalHits() {
    ArrayList<DigiTrackerHit> hList = new ArrayList<DigiTrackerHit>(_hitList.size());
    for (DigiTrackerHit hit : _hitList) {
      if (hit.isComposite()) {
        hList.addAll(hit.getElementalHits());
      } else {
        hList.add(hit);
      }
    }
    return hList;
  }
  
// -- Modifiers :  -------------------------------------------------------------
  
  /** Add a hit to this composite hit. */
  public void addHit(DigiTrackerHit hit) {
    if (_hitList == null) {
      _signal = hit.getSignal();
      _time = hit.getTime();
      _sensor = hit.getSensor();
      _channel = hit.getChannel();
      _hitList = new ArrayList<DigiTrackerHit>(2);
    } else {
      if ( ! ( _sensor == hit.getSensor() && _channel == hit.getChannel() ) ) {
        System.out.println("throwing");
        throw new IllegalArgumentException("Mismatched cell ID");
      }
      double signal = hit.getSignal();
      _time = (_time * _signal + hit.getTime() * signal) / (_signal + signal);
      _signal += signal;
    }
    _hitList.add(hit);
  }
  
  /** Compact storage. */
  public void trimToSize() {
    _hitList.trimToSize();
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  protected ArrayList<DigiTrackerHit> _hitList;

}
