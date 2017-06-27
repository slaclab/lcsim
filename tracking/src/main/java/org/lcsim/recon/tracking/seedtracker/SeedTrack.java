/*
 * SeedTrack.java
 *
 * Created on June 17, 2008, 11:34 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker;

import org.lcsim.event.base.BaseTrack;

/**
 * SeedTrack extends the BaseTrack class to methods to access the strategy
 * used for finding the track.
 * @author Richard Partridge
 * @version 1.0
 */
public class SeedTrack extends BaseTrack {
    private SeedStrategy _strategy;
    private SeedCandidate _seed;
    
    /**
     * Creates a new instance of SeedTrack.
     */
    public SeedTrack() {
        super();
    }
    
    /**
     * Set the strategy used for finding this track.
     * @param strategy strategy used to find this track
     */
    public void setStratetgy(SeedStrategy strategy) {
        _strategy = strategy;
        return;
    }
    
    /**
     * Return the strategy used to find this track.
     * @return strategy used to find the track
     */
    public SeedStrategy getStrategy() {
        return _strategy;
    }
    
    /**
     * Store the SeedCandidate that this track was constructed from.
     * @param seed seed candidate
     */
    public void setSeedCandidate(SeedCandidate seed) {
        _seed = seed;
        return;
    }
    
    /**
     * Return the SeedCandidate that this track was constructed from.
     * @return seed candidate
     */
    public SeedCandidate getSeedCandidate() {
        return _seed;
    }
}
