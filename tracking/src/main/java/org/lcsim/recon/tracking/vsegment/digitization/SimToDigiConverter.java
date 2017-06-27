package org.lcsim.recon.tracking.vsegment.digitization;

import java.util.List;

import org.lcsim.geometry.Detector;
import org.lcsim.event.SimTrackerHit;

import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.geom.SegmentationManager;

/**
 * Abstract class to be extended by classes that provide algorithms for conversion
 * of {@link SimTrackerHit}s into {@link DigiTrackerHit}s.
 *
 * @author D.Onoprienko
 * @version $Id: SimToDigiConverter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
abstract public class SimToDigiConverter {
  
  /**
   * Convert a list of <tt>SimTrackerHits</tt> that were produced in a single
   * particle-sensor crossing into a list of <tt>DigiTrackerHits</tt>.
   */
  abstract public List<DigiTrackerHit> convert(List<SimTrackerHit> hits);
  
  /** Called by framework to set segmentation manager. */
  public void setSegmentationManager(SegmentationManager segMan) {
    _segMan = segMan;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  protected SegmentationManager _segMan;
}
