package org.lcsim.recon.tracking.vsegment.mctruth;

import java.util.*;

import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;
import org.lcsim.recon.tracking.vsegment.hitmaking.OldTrackerHit;

/**
 * An object of this class provides convenient access to Monte Carlo truth information.
 * In order for this object to be created, an instance of {@link MCTruthDriver} should
 * be added to the processing chain (usually as the first driver).
 *
 * @author D. Onoprienko
 * @version $Id: MCTruth.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class MCTruth {
  
// -- Constructors :  ----------------------------------------------------------
  
  MCTruth(MCTruthDriver mcTruthDriver, EventHeader event) {
    _mcDriver = mcTruthDriver;
    _event = event;
    _digiToSimGroup = new HashMap<DigiTrackerHit, SimGroup>();
    _missedSimGroups = new ArrayList<SimGroup>();
  }
  
// -- Getting SimTrackerHits from which the object was produced :  -------------
  
  /**
   * Returns a list of <tt>SimGroup</tt> objects containing <tt>SimTrackerHits</tt>
   * that contributed to the given <tt>DigiTrackerHit</tt>. 
   */
  public List<SimGroup> getSimGroups(DigiTrackerHit hit) {
    ArrayList<SimGroup> out = new ArrayList<SimGroup>(1);
    List<DigiTrackerHit> elHits= hit.getElementalHits();
    for (DigiTrackerHit elHit : elHits) {
      SimGroup group = _digiToSimGroup.get(elHit);
      if (group != null) out.add(group);
    }
    return out;
  }
  
  /**
   * Returns a list of <tt>SimGroup</tt> objects containing <tt>SimTrackerHits</tt>
   * that contributed to the given <tt>TrackerCluster</tt>. 
   */
  public List<SimGroup> getSimGroups(TrackerCluster cluster) {
    List<SimGroup> out = null;
    for (DigiTrackerHit hit : cluster.getDigiHits()) {
      if (out == null) {
        out = getSimGroups(hit);
      } else {
        out.addAll(getSimGroups(hit));
      }
    }
    return out;
  }
  
  /**
   * Returns a list of <tt>SimGroup</tt> objects containing <tt>SimTrackerHits</tt>
   * that contributed to the given <tt>TrackerHit</tt>. 
   */
  public List<SimGroup> getSimGroups(TrackerHit hit) {
    return getSimGroups(hit.getCluster());
  }
  
  /**
   * Returns a list of <tt>SimGroup</tt> objects containing <tt>SimTrackerHits</tt>
   * that contributed to the given hit. 
   */
  public List<SimGroup> getSimGroups(org.lcsim.event.TrackerHit oldHit) {
    if (oldHit instanceof OldTrackerHit) {
      List<SimGroup> out = null;
      for (TrackerCluster cluster : ((OldTrackerHit)oldHit).getClusters()) {
        if (out == null) {
          out = getSimGroups(cluster);
        } else {
          out.addAll(getSimGroups(cluster));
        }
      }
      return out;
    } else {
      throw new IllegalArgumentException();
    }
  }
  
  /**
   * Returns a collection of all <tt>SimGroup</tt> objects containing <tt>SimTrackerHits</tt>
   * that contributed to any <tt>DigiTrackerHits</tt>. 
   */
  public Collection<SimGroup> getSimGroups() {
    return _digiToSimGroup.values();
  }
  
  /**
   * Returns a collection of all <tt>SimGroup</tt> objects containing <tt>SimTrackerHits</tt>
   * that did not contribute to any <tt>DigiTrackerHits</tt>. 
   */
  public List<SimGroup> getMissedSimGroups() {
    return _missedSimGroups;
  }
  
