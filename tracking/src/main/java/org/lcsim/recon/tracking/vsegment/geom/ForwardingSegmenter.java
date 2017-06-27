package org.lcsim.recon.tracking.vsegment.geom;
import org.lcsim.conditions.ConditionsManager;

import java.util.*;

import org.lcsim.event.SimTrackerHit;
import org.lcsim.geometry.Detector;

/**
 * Base class for implementing {@link Segmenter}s that forward ID or {@link Sensor}
 * creation calls to their daughter segmenters. See {@link AbstractSegmenter} for
 * details on how to chain segmenters.
 * <p>
 * Subclasses should implement {@link #chooseSegmenter(SimTrackerHit)} method to select
 * a daughter segmenter that will handle a particular simulated hit. Daughter segmenters
 * should be added to the parent segmenter through calls to 
 * {@link #addDaughterSegmenter(AbstractSegmenter)}.
 *
 * @author D. Onoprienko
 * @version $Id: ForwardingSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
abstract public class ForwardingSegmenter extends AbstractSegmenter {
  
// -- Constructors :  ----------------------------------------------------------
  
  /** Default constructor. */
  protected ForwardingSegmenter() {
    _daughters = new ArrayList<AbstractSegmenter>();
  }
  
// -- Choosing daughter segmenter :  -------------------------------------------
  
  /**
   * Subclasses should implement this method to choose daughter <tt>Segmenter</tt>
   * that can handle the given hit.
   */
  abstract public AbstractSegmenter chooseSegmenter(SimTrackerHit hit);
  
// -- Implementing Segmenter :  ------------------------------------------------
  
  /**
   * Returns a list of <tt>SensorsID</tt> corresponding to all virtual segments
   * in the part of the detector handled by this <tt>Segmenter</tt>.
   */
  public List<Integer> getSensorIDs() {
    List<Integer> sensorIDs = null;
    for (AbstractSegmenter daughter : _daughters) {
      if (sensorIDs == null) {
        sensorIDs = daughter.getSensorIDs();
      } else {
        sensorIDs.addAll(daughter.getSensorIDs());
      }
    }
    //System.out.println("Segmenter "+this+" returning "+sensorIDs.size()+" IDs");
    return sensorIDs;
  }

  /**
   * Returns integer <tt>SensorID</tt> uniquely identifying a {@link Sensor} object
   * within the whole detector, given the simulated hit.
   */
  public int getSensorID(SimTrackerHit hit) {
    AbstractSegmenter daughter = chooseSegmenter(hit);
    return (daughter == null) ? null : daughter.getSensorID(hit);
  }
  
  /**
   * Creates a new {@link Sensor} object given full <tt>SensorID</tt>.
   * Caution: for the sake of speed, no checking is done to veryfy that the 
   * supplied <tt>SensorID</tt> is valid and should be handled by this <tt>Segmenter</tt>.
   * Giving this method an invalid <tt>SensorID</tt> may produce unpredictable results.
   */
  public Sensor getSensor(int sensorID) {
    return _daughters.get(idToDaughterIndex(sensorID)).getSensor(sensorID);
  }
  
  /**
   * Returns a list of <tt>Sensors</tt> that might contain hits that should be combined
   * with hits in the <tt>Sensor</tt> whose <tt>sensorID</tt> is supplied as an argument
   * to form stereo pairs. 
   * Default implementation forwards the call to the appropriate daughter segmenter. 
   * Subclasses may override.
   */
  public List<Integer> getStereoPartners(int sensorID) {
    return _daughters.get(idToDaughterIndex(sensorID)).getStereoPartners(sensorID);
  }
  
// -- Initialization :  --------------------------------------------------------
  
  /**
   * Detector dependent initialization.
   * Subclasses should override this method if they need to perform any detector
   * dependent initialization, but they should call {@link #updateDaughterSegmenters(Detector)}
   * from this method to have their daughter <tt>Segmenter</tt>s initialized as well.
   */
  public void detectorChanged(Detector detector) {
//    System.out.println(" ");
//    System.out.println("Updating " + this + " with " + detector.getName());
    updateDaughterSegmenters(detector);
  }
  
  /** 
   * Calls {@link #detectorChanged(Detector)} methods of daughter <tt>Segmenter</tt>s.
   * If subclasses override {@link #detectorChanged(Detector)} method, they should
   * call this method to have daughter <tt>Segmenter</tt>s initialized.
   */
  protected void updateDaughterSegmenters(Detector detector) {
    for (AbstractSegmenter daughter : _daughters) daughter.detectorChanged(detector);
  }
  
// -- Handling of prefixes and postfixes :  ------------------------------------
  
  /**
   * Set <tt>pretfix</tt> value and <tt>postfix</tt> length for this <tt>Segmenter</tt>.
   */
  public void setPrefix(int prefix, int postfixLength) {
    //System.out.println("Setting prefix for "+this+" prefix "+prefix+" length "+postfixLength);
    super.setPrefix(prefix, postfixLength);
    _daughters.trimToSize();
    int daughterIdLdength = getIdSize(_daughters.size()-1);
    _daughterPostfixLength = postfixLength - daughterIdLdength;
    _daughterIdMask = 0;
    for (int i=0; i<daughterIdLdength; i++) _daughterIdMask = (_daughterIdMask << 1) | 1;
    _daughterIdMask = _daughterIdMask << _daughterPostfixLength;
    for (int daughterIndex=0; daughterIndex < _daughters.size(); daughterIndex++) {
      _daughters.get(daughterIndex).setPrefix((prefix << daughterIdLdength) | daughterIndex , _daughterPostfixLength);
    }
  }
  
  /**
   * Extract daughter <tt>Segmenter</tt> index from full <tt>SensorID</tt>.
   */
  protected int idToDaughterIndex(int sensorID) {
    return (_daughterIdMask & sensorID) >> _daughterPostfixLength;
  }
  
  /**
   * Returns minimum <tt>postfix</tt> length required by this <tt>Segmenter</tt>
   * to accomodate all its daughters and their <tt>postfix</tt>es.
   */
  protected int getNativePostfixLength() {
    int daughterIdLdength = getIdSize(_daughters.size()-1);
    int maxDaughterPostfixLength = 0;
    for (AbstractSegmenter daughter : _daughters) {
      maxDaughterPostfixLength = Math.max(daughter.getNativePostfixLength(), maxDaughterPostfixLength);
    }
    return daughterIdLdength + maxDaughterPostfixLength;
  }
  
// -- Adding / Removing daughters :  -------------------------------------------

  /** Add daughter <tt>Segmenter</tt>. */
  public void addDaughterSegmenter(AbstractSegmenter daughter) {
    _daughters.add(daughter);
  }
  
  /** Remove daughter <tt>Segmenter</tt>. */
  public void removeDaughterSegmenter(AbstractSegmenter daughter) {
    _daughters.remove(daughter);
  }
  
  /** Remove all daughter <tt>Segmenter</tt>s. */
  public void removeAllDaughterSegmenters() {
    _daughters.clear();
  }

// -- Private parts :  ---------------------------------------------------------

  private ArrayList<AbstractSegmenter> _daughters;
  
  protected int _daughterPostfixLength;
  protected int _daughterIdMask;

}
