/*
 * SeedCandidate.java
 *
 * Created on August 3, 2007, 11:05 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Set;
import org.lcsim.event.MCParticle;
import org.lcsim.fit.helicaltrack.HelicalTrackFit;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.fit.helicaltrack.HelixParamCalculator;
import org.lcsim.fit.helicaltrack.MultipleScatter;

/**
 * The SeedCandidate class encapsulates information about a track seed as it
 * progresses throught the SeedTracker track finding algorithm.
 * @author Richard Partridge
 * @version 1.0
 */
public class SeedCandidate {
    
    private List<HelicalTrackHit> _hits;
    private HelicalTrackFit _helix;
    private double _bfield;
    private SeedStrategy _strategy;
    private Map<HelicalTrackHit, MultipleScatter> _msmap;
    private List<ScatterAngle> _scatters;
    private LinkedList<SeedLayer> _unchecked;
    private Set<MCParticle> _mcpset;
    private Set<Integer> _pdgset;
    private boolean _debug = false;
    
    /**
     * Create an empty SeedCandidate.
     */
    public SeedCandidate(SeedStrategy strategy, double bfield) {
        _strategy = strategy;
        _bfield = bfield;
        _hits = new LinkedList<HelicalTrackHit>();
        _msmap = new HashMap<HelicalTrackHit, MultipleScatter>();
        _mcpset = new HashSet<MCParticle>();
        _pdgset = new HashSet<Integer>();
     }
    
    /**
     * Create a new SeedCandidate from a list of hits.
     * @param hits list of hits for this SeedCandidate
     * @param strategy strategy used for this SeedCandidate
     */
    public SeedCandidate(List<HelicalTrackHit> hits, SeedStrategy strategy, double bfield) {
        this(strategy, bfield);
        _hits.addAll(hits);
        FindMCParticles();
    }
    
    /**
     * Create a new SeedCandidate from a list of hits and a helix.
     * @param hits list of hits for this SeedCandidate
     * @param strategy strategy used for this SeedCandidate
     * @param helix HelicalTrackFit associated with the SeedCandidate
     */
    public SeedCandidate(List<HelicalTrackHit> hits, SeedStrategy strategy, HelicalTrackFit helix, double bfield) {
        this(hits, strategy, bfield);
        _helix = helix;
    }

    public SeedCandidate(List<HelicalTrackHit> hits, SeedStrategy strategy) {
        this(hits, strategy, 0.);
    }

    /**
     * Creates a clone of an existing instance of SeedCandidate.
     * @param seed existing SeedCandidate to be cloned
     */
    public SeedCandidate(SeedCandidate seed) {
        this(seed._strategy, seed._bfield);
        _hits.addAll(seed.getHits());
        _helix = seed.getHelix();
        _msmap.putAll(seed.getMSMap());
        List<ScatterAngle> oldscat = seed.getScatterAngles();
        if (oldscat != null) _scatters = new ArrayList<ScatterAngle>(oldscat);
        setUncheckedLayers(seed.getUncheckedLayers());
        _mcpset.addAll(seed.getMCParticles());
        _pdgset.addAll(seed.getTruePDG());
    }
    
    /**
     * Assign a list of TrackerHits to the SeedCandidate.
     * @param hits list of hits for this SeedCandidate
     */
    public void setHits(List<HelicalTrackHit> hits) {
        _hits.clear();
        _hits.addAll(hits);
        FindMCParticles();
        return;
    }
    
    /**
     * Add a hit to the SeedCandidate.
     * @param hit TrackerHit to be added to the SeedCandidate
     */
    public void addHit(HelicalTrackHit hit) {
        //  If this is a new hit, add it to the list of hits and calculate the multiple scattering error
        if (!_hits.contains(hit)) {
            _hits.add(hit);
            UpdateMSMap(hit);

            //  If this is the first hit, find the associated MC
            if (_hits.size() == 1) {
                FindMCParticles();
            } else {
                CheckHit(hit);
            }
        }

        return;
    }
    
    /**
     * Return a list of hits associated with this SeedCandidate.
     * @return list of hits
     */
    public List<HelicalTrackHit> getHits() {
        return _hits;
    }
    
    /**
     * Set the SeedStrategy used to construct this SeedCandidate.
     * @param strategy strategy used to construct this SeedCandidate
     */
    
    public void setStrategy(SeedStrategy strategy) {
        _strategy = strategy;
        return;
    }
    
    /**
     * Return the SeedStrategy used to construct this SeedCandidate.
     * @return strategy used to construct this SeedCandidate
     */
    public SeedStrategy getSeedStrategy() {
        return _strategy;
    }
    
    /**
     * Associate a HelicalTrackFit with this SeedCandidate.
     * @param helix helix for this SeedCandidate
     */
    public void setHelix(HelicalTrackFit helix) {
        _helix = helix;
       return;
    }
    
