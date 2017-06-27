/*
 * SiTrackerHit.java
 *
 * Created on November 19, 2007, 11:34 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;
import java.util.List;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.tracker.silicon.ChargeCarrier;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.detector.tracker.silicon.SiTrackerIdentifierHelper;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.TrackerHit;

/**
 *
 * @author tknelson
 */
public class SiTrackerHit extends TransformableTrackerHit
{
    
    // Cached derived quantities
    private SiSensorElectrodes _electrodes;
    
    /** Creates a new instance of SiTrackerHit */
    public SiTrackerHit(Hep3Vector position_vector, SymmetricMatrix covariance_matrix, double energy, double time, List<RawTrackerHit> raw_hits, TrackerHitType decoded_type)
    {
        super(position_vector, covariance_matrix, energy, time, raw_hits, decoded_type);
    }
    
    public SiTrackerHit(TrackerHit hit)
    {
        super(hit);
    }
    
    public SiTrackerHit(TrackerHit hit, TrackerHitType.CoordinateSystem coordinate_system)
    {
        super(hit,coordinate_system);
    }
    
    public SiTrackerHit getTransformedHit(TrackerHitType.CoordinateSystem coordinate_system)
    {
        return new SiTrackerHit(super.getTransformedHit(coordinate_system));
    }
    
    public SiTrackerHit getTransformedHit(ITransform3D global_to_local)
    {
        return new SiTrackerHit(super.getTransformedHit(global_to_local));
    }
    
    // Additional information
    public SiSensor getSensor()
    {
        return (SiSensor)super.getSensor();
    }
    
    public SiSensorElectrodes getReadoutElectrodes()
    {
        if (_electrodes == null)
        {
            RawTrackerHit raw_hit = this.getRawHits().get(0);
            SiTrackerIdentifierHelper id_helper = getIdentifierHelper();
            _electrodes = getSensor().getReadoutElectrodes(ChargeCarrier.getCarrier(id_helper.getSideValue(raw_hit.getIdentifier())));
        }
        return _electrodes;
    }
    
    public SiTrackerIdentifierHelper getIdentifierHelper()
    {
        return (SiTrackerIdentifierHelper)super.getIdentifierHelper();
    }
    
}
