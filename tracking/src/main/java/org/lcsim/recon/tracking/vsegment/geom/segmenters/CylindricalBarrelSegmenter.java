package org.lcsim.recon.tracking.vsegment.geom.segmenters;

import java.util.*;

import hep.physics.vec.BasicHep3Vector;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.Subdetector;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IGeometryInfo;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.solids.Tube;
import org.lcsim.units.clhep.SystemOfUnits;

import org.lcsim.recon.tracking.vsegment.geom.AbstractSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.RegionSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.geom.sensortypes.Cylinder;
import org.lcsim.recon.tracking.vsegment.transform.CartesianToCylindrical;

/**
 * Simplistic <tt>Segmenter</tt> that tiles barrel cylinders with Z-parallel strips or pixels.
 * <p>
 * Each barrel layer will correspond to a single {@link Sensor} object, with the
 * <tt>postfix</tt> equal to layer number.
 * 
 * 
 * @author D.Onoprienko
 * @version $Id: CylindricalBarrelSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class CylindricalBarrelSegmenter extends RegionSegmenter {
  
// -- Constructors and initialization :  ---------------------------------------

  /**
   * Creates a new instance of CylindricalBarrelSegmenter.
   * Subdetector name supplied to the constructor is used to provide reasonable 
   * defaults for strip width and length.
   */
  public CylindricalBarrelSegmenter(String subdetectorName) {
    _sdName = subdetectorName;
    if (_sdName == "TrackerBarrel") {
      _stripWidth = 25. * SystemOfUnits.micrometer;
      _stripLength = 10. * SystemOfUnits.cm;
    } else if (_sdName == "VertexBarrel") {
      _stripWidth = 25. * SystemOfUnits.micrometer;
      _stripLength = 25. * SystemOfUnits.micrometer;
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
    for (IDetectorElement de : _detElts) {
      if (!(de.getGeometry().getLogicalVolume().getSolid() instanceof Tube)) {
        throw new RuntimeException("You are trying to apply CylindricalBarrelSegmenter to detector whose barrel is not made of Tubes");
      }
    }
    Collections.sort(_detElts, new Comparator<IDetectorElement>() {
      public int compare(IDetectorElement s1, IDetectorElement s2) {
        return (int)Math.signum(((Tube)(s1.getGeometry().getLogicalVolume().getSolid())).getInnerRadius()
                              - ((Tube)(s2.getGeometry().getLogicalVolume().getSolid())).getInnerRadius());
      }
    });
    _nLayers = _detElts.size();
    _radius = new double[_nLayers];
    _length = new double[_nLayers];
    _thickness = new double[_nLayers];
    int postfix = 0;
    for (IDetectorElement del : _detElts) {
      IGeometryInfo gInfo = del.getGeometry();
      Tube solid = (Tube) gInfo.getLogicalVolume().getSolid();
      double rInner = solid.getInnerRadius();
      double rOuter = solid.getOuterRadius();
      _radius[postfix] = (rInner + rOuter)/2.;
      _length[postfix] = solid.getZHalfLength()*2.;
      _thickness[postfix] = (rOuter - rInner);
      postfix++;
    }
  }
  
// -- Setters :  ---------------------------------------------------------------

  /** Set strip width. Default is 5 micron. */
  public void setStripWidth(double pitch) {
    _stripWidth = pitch;
  }
  
  /** Set strip length. Default is 10 cm. */
  public void setStripLength(double length) {
    _stripLength = length;
  }
// -----------------------------------------------------------------------------

  /**
   * Returns sensor ID postfix corresponding to the given position. This postfix must 
   * be positive  integer, unique within the part of the detector handled by this 
   * <tt>Segmenter</tt>  object. The final Sensor ID will be constructed taking into
   * account the prefix set through a call to {@link #setPrefix} method.
   */
  public int makePostfix(SimTrackerHit hit) {
    return hit.getLayer();
  }
  
  /**
   * Returns maximum postfix value that can be returned by
   * {@link #makePostfix(SimTrackerHit)} method of this <tt>Segmenter</tt>.
   */
  public int getMaxPostfix() {
    return _nLayers-1;
  }

  /** Creates a {@link Sensor} object given the ID. */
  public Sensor makeSensor(int postfix) {
    SensorType type = new Cylinder(_radius[postfix], _length[postfix], _thickness[postfix], _stripWidth, _stripLength);
    return new Sensor(_detElts.get(postfix), postfixToID(postfix), type, _trans, _rot);
  }
  
  /**
   * Returnes <tt>null</tt> since there is no stereo in cylindrical barel.
   */
  public List<Integer> getStereoPartners(int sensorID) {
    return null;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  String _sdName;
  
  List<IDetectorElement> _detElts;
  
  private int _nLayers;
  private double[] _radius;
  private double[] _length;
  private double[] _thickness;
  private double _stripLength;
  private double _stripWidth;
  
  private CartesianToCylindrical _rot = new CartesianToCylindrical();
  private BasicHep3Vector _trans = new BasicHep3Vector();

}
