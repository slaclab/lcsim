package org.lcsim.recon.tracking.vsegment;

import org.lcsim.units.clhep.SystemOfUnits;

import org.lcsim.recon.tracking.vsegment.geom.segmenters.CylindricalBarrelSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.segmenters.DiskTrackerToWedgesSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.segmenters.DiskTrackerToRingsSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.segmenters.SubdetectorBasedSegmenter;

/**
 *
 *
 * @author D. Onoprienko
 * @version $Id: ExampleSegmenter1.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class ExampleSegmenter1 extends SubdetectorBasedSegmenter {
  
// -- Constructors :  ----------------------------------------------------------
  
  public ExampleSegmenter1() {
    
    // 25 um pixels in VTX Barrel
    
    CylindricalBarrelSegmenter vtxBarrelSegmenter = new CylindricalBarrelSegmenter("VertexBarrel");
    vtxBarrelSegmenter.setStripLength(25.*SystemOfUnits.micrometer);
    vtxBarrelSegmenter.setStripWidth(25.*SystemOfUnits.micrometer);
    setSegmenter("VertexBarrel", vtxBarrelSegmenter);
    
    // 10 cm x 25 um strips in outer Tracker Barrel
    
    CylindricalBarrelSegmenter trackerBarrelSegmenter = new CylindricalBarrelSegmenter("TrackerBarrel");
    trackerBarrelSegmenter.setStripLength(10.*SystemOfUnits.cm);
    trackerBarrelSegmenter.setStripWidth(25.*SystemOfUnits.micrometer);
    setSegmenter("TrackerBarrel", trackerBarrelSegmenter);
    
    // 25 um pixels in VTX Endcap
    
    DiskTrackerToRingsSegmenter vtxEndcapSegmenter = new DiskTrackerToRingsSegmenter("VertexEndcap");
    vtxEndcapSegmenter.setStripLength(25.*SystemOfUnits.micrometer);
    vtxEndcapSegmenter.setStripWidth(25.*SystemOfUnits.micrometer);
    setSegmenter("VertexEndcap", vtxEndcapSegmenter);
    
    // 15 degrees stereo wedges in Forward Tracker
    
    DiskTrackerToWedgesSegmenter trackerForwardSegmenter = new DiskTrackerToWedgesSegmenter("TrackerForward");
    trackerForwardSegmenter.setStripLength(10.*SystemOfUnits.cm);
    trackerForwardSegmenter.setStripWidth(25.*SystemOfUnits.micrometer);
    trackerForwardSegmenter.setNumberOfPhiSlices(24);
    setSegmenter("TrackerForward", trackerForwardSegmenter);
    
    // 15 degrees stereo wedges in outer Tracker Endcap
    
    DiskTrackerToWedgesSegmenter trackerEndcapSegmenter = new DiskTrackerToWedgesSegmenter("TrackerEndcap");
    trackerEndcapSegmenter.setNumberOfRadialSlices(new int[]{3,5,8,10, 10});
    trackerEndcapSegmenter.setStripWidth(25.*SystemOfUnits.micrometer);
    trackerEndcapSegmenter.setNumberOfPhiSlices(24);
    setSegmenter("TrackerEndcap", trackerEndcapSegmenter);
  }
  
// -- Private parts :  ---------------------------------------------------------
  
}
