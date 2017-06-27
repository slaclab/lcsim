package org.lcsim.recon.tracking.vsegment.geom.segmenters;

import java.util.*;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.Subdetector;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IGeometryInfo;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.solids.Tube;
import org.lcsim.recon.cat.util.Const;

import org.lcsim.recon.tracking.vsegment.geom.AbstractSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.RegionSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.geom.sensortypes.Ring;
import org.lcsim.recon.tracking.vsegment.transform.Rotation3D;
import org.lcsim.recon.tracking.vsegment.transform.Axis;

/**
 * 
 * Simplistic <tt>Segmenter</tt> that tiles endcap disks with strips or pixels.
 * <p>
 * Each disk will correspond to a {@link Sensor} object. Postfixes are assigned in
 * the increasing Z order. Sensors corresponding to layers facing away from the center
 * of the detector are rotated by an angle set through a call to 
 * {@link #setStereoAngle(double angle)}.
 * 
 * @author D.Onoprienko
 * @version $Id: DiskTrackerToRingsSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class DiskTrackerToRingsSegmenter extends RegionSegmenter {
  
// -- Constructors and initialization :  ---------------------------------------
  
  /**
   * 
   * Creates a new instance of DiskTrackerToRingsSegmenter.
   * Subdetector name supplied to the constructor is used to provide reasonable 
   * defaults for strip width, length, and stereo angle.
   */
  public DiskTrackerToRingsSegmenter(String subdetectorName) {
    _sdName = subdetectorName;
    if (_sdName == "TrackerEndcap") {
      setStripWidth(25.*Const.micrometer);
      setStripLength(10.*Const.cm);
      setStereoAngle(Math.PI / 2.);
    } else if (_sdName == "VertexEndcap" || _sdName == "TrackerForward") {
      setStripWidth(25.*Const.micrometer);
      setStripLength(25.*Const.micrometer);
      setStereoAngle(0.);
    }
  }

  /** 
   * Detector-dependent initialization.
   */
  public void detectorChanged(Detector detector) {
    super.detectorChanged(detector);
    Subdetector sub = detector.getSubdetector(_sdName);
    if (sub == null) return;
    _detElts = AbstractSegmenter.getLeaves(sub.getDetectorElement());
    Collections.sort(_detElts, new Comparator<IDetectorElement>() {
      public int compare(IDetectorElement s1, IDetectorElement s2) {
        return (int)Math.signum(s1.getGeometry().getPosition().z()-s2.getGeometry().getPosition().z());
      }
    });
    _nDisks = _detElts.size();
    _nLayers = _nDisks/2;
    _radiusInner = new double[_nDisks];
    _radiusOuter = new double[_nDisks];
    _thickness = new double[_nDisks];
    _z = new double[_nDisks];
    int postfix = 0;
    for (IDetectorElement del : _detElts) {
      IGeometryInfo gInfo = del.getGeometry();
      Tube solid = (Tube) gInfo.getLogicalVolume().getSolid();
      _radiusInner[postfix] = solid.getInnerRadius();
      _radiusOuter[postfix] = solid.getOuterRadius();
      _thickness[postfix] = solid.getZHalfLength()*2.;
      _z[postfix] = gInfo.getPosition().z();
      postfix++;
    }
  }
  
// -- Setters :  ---------------------------------------------------------------

  /**
   * Set strip width.
   * Default is 25 micron.
   */
  public void setStripWidth(double pitch) {
    _stripWidth = pitch;
  }

  /**
   * Set strip length. 
   * Default is 10 cm for "TrackerEndcap", 25 micron for "VertexEndcap" and "TrackerForward".
   */
  public void setStripLength(double length) {
    _stripLength = length;
  }

  /**
   * Set stereo angle.
   * Default is 90 degrees for "TrackerEndcap", 0 for "VertexEndcap" and "TrackerForward".
   */
  public void setStereoAngle(double angle) {
    _rot1 = new Rotation3D(Axis.Z, angle);
  }

// -- Implementing RegionSegmenter :  ------------------------------------------

  /**
   * Returns sensor ID postfix corresponding to the given position.
   */
  public int makePostfix(SimTrackerHit hit) {
    int layer = hit.getLayer();
    return hit.getPoint()[2] < 0. ? _nLayers - layer - 1  : _nLayers + layer ;
  }
  
  /**
   * Returns maximum postfix value that can be returned by
   * {@link #makePostfix(SimTrackerHit)} method of this <tt>Segmenter</tt>.
   */
  public int getMaxPostfix() {
    return _nDisks - 1;
  }

  /** Creates a {@link Sensor} object given the ID. */
  public Sensor makeSensor(int postfix) {
    SensorType type = new Ring(_radiusInner[postfix], _radiusOuter[postfix], _stripWidth, _stripLength, _thickness[postfix]);
    Hep3Vector trans = new BasicHep3Vector(0.,0.,_z[postfix]);
    int layer = (postfix < _nDisks) ? _nDisks - postfix - 1 : postfix - _nDisks ;
    Rotation3D rot = (layer % 2 == 0) ? _rot0 : _rot1 ;
    return new Sensor(_detElts.get(postfix), postfixToID(postfix), type, trans, rot);
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  String _sdName;
  private List<IDetectorElement> _detElts;
  
  private int _nDisks;
  private int _nLayers;
  private double[] _radiusInner;
  private double[] _radiusOuter;
  private double[] _thickness;
  private double[] _z;
  private double _stripLength;
  private double _stripWidth;
  
  private Rotation3D _rot0 = new Rotation3D();
  private Rotation3D _rot1;

}
