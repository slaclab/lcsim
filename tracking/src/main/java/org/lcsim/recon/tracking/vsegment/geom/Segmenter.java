package org.lcsim.recon.tracking.vsegment.geom;

import java.util.*;

import org.lcsim.event.SimTrackerHit;
import org.lcsim.geometry.Detector;

/**
 * Any class that implement this interface defines virtual segmentation of either
 * entire detector or some part of it.
 * <p>
 * Additional machinery is provided for chaining <tt>Segmenters</tt> - see
 * {@link AbstractSegmenter} for details.
 *
 * @author D. Onoprienko
 * @version $Id: Segmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public interface Segmenter {
  
  /**
   * Returns a list of <tt>SensorsIDs</tt> corresponding to all virtual segments
   * in the part of the detector handled by this <tt>Segmenter</tt>.
   */
  public List<Integer> getSensorIDs();
  
  /**
   * Returns integer <tt>SensorID</tt> uniquely identifying a {@link Sensor} object
   * within the whole detector, given the simulated hit. 
   * Returns "-1" if the hit is outside of any sensor.
   */
  public int getSensorID(SimTrackerHit hit);
  
  /**
   * Creates a new {@link Sensor} object given full <tt>SensorID</tt>.
   */
  public Sensor getSensor(int sensorID);
  
  /**
   * Detector dependent initialization.
   */
  public void detectorChanged(Detector detector);
  
  /**
   * Returns a list of <tt>Sensors</tt> that might contain hits that should be combined
   * with hits in the <tt>Sensor</tt> whose <tt>sensorID</tt> is supplied as an argument
   * to form stereo pairs. 
   * If an empty list is returned, hits on this sensor will be ignored when forming
   * crosses. If <tt>null</tt> is returned, 
   * {@link org.lcsim.contrib.onoprien.tracking.hitmaking.TrackerHitConverter} will, by
   * default, create a single 3-dimensional hit at the center of the strip.
   */
  public List<Integer> getStereoPartners(int sensorID);
  
}