// -- Getting MCParticles :  ---------------------------------------------------
  
  /** Returns <tt>MCParticle</tt> that produced <tt>SimTrackerHits</tt> in the given <tt>SimGroup</tt>. */
  public MCParticle getMCParticle(SimGroup simGroup) {
    return simGroup.getMCParticle();
  }
  
  /** Returns a list of <tt>MCParticles</tt> that contributed to the given <tt>DigiTrackerHit</tt>. */
  public List<MCParticle> getMCParticles(DigiTrackerHit hit) {
    ArrayList<MCParticle> out = new ArrayList<MCParticle>();
    for (DigiTrackerHit elementalHit : hit.getElementalHits()) {
      MCParticle mc = elementalHit.getMCParticle();
      if ((mc != null) && (! out.contains(mc))) out.add(mc);
    }
    out.trimToSize();
    return out;
  }
  
  /** Returns a list of <tt>MCParticles</tt> that contributed to the given <tt>TrackerCluster</tt>. */
  public List<MCParticle> getMCParticles(TrackerCluster cluster) {
    List<MCParticle> out = null;
    for (DigiTrackerHit hit : cluster.getDigiHits()) {
      if (out == null) {
        out = getMCParticles(hit);
      } else {
        for (MCParticle mc : getMCParticles(hit)) {
          if (! out.contains(mc)) out.add(mc);
        }
      }
    }
    return out;
  }
  
  /** Returns a list of <tt>MCParticles</tt> that contributed to the given <tt>TrackerHit</tt>. */
  public List<MCParticle> getMCParticles(TrackerHit hit) {
    return getMCParticles(hit.getCluster());
  }
  
  /** Returns a list of <tt>MCParticles</tt> that contributed to the given hit. */
  public List<MCParticle> getMCParticles(org.lcsim.event.TrackerHit oldHit) {
    if (oldHit instanceof OldTrackerHit) {
      List<MCParticle> out = null;
      for (TrackerCluster cluster : ((OldTrackerHit)oldHit).getClusters()) {
        if (out == null) {
          out = getMCParticles(cluster);
        } else {
          for (MCParticle mc : getMCParticles(cluster)) {
            if (! out.contains(mc)) out.add(mc);
          }
        }
      }
      return out;
    } else {
      throw new IllegalArgumentException();
    }
  }
  
// -- Looking up tracking objects by MCParticle :  -----------------------------

  /**
   * Returns a list of <tt>SimGroups</tt> associated with the given MCParticle, sorted by time.
   * The list returned by this method belongs to this <tt>MCTruth</tt> object,
   * clients should not modify it.
   */
  public List<SimGroup> getSimGroups(MCParticle mcParticle) {
    if (_mcToSimGroupList == null) {
      _mcToSimGroupList = new HashMap<MCParticle, ArrayList<SimGroup>>();
      for (SimGroup group : _digiToSimGroup.values()) {
        MCParticle mc = group.getMCParticle();
        ArrayList<SimGroup> groupList = _mcToSimGroupList.get(mc);
        if (groupList == null) {
          groupList = new ArrayList<SimGroup>();
          _mcToSimGroupList.put(mc, groupList);
        }
        groupList.add(group);
      }
      for (SimGroup group : _missedSimGroups) {
        MCParticle mc = group.getMCParticle();
        ArrayList<SimGroup> groupList = _mcToSimGroupList.get(mc);
        if (groupList == null) {
          groupList = new ArrayList<SimGroup>();
          _mcToSimGroupList.put(mc, groupList);
        }
        groupList.add(group);        
      }
      for (ArrayList<SimGroup> groupList : _mcToSimGroupList.values()) {
        Collections.sort(groupList, _compSimGroup);
        groupList.trimToSize();
      }
    }
    List<SimGroup> out = _mcToSimGroupList.get(mcParticle);
    if (out == null) out = Collections.emptyList();
    return out;
  }
  
  /**
   * Returns a list of <tt>TrackerClusters</tt> associated with the given MCParticle, sorted by time.
   * The list returned by this method belongs to this <tt>MCTruth</tt> object,
   * clients should not modify it.
   */
  public List<TrackerCluster> getTrackerClusters(MCParticle mcParticle) {
    if (_mcToTrackerCluster == null) {
      _mcToTrackerCluster = new HashMap<MCParticle, ArrayList<TrackerCluster>>();
      for (List<TrackerCluster> clusterList : _trackingClusters.values()) {
        for (TrackerCluster cluster : clusterList) {
          List<MCParticle> mcList = getMCParticles(cluster);
          for (MCParticle mc : mcList) {
            ArrayList<TrackerCluster> clusters = _mcToTrackerCluster.get(mc);
            if (clusters == null) {
              clusters = new ArrayList<TrackerCluster>();
              _mcToTrackerCluster.put(mc, clusters);
            }
            clusters.add(cluster);
          }
        }
      }
      for (ArrayList<TrackerCluster> clusters : _trackingClusters.values()) {
        Collections.sort(clusters, _compTrackerCluster);
        clusters.trimToSize();        
      }
    }
    List<TrackerCluster> out = _mcToTrackerCluster.get(mcParticle);
    if (out == null) out = Collections.emptyList();
    return out;
  }
  
