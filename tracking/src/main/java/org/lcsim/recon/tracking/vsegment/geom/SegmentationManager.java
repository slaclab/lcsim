package org.lcsim.recon.tracking.vsegment.geom;

import java.lang.ref.SoftReference;
import java.util.*;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsEvent;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.geometry.Detector;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.util.Driver;

import org.lcsim.recon.tracking.vsegment.transform.Rotation3D;

/**
 * This class handles creation, caching and run-time access to {@link Sensor} objects and segmentation information.
 * <p>
 * Typically, a <tt>Driver</tt> that controls event reconstruction creates an
 * instance of this class, supplying {@link Segmenter} object that defines virtual 
 * segmentation of the detector, and adds it to the event processing chain before any
 * drivers that need access to {@link Sensor}s. See {@link org.lcsim.contrib.onoprien.tracking.ExampleDriver1}
 * or {@link org.lcsim.contrib.onoprien.tracking.ExampleDriver2}.
 * <p>
 * Other drivers that need access to <tt>SegmentationManager</tt> can fetch it from the
 * event record by calling <tt>event.get("SegmentationManager")</tt>.
 * <p>
 * By default, <tt>Sensor</tt> objects are created as needed (that is, when there are hits
 * in those sensors), and are kept in cache unless JVM starts running out of memory.
 * If the user needs <tt>Sensor</tt> objects corresponding to all virtual segments of
 * the detector to be created before data processing, <tt>SegmentationManager</tt>
 * can be asked to do so by a call to <tt>set("MAKE_SENSORS_ON_DETECTOR_CHANGE", true)</tt>.
 * It usually makes sense to do this if the user plans to use {@link #getSensors}
 * method in the future.
 *
 * @author D.Onoprienko
 * @version $Id: SegmentationManager.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class SegmentationManager extends Driver implements ConditionsListener {
  
// -- Constructors, initialization, and cleanup :  -----------------------------

  /**
   * Constructs a new instance of SegmentationManager.
   *
   * @param segmenter  Segmenter that defines virtual segmentation of the detector.
   */
  public SegmentationManager(Segmenter segmenter) {
    _segmenter = segmenter;
    _notInitialized = true;
    _createSensorsOnDetectorChange = true;
    _sensorWeakMap = new HashMap<Integer, SoftReference<Sensor>>();
    _cacheStereoRequests = true;
    _stereoMap = new HashMap<Sensor, List<Sensor>>();
    ConditionsManager.defaultInstance().addConditionsListener(this);
  }

  /** Called by framework whenever <tt>ConditionsEvent</tt> is dispatched by <tt>ConditionsManager</tt>. */
  public void conditionsChanged(ConditionsEvent event) {
    _notInitialized = true;
  }

  /**
   * Detector-dependent initialization.
   * Clears sensor map, calls <tt>detectorChanged(Detector)</tt> methods of all 
   * segmenters, then assign prefixes to segmenters.
   */
  private void detectorChanged() {
    ConditionsManager conMan = ConditionsManager.defaultInstance();
    Detector detector = null;
    try {
      detector = conMan.getCachedConditions(Detector.class,"compact.xml").getCachedData();
    } catch (ConditionsSetNotFoundException x) {}
    if (detector != null) {
      if (_createSensorsOnDetectorChange) {
        _sensorWeakMap = null;
        _sensorMap = new HashMap<Integer, Sensor>();
      } else {
        _sensorWeakMap = new HashMap<Integer, SoftReference<Sensor>>();
        _sensorMap = null;
      }
      if (_cacheStereoRequests) _stereoMap = new HashMap<Sensor, List<Sensor>>();
      _segmenter.detectorChanged(detector);
      if (_segmenter instanceof AbstractSegmenter) ((AbstractSegmenter)_segmenter).setPrefix(0);
      if (_createSensorsOnDetectorChange) {
        List<Integer> sensorIDs = _segmenter.getSensorIDs();
        for (int sensorID : sensorIDs) {
          _sensorMap.put(sensorID, _segmenter.getSensor(sensorID));
        };
      }
      _navigator = new Navigator(detector);
    } else {
      throw new RuntimeException("SegmentationManager cannot initialize, no Detector");      
    }
  }

// -- Setters :  ---------------------------------------------------------------

  /**
   * Set any <tt>boolean</tt> parameter. 
   * The following parameters can be set with this method:
   * <dl>
   * <dt>"MAKE_SENSORS_ON_DETECTOR_CHANGE"</dt> <dd>If set to <tt>true</tt>, <tt>Sensor</tt>
   *            objects corresponding to all virtual segments are created whenever the 
   *            detector information becomes available (or changes), and are kept in memory
   *            until the end of the job. 
   *            Default: <tt>true</tt>.</dd>
   * <dt>"CACHE_STEREO_REQUESTS"</dt> <dd>If set to <tt>true</tt>, the output of calls to
   *            {@link #getStereoPartners} will be cached. 
   *            Default: <tt>true</tt>.</dd></dl>
   * 
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, boolean value) {
    if (name.equalsIgnoreCase("MAKE_SENSORS_ON_DETECTOR_CHANGE")) {
      _createSensorsOnDetectorChange = value;
    } else if (name.equalsIgnoreCase("CACHE_STEREO_REQUESTS")) {
      _cacheStereoRequests = value;
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
// -- Getters :  ---------------------------------------------------------------
  
  /** 
   * Returns <tt>Navigator</tt> that provides various convenience methods for use
   * with <tt>Sensors</tt> created by this <tt>SegmentationManager</tt>.
   */
  public Navigator getNavigator() {
    return _navigator;
  }
  
