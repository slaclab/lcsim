package org.lcsim.recon.tracking.vsegment.geom.segmenters;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IGeometryInfo;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.solids.Tube;
import org.lcsim.event.SimTrackerHit;

import org.lcsim.recon.tracking.vsegment.geom.RegionSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.geom.sensortypes.WedgeSideParallel;
import org.lcsim.recon.tracking.vsegment.transform.Axis;
import org.lcsim.recon.tracking.vsegment.transform.Rotation3D;
import org.lcsim.recon.tracking.vsegment.transform.Transformation3D;

/**
 * Simplistic <tt>Segmenter</tt> that tiles a single disk with wedges.
 * 
 * @author D. Onoprienko
 * @version $Id: DiskToWedgesSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class DiskToWedgesSegmenter extends RegionSegmenter {
  
// -- Constructors :  ----------------------------------------------------------
  
  public DiskToWedgesSegmenter(IDetectorElement disk, int nRadialSlices, int nPhiSlices, double pitch, boolean left) {
    
    _de = disk;
    _left = left;
    _pitch = pitch;
    _nPhi = nPhiSlices;
    _nRadial = nRadialSlices;
    
    IGeometryInfo gInfo = _de.getGeometry();
    Tube solid = (Tube) gInfo.getLogicalVolume().getSolid();
    _rMiddleMin = solid.getInnerRadius();
    double rMax = solid.getOuterRadius();
    double halfThickness = solid.getZHalfLength();

    _z = gInfo.getPosition().z();
    _zMin = _z - halfThickness;
    _zMax = _z + halfThickness;
    
    _deltaPhi = (2.*Math.PI)/nPhiSlices;
    _rMin = _rMiddleMin/Math.cos(_deltaPhi/2.);
    _deltaR = (rMax - _rMin) / nRadialSlices;
    _deltaRMiddle = _deltaR * Math.cos(_deltaPhi/2.);
    
    _sType = new SensorType[_nRadial];
    for (int indexR=0; indexR < _nRadial; indexR++) {
      _sType[indexR] = new WedgeSideParallel(_rMin+_deltaR*indexR, _deltaR, _deltaPhi, _pitch, _left, 2.*halfThickness);
    }

    _rotation = new Transformation3D[_nPhi];
    for (int indexPhi=0; indexPhi < _nPhi; indexPhi++ ) {
      double angle = (left) ? _deltaPhi * (indexPhi + 1) - Math.PI/2. : _deltaPhi * indexPhi - Math.PI/2.;
      _rotation[indexPhi] = new Rotation3D(Axis.Z, angle);
    }
  }
// -- Implementing RegionSegmenter :  ------------------------------------------

  /**
   * Returns <tt>postfix</tt> corresponding to the position of the given simulated hit. 
   */
  protected int makePostfix(SimTrackerHit hit) {
    
    double[] pos = hit.getPoint();
    
    if (pos[2]<_zMin || pos[2]>_zMax) return -1;
    
    double r = Math.hypot(pos[0],pos[1]);
    double phi = Math.atan2(pos[1], pos[0]);
    phi = (phi > 0.) ? phi : phi + Math.PI * 2.;
    
    int indexPhi = (int) Math.floor(phi/_deltaPhi);
    if (indexPhi < 0) indexPhi = 0;
    if (indexPhi >= _nPhi) indexPhi = _nPhi -1;
    
    double rMiddle = r * Math.cos(phi - _deltaPhi*(indexPhi+.5));
    int indexR = (int) Math.floor((rMiddle - _rMiddleMin) / _deltaRMiddle);
    if ((indexR < 0) || (indexR >= _nRadial)) return -1;
    
    return (_nRadial * indexPhi) + indexR;
  }
  
  /**
   * Returns maximum postfix value that can be returned by
   * {@link #makePostfix(SimTrackerHit)} method of this <tt>Segmenter</tt> object.
   */
  protected int getMaxPostfix() {
    return (_nRadial * _nPhi) - 1;
  }

  /**
   * 
   * Creates a new {@link Sensor} object given the <tt>postfix</tt>.
   */
  protected Sensor makeSensor(int postfix) {
    int indexR = postfix % _nRadial;
    int indexPhi = postfix / _nRadial;
    double r = _rMin + indexR * _deltaR;
    double phi = (indexPhi + 1) * _deltaPhi;
    Hep3Vector translation = new BasicHep3Vector(r*Math.cos(phi), r*Math.sin(phi), _z);
    return new Sensor(_de, postfixToID(postfix), _sType[indexR], translation, _rotation[indexPhi]);
  }

// -- Private parts :  ---------------------------------------------------------
  
  IDetectorElement _de;
  
  boolean _left;
  double _pitch;
  
  int _nPhi;
  int _nRadial;
  
  double _z;
  double _zMin;
  double _zMax;
  
  double _rMin;
  double _rMiddleMin;
  
  double _deltaPhi;
  double _deltaR;
  double _deltaRMiddle;
  
  SensorType[] _sType;
  Transformation3D[] _rotation;
}