    /**
     * Return the HelicalTrackFit associated with this SeedCandidate.
     * @return HelicalTrackFit associated with the SeedCandidate
     */
    public HelicalTrackFit getHelix() {
        return _helix;
    }
    
    public void setMSMap(Map<HelicalTrackHit, MultipleScatter> msmap) {
        _msmap = msmap;
        return;
    }

    public Map<HelicalTrackHit, MultipleScatter> getMSMap() {
        return _msmap;
    }
        
    public void setScatterAngles(List<ScatterAngle> scatters) {
        
        //  Save the list of MS scattering angles
        _scatters = scatters;
        
        //  Calculate the multiple scattering error for each hit
        for (HelicalTrackHit hit : _hits) {
            UpdateMSMap(hit);
        }
        
        return;
    }
    
    public List<ScatterAngle> getScatterAngles() {
        return _scatters;
    }

    public void setUncheckedLayers(List<SeedLayer> unchecked) {
        _unchecked = new LinkedList<SeedLayer>();
        if (unchecked != null) _unchecked.addAll(unchecked);
        return;
    }

    public LinkedList<SeedLayer> getUncheckedLayers() {
        return _unchecked;
    }

    public SeedLayer getNextLayer() {
        return _unchecked.poll();
    }

    public Set<MCParticle> getMCParticles() {
        return _mcpset;
    }

    public void setTruePDG(int pdgid) {
        _pdgset.add(pdgid);
    }
    
    public Set<Integer> getTruePDG() {
        return _pdgset;
    }

    public boolean isTrueSeed() {
        if (_mcpset.size() == 0) return false;
        if (_pdgset.size() == 0) return true;
        for (MCParticle mcp : _mcpset) {
            for (int truepdg : _pdgset) {
                if (mcp.getPDGID() == truepdg) return true;
            }
        }
        return false;
    }

    public double getBField() {
        return _bfield;
    }
    
    /*
     * Print information about this SeedCandidate
     */
    @Override
    public String toString() {
        String str = "SeedCandidate:\n";
        str += String.format("%s",this.getHelix().toString());
        List<HelicalTrackHit> hits = this.getHits();
        str += String.format("chi2=%f, strategy=%s and %d hits:\n",this.getHelix().chisqtot(),this.getSeedStrategy().getName(),hits.size());
        for (HelicalTrackHit hit : hits) {
            double drphi_ms = this.getMSMap().get(hit).drphi();
            double dz_ms = this.getMSMap().get(hit).dz();
            double dz = Math.sqrt(hit.getCorrectedCovMatrix().diagonal(2));
            str += String.format("Layer=%d c_pos=%s drphi=%f drphi_ms=%f dz=%f dz_ms=%f\n",hit.Layer(),hit.getCorrectedPosition().toString()
                                                                                  ,hit.drphi(),drphi_ms,dz,dz_ms);
        }
        return str;
    }
    
    private void UpdateMSMap(HelicalTrackHit hit) {
        if (_helix == null) return;
        if (_scatters == null) return;
        _msmap.put(hit, MultipleScattering.CalculateScatter(hit, _helix, _scatters));
        return;
    }

    private void FindMCParticles() {

        if (_hits.size() == 0) return;
        for (MCParticle mcp : _hits.get(0).getMCParticles()) {
            boolean good = true;
            for (HelicalTrackHit hit : _hits) {
                if (!hit.getMCParticles().contains(mcp)) {
                    good = false;
                    break;
                }
            }
            if (!good) continue;
            if (CheckMCParticle(mcp)) _mcpset.add(mcp);
        }

        return;
    }

    private boolean CheckMCParticle(MCParticle mcp) {

        //  Check that we have a strategy defined
        if (_strategy == null) return false;
        if(mcp==null)return false;
        //  Get the helix parameters for this MC Particle
        HelixParamCalculator mchelix = new HelixParamCalculator(mcp, _bfield);

        //  Check that the momentum of the MC particle is above the cut
        if (mchelix.getMCTransverseMomentum() < _strategy.getMinPT()) return false;

        //  Check the x-y impact parameter
        if (Math.abs(mchelix.getDCA()) > _strategy.getMaxDCA()) return false;

        //  Check the s-z impact parameter
        if (Math.abs(mchelix.getZ0()) > _strategy.getMaxZ0()) return false;

        return true;
    }

    private void CheckHit(HelicalTrackHit hit) {
        
        //  First check if we have any true MCParticles
        if (_mcpset.size() == 0) return;
        
        //  Get an iterator for the set so we can remove mcp's that don't match
        Iterator<MCParticle> iter = _mcpset.iterator();
        
        //  Loop over the MCParticles that make this a "true seed"
        while (iter.hasNext()) {
            MCParticle mcp = iter.next();
            
            //  Remove MCParticles from the true seed list if they aren't associated with this hit
            if (!hit.getMCParticles().contains(mcp)) iter.remove();
            }

        return;
    }
}