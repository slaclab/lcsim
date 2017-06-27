package org.lcsim.recon.tracking.vsegment.hit.base;

import java.util.List;
import java.util.ArrayList;

import org.lcsim.event.MCParticle;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;

/**
 * Elemental DigiTrackerHit. 
 * In simulation, this is a digitized signal from a single channel produced by a 
 * single <tt>MCParticle</tt>. In data, this is a single channel signal after calibration.
 *
 * @author D.Onoprienko
 * @version $Id: DigiTrackerHitElemental.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class DigiTrackerHitElemental extends DigiTrackerHitAdapter {
  
// -- Constructors :  ----------------------------------------------------------
  
  /** Constract from parameters. */
  public DigiTrackerHitElemental(double signal, double time, Sensor sensor, int channel, MCParticle mcParticle) {
    _signal = signal;
    _time = time;
    _sensor = sensor;
    _channel = channel;
    _mcParticle = mcParticle;
  }
  
  /** Constract from parameters with no associated <tt>MCParticle</tt>. */
  public DigiTrackerHitElemental(double signal, double time, Sensor sensor, int channel) {
    this(signal, time, sensor, channel, null);
  }

  /** Copy constructor. */
  public DigiTrackerHitElemental(DigiTrackerHitElemental digiHit) {
    _signal = digiHit._signal;
    _time = digiHit._time;
    _sensor = digiHit._sensor;
    _channel = digiHit._channel;
    _mcParticle = digiHit._mcParticle;    
  }
  
// -- Getters :  ---------------------------------------------------------------

  /**  
   * Returns <tt>true</tt> if the hit is a superposition of more than one elemental hit. 
   * Objects of this class always return <tt>false</tt>.
   */
  public boolean isComposite() {return false;}

  /**
   * Returns <tt>MCParticle</tt> that produced the hit.
   * If the hit is composite, or does not have <tt>MCParticle</tt> associated with
   * it (noise, beam test data, etc.), returns <tt>null</tt>.
   */
  public MCParticle getMCParticle() {return _mcParticle;}

  /**
   * Returns a list of underlying elemental hits.
   * Since the hit is not composite, returns a list with a single element - this hit.
   */
  public List<DigiTrackerHit> getElementalHits() {
    List<DigiTrackerHit> hitList = new ArrayList<DigiTrackerHit>(1);
    hitList.add(this);
    return hitList;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  protected MCParticle _mcParticle;

}
