package org.lcsim.recon.tracking.vsegment.geom.segmenters;

import java.util.*;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.Subdetector;

import org.lcsim.recon.tracking.vsegment.geom.AbstractSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.ForwardingSegmenter;

/**
 * Forwarding segmenter that chooses daughter segmenter based on the {@link IDetectorElement}
 * associated with the hit. Daughter segmenters are prefixed in the order of increasing Z
 * of the disks they handle. Subclasses should implement {@link #assignSegmenter(IDetectorElement)}
 * method that assigns segmenters to disks.
 *
 * @author D. Onoprienko
 * @version $Id: DiskTrackerSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
abstract public class DiskTrackerSegmenter extends ForwardingSegmenter {
  
// -- Constructors :  ----------------------------------------------------------
  
  public DiskTrackerSegmenter(String subdetectorName) {
    _subdName = subdetectorName;
  }
  
// -- Choosing daughter Segmenter :  -------------------------------------------

  /**
   * Returns daughter <tt>Segmenter</tt> that can handle the given hit.
   */
  public AbstractSegmenter chooseSegmenter(SimTrackerHit hit) {
    return _deToSegmenter.get(hit.getDetectorElement());
  }
  
// -- Initialization :  --------------------------------------------------------
  
  public void detectorChanged(Detector detector) {
    Subdetector sub = detector.getSubdetector(_subdName);
    if (sub == null) return;
    _dElements = AbstractSegmenter.getLeaves(sub.getDetectorElement());
    Collections.sort(_dElements, new Comparator<IDetectorElement>() {
      public int compare(IDetectorElement s1, IDetectorElement s2) {
        return (int)Math.signum(s1.getGeometry().getPosition().z()-s2.getGeometry().getPosition().z());
      }
    });
    _deToSegmenter = new HashMap<IDetectorElement,AbstractSegmenter>();
    removeAllDaughterSegmenters();
    for (IDetectorElement de : _dElements) {
      AbstractSegmenter segmenter = assignSegmenter(de);
      addDaughterSegmenter(segmenter);
      _deToSegmenter.put(de, segmenter);
    }
    updateDaughterSegmenters(detector);
  }
  
  /**
   * Subclasses should implement this method to return <tt>Segmenter</tt> that 
   * handles hits in the given <tt>DetectorElement</tt>.
   */
  abstract public AbstractSegmenter assignSegmenter(IDetectorElement de);
  
// -- Utility methods :  -------------------------------------------------------
  
  /**
   * Returns layer number for the disk.
   */
  public int getLayer(IDetectorElement de) {
    int index = _dElements.indexOf(de);
    int nLayers = _dElements.size()/2;
    return (index < nLayers) ? nLayers-index-1 : index - nLayers;
  }
  
  /**
   * Returns superlayer number for the disk.
   * Superlayer is a pair of sensor disks on opposite sides of the same support disk.
   */
  public int getSuperlayer(IDetectorElement de) {
    return getLayer(de)/2;
  }
  
  /**
   * Returns <tt>true</tt> if the disk is on the side of a superlayer that faces 
   * the center of the detector.
   */
  public boolean isInner(IDetectorElement de) {
    return (getLayer(de) % 2) == 0;
  }
  
  protected int getOtherSideIndex(int daughterIndex) {
    return (daughterIndex % 2 == 0) ? daughterIndex + 1 : daughterIndex - 1 ;
  }

// -- Private parts :  ---------------------------------------------------------
  
  protected String _subdName;
  protected List<IDetectorElement> _dElements;
  
  protected HashMap<IDetectorElement,AbstractSegmenter> _deToSegmenter;
}
