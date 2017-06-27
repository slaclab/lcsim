package org.lcsim.recon.tracking.vsegment;

import java.util.*;

import hep.aida.*;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.units.clhep.SystemOfUnits;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import org.lcsim.recon.tracking.vsegment.clustering.ClusteringDriver;
import org.lcsim.recon.tracking.vsegment.clustering.clusterers.NearestNeighborClusterer;
import org.lcsim.recon.tracking.vsegment.digitization.SimToDigiDriver;
import org.lcsim.recon.tracking.vsegment.digitization.SimToDigiConverter;
import org.lcsim.recon.tracking.vsegment.digitization.algorithms.ConverterSimple;
import org.lcsim.recon.tracking.vsegment.geom.SegmentationManager;
import org.lcsim.recon.tracking.vsegment.geom.Segmenter;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;
import org.lcsim.recon.tracking.vsegment.hitmaking.HitMakingDriver;
import org.lcsim.recon.tracking.vsegment.hitmaking.TrackerHitConverter;
import org.lcsim.recon.tracking.vsegment.hitmaking.TrackerHitMaker;
import org.lcsim.recon.tracking.vsegment.hitmaking.hitmakers.TrackerHitMakerBasic;
import org.lcsim.recon.tracking.vsegment.mctruth.MCTruth;
import org.lcsim.recon.tracking.vsegment.mctruth.MCTruthDriver;
import org.lcsim.recon.tracking.vsegment.mctruth.SimGroup;

/**
 * An Example of how to define virtual segmentation of the tracker, 
 * and use {@link TrackerHitConverter} to create
 * "standard" <tt>org.lcsim.event.TrackerHits</tt>.
 * Parameters are chosen to define reasonable segmentation for SiD01.
 *
 * @author D. Onoprienko
 * @version $Id: ExampleDriver2.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class ExampleDriver2 extends Driver {
  
// -- Constructors :  ----------------------------------------------------------
  
  public ExampleDriver2() {

    add(new MCTruthDriver());
    
    // Segmentation description :
    
    Segmenter segmenter = new ExampleSegmenter1();
    SegmentationManager segMan = new SegmentationManager(segmenter);
    SegmentationManager.setDefaultInstance(segMan);
    add(segMan);
    
    // Digitization :

    SimToDigiConverter converter = new ConverterSimple();
    SimToDigiDriver conversionDriver = new SimToDigiDriver(converter);
    conversionDriver.set("OUTPUT_MAP_NAME", "DigiTrackerHits");
    add(conversionDriver);

    // Clustering :
    
    ClusteringDriver clusteringDriver = new ClusteringDriver(new NearestNeighborClusterer());
    clusteringDriver.set("INPUT_MAP_NAME","DigiTrackerHits");
    clusteringDriver.set("OUTPUT_MAP_NAME","TrackerClusters");
    add(clusteringDriver);
    
    // Conversion to "standard" org.lcsim.event.TrackerHit :  ------------------
    
    TrackerHitConverter hitConverter = new TrackerHitConverter();
    hitConverter.set("INPUT_CLUSTER_MAP_NAME", "TrackerClusters");
    hitConverter.set("OUTPUT_HIT_LIST_NAME", "StandardTrackerHits");
    add(hitConverter);
  }
  
  
// -- Event processing :  ------------------------------------------------------

  public void process(EventHeader event) {
    
    System.out.println(" ");
    System.out.println("Event "+event.getEventNumber());

    super.process(event);
    
    MCTruth mcTruth = (MCTruth) event.get("MCTruth");
    
    System.out.println(" ");
    
    int n = 0;
    HashMap<Sensor, List<DigiTrackerHit>> digiMap = 
            (HashMap<Sensor, List<DigiTrackerHit>>) event.get("DigiTrackerHits");
    for (List<DigiTrackerHit> digiList : digiMap.values()) n += digiList.size();
    System.out.println("Created " + n + " DigiTrackerHits on " + digiMap.keySet().size() + " sensors");
    
    n = 0;
    HashMap<Sensor, List<TrackerCluster>> clusterMap = 
            (HashMap<Sensor, List<TrackerCluster>>) event.get("TrackerClusters");
    for (List<TrackerCluster> clusterList : clusterMap.values()) n += clusterList.size();
    System.out.println("Created " + n + " TrackerClusters on " + clusterMap.keySet().size() + " sensors");
    
    n = 0;
    List<org.lcsim.event.TrackerHit> oldHitList = (List<org.lcsim.event.TrackerHit>) event.get("StandardTrackerHits");
    System.out.println("Created " + oldHitList.size() + " org.lcsim.event.TrackerHits");
  }

}
