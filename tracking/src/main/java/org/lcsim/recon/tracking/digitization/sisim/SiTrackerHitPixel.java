/*
 * SiTrackerHitPixel.java
 *
 * Created on December 12, 2007, 10:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;
import java.util.List;
import org.lcsim.detector.ITransform3D;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.TrackerHit;

/**
 *
 * @author tknelson
 */
public class SiTrackerHitPixel extends SiTrackerHit
{
    
    /** Creates a new instance of SiTrackerHitPixel */
    public SiTrackerHitPixel(Hep3Vector position_vector, SymmetricMatrix covariance_matrix, double energy, double time, List<RawTrackerHit> raw_hits, TrackerHitType decoded_type)
    {
        super(position_vector, covariance_matrix, energy, time, raw_hits, decoded_type);
    }
    
    public SiTrackerHitPixel(TrackerHit hit)
    {
        super(hit);
    }
    
    public SiTrackerHitPixel(TrackerHit hit, TrackerHitType.CoordinateSystem coordinate_system)
    {
        super(hit,coordinate_system);
    }
    
    public SiTrackerHitPixel getTransformedHit(TrackerHitType.CoordinateSystem coordinate_system)
    {
        return new SiTrackerHitPixel(super.getTransformedHit(coordinate_system));
    }
    
    public SiTrackerHitPixel getTransformedHit(ITransform3D global_to_local)
    {
        return new SiTrackerHitPixel(super.getTransformedHit(global_to_local));
    }
    
}