// -- Event processing :  ------------------------------------------------------

  /** Called by framework to process event. */
  public void process(EventHeader event) {
    if (_notInitialized) {
      detectorChanged();
      _notInitialized = false;
    }
    event.put("SegmentationManager", this);
    super.process(event);
  }
  
// -- Sensor and channel lookup, ID conversions :  -----------------------------
  
  /**
   * Returns a collection of <tt>Sensors</tt> corresponding to all virtual segments of the detector.
   */
  public Collection<Sensor> getSensors() {
    if (_createSensorsOnDetectorChange) {
      return _sensorMap.values();
    } else {
      List<Integer> sensorIDs = _segmenter.getSensorIDs();
      ArrayList<Sensor> sensors = new ArrayList<Sensor>(sensorIDs.size());
      for (int sensorID : sensorIDs) sensors.add(getSensor(sensorID));
      return sensors;
    }
  }

  /**
   * Returns {@link Sensor} object corresponding to the given sensor ID.
   */
  public Sensor getSensor(int sensorID) {
    Sensor sensor = null;
    if (_createSensorsOnDetectorChange) {
      sensor = _sensorMap.get(sensorID);
    } else {
      SoftReference<Sensor> ref = _sensorWeakMap.get(sensorID);
      if (ref != null) sensor = ref.get();
      if (sensor == null) {
        sensor = _segmenter.getSensor(sensorID);
        if (sensor != null) _sensorWeakMap.put(sensorID, new SoftReference<Sensor>(sensor));
      }
    }
    return sensor;
  }

  /** 
   * Returns channel ID given sensor and global position.
   * Returns <tt>-1</tt> if <tt>position</tt> does not belong to this sensor.
   */
  public int getChannelID(Sensor sensor, Hep3Vector position) {
    return sensor.getType().getChannelID(sensor.globalToLocal(position));
  }

  /** 
   * Returns channel ID given sensor and global position extracted from the supplied
   * {@link SimTrackerHit} object.
   * Returns <tt>-1</tt> if the position does not belong to this sensor.
   */
  public int getChannelID(Sensor sensor, SimTrackerHit hit) {
    Hep3Vector position = new BasicHep3Vector(hit.getPoint());
    return getChannelID(sensor, position);
  }

  /** 
   * Returns channel ID given sensor ID and global position.
   * Returns <tt>-1</tt> if <tt>sensorID</tt> is invalid, or <tt>position<tt>
   * does not belong to this sensor.
   */
  public int getChannelID(int sensorID, Hep3Vector position) {
    Sensor sensor = getSensor(sensorID);
    return (sensor == null) ? -1 : getChannelID(sensor, position);
  }
  
  /** 
   * Converts cell ID and position obtained from {@link SimTrackerHit} object to 
   * sensor ID, and returns {@link Sensor} object corresponding to this ID.
   */
  public Sensor getSensor(SimTrackerHit hit) {
    int id = getSensorID(hit);
    return (id == -1) ? null : getSensor(id);
  }

  /** 
   * Converts cell ID and position obtained from {@link SimTrackerHit} object to sensor ID.
   * FIXME: should be just return _segmenter.getSensorID(hit);
   * The rest is a workaround for Detector being created twice bug.
   */
  public int getSensorID(SimTrackerHit hit) {
    return _segmenter.getSensorID(hit);
  }

// -- Getting info about Sensors :  --------------------------------------------

  /**
   * Returns a list of <tt>Sensors</tt> that might contain hits that should be combined
   * with hits in the <tt>Sensor</tt> supplied as an argument to form stereo pairs. 
   * If the <tt>Segmenter</tt> used by this <tt>SegmentationManager</tt> does not support 
   * stereo partner lookup, an empty list is returned.
   */  
  public List<Sensor> getStereoPartners(Sensor sensor) {
    List<Sensor> partners = null;
    if (_cacheStereoRequests) partners = _stereoMap.get(sensor);
    if (partners == null) {
      List<Integer> partnerIDs = _segmenter.getStereoPartners(sensor.getID());
      if (partnerIDs == null) return null;
      partners = new ArrayList<Sensor>(partnerIDs.size());
      for (int sensorID : partnerIDs) partners.add(getSensor(sensorID));
    }
    if (_cacheStereoRequests) _stereoMap.put(sensor, partners);
    return partners;
  }
  
// -- Static access to segmentation manager :  ---------------------------------
  
  /** Set default segmentation manager. */
  static public void setDefaultInstance(SegmentationManager segMan) {
    _defaultSegMan = segMan;
  }
  
  /**
   * Returns segmentation manager that has been previously set with a call to
   * {@link #setDefaultInstance(SegmentationManager)} method.
   */
  static public SegmentationManager defaultInstance() {
    return _defaultSegMan;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  static private SegmentationManager _defaultSegMan;
  private boolean _notInitialized;
  
  private HashMap<Integer, SoftReference<Sensor>> _sensorWeakMap;
  private HashMap<Integer, Sensor> _sensorMap;
  
  private Segmenter _segmenter;
  private Navigator _navigator;
  
  private boolean _createSensorsOnDetectorChange;

  private boolean _cacheStereoRequests;
  private HashMap<Sensor, List<Sensor>> _stereoMap;

}