// -- Stereo :  ----------------------------------------------------------------
  
  /**
   * Returns <tt>true</tt> if the hit supplied as an argument is a cross between hits
   * in stereo layer, and no MCParticle contributed to both of its parent clusters.
   */
  public boolean isGhost(OldTrackerHit hit) {
    if (! hit.isStereo()) return false;
    List<TrackerCluster> clusters = hit.getClusters();
    List<MCParticle> mcList1 = getMCParticles(clusters.get(0));
    List<MCParticle> mcList2 = getMCParticles(clusters.get(1));
    for (MCParticle mc : mcList1) {
      if (mcList2.contains(mc)) return false;
    }
    return true;
  }

// -- Modifiers :  -------------------------------------------------------------
  
  /**
   * This method is called by <tt>DigitizationDriver</tt> to create a new <tt>SimGroup</tt>
   * and store it in this <tt>MCTruth</tt> object.
   */
  public void addSimGroup(Collection<SimTrackerHit> simTrackerHits, Collection<DigiTrackerHit> digiTrackerHits) {
    SimGroup simGroup = new SimGroup(simTrackerHits, digiTrackerHits);
    if (digiTrackerHits.isEmpty()) {
      _missedSimGroups.add(simGroup);
    } else {
      for (DigiTrackerHit dHit : digiTrackerHits) {
        _digiToSimGroup.put(dHit, simGroup);
      }
    }
  }
  
  /** 
   * Called by <tt>ClusteringDriver</tt> to store a reference to the <tt>TrackerCluster</tt> map.
   */
  public void setTrackerClusters(HashMap<Sensor, ArrayList<TrackerCluster>> clusters) {
    _trackingClusters = clusters;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  private MCTruthDriver _mcDriver;
  private EventHeader _event;
  private HashMap<Sensor, ArrayList<TrackerCluster>> _trackingClusters;

  private HashMap<DigiTrackerHit, SimGroup> _digiToSimGroup;
  private ArrayList<SimGroup> _missedSimGroups;
  
  private HashMap<MCParticle, ArrayList<SimGroup>> _mcToSimGroupList;
  private HashMap<MCParticle, ArrayList<TrackerCluster>> _mcToTrackerCluster;
  
  private static Comparator<SimGroup> _compSimGroup = new Comparator<SimGroup>() {
    public int compare(SimGroup s1, SimGroup s2) {
      return (int)Math.signum(s1.getTime() - s2.getTime());
    }
  };
  private static Comparator<TrackerCluster> _compTrackerCluster = new Comparator<TrackerCluster>() {
    public int compare(TrackerCluster s1, TrackerCluster s2) {
      return (int)Math.signum(s1.getTime() - s2.getTime());
    }
  };
  
}
