/*
 * SortHits.java
 *
 * Created on February 13, 2008, 12:55 PM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.Comparator;

import org.lcsim.fit.helicaltrack.HelicalTrackFit;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;

/**
 *
 * @author Richard Partridge
 * @version 1.0
 */
public class SortHits implements Comparator<HelicalTrackHit> {
    private double _xc;
    private double _yc;
    private double _R;
    private double _R2; 
    /** Creates a new instance of SortHits */
    public SortHits(HelicalTrackFit helix) {
        //  Find the helix center, radius, error
        _xc = helix.xc();
        _yc = helix.yc();
        _R = helix.R();
        _R2 = _R*_R; 
    }
    
    public int compare(HelicalTrackHit a, HelicalTrackHit b) {
        double xa = a.x();
        double xb = b.x();
        double ya = a.y();
        double yb = b.y();
        double difa = (xa - _xc)*(xa - _xc) + (ya - _yc)*(ya - _yc) - _R2;
        double difb = (xb - _xc)*(xb - _xc) + (yb - _yc)*(yb - _yc) - _R2;
        if (difa * difa > difb * difb) return 1;
        if (difa * difa < difb * difb) return -1;
        return 0;
    }
}