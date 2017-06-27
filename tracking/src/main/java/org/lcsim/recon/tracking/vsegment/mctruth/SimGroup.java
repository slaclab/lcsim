package org.lcsim.recon.tracking.vsegment.mctruth;

import java.util.*;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;

import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;

/**
 * Group of {@link SimTrackerHit} objects created in a single sensor-particle crossing.
 *
 * @author D. Onoprienko
 * @version $Id: SimGroup.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class SimGroup {
  
// -- Constructors :  ----------------------------------------------------------
  
  public SimGroup(Collection<SimTrackerHit> simTrackerHits, Collection<DigiTrackerHit> digiTrackerHits) {
    _simList = new ArrayList<SimTrackerHit>(simTrackerHits.size());
    _simList.addAll(simTrackerHits);
    Collections.sort(_simList, _compSimHit);
    _digiList = new ArrayList<DigiTrackerHit>(digiTrackerHits.size());
    _digiList.addAll(digiTrackerHits);
    Collections.sort(_digiList);
  }
  
// -- Getters :  ---------------------------------------------------------------
  
  /** Returns <tt>true</tt> if this group contains the given <tt>SimTrackerHit</tt>. */
  public boolean contains(SimTrackerHit hit) {return _simList.contains(hit);}
  
  /** Returns <tt>true</tt> if the diven elemental <tt>DigiTrackerHit</tt> was produced from this group. */
  public boolean contains(DigiTrackerHit hit) {return _digiList.contains(hit);}
  
  /** Returns an average energy deposition weighted position of <tt>SimTrackerHits</tt> in this group. */
  public Hep3Vector getPosition() {
    double[] pos = {0.,0.,0.};
    double sig = 0.;
    double totalSig = getSignal();
    for (SimTrackerHit hit : _simList) {
      double[] p = hit.getPoint();
      double s = (totalSig > Float.MIN_VALUE) ? hit.getdEdx() : 1.;
      for (int i=0; i<3; i++) pos[i] += p[i]*s;
      sig += s;
    }
    return new BasicHep3Vector(pos[0]/sig, pos[1]/sig, pos[2]/sig);
  }
  
  /** Returns combined energy deposition of all <tt>SimTrackerHits</tt> in this group. */
  public double getSignal() {
    double sig = 0.;
    for (SimTrackerHit hit : _simList) {
      sig += hit.getdEdx();
    }
    return sig;
  }
  
  /** Returns average time for <tt>SimTrackerHits</tt> in this group. */
  public double getTime() {
    double time = 0.;
    for (SimTrackerHit hit : _simList) {
      time += hit.getTime();
    }
    time /= _simList.size();
    return time;
  }
  
  /** Returns <tt>MCParticle</tt> that produced <tt>SimTrackerHits</tt> in this group. */
  public MCParticle getMCParticle() {
    return _simList.get(0).getMCParticle();
  }
  
  /** Returns a list of <tt>SimTrackerHits</tt> in this group. */
  public List<SimTrackerHit> getSimTrackerHits() {return _simList;}
  
  /** Returns a list of elemental <tt>DigiTrackerHit</tt> produced from <tt>SimTrackerHits</tt> in this group. */
  public List<DigiTrackerHit> getDigiTrackerHits() {return _digiList;}
  
// -- Private parts :  ---------------------------------------------------------
  
  ArrayList<SimTrackerHit> _simList;
  ArrayList<DigiTrackerHit> _digiList;
  
  static Comparator<SimTrackerHit> _compSimHit = new Comparator<SimTrackerHit>() {
    public int compare(SimTrackerHit s1, SimTrackerHit s2) {
      return (int)Math.signum(s1.getTime() - s2.getTime());
    }
  };
}
