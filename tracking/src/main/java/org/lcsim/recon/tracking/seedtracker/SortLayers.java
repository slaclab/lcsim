/*
 * SortLayers.java
 *
 * Created on February 23, 2011, 17:05 PM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.lcsim.fit.helicaltrack.HelicalTrackHit;

/**
 * Comparator used to sort layers by numbers of hits in the layer in
 * ascending order (i.e., layer with fewest hits is first).
 *
 * @author Richard Partridge
 * @version 1.0
 */
public class SortLayers implements Comparator<SeedLayer> {

    private Map<SeedLayer, List<HelicalTrackHit>> _lyrmap;

    /**
     * SortLayers constructor.
     *
     * @param lyrmap map that stores list of hits keyed by layer
     */
    public SortLayers(Map<SeedLayer, List<HelicalTrackHit>> lyrmap) {

        _lyrmap = lyrmap;
    }

    /**
     * Comparison method that returns 1 if layer b has fewer hits than layer a.
     *
     * @param a
     * @param b
     * @return
     */
    public int compare(SeedLayer a, SeedLayer b) {
        int na = _lyrmap.get(a).size();
        int nb = _lyrmap.get(b).size();
        if (na > nb) return 1;
        if (na < nb) return -1;
        return 0;
    }
}