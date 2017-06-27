package org.lcsim.recon.tracking.vsegment.hitmaking;

import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;

/**
 * Interface to be conformed to by classes that implement algorithms for producing
 * {@link TrackerHit} objects from {@link TrackerCluster} objects, possibly taking
 * into account information from tracks associated with the hits.
 *
 * @author D.Onoprienko
 * @version $Id: TrackerHitMaker.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public interface TrackerHitMaker {
  
  /**
   * Makes a new <tt>TrackerHit</tt>.
   */
  public TrackerHit make(TrackerCluster cluster);

}
