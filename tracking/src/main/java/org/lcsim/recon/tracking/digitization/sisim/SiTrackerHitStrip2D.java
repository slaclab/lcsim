/*
 * SiTrackerHitStrip2D.java
 *
 * Created on December 12, 2007, 10:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.solids.GeomOp3D;
import org.lcsim.detector.solids.LineSegment3D;
import org.lcsim.detector.solids.Plane3D;
import org.lcsim.detector.solids.Point3D;
import org.lcsim.detector.tracker.silicon.SiTrackerModule;
import org.lcsim.event.MCParticle;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.RelationalTable;
import org.lcsim.event.TrackerHit;

/**
 *
 * @author tknelson
 */
public class SiTrackerHitStrip2D extends SiTrackerHit
{
    
    List<SiTrackerHitStrip1D> _hits_1D;
    List<SiTrackerHitStrip1D> _hits_1D_transformed;
    
    /**
     * Creates a new instance of SiTrackerHitStrip2D
     */
    
    // For creating fresh hits
    public SiTrackerHitStrip2D(SymmetricMatrix covariance_matrix, double energy, double time,
            List<RawTrackerHit> raw_hits, TrackerHitType decoded_type, List<SiTrackerHitStrip1D> hits_1D)
    {
        super(new BasicHep3Vector(0,0,0), covariance_matrix, energy, time, raw_hits, decoded_type);
        _hits_1D = hits_1D;
    }
    
    
    // For creating transformed versions of hits
    public SiTrackerHitStrip2D(TrackerHit hit, List<SiTrackerHitStrip1D> hits_1D)
    {
        super(hit);
        _hits_1D = hits_1D;
    }
    
    // For creating SiTrackerStrip2D from persisted TrackerHits
    public SiTrackerHitStrip2D(TrackerHit hit, RelationalTable hits_1D)
    {
        super(hit);
        _hits_1D = new ArrayList((Set<SiTrackerHitStrip1D>)hits_1D.allTo(hit));
    }
    
    public SiTrackerHitStrip2D(TrackerHit hit, RelationalTable hits_1D, TrackerHitType.CoordinateSystem coordinate_system)
    {
        super(hit,coordinate_system);
        _hits_1D = new ArrayList((Set<SiTrackerHitStrip1D>)hits_1D.allTo(hit));
    }
    
    // Get transformed version of hits
    public SiTrackerHitStrip2D getTransformedHit(TrackerHitType.CoordinateSystem coordinate_system)
    {
        return new SiTrackerHitStrip2D(super.getTransformedHit(coordinate_system),getHits1D());
    }
    
    public SiTrackerHitStrip2D getTransformedHit(ITransform3D global_to_local)
    {
        return new SiTrackerHitStrip2D(super.getTransformedHit(global_to_local),getHits1D());
    }
    
    // Methods specific to 2d hits
    public double[] getPosition()
    {
        return getPositionAsVector().v();
    }
    
    public Hep3Vector getPositionAsVector()
    {
        // get IP (global origin) in local coordinates - could be more clever
        Point3D origin = new Point3D(getLocalToGlobal().inverse().transformed(new BasicHep3Vector(0,0,0)));
        return getPositionWithLineFrom(origin);
    }

    // Get the module
    public SiTrackerModule getModule()
    {
        return (SiTrackerModule)getSensor().getParent();
    }
    
    // Get position referred to specific tracks: input and return parameters are in current coordinates of the hit!
    // Get position if hit is due to track of infinite momentum originating at some point in current coordinates
    public Hep3Vector getPositionWithLineFrom(Point3D origin)
    {
        List<Point3D> plane_points = new ArrayList<Point3D>();
        plane_points.add(origin);
        plane_points.addAll(getHits1DTransformed().get(0).getHitSegment().getPoints());
        Plane3D plane = new Plane3D(plane_points);
        
        Point3D start_point = GeomOp3D.intersection(getHits1DTransformed().get(1).getHitSegment(),plane);
        
        plane_points.clear();
        plane_points.add(origin);
        plane_points.addAll(getHits1DTransformed().get(1).getHitSegment().getPoints());
        plane = new Plane3D(plane_points);
        
        Point3D end_point = GeomOp3D.intersection(getHits1DTransformed().get(0).getHitSegment(),plane);
        
        LineSegment3D hitsegment_2d = new LineSegment3D(start_point,end_point);
        
        return hitsegment_2d.getEndPoint(hitsegment_2d.getLength()/2);
    }
    
    // Get position if hit is due to track crossing in a given direction (momentum vector)
    public Hep3Vector getPositionWithDirection(Hep3Vector direction)
    {
        Hep3Vector normal = VecOp.cross(direction,getHits1DTransformed().get(0).getUnmeasuredCoordinate());
        Point3D hitpoint = new Point3D(getHits1DTransformed().get(0).getPositionAsVector());
        Plane3D plane = new Plane3D(normal,hitpoint);
        
        Point3D start_point = GeomOp3D.intersection(getHits1DTransformed().get(1).getHitSegment(),plane);
        
        normal = VecOp.cross(direction,getHits1DTransformed().get(1).getUnmeasuredCoordinate());
        hitpoint = new Point3D(getHits1DTransformed().get(1).getPositionAsVector());
        plane = new Plane3D(normal,hitpoint);
        
        Point3D end_point = GeomOp3D.intersection(getHits1DTransformed().get(0).getHitSegment(),plane);
        
        LineSegment3D hitsegment_2d = new LineSegment3D(start_point,end_point);
        
        return hitsegment_2d.getEndPoint(hitsegment_2d.getLength()/2);
    }
    
    public List<SiTrackerHitStrip1D> getHits1D()
    {
        return _hits_1D;
    }
    
    public List<SiTrackerHitStrip1D> getHits1DTransformed()
    {
        if (_hits_1D_transformed == null)
        {
            _hits_1D_transformed = new ArrayList<SiTrackerHitStrip1D>();
            for (SiTrackerHitStrip1D hit_1D : _hits_1D)
            {
                if (getCoordinateSystem() == TrackerHitType.CoordinateSystem.UNKNOWN)
                {
                    _hits_1D_transformed.add((SiTrackerHitStrip1D)hit_1D.getTransformedHit(getLocalToGlobal().inverse()));
                }
                else
                {
                    _hits_1D_transformed.add((SiTrackerHitStrip1D)hit_1D.getTransformedHit(getCoordinateSystem()));
                }
            }
        }
        return _hits_1D_transformed;
    }
    
    public boolean isGhost()
    {
        for (MCParticle particle1 : getHits1D().get(0).getMCParticles())
        {
            if (getHits1D().get(1).getMCParticles().contains(particle1)) return true;
        }
        return false;
    }
    
}
