package org.lcsim.recon.tracking.vsegment.hit.base;

import java.util.List;
import java.util.ArrayList;

import org.lcsim.event.MCParticle;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;

/**
 * Adapter class that provides partial implementation of {@link DigiTrackerHit} interface. 
 *
 * @author D.Onoprienko
 * @version $Id: DigiTrackerHitAdapter.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
abstract public class DigiTrackerHitAdapter implements DigiTrackerHit {
  
// -- Getters :  ---------------------------------------------------------------

  /** 
   * Returns signal in the channel.
   * The signal value is the digitization algorithm output if running on Monte Carlo,
   * calibrated and corrected signal if runnung on data.
   */
  public double getSignal() {return _signal;}

  /** Returns time associated with the hit. */
  public double getTime() {return _time;}

  /** Returns {@link Sensor} object this hit belongs to. */
  public Sensor getSensor() {return _sensor;}
  
  /** Returns channel ID on the sensor. */
  public int getChannel() {return _channel;}
  
  /**
   * Defines natural ordering of hits based on their sensor and channel ID.
   * If sensor IDs are equal, the ordering is based on channel ID. If channel IDs
   * are equal, too, then the ordering is based on hash codes to make it stable 
   * and consistent with equals.
   */
  public int compareTo(DigiTrackerHit hit) {
    if (getSensor() == hit.getSensor()) {
      if (getChannel() == hit.getChannel()) {
        return hashCode() - hit.hashCode();
      } else {
        return getChannel() - hit.getChannel();
      }
    } else {
      return getSensor().getID() - hit.getSensor().getID();
    }
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  protected double _signal;
  protected double _time;
  protected Sensor _sensor;
  protected int _channel;

}
