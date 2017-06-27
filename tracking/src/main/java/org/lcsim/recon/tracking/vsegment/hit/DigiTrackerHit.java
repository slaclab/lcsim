package org.lcsim.recon.tracking.vsegment.hit;

import java.util.List;

import org.lcsim.event.MCParticle;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;

/**
 * Representation of signal from a single tracker channel (pixel or strip).
 *
 * @author D.Onoprienko
 * @version $Id: DigiTrackerHit.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public interface DigiTrackerHit extends Comparable<DigiTrackerHit> {
  
  /** 
   * Returns signal in the channel.
   * The signal value is the digitization algorithm output if running on Monte Carlo,
   * calibrated and corrected signal if runnung on data.
   */
  public double getSignal();
  
  /** Returns time associated with the hit. */
  public double getTime();
  
  /** Returns {@link Sensor} object this hit belongs to. */
  public Sensor getSensor();
  
  /** Returns channel ID on the sensor. */
  public int getChannel();
  
  /**  Returns <tt>true</tt> if the hit is a superposition of more than one elemental hit. */
  public boolean isComposite();
  
  /**
   * Returns a list of underlying elemental hits.
   * If the hit is not composite, returns a list with a single element - this hit.
   */
  public List<DigiTrackerHit> getElementalHits();
  
  /**
   * Returns <tt>MCParticle</tt> that produced the hit.
   * If the hit is composite, or does not have <tt>MCParticle</tt> associated with
   * it (noise, beam test data, etc.), returns <tt>null</tt>.
   */
  public MCParticle getMCParticle();
  
}
