package org.lcsim.recon.tracking.vsegment.hitmaking;

import java.util.*;

import org.lcsim.event.EventHeader;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.util.Driver;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;

import static org.lcsim.recon.tracking.vsegment.hitmaking.HitMakingDriver.UsedClusters;

/**
 * Driver that that constructs {@link TrackerHit} collection given {@link TrackerCluster}
 * collection. 
 *
 * @author D. Onoprienko
 * @version $Id: HitMakingDriver.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class HitMakingDriver extends Driver {
  
// -- Constructors :  ----------------------------------------------------------
  
  public HitMakingDriver(TrackerHitMaker hitMaker) {
    _clusterMapName = "INPUT_MAP_NAME";
    _hitMapName= "OUTPUT_MAP_NAME";
    _hitMaker = hitMaker;
    _registerHitsWithClusters = false;
    _usedClusters = UsedClusters.INCLUDE_NEW;
    
  }

// -- Setters :  ---------------------------------------------------------------

  /**
   * Set any <tt>String</tt> parameter. 
   * The following parameters can be set with this method:
   * <p><dl>
   * <dt>"INPUT_MAP_NAME"</dt> <dd>Name of input collection of tracker clusters
   *       (type <tt>HashMap&lt;Sensor, ArrayList&lt;TrackerCluster&gt;&gt;</tt>). 
   *       <br>Default: "TrackerClusters".</dd>
   * <dt>"OUTPUT_MAP_NAME"</dt> <dd>Name of output collection of tracker hits
   *       (type <tt>HashMap&lt;Sensor, ArrayList&lt;TrackerHit&gt;&gt;</tt>). 
   *       <br>Default: "TrackerHits".</dd>
   * <dt>"USED_CLUSTERS"</dt> <dd>Tell the driver what to do with <tt>Clusters</tt>
   *       that already have <tt>TYrackerHits</tt> associated with them. Possible values:
   *           <tt>"SKIP"</tt> - ignore used clusters;
   *           <tt>"INCLUDE_OLD"</tt> - include existing <tt>TrackerHits</tt> associated with
   *                                    used clusters into the output collection;
   *           <tt>"INCLUDE_NEW"</tt> - produce new <tt>TrackerHits</tt> from used clusters,
   *                                    and include them into output collection.
   *           <br>Default: "INCLUDE_NEW".</dd></dl>
   * 
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, String value) {
    if (name.equalsIgnoreCase("INPUT_MAP_NAME")) {
      _clusterMapName = value;
    } else if (name.equalsIgnoreCase("OUTPUT_MAP_NAME")) {
      _hitMapName = value;
    } else if (name.equalsIgnoreCase("USED_CLUSTERS")) {
      if (value.equalsIgnoreCase("SKIP")) {
        _usedClusters = UsedClusters.SKIP;
      } else if (value.equalsIgnoreCase("INCLUDE_OLD")) {
        _usedClusters = UsedClusters.INCLUDE_OLD;
      } else if (value.equalsIgnoreCase("INCLUDE_NEW")) {
        _usedClusters = UsedClusters.INCLUDE_NEW;
      } else if (value.equalsIgnoreCase("UPDATE_SKIP")) {
        _usedClusters = UsedClusters.UPDATE_SKIP;
        throw new IllegalArgumentException("Not yet implemented: set(" + name + ", " + value + ")");
      } else if (value.equalsIgnoreCase("UPDATE_INCLUDE")) {
        _usedClusters = UsedClusters.UPDATE_INCLUDE;
        throw new IllegalArgumentException("Not yet implemented: set(" + name + ", " + value + ")");
      } else {
        throw new IllegalArgumentException("Illegal value: set(" + name + ", " + value + ")");
      }
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }

  /**
   * Set any <tt>boolean</tt> parameter. 
   * The following parameters can be set with this method:
   * <p><dl>
   * <dt>"REGISTER_HITS_WITH_CLUSTERS"</dt> <dd>If set to <tt>true</tt>, newly created
   *           tracker hits will be registered with clusters from which they were 
   *           produced, and can be later accessed through a call to 
   *           {@link TrackerCluster#getTrackerHits} method.
   *           <br>Default: <tt>false</tt>.</dd></dl>
   * 
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, boolean value) {
    if (name.equalsIgnoreCase("REGISTER_HITS_WITH_CLUSTERS")) {
      _registerHitsWithClusters = value;
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
// -- Event processing :  ------------------------------------------------------
  
  public void process(EventHeader event) {
    
//    System.out.println(" ");
//    System.out.println("Starting TrackerHit making");
    
    super.process(event);
    
    HashMap<Sensor, ArrayList<TrackerCluster>> clusterMap = 
            (HashMap<Sensor, ArrayList<TrackerCluster>>) event.get(_clusterMapName);
    HashMap<Sensor, ArrayList<TrackerHit>> hitMap = new HashMap<Sensor, ArrayList<TrackerHit>>();

    for (Sensor sensor : clusterMap.keySet()) {
      List<TrackerCluster> clusterList = clusterMap.get(sensor);
      ArrayList<TrackerHit> hitList = new ArrayList<TrackerHit>(clusterList.size());
      for (TrackerCluster cluster : clusterList) {
        List<TrackerHit> oldHits = cluster.getTrackerHits();
        if (oldHits.isEmpty() || _usedClusters == UsedClusters.INCLUDE_NEW) {
          TrackerHit hit = _hitMaker.make(cluster);
          hitList.add(hit);
          if (_registerHitsWithClusters) {
            cluster.addTrackerHit(hit);
          }
        } else if (_usedClusters == UsedClusters.INCLUDE_OLD) {
          hitList.addAll(oldHits);
        }
      }
      if (! hitList.isEmpty()) {
        hitList.trimToSize();
        hitMap.put(sensor, hitList);
      }
    }

    event.put(_hitMapName, hitMap);
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  private String _clusterMapName;
  private String _hitMapName;
  
  protected boolean _registerHitsWithClusters;
  
  protected TrackerHitMaker _hitMaker;
  
  protected enum UsedClusters {SKIP, INCLUDE_OLD, INCLUDE_NEW, UPDATE_SKIP, UPDATE_INCLUDE}
  protected UsedClusters _usedClusters;
}
