package org.lcsim.recon.tracking.digitization;

import java.util.HashMap;
import java.util.List;

import org.lcsim.detector.IReadout;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.event.SimTrackerHit;

public class SimTrackerHitReadoutMap extends HashMap<IReadout, List<SimTrackerHit>> {
    
    public List<SimTrackerHit> getHits(SiSensor sensor) {
        return this.get(sensor.getReadout());
    }
}
