package org.lcsim.recon.tracking.vsegment.geom.segmenters;

import java.util.*;

import org.lcsim.geometry.Detector;
import org.lcsim.geometry.Subdetector;
import org.lcsim.event.SimTrackerHit;

import org.lcsim.recon.tracking.vsegment.geom.AbstractSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.ForwardingSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.SegmentationManager;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;

/**
 * <tt>Segmenter</tt> that forwards <tt>postfix</tt> and {@link Sensor} creation calls 
 * to daughter Segmenters based on subdetector where the hit occured.
 * 
 * 
 * @author D.Onoprienko
 * @version $Id: SubdetectorBasedSegmenter.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class SubdetectorBasedSegmenter extends ForwardingSegmenter {
  
// -- Constructors and initialization :  ---------------------------------------
  
  /** Create an instance of <tt>SubdetectorBasedSegmenter</tt>. */
  public SubdetectorBasedSegmenter() {
    _subdetectorNames = new ArrayList<String>();
    _segmenters = new ArrayList<AbstractSegmenter>();
  }
  
  /** 
   * Detector dependent initialization.
   */
  public void detectorChanged(Detector detector) {
    _subdetectorNames.trimToSize();
    _segmenters.trimToSize();
    _sdToSegmenter = new HashMap<Subdetector, AbstractSegmenter>();
    removeAllDaughterSegmenters();
    for (int i=0; i<_segmenters.size(); i++) {
      String name = _subdetectorNames.get(i);
      Subdetector subDet = detector.getSubdetector(name);
      if (subDet != null) {
        AbstractSegmenter segmenter = _segmenters.get(i);
        _sdToSegmenter.put(subDet, segmenter);
        addDaughterSegmenter(segmenter);
      }
    }
    super.detectorChanged(detector);
    //updateDaughterSegmenters(detector);
  }
  
// -- Setters :  ---------------------------------------------------------------

  /** 
   * Set segmenter that will handle a particular subdetector.
   *
   * @param subdetectorName  Name of the subdetector to be handled by the supplied <tt>Segmenter</tt>
   * @param segmenter        <tt>Segmenter</tt> to be used
   */
  public void setSegmenter(String subdetectorName, AbstractSegmenter segmenter) {
    _subdetectorNames.add(subdetectorName);
    _segmenters.add(segmenter);
  }
  
// -- Choosing Segmenter :  ----------------------------------------------------
  
  /**
   * Returns daughter <tt>Segmenter</tt> that can handle the given hit.
   */
  public AbstractSegmenter chooseSegmenter(SimTrackerHit hit) {
    return _sdToSegmenter.get(hit.getSubdetector());
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  private ArrayList<String> _subdetectorNames;
  private ArrayList<AbstractSegmenter> _segmenters;
  private HashMap<Subdetector, AbstractSegmenter> _sdToSegmenter;
  
}
