package org.lcsim.recon.tracking.vsegment.geom;

import java.util.*;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IGeometryInfo;
import org.lcsim.detector.IPhysicalVolume;

/**
 * Base class for {@link ForwardingSegmenter}s and {@link RegionSegmenter}s that 
 * can be chained together to describe virtual segmentation of the entire detector.
 * <p>
 * A <tt>ForwardingSegmenter</tt> can have any number of daughter segmenters, each 
 * handling a particular part of the detector. Each daughter is either another 
 * <tt>ForwardingSegmenter</tt>, or a <tt>RegionSegmenter</tt> that does the actual
 * {@link Sensor} object creation, and assigns an integer ID (<tt>postfix</tt>) to
 * each <tt>Sensor</tt> within the region it is responsible for. The result is a tree
 * of segmenters, with <tt>ForwardingSegmenter</tt>s at its top and intermediate nodes,
 * and <tt>RegionSegmenter</tt>s as its leaves. {@link SegmentationManager} automatically
 * assigns prefixes to all <tt>RegionSegmenter</tt>s, making sure that <tt>SensorID</tt>
 * they assign to <tt>Sensors</tt> are unique within the whole detector. 
 * <p>
 * See {@link org.lcsim.contrib.onoprien.tracking.ExampleDriver1} for an example of 
 * chaining several different segmenters.
 *
 * @author D. Onoprienko
 * @version $Id: AbstractSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
abstract public class AbstractSegmenter implements Segmenter {
  
// -- Constructors :  ----------------------------------------------------------
  
  AbstractSegmenter() {
  }
// -- Handling of prefixes and postfixes :  ------------------------------------
  
  /** 
   * Set <tt>prefix</tt> for this <tt>Segmenter</tt>.
   * The number of bits reserved for <tt>postfix</tt> will be calculated automatically.
   * For the top level <tt>Segmenter</tt>, this method will be called by
   * <tt>SegmentationManager</tt> with <tt>prefix</tt> equal to zero.
   */
  void setPrefix(int prefix) {
    setPrefix(prefix, getNativePostfixLength());
  }
  
  /**
   * Set <tt>pretfix</tt> value and <tt>postfix</tt> length for this <tt>Segmenter</tt>.
   */
  void setPrefix(int prefix, int postfixLength) {
    _prefix = prefix;
    _postfixLength = postfixLength;
    _postfixMask = 0;
    for (int i=0; i<postfixLength; i++) _postfixMask = (_postfixMask << 1) | 1;
    _prefixTemplate = prefix << _postfixLength;
  }
  
  
  /**
   * Returns minimum number of bits required to hold any postfix that can be used
   * by this <tt>Segmenter</tt>.
   */
  abstract protected int getNativePostfixLength();
  
  /**
   * Get <tt>prefix</tt> used by this <tt>OldSegmenter</tt>.
   */
  final protected int getPrefix() {
    return _prefix;
  }
  
  /** Convert <tt>postfix</tt> to full <tt>SensorID</tt>. */
  final protected int postfixToID(int postfix) {
    return (postfix == -1) ? -1 : (_prefixTemplate | postfix);
  }

  /** Extract <tt>postfix</tt> from full <tt>SensorID</tt>. */
  final protected int idToPostfix(int sensorID) {
    return sensorID & _postfixMask;
  }
  
// -- Static utility methods :  ------------------------------------------------
  
  /**
   * Returns the number of bits required to hold an integer between <tt>0</tt> and <tt>maxID</tt>.
   */
  static int getIdSize(int maxID) {
    return (int) Math.ceil(Math.log(maxID+0.8)/Math.log(2.));
  }
  
  /**
   * Returns a list of sensitive lowest-level decendents of the diven detector element.
   * FIXME: This should be a DetectorElement method !
   */
  static public List<IDetectorElement> getLeaves(IDetectorElement del) {
    ArrayList<IDetectorElement> out = new ArrayList<IDetectorElement>();
    if (del.hasChildren()) {
      for (IDetectorElement child : del.getChildren()) {
        out.addAll(getLeaves(child));
      }
    } else {
      IGeometryInfo gInfo = del.getGeometry();
      if (gInfo != null) {
        IPhysicalVolume pVol = gInfo.getPhysicalVolume();
        if (pVol != null) out.add(del);
//        if (pVol != null && pVol.isSensitive()) out.add(del); Returns false for every volume - why ?
      }
    }
    return out;
  }
  
// -- Private parts :  ---------------------------------------------------------

  protected int _prefix;
  protected int _postfixLength;
  protected int _postfixMask;
  protected int _prefixTemplate;
  
}
