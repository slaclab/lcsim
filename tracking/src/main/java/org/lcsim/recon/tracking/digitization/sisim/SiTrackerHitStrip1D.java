/*
 * SiTrackerHitStrip1D.java
 *
 * Created on December 12, 2007, 10:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import java.util.List;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.solids.LineSegment3D;
import org.lcsim.detector.solids.Point3D;
import org.lcsim.detector.tracker.silicon.SiStrips;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.TrackerHit;

/**
 *
 * @author tknelson
 */
public class SiTrackerHitStrip1D extends SiTrackerHit
{
    private Hep3Vector _measured_coordinate;
    private Hep3Vector _unmeasured_coordinate;
    
    private LineSegment3D _hit_segment;
    
    /**
     * Creates a new instance of SiTrackerHitStrip1D
     */
    public SiTrackerHitStrip1D(Hep3Vector position_vector, SymmetricMatrix covariance_matrix, double energy, double time, List<RawTrackerHit> raw_hits, TrackerHitType decoded_type)
    {
        super(position_vector, covariance_matrix, energy, time, raw_hits, decoded_type);
    }
    
    public SiTrackerHitStrip1D(TrackerHit hit)
    {
        super(hit);
    }
    
    public SiTrackerHitStrip1D(TrackerHit hit, TrackerHitType.CoordinateSystem coordinate_system)
    {
        super(hit,coordinate_system);
    }
    
    public SiTrackerHitStrip1D getTransformedHit(TrackerHitType.CoordinateSystem coordinate_system)
    {
        return new SiTrackerHitStrip1D(super.getTransformedHit(coordinate_system));
    }
    
    public SiTrackerHitStrip1D getTransformedHit(ITransform3D global_to_local)
    {
        return new SiTrackerHitStrip1D(super.getTransformedHit(global_to_local));
    }
    
    // Access information specific to 1D strip hits
    public double getHitLength()
    {
        double hit_length = 0;
        for (RawTrackerHit raw_hit : getRawHits())
        {
            hit_length = Math.max( hit_length,
                    ((SiStrips)getReadoutElectrodes()).
                    getStripLength(getIdentifierHelper().getElectrodeValue(raw_hit.getIdentifier())) );
            
//            System.out.println("Strip length: "+((SiStrips)getReadoutElectrodes()).
//                    getStripLength(getIdentifierHelper().getElectrodeValue(raw_hit.getIdentifier())));
        }
//        System.out.println("    Hit length: "+hit_length);
        
        return hit_length;
    }
    
    public LineSegment3D getHitSegment()
    {
        if (_hit_segment == null)
        {
            Hep3Vector direction = getUnmeasuredCoordinate();
            double length = getHitLength();
            Point3D startpoint = new Point3D(VecOp.add(getPositionAsVector(),VecOp.mult(-length/2,direction)));
            _hit_segment = new LineSegment3D(startpoint,direction,length);
        }
        return _hit_segment;
    }
    
    public Hep3Vector getMeasuredCoordinate()
    {
        if (_measured_coordinate == null)
        {
            ITransform3D electrodes_to_global = getReadoutElectrodes().getLocalToGlobal();
            ITransform3D global_to_hit = getLocalToGlobal().inverse();
            ITransform3D electrodes_to_hit = Transform3D.multiply(global_to_hit,electrodes_to_global);
            
            _measured_coordinate = electrodes_to_hit.rotated(getReadoutElectrodes().getMeasuredCoordinate(0));
        }
        return _measured_coordinate;
    }
    
    public Hep3Vector getUnmeasuredCoordinate()
    {
        if (_unmeasured_coordinate == null)
        {
            ITransform3D electrodes_to_global = getReadoutElectrodes().getLocalToGlobal();
            ITransform3D global_to_hit = getLocalToGlobal().inverse();
            ITransform3D electrodes_to_hit = Transform3D.multiply(global_to_hit,electrodes_to_global);
            
            _unmeasured_coordinate = electrodes_to_hit.rotated(getReadoutElectrodes().getUnmeasuredCoordinate(0));
        }
        return _unmeasured_coordinate;
    }
    
}
