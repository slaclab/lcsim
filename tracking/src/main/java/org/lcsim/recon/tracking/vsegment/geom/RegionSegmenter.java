package org.lcsim.recon.tracking.vsegment.geom;

import java.util.*;

import org.lcsim.event.SimTrackerHit;
import org.lcsim.geometry.Detector;

/**
 * Base class for implementing {@link Segmenter}s that describe virtual segmentation 
 * of a certain part of the detector.
 * <p>
 * Within that part, each <tt>Sensor</tt> is identified by a unique integer <tt>postfix</tt>.
 * Each <tt>RegionSegmenter</tt> can be assigned an integer <tt>prefix</tt>. After that,
 * <tt>SensorID</tt> values returned by <tt>getSensorID(SimTrackerHit)</tt> method and assigned to
 * <tt>Sensor</tt> objects created by this segmenter will be composed of bits
 * containing the <tt>prefix</tt> and bits containing the <tt>postfix</tt>.
 * <p>
 * Objects of this class are intended to be used either as top level segmenters
 * describing segmentation of the whole detector and provided directly to 
 * {@link SegmentationManager} in its constructor, or as bottom level segmenters
 * in a tree of <tt>AbstractSegmenters</tt>. In the latter case, they should be added as 
 * daughters to <tt>ForwardingSegmenters</tt>, and their prefixes will be set automatically.
 * See {@link AbstractSegmenter} for details on how to chain <tt>Segmenters</tt>.
 * <p>
 * Subclasses should implement {@link #makePostfix(SimTrackerHit)}, {@link #makeSensor(int)},
 * {@link #getMaxPostfix()}, and {@link #isPostfixValid(int)} methods. In addition, 
 * {@link #detectorChanged(Detector)} method can be overridden if any detector 
 * dependent initialization is required.
 *
 * @author D. Onoprienko
 * @version $Id: RegionSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
abstract public class RegionSegmenter extends AbstractSegmenter {
  
// -- Constructors :  ----------------------------------------------------------
  
  /** Default constructor. */
  protected RegionSegmenter() {
  }
  
// -- To be implemented by subclusses :  ---------------------------------------
  
  /**
   * Subclasses should implement this method to return <tt>postfix</tt> corresponding 
   * to the position of the given simulated hit. 
   * The <tt>postfix</tt> must be non-negative integer, unique within the part of 
   * the detector handled by this <tt>Segmenter</tt>  object. If the hit is outside
   * of any sensor, "-1" should be returned.
   */
  abstract protected int makePostfix(SimTrackerHit hit);

  /**
   * Subclasses should implement this method to create a new {@link Sensor} object given
   * the <tt>postfix</tt>. If the postfix is invalid, <tt>null</tt> should be returned.
   */
  abstract protected Sensor makeSensor(int postfix);
  
  /**
   * Subclasses should implement this method to return maximum postfix value that can be
   * returned by {@link #makePostfix(SimTrackerHit)} method of this <tt>Segmenter</tt> object.
   */
  abstract protected int getMaxPostfix();
  
  /**
   * Subclasses should override this method to return <tt>true</tt> if the given 
   * <tt>postfix</tt> corresponds to a valid <tt>Sensor</tt> object that can be created
   * by this <tt>RegionSegmenter</tt>. Default implementation is provided, returning
   * <tt>true</tt> if the value of <tt>postfix</tt> is between zero and the value
   * returned by {@link #getMaxPostfix()}.
   */
  protected boolean isPostfixValid(int postfix) {
    return postfix >= 0 && postfix <= getMaxPostfix();
  }
  
// -- Implementing Segmenter :  ------------------------------------------------
  
  /**
   * Returns a collection of <tt>Sensors</tt> corresponding to all virtual segments
   * in the part of the detector handled by this <tt>Segmenter</tt>.
   */
  public List<Integer> getSensorIDs() {
    int nSensors = getMaxPostfix() + 1;
    ArrayList<Integer> sensorIDs = new ArrayList<Integer>(nSensors);
    for (int postfix=0; postfix < nSensors; postfix++) {
      if (isPostfixValid(postfix)) sensorIDs.add(postfixToID(postfix));
    }
    //System.out.println("Segmenter "+this+" returning "+sensorIDs.size()+" IDs");
    return sensorIDs;
  }
  
  /**
   * Returns integer <tt>SensorID</tt> uniquely identifying a {@link Sensor} object
   * within the whole detector, given the simulated hit.
   */
  public int getSensorID(SimTrackerHit hit) {
    int postfix = makePostfix(hit);
//    if (postfix == -1) {
//      System.out.println(" ");
//      System.out.println("From RegionSegmenter.getSensorID(hit) :");
//      System.out.println("Segmenter " + this + " failed to produce Sensor");
//      System.out.println("for hit in " + hit.getSubdetector().getName() + " layer " + hit.getLayer());
//      System.out.println(" ");
//    }
    return (postfix == -1) ? -1 : postfixToID(postfix);
  }
  
  /**
   * Creates a new {@link Sensor} object given full <tt>SensorID</tt>.
   * For the sake of speed, no checking is done to verify that the supplied 
   * <tt>SensorID</tt> belongs to the part of the detector that should be handled 
   * by this <tt>OldSegmenter</tt> - be careful.
   */
  public Sensor getSensor(int sensorID) {
    return makeSensor(idToPostfix(sensorID));
  }
  
  /**
   * Creates a new {@link Sensor} object given simulated hit.
   */
  public Sensor getSensor(SimTrackerHit hit) {
    int postfix = makePostfix(hit);
    return (postfix == -1) ? null : makeSensor(postfix);
  }
  
  /** 
   * Called by the framework whenever detector geometry changes.
   * Subclasses can override this method if they need to perform any 
   * detector-dependent initialization.
   */
  public void detectorChanged(Detector detector) {
//    System.out.println(" ");
//    System.out.println("Updated " + this + " with " + detector.getName());
//    System.out.println("Created " + (getMaxPostfix()+1) + " sensors");
  }
  
  /**
   * Returns a list of <tt>Sensors</tt> that might contain hits that should be combined
   * with hits in the <tt>Sensor</tt> whose <tt>sensorID</tt> is supplied as an argument
   * to form stereo pairs.
   * Default implementation returns an empty list. Subclasses may override.
   */
  public List<Integer> getStereoPartners(int sensorID) {
    return Collections.emptyList();
  }
  
// -- Handling prefixes and postfixes :  ---------------------------------------
  
  /**
   * Set <tt>pretfix</tt> value and <tt>postfix</tt> length for this <tt>Segmenter</tt>.
   */
  public void setPrefix(int prefix, int postfixLength) {
//    System.out.println("Setting prefix for "+this+" prefix "+prefix+" length "+postfixLength);
    super.setPrefix(prefix, postfixLength);
    if (getIdSize(prefix) + postfixLength > 32) {
      throw new IllegalArgumentException("Combined prefix and postfix length cannot be more than 32");
    } else if (postfixLength < getNativePostfixLength()) {
      throw new IllegalArgumentException("Attempt to set insufficient postfix length");
    }
  }
  
  /**
   * Returns minimum number of bits required to hold any postfix that can be returned by
   * {@link #makePostfix(SimTrackerHit)} method of this <tt>Segmenter</tt>.
   */
  public int getNativePostfixLength() {
    return getIdSize(getMaxPostfix());
  }
  
}
