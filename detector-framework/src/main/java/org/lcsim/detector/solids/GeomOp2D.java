package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

/**
 *
 * @author tknelson
 */
public class GeomOp2D
{
    
    // All operations are defined on x,y plane
    public static final Plane3D PLANE = new Plane3D(new BasicHep3Vector(0,0,1),0);
    
    public static final double DISTANCE_TOLERANCE = 1E-9;
    public static final double ANGULAR_TOLERANCE = 1E-9;
    
    /** Creates a new instance of GeomOp2D */
    public GeomOp2D()
    {
    }
    
    public static boolean intersects(Line3D line, LineSegment3D linesegment)
    {
        Hep3Vector diff_start = VecOp.sub(linesegment.getStartPoint(),line.getStartPoint());
        Hep3Vector diff_end = VecOp.sub(linesegment.getEndPoint(),line.getStartPoint());
        Hep3Vector start_cross = VecOp.cross(line.getDirection(),diff_start);
        Hep3Vector end_cross = VecOp.cross(line.getDirection(),diff_end);
        return (VecOp.dot(start_cross,end_cross) < 0);
    }
    
//    public Point3D intersection(Line3D line, LineSegment3D linesegment)
//    {
//        
//        
//        
//    }
    
    
    
}
