package org.lcsim.recon.tracking.digitization.sisim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.event.SimTrackerHit;

/**
 * Map of sensors to list of hits.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class SensorHitMap extends HashMap<IDetectorElement, List<SimTrackerHit>> {

    static SensorHitMap create(List<SimTrackerHit> hits) {
        SensorHitMap hitMap = new SensorHitMap();
        for (SimTrackerHit hit : hits) {
            hitMap.getHits(hit.getDetectorElement()).add(hit);
        }
        return hitMap;
    }

    void addHits(List<SimTrackerHit> hits) {
        for (SimTrackerHit hit : hits) {
            getHits(hit.getDetectorElement()).add(hit);
        }
    }
    
    List<SimTrackerHit> getHits(IDetectorElement sensor) {
        if (get(sensor) == null) {
            put(sensor, new ArrayList<SimTrackerHit>());
        }
        return get(sensor);
    }    
}
