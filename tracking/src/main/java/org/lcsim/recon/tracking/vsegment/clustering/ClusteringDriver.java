package org.lcsim.recon.tracking.vsegment.clustering;

import java.util.*;

import org.lcsim.event.EventHeader;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.util.Driver;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.base.TrackerClusterBasic;
import org.lcsim.recon.tracking.vsegment.mctruth.MCTruth;

/**
 * Driver that handles clustering of {@link DigiTrackerHit} objects.
 * The driver fetches a map of digitized hits from the event, and 
 * creates a map of clusters. 
 *
 * @author D.Onoprienko
 * @version $Id: ClusteringDriver.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class ClusteringDriver extends Driver {

// -- Constructors :  ----------------------------------------------------------
  
  public ClusteringDriver(Clusterer clusterer) {
    _clusterer = clusterer;
    _inMapName = "DigiTrackerHits";
    _outMapName = "TrackerClusters";
  }

// -- Setters :  ---------------------------------------------------------------

  /**
   * Set any <tt>String</tt> parameter. 
   * The following parameters can be set with this method:
   * <dl>
   * <dt>"INPUT_MAP_NAME"</dt> <dd>Name of input collection of digitized hits
   *                 (type <tt>HashMap<Sensor, ArrayList<DigiTrackerHit>></tt>). 
   *                 Default: "DigiTrackerHit".</dd>
   * <dt>"OUTPUT_MAP_NAME"</dt> <dd>Name of output collection of clusters
   *             (type <tt>HashMap<Sensor, ArrayList<TrackerCluster>></tt>). 
   *             Default: "TrackerCluster".</dd></dl>
   * 
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, String value) {
    if (name.equalsIgnoreCase("INPUT_MAP_NAME")) {
      _inMapName = value;
    } else if (name.equalsIgnoreCase("OUTPUT_MAP_NAME")) {
      _outMapName = value;
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
// -- Event processing :  ------------------------------------------------------
  
  public void process(EventHeader event) {
    
//    System.out.println(" ");
//    System.out.println("Starting clustering");
    
    super.process(event);
        
    HashMap<Sensor, ArrayList<DigiTrackerHit>> inMap = (HashMap<Sensor, ArrayList<DigiTrackerHit>>) event.get(_inMapName);
    HashMap<Sensor, ArrayList<TrackerCluster>> outMap = new HashMap<Sensor, ArrayList<TrackerCluster>>();

    for (Sensor sensor : inMap.keySet()) {
      ArrayList<TrackerCluster> clusterList = _clusterer.findClusters(sensor, inMap.get(sensor));
      if ( ! clusterList.isEmpty()) outMap.put(sensor, clusterList);
    }
    
    MCTruth mcTruth = null;
    try {
      mcTruth = (MCTruth) event.get("MCTruth");
    } catch (IllegalArgumentException x) {}
    if (mcTruth != null) mcTruth.setTrackerClusters(outMap);
    
    event.put(_outMapName, outMap);
  }

// -- Private parts :  ---------------------------------------------------------
  
  protected String _inMapName;
  protected String _outMapName;
  protected Clusterer _clusterer;
}
