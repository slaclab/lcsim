package org.lcsim.recon.tracking.vsegment.geom;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.converter.compact.SubdetectorDetectorElement;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.Subdetector;

/**
 * Utility class that contains helper methods for geometry navigation.
 *
 * @author D. Onoprienko
 * @version $Id: Navigator.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public final class Navigator {
  
// -- Constructors :  ----------------------------------------------------------
 
  public Navigator(Detector detector) {
	this.detector = detector;
    _vtxBarrel = detector.getSubdetector("VertexBarrel");
    _vtxEndcap = detector.getSubdetector("VertexEndcap");
    _trkBarrel = detector.getSubdetector("TrackerBarrel");
    _trkEndcap = detector.getSubdetector("TrackerEndcap");
    try {
      _trkForward = detector.getSubdetector("TrackerForward");
    } catch (Exception x) {}
  }
  
// -- Subdetectors :  ----------------------------------------------------------
  
  public Subdetector VERTEX_BARREL() {return _vtxBarrel;}
  public Subdetector VERTEX_ENDCAP() {return _vtxEndcap;}
  public Subdetector TRACKER_FORWARD() {return _trkForward;}
  public Subdetector TRACKER_BARREL() {return _trkBarrel;}
  public Subdetector TRACKER_ENDCAP() {return _trkEndcap;}
  
// -- Sensor location in the detector hierarchy :  -----------------------------
  
  public int getLayer(Sensor sensor) {
    IDetectorElement de = sensor.getDetectorElement();
    return de.getIdentifierHelper().getValue(de.getIdentifier(), "layer");
  }
  
  public int getSuperLayer(Sensor sensor) {
    Subdetector sd = getSubdetector(sensor);
    int layer = getLayer(sensor);
    if ((sd == _vtxBarrel) || (sd == _vtxEndcap) || (sd == _trkBarrel)) {
      return layer;
    } else {
      return layer/2;
    }
  }
  
  public Subdetector getSubdetector(Sensor sensor) {
	  // Replaced with lookup in Detector by system ID.  --JM
	  try {
		  IIdentifierHelper helper = sensor.getDetectorElement().getIdentifierHelper();
		  int sysid = helper.getValue(sensor.getDetectorElement().getIdentifier(), "system");	  
		  return detector.getSubdetector(sysid);
	  }
	  catch (Exception x)
	  {
		  throw new RuntimeException(x);
	  }
//    return sensor.getDetectorElement().findAncestors(SubdetectorDetectorElement.class).get(0).getSubdetector();
  }
  
  public int getSignZ(Sensor sensor) {
    double z = sensor.getDetectorElement().getGeometry().getPosition().z();
    if (z > Double.MIN_VALUE) {
      return 1;
    } else if (z < - Double.MIN_VALUE) {
      return -1;
    } else {
      return 0;
    }
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  private Subdetector _vtxBarrel;
  private Subdetector _vtxEndcap;
  private Subdetector _trkForward;
  private Subdetector _trkBarrel;
  private Subdetector _trkEndcap;
  private Detector detector;  
}
