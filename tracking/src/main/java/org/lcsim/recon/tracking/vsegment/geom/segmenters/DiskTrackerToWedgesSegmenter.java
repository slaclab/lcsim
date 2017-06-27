package org.lcsim.recon.tracking.vsegment.geom.segmenters;

import java.util.*;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IGeometryInfo;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.solids.Tube;

import org.lcsim.recon.tracking.vsegment.geom.AbstractSegmenter;

/**
 *
 *
 * @author D. Onoprienko
 * @version $Id: DiskTrackerToWedgesSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class DiskTrackerToWedgesSegmenter extends DiskTrackerSegmenter {
  
// -- Constructors :  ----------------------------------------------------------
  
  public DiskTrackerToWedgesSegmenter(String subdetectorName) {
    super(subdetectorName);
  }
  
// -- Implementing DiskTrackerSegmenter :  -------------------------------------
  
  /**
   * Return <tt>Segmenter</tt> that handles hits in the given <tt>DetectorElement</tt>.
   */
  public AbstractSegmenter assignSegmenter(IDetectorElement de) {
    
    checkGeometry();
    
    int nRadialSlices;
    if (_radialSlicesBySuperlayer == null) {
      IGeometryInfo gInfo = de.getGeometry();
      Tube solid = (Tube) gInfo.getLogicalVolume().getSolid();
      double radiusInner = solid.getInnerRadius();
      double radiusOuter = solid.getOuterRadius();
      nRadialSlices = (int)Math.round((radiusOuter - radiusInner)/_stripLength);
    } else {
      nRadialSlices = _radialSlicesBySuperlayer[getSuperlayer(de)];
    }
    
    int nPhiSlices;
    if (_phiSlicesBySuperlayer == null) {
      nPhiSlices = _phiSlices;
    } else {
      nPhiSlices = _phiSlicesBySuperlayer[getSuperlayer(de)];
    }
   
    double pitch;
    if (_pitchBySuperlayer == null) {
      pitch = _pitch;
    } else {
      pitch = _pitchBySuperlayer[getSuperlayer(de)];
    }
    
    boolean left = isInner(de);
    
    return new DiskToWedgesSegmenter(de, nRadialSlices, nPhiSlices, pitch, left);
  }

// -- Stereo partners :  -------------------------------------------------------
  
  /**
   * Returns a list of <tt>Sensors</tt> that might contain hits that should be combined
   * with hits in the <tt>Sensor</tt> whose <tt>sensorID</tt> is supplied as an argument
   * to form stereo pairs. 
   */
  public List<Integer> getStereoPartners(int sensorID) {
    int partnerDisk = getOtherSideIndex(idToDaughterIndex(sensorID));
    List<Integer> out = new ArrayList<Integer>(1);
    out.add((partnerDisk << _daughterPostfixLength) | (sensorID & ~_daughterIdMask));
    return out;
  }

// -- Setters :  ---------------------------------------------------------------
  
  /**
   * Set strip length.
   * The actual strip length in each disk will be adjusted to create an integral
   * number of radial slices.
   */
  public void setStripLength(double length) {
    _radialSlicesBySuperlayer = null;
    _stripLength = length;
  }
  
  /**
   * Set the number of radial slices in each superlayer.
   */
  public void setNumberOfRadialSlices(int[] nRadialSlices) {
    //_radialSlicesBySuperlayer = Arrays.copyOf(nRadialSlices, nRadialSlices.length); // Need JDK 1.6
    _radialSlicesBySuperlayer = nRadialSlices;
  }
  
  /**
   * Set the number of axial slices in all superlayers.
   */
  public void setNumberOfPhiSlices(int nPhiSlices) {
    _phiSlicesBySuperlayer = null;
    _phiSlices = nPhiSlices;
  }
  
  /**
   * Set the number of axial slices in each superlayer.
   */
  public void setNumberOfPhiSlices(int[] nPhiSlices) {
    //_phiSlicesBySuperlayer = Arrays.copyOf(nPhiSlices, nPhiSlices.length); // Need JDK 1.6
    _phiSlicesBySuperlayer = nPhiSlices;
  }
  
  /**
   * Set the strip width in all superlayers.
   */
  public void setStripWidth(double pitch) {
    _pitchBySuperlayer = null;
    _pitch = pitch;
  }
  
  /**
   * Set the strip width in each superlayer.
   */
  public void setStripWidth(double[] pitch) {
    //_pitchBySuperlayer = Arrays.copyOf(pitch, pitch.length); // Need JDK 1.6
    _pitchBySuperlayer = pitch;
  }
  
// -- Helpers :  ---------------------------------------------------------------
  
  protected void checkGeometry() {
    int nDisks = _dElements.size()/4;
    String m1 = "Disk tracker "+_subdName +" contains "+nDisks +" disks in each endcap, but you only supplied ";
    if (_radialSlicesBySuperlayer != null && _radialSlicesBySuperlayer.length < nDisks ) {
      throw new RuntimeException(m1 + "number of radial slices for " + _radialSlicesBySuperlayer.length + " superlayers");
    } else if (_phiSlicesBySuperlayer != null && _phiSlicesBySuperlayer.length < nDisks ) {
      throw new RuntimeException(m1 + "number of phi slices for " + _radialSlicesBySuperlayer.length + " superlayers");
    } else if (_pitchBySuperlayer != null && _pitchBySuperlayer.length < nDisks ) {
      throw new RuntimeException(m1 + "strip widths for " + _radialSlicesBySuperlayer.length + " superlayers");
    }
  }
    
// -- Private parts :  ---------------------------------------------------------
  
  double _stripLength;
  int[] _radialSlicesBySuperlayer;

  int _phiSlices;
  int[] _phiSlicesBySuperlayer;
  
  double _pitch;
  double[] _pitchBySuperlayer;
}
