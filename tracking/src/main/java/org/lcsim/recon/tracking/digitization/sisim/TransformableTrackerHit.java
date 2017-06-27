/*
 * TransformableTrackerHit.java
 *
 * Created on December 4, 2007, 11:05 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.util.List;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.Transform3D;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.TrackerHit;

/**
 *
 * @author tknelson
 */
public class TransformableTrackerHit extends BaseTrackerHit
{
    // Elements of TrackerHit interface
    private TrackerHitType _decoded_type;
    
    private ITransform3D _local_to_global; // Key element that goes beyond TrackerHit in functionality
    
    // Cached derived quantities
    private TrackerHitType.CoordinateSystem _coordinate_system;
    private TrackerHitType.MeasurementType _measurement_type;    
    
    /**
     * Creates a new instance of TransformableTrackerHit
     */
    
    // Basic constructor
    public TransformableTrackerHit(Hep3Vector position_vector, SymmetricMatrix covariance_matrix, double energy, double time, List<RawTrackerHit> raw_hits, TrackerHitType decoded_type)
    {
        super(position_vector,covariance_matrix,energy,time,raw_hits,TrackerHitType.encoded(decoded_type));
        
        _decoded_type = decoded_type;
        
        if (getCoordinateSystem() == TrackerHitType.CoordinateSystem.GLOBAL)
        {
            _local_to_global = new Transform3D();
        }
        else if (getCoordinateSystem() == TrackerHitType.CoordinateSystem.SENSOR)
        {
            _local_to_global = getSensor().getGeometry().getLocalToGlobal();
        }
        else
        {
            throw new RuntimeException("Cannot instantiate a BaseTrackerHit object with unknown coordinates!");
        }
    }
    
    // Construct from TrackerHit
    public TransformableTrackerHit(TrackerHit hit)
    {
        this(
                new BasicHep3Vector(hit.getPosition()[0],hit.getPosition()[1],hit.getPosition()[2]),
                new SymmetricMatrix( 3, hit.getCovMatrix(), true),
                hit.getdEdx(),
                hit.getTime(),
                hit.getRawHits(),
                TrackerHitType.decoded(hit.getType())
                );
    }
    
    // Construct from TrackerHit and specified coordinate system
    public TransformableTrackerHit(TrackerHit hit, TrackerHitType.CoordinateSystem coordinate_system)
    {
        this(hit);
        
        // Change coordinates of hit
        ITransform3D global_to_local = getGlobalToHit(coordinate_system);        
        ITransform3D hit_to_local = Transform3D.multiply(global_to_local,_local_to_global);  // transformation to apply
        
        _position_vector = hit_to_local.transformed(_position_vector);
        _covariance_matrix = hit_to_local.transformed(_covariance_matrix);
        _local_to_global = global_to_local.inverse(); // new coordinates of hit
        _decoded_type = new TrackerHitType(coordinate_system,getMeasurementType()); // change coordinate system
    }
    
    // produce transformed copies of current hit
    //===========================================
    
    // This can put any TransformableTrackerHit back into standard coordinates
    public TransformableTrackerHit getTransformedHit(TrackerHitType.CoordinateSystem coordinate_system)
    {
        // Change coordinates of hit
        ITransform3D global_to_local = getGlobalToHit(coordinate_system);        
        ITransform3D hit_to_local = Transform3D.multiply(global_to_local,_local_to_global);  // transformation to apply
        
        Hep3Vector position_vector = hit_to_local.transformed(_position_vector);
        SymmetricMatrix covariance_matrix = hit_to_local.transformed(_covariance_matrix);
        double energy = getdEdx();
        double time = getTime();
        List<RawTrackerHit> raw_hits = getRawHits();
        TrackerHitType decoded_type = new TrackerHitType(coordinate_system,getMeasurementType()); // change coordinate system
        
        return new TransformableTrackerHit(position_vector,covariance_matrix,energy,time,raw_hits,decoded_type);
    }
    
    // This can put any TransformableTrackerHit into any coordinates.  Such hits are not persistable
    public TransformableTrackerHit getTransformedHit(ITransform3D global_to_local)
    {
        // Change coordinates of hit
        ITransform3D hit_to_local = Transform3D.multiply(global_to_local,_local_to_global);  // transformation to apply
        
        Hep3Vector position_vector = hit_to_local.transformed(_position_vector);
        SymmetricMatrix covariance_matrix = hit_to_local.transformed(_covariance_matrix);
        double energy = getdEdx();
        double time = getTime();
        List<RawTrackerHit> raw_hits = getRawHits();
        
        ITransform3D local_to_global = global_to_local.inverse(); // new coordinates of hit

        TrackerHitType dummy_type = new TrackerHitType(TrackerHitType.CoordinateSystem.GLOBAL,getMeasurementType()); // dummy type to allow construciton
        TrackerHitType decoded_type = new TrackerHitType(TrackerHitType.CoordinateSystem.UNKNOWN,getMeasurementType()); // real coordinate system is unknown
        
        // Make a new TransformableTrackerHit with dummy CoordinateSystem
        TransformableTrackerHit new_hit = new TransformableTrackerHit(position_vector,covariance_matrix,energy,time,raw_hits,dummy_type);
        
        // Set correct information about coordinate system
        new_hit._decoded_type = decoded_type;
        new_hit._local_to_global = local_to_global;
        
        return new_hit;
    }

    // Additional information for hits
    //=================================
    public boolean isPersistable()
    {
        return (this.getCoordinateSystem() != TrackerHitType.CoordinateSystem.UNKNOWN);
    }
    
    public ITransform3D getLocalToGlobal()
    {
        return _local_to_global;
    }
    
    public TrackerHitType.CoordinateSystem getCoordinateSystem()
    {
        if (_coordinate_system == null)
        {
            _coordinate_system = _decoded_type.getCoordinateSystem();
        }
        return _coordinate_system;
    }
    
    public TrackerHitType.MeasurementType getMeasurementType()
    {
        if (_measurement_type == null)
        {
            _measurement_type = _decoded_type.getMeasurementType();
        }
        return _measurement_type;
    }
    
    // Private - Get transform for hit given coordinate system type
    private ITransform3D getGlobalToHit(TrackerHitType.CoordinateSystem coordinate_system)
    {
        if (coordinate_system == TrackerHitType.CoordinateSystem.GLOBAL)
        {
            return new Transform3D();
        }
        else if (coordinate_system == TrackerHitType.CoordinateSystem.SENSOR)
        {
            return ((List<RawTrackerHit>)getRawHits()).get(0).getDetectorElement().getGeometry().getGlobalToLocal();
        }
        else
        {
            throw new RuntimeException("Cannot determine Transform3D to UNKNOWN coordinates!");
        }
    }
    
}

