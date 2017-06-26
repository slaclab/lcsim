package org.lcsim.detector.solids;

import hep.physics.matrix.BasicMatrix;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import net.jafama.FastMath;

import java.util.List;

/**
 *
 * @author tknelson
 */
public class GeomOp3D
{
    
    public static final double DISTANCE_TOLERANCE = 1E-9;
    public static final double ANGULAR_TOLERANCE = 1E-9;
    
    /**
     * Creates a new instance of GeomOp3D
     */
    public GeomOp3D()
    {
    }
    
    // equivalence tests (objects are same within tolerances)
    //=======================================================
    public static boolean equals(Point3D point1, Point3D point2)
    {
        return intersects(point1, point2);
    }
    
    public static boolean equals(Plane3D plane1, Plane3D plane2)
    {
//        System.out.println("Equals returns: " + ( parallel(plane1,plane2) &&
//                VecOp.dot(plane1.getNormal(),plane2.getNormal()) > 0 &&
//                Math.abs(plane1.getDistance() - plane2.getDistance()) < DISTANCE_TOLERANCE));
        
        return ( parallel(plane1,plane2) &&
                VecOp.dot(plane1.getNormal(),plane2.getNormal()) > 0 &&
                Math.abs(plane1.getDistance() - plane2.getDistance()) < DISTANCE_TOLERANCE);
    }
    
    // intersection tests
    //===================
    
    // Point3D to X
    //-----------
    
    /**
     * point - point intersection test (are same point)
     *
     * @param   point1
     * @param   point2
     * @return  true if points are closer than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Point3D point1, Point3D point2)
    {
        return (distanceBetween(point1,point2) < DISTANCE_TOLERANCE);
    }
    
    /**
     * point - line intersection test (point lies along line)
     *
     * @param   point
     * @param   line
     * @return  true if point is closer to line than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Point3D point, Line3D line)
    {
        return (distanceBetween(point,line) < DISTANCE_TOLERANCE );
    }
    
    /**
     * point - linesegment intersection test (point lies along linesegment)
     *
     * @param   point
     * @param   linesegment
     * @return  true if point is closer to linesegment than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Point3D point, LineSegment3D linesegment)
    {
        if ( intersects(point,linesegment.getLine()) )
        {
            double length_at_point = VecOp.dot( VecOp.sub(point,linesegment.getStartPoint()), linesegment.getDirection() );
            return ( (length_at_point + DISTANCE_TOLERANCE) > 0 && (length_at_point - DISTANCE_TOLERANCE < linesegment.getLength()) );
        }
        else
        {
            return false;
        }
    }
    
    /**
     * point - plane intersection test (point lies on plane)
     *
     * @param   point
     * @param   plane
     * @return  true if point is closer to plane than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Point3D point, Plane3D plane)
    {
        return ( Math.abs(distanceBetween(point,plane)) < DISTANCE_TOLERANCE);
    }
    
    /**
     * point - polygon intersection test (point lies on polygon)
     *
     * @param   point
     * @param   polygon
     * @return  true if point is closer to polygon than DISTANCE_TOLERANCE
     */
    // (this is better optimized than using distanceBetween)
    public static boolean intersects(Point3D point, Polygon3D polygon)
    {
        List<Point3D> vertices = polygon.getClosedVertices();
        
        double angle_sum = 0.0;
        for (int ivertex = 0; ivertex < vertices.size()-1; ivertex++)
        {
            Hep3Vector v1_vec = VecOp.sub(vertices.get(ivertex),point);
            Hep3Vector v2_vec = VecOp.sub(vertices.get(ivertex+1),point);
            
            double v1_mag = v1_vec.magnitude();
            double v2_mag = v2_vec.magnitude();
            
            if (v1_mag < DISTANCE_TOLERANCE || v2_mag < DISTANCE_TOLERANCE)
            {
                return true;
            }
            else
            {
                angle_sum += net.jafama.FastMath.acos(VecOp.dot(v1_vec,v2_vec)/(v1_mag*v2_mag));
            }
        }
        
        if ( Math.abs(angle_sum - 2*Math.PI) < polygon.nSides()*ANGULAR_TOLERANCE && intersects(point,polygon.getPlane()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    //Line3D to X
    //-----------
    
    /**
     * line - line intersection test
     *
     * @param   line1
     * @param   line2
     * @return  true if lines pass closer than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Line3D line1, Line3D line2)
    {
        return (distanceBetween(line1, line2) < DISTANCE_TOLERANCE);
    }
    
    /**
     * line - linesegment intersection test
     *
     * @param   line
     * @param   linesegment
     * @return  true if line passes closer to linesegment than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Line3D line, LineSegment3D linesegment)
    {
        return (distanceBetween(line, linesegment) < DISTANCE_TOLERANCE);
    }
    
    /**
     * line - plane intersection test
     *
     * @param   line
     * @param   plane
     * @return  true if line passes closer to plane than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Line3D line, Plane3D plane)
    {
        return ( Math.abs(distanceBetween(line, plane)) < DISTANCE_TOLERANCE);
    }
    
    /**
     * line - polygon intersection test
     *
     * @param   line
     * @param   polygon
     * @return  true if line passes closer to polygon than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Line3D line, Polygon3D polygon)
    {
        if (parallel(line,polygon.getPlane()))
        {
            if (intersects(line,polygon.getPlane())) // line is in plane
            {
                for (LineSegment3D edge : polygon.getEdges())
                {
                    if (intersects(line,edge)) return true;
                }
                return false;
            }
            else
            {
                return false;
            }
        }
        else
        {
            Point3D plane_intersection = intersection(line,polygon.getPlane());
            return intersects(plane_intersection,polygon);
        }
    }
    
    
    // Line segment to X
    //------------------
    
    /**
     * linesegment - linesegment intersection test
     *
     * @param   linesegment1
     * @param   linesegment2
     * @return  true if linesegments pass closer than DISTANCE_TOLERANCE
     */
    public static boolean intersects(LineSegment3D linesegment1, LineSegment3D linesegment2)
    {
        return (distanceBetween(linesegment1, linesegment2) < DISTANCE_TOLERANCE);
    }
    
    /**
     * linesegment - plane intersection test
     *
     * @param   linesegment
     * @param   plane
     * @return  true if linesegment passes closer to plane than DISTANCE_TOLERANCE
     */
    public static boolean intersects(LineSegment3D linesegment, Plane3D plane)
    {
        double start_distance = distanceBetween(linesegment.getStartPoint(),plane);
        double end_distance = distanceBetween(linesegment.getEndPoint(),plane);
        if (Math.abs(start_distance) < DISTANCE_TOLERANCE || Math.abs(end_distance) < DISTANCE_TOLERANCE)
        {
            return true;
        }
        else if (start_distance * end_distance < 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * linesegment - polygon intersection test
     *
     * @param   linesegment
     * @param   plane
     * @return  true if linesegment passes closer to polygon than DISTANCE_TOLERANCE
     */
    public static boolean intersects(LineSegment3D linesegment, Polygon3D polygon) // IS IMPLEMENTATION IN DISTANCEBETWEEN LINESEGMENT,POLYGON BETTER?
    {
        Line3D line = linesegment.getLine();
        Plane3D plane = polygon.getPlane();
        
        if (parallel(line,plane))
        {
            if (intersects(line,plane)) // line is in plane
            {
                for (LineSegment3D edge : polygon.getEdges())
                {
                    if (intersects(linesegment,edge)) return true;
                }
                return (intersects(linesegment.getStartPoint(),polygon) || intersects(linesegment.getEndPoint(),polygon));
            }
            else
            {
                return false;
            }
        }
        else
        {
            Point3D line_plane_intersection = intersection(line,plane);
            return (intersects(line_plane_intersection,polygon) && intersects(linesegment,plane));
        }
    }
    
    // Plane to X
    //-----------
    
    /**
     * plane - plane intersection test
     *
     * @param   plane1
     * @param   plane2
     * @return  true if planes pass closer than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Plane3D plane1, Plane3D plane2)
    {
        return (Math.abs(distanceBetween(plane1,plane2)) < DISTANCE_TOLERANCE);
    }
    
    /**
     * plane - polygon intersection test
     *
     * @param   plane
     * @param   polygon
     * @return  true if plane passes closer to polygon than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Plane3D plane, Polygon3D polygon)
    {
        boolean pos = false;
        boolean neg = false;
        for (Point3D vertex : polygon.getVertices())
        {
            if ( VecOp.dot(vertex,polygon.getNormal()) - plane.getDistance() + DISTANCE_TOLERANCE > 0 ) pos = true;
            if ( VecOp.dot(vertex,polygon.getNormal()) - plane.getDistance() - DISTANCE_TOLERANCE < 0 ) neg = true;
            if (pos && neg) return true;
        }
        return false;
    }
    
    // Polygon to X
    //-------------
    
    /**
     * polygon - polygon intersection test
     *
     * @param   polygon1
     * @param   polygon2
     * @return  true if polygons pass closer than DISTANCE_TOLERANCE
     */
    public static boolean intersects(Polygon3D polygon1, Polygon3D polygon2)
    {
        for (LineSegment3D edge : polygon1.getEdges())
        {
            if (intersects(edge,polygon2)) return true;
        }
        return false;
    }
    
    // Nearest distance between objects
    //=================================
    
    // Point3D to X
    //--------------
    
    /**
     * point - point distance
     *
     * @param   point1
     * @param   point2
     * @return  distance between points
     */
    public static double distanceBetween(Point3D point1, Point3D point2)
    {
        return VecOp.sub(point2,point1).magnitude();
    }
    
    /**
     * point - line distance
     *
     * @param   point
     * @param   line
     * @return  closest distance between point and line
     */
    public static double distanceBetween(Point3D point, Line3D line)
    {
        return VecOp.cross(line.getDirection(),VecOp.sub(point,line.getStartPoint())).magnitude();
    }
    
    /**
     * point - linesegment distance
     *
     * @param   point
     * @param   linesegment
     * @return  closest distance between point and linesegment
     */
    public static double distanceBetween(Point3D point, LineSegment3D linesegment)
    {
        Hep3Vector diff = VecOp.sub(point,linesegment.getStartPoint());
        double dot = VecOp.dot(diff,linesegment.getDirection());
        if (dot <= 0) return distanceBetween(point,linesegment.getStartPoint());
        else if (dot >= linesegment.getLength()) return distanceBetween(point,linesegment.getEndPoint());
        else return distanceBetween(point,linesegment.getLine());
    }
    
    /**
     * point - plane distance
     *
     * @param   point
     * @param   line
     * @return  closest distance between point and plane (signed in direciton of normal to plane)
     */
    public static double distanceBetween(Point3D point, Plane3D plane)
    {
        return VecOp.dot(point,plane.getNormal())-plane.getDistance();
    }
    
    /**
     * point - polygon distance
     *
     * @param   point
     * @param   polygon
     * @return  closest distance between point and polygon
     */
    public static double distanceBetween(Point3D point, Polygon3D polygon)  // FIXME (there is a bug here ... getting negative values)
    {
        double distance = Double.POSITIVE_INFINITY;
        
        double point_plane_distance = distanceBetween(point,polygon.getPlane());
        Point3D projected_point = new Point3D(VecOp.sub(point,VecOp.mult(point_plane_distance,polygon.getPlane().getNormal())));
        
        if (intersects(projected_point,polygon))
        {
            distance = Math.min(distance,point_plane_distance);
        }
        else
        {
            for (LineSegment3D edge : polygon.getEdges())
            {
                distance = Math.min(distanceBetween(point,edge),distance);
            }
        }
        return distance;
    }
    
    // Line3D to X
    //----------
    
    /**
     * line - line distance
     *
     * @param   line1
     * @param   line2
     * @return  closest distance between lines
     */
    public static double distanceBetween(Line3D line1, Line3D line2)
    {
        Hep3Vector start_diff = VecOp.sub(line2.getStartPoint(),line1.getStartPoint());
        if (parallel(line1,line2))
        {
            return VecOp.cross(start_diff,line1.getDirection()).magnitude();
        }
        else
        {
            Hep3Vector normal = VecOp.cross(line1.getDirection(),line2.getDirection());
            double denominator = normal.magnitude();
            return Math.abs(VecOp.dot(normal,start_diff)/denominator);
        }
    }
    
    /**
     * line - linesegment distance
     *
     * @param   line
     * @param   linesegment
     * @return  closest distance between line and linesegment
     */
    public static double distanceBetween(Line3D line, LineSegment3D linesegment)
    {
        Hep3Vector start_diff = VecOp.sub(linesegment.getStartPoint(),line.getStartPoint());
        if (parallel(line,linesegment.getLine()))
        {
//            System.out.println("Lines are parallel ");
            return VecOp.cross(start_diff,line.getDirection()).magnitude();
        }
        else
        {
            double[] pca = linesPCA(line,linesegment);                         // FIXME (check usage here) (fixed?)
            
//            System.out.println("pca returns: "+ pca[0]+", "+pca[1]);
            
            pca[1] = Math.max(0.0,pca[1]);
            pca[1] = Math.min(linesegment.getLength(),pca[1]);
            
//            System.out.println("closest point on line is: "+line.getEndPoint(pca[0]));
//            System.out.println("closest point on line segment is: "+linesegment.getLine().getEndPoint(pca[1]));
            
            return distanceBetween(line.getEndPoint(pca[0]),linesegment.getLine().getEndPoint(pca[1]));
        }
    }
    
    /**
     * line - plane distance
     *
     * @param   line
     * @param   plane
     * @return  closest distance between line and plane (signed in direction of normal to plane)
     */
    public static double distanceBetween(Line3D line, Plane3D plane)
    {
        if (Math.abs(VecOp.dot(line.getDirection(),plane.getNormal())) > DISTANCE_TOLERANCE) return 0;
        else return distanceBetween(line.getStartPoint(),plane);
    }
    
    /**
     * line - polygon distance
     *
     * @param   line
     * @param   polygon
     * @return  closest distance between line and polygon
     */
    public static double distanceBetween(Line3D line, Polygon3D polygon)
    {
        double distance = Double.POSITIVE_INFINITY;
        
        for (LineSegment3D edge : polygon.getEdges())
        {
            distance = Math.min(distance,distanceBetween(line,edge));
        }
        
        Point3D plane_intersection = intersection(line,polygon.getPlane());
        if (intersects(plane_intersection,polygon))
        {
            distance = 0;
        }
        return distance;
    }
    
    
    // LineSegment3D to X
    //-----------------
    
    /**
     * linesegment - linesegment distance
     *
     * @param   linesegment1
     * @param   linesegment2
     * @return  closest distance between linesegments
     */
    public static double distanceBetween(LineSegment3D linesegment1, LineSegment3D linesegment2)
    {
        Hep3Vector start_diff = VecOp.sub(linesegment2.getStartPoint(),linesegment1.getStartPoint());
        if (parallel(linesegment1.getLine(),linesegment2.getLine()))
        {
            Hep3Vector end_diff = VecOp.sub(linesegment2.getEndPoint(),linesegment1.getEndPoint());
            
            double start_projection = VecOp.dot(start_diff,linesegment1.getDirection());
            double end_projection = VecOp.dot(end_diff,linesegment1.getDirection());
            if (start_projection < 0 && end_projection < 0)
            {
                if (start_projection < end_projection)
                {
                    return distanceBetween(linesegment1.getStartPoint(),linesegment2.getEndPoint());
                }
                else
                {
                    return distanceBetween(linesegment1.getStartPoint(),linesegment2.getStartPoint());
                }
            }
            else if (start_projection > linesegment1.getLength() && end_projection > linesegment1.getLength())
            {
                if (start_projection > end_projection)
                {
                    return distanceBetween(linesegment1.getEndPoint(),linesegment2.getEndPoint());
                }
                else
                {
                    return distanceBetween(linesegment1.getEndPoint(),linesegment2.getEndPoint());
                }
            }
            else
            {
                return VecOp.cross(start_diff,linesegment1.getDirection()).magnitude();
            }
        }
        else
        {
            double[] pca = linesPCA(linesegment1,linesegment2);                         // FIXME (check usage here) (fixed?)
            
            pca[0] = Math.max(0.0,pca[0]);
            pca[0] = Math.min(linesegment1.getLength(),pca[0]);
            
            pca[1] = Math.max(0.0,pca[1]);
            pca[1] = Math.min(linesegment2.getLength(),pca[1]);
            
            return distanceBetween(linesegment1.getEndPoint(pca[0]),linesegment2.getEndPoint(pca[1]));
        }
        
    }
    
    /**
     * linesegment - plane distance
     *
     * @param   linesegment
     * @param   plane
     * @return  closest distance between linesegment and plane (signed in direction of normal to plane)
     */
    public static double distanceBetween(LineSegment3D linesegment, Plane3D plane)
    {
        if (intersects(linesegment,plane))
        {
            return 0;
        }
        else
        {
            return Math.min(distanceBetween(linesegment.getStartPoint(),plane),distanceBetween(linesegment.getEndPoint(),plane));
        }
    }
    
    /**
     * linesegment - polygon distance
     *
     * @param   linesegment
     * @param   polygon
     * @return  closest distance between linesegment and polygon
     */
    public static double distanceBetween(LineSegment3D linesegment, Polygon3D polygon)
    {
        Line3D line = linesegment.getLine();
        Plane3D plane = polygon.getPlane();
        
        // if not parallel check for line segment crossing polygon interior
        if (!parallel(line,plane))
        {
            if (intersects(intersection(line,plane),polygon) && intersects(linesegment,polygon.getPlane())) return 0;
        }
        
        double distance = Double.POSITIVE_INFINITY;
        // line segments to edges of polygon
        for (LineSegment3D edge : polygon.getEdges())
        {
            distance = Math.min(distance,distanceBetween(linesegment,edge));
        }
        
        // line segment endpoints to interior of polygon
        for (Point3D endpoint : linesegment.getPoints())
        {
            LineSegment3D closest_approach = lineBetween(endpoint,plane);
            if (intersects(closest_approach.getEndPoint(),polygon))
            {
                distance = Math.min(distance,closest_approach.getLength());
            }
        }
        
        return distance;
    }
    
    
    // Plane3D to X
    //-----------
    
    /**
     * plane - plane distance
     *
     * @param   plane1
     * @param   plane2
     * @return  closest distance between planes
     */
    public static double distanceBetween(Plane3D plane1, Plane3D plane2)  // FIXME!!!!!!!!!!!!!!
    {
        if ( parallel(plane1,plane2) )
        {
            return plane2.getDistance()-VecOp.dot(plane1.getNormal(),plane2.getNormal())*plane1.getDistance();
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * plane - polygon distance
     *
     * @param   plane
     * @param   polygon
     * @return  closest distance between plane and polygon (signed in direction of normal to plane)
     */
    public static double distanceBetween(Plane3D plane, Polygon3D polygon)
    {
        if (intersects(plane,polygon)) return 0;
        
        double abs_distance = Double.POSITIVE_INFINITY;
        double distance = Double.POSITIVE_INFINITY;
        
        for (Point3D point : polygon.getVertices())
        {
            double vertex_distance = distanceBetween(point,plane);
            if (Math.abs(vertex_distance) < abs_distance)
            {
                abs_distance = Math.abs(vertex_distance);
                distance = vertex_distance;
                if (abs_distance < DISTANCE_TOLERANCE) return distance;
            }
        }
        return distance;
    }
    
    // Polygon3D to X
    //-------------
    
    /**
     * polygon - polygon distance
     *
     * @param   polygon1
     * @param   polygon2
     * @return  closest distance between polygons
     */
    // (this may be far from optimal)
    public static double distanceBetween(Polygon3D polygon1, Polygon3D polygon2)
    {
        double distance = Double.POSITIVE_INFINITY;
        for (LineSegment3D edge : polygon1.getEdges())
        {
            Math.min(distance,distanceBetween(edge,polygon2));
            if (distance < DISTANCE_TOLERANCE) return 0;
        }
        for (LineSegment3D edge : polygon2.getEdges())
        {
            Math.min(distance,distanceBetween(edge,polygon1));
            if (distance < DISTANCE_TOLERANCE) return 0;
        }
        return distance;
    }
    
    // Intersections
    //==============
    
    // Line3D - X
    //------------
    
    // Polygon3D to X
    //-------------
    
    /**
     * line - plane intersection (must test for for parallelism)
     *
     * @param   line
     * @param   plane
     * @return  point of intersection between line and plane
     */
    public static Point3D intersection(Line3D line, Plane3D plane)
    {
        double distance_to_plane = -distanceBetween(line.getStartPoint(),plane);
        double slope_to_plane = VecOp.dot(line.getDirection(),plane.getNormal());
        if (slope_to_plane == 0)
        {
            throw new RuntimeException("Line is parallel to plane!  Please check first with parallel(Line line,Plane plane)");
        }
        else
        {
            return line.getEndPoint(distance_to_plane/slope_to_plane);
        }
    }
    
    // Plane3D - X
    //----------
    
    /**
     * plane - plane intersection (must test for for parallelism)
     *
     * @param   plane1
     * @param   plane2
     * @return  line of intersection between two planes
     */
    public static Line3D intersection(Plane3D plane1, Plane3D plane2)
    {
        Hep3Vector direction = VecOp.cross(plane1.getNormal(),plane2.getNormal());
        
        if (direction.magnitudeSquared() == 0)
        {
            throw new RuntimeException("Planes are parallel!  Please check first with parallel(Plane plane1,Plane plane2)");
        }
        
        double dot_normals = VecOp.dot(plane1.getNormal(),plane2.getNormal());
        
        double determinant = 1 - Math.pow(dot_normals,2);
        
        double c1 = (plane1.getDistance()-plane2.getDistance()*dot_normals)/determinant;
        double c2 = (plane2.getDistance()-plane1.getDistance()*dot_normals)/determinant;
        
        Point3D point = new Point3D( VecOp.add(VecOp.mult(c1,plane1.getNormal()), VecOp.mult(c2,plane2.getNormal()) ) );
        
        return new Line3D(point,direction);
        
    }
    
    
    
    // Line3D segments of closest approach
    //==================================
    
    // Point3D to X
    //-----------
    
    /**
     * point - point closest approach (linesegment between two points)
     *
     * @param   point1
     * @param   point2
     * @return  linesegment between point1 and point2
     */
    public static LineSegment3D lineBetween(Point3D point1, Point3D point2)
    {
        return new LineSegment3D(point1, point2);
    }
    
    /**
     * point - line closest approach
     *
     * @param   point
     * @param   line
     * @return  shortest linesegment from point to line
     */
    public static LineSegment3D lineBetween(Point3D point, Line3D line)
    {
        Point3D closest_point = line.getEndPoint(VecOp.dot(VecOp.sub(point,line.getStartPoint()),line.getDirection()));
        return new LineSegment3D(point,closest_point);
    }
    
    /**
     * point - linesegment closest approach
     *
     * @param   point
     * @param   linesegment
     * @return  shortest linesegment from point to linesegment
     */
    public static LineSegment3D lineBetween(Point3D point, LineSegment3D linesegment)
    {
        Hep3Vector diff = VecOp.sub(point,linesegment.getStartPoint());
        double dot = VecOp.dot(diff,linesegment.getDirection());
        if (dot <= 0) return new LineSegment3D(point,linesegment.getStartPoint());
        else if (dot >= linesegment.getLength()) return new LineSegment3D(point,linesegment.getEndPoint());
        else return new LineSegment3D(point,linesegment.getEndPoint(dot));
    }
    
    /**
     * point - plane closest approach
     *
     * @param   point
     * @param   plane
     * @return  shortest linesegment from point to plane
     */
    public static LineSegment3D lineBetween(Point3D point, Plane3D plane)
    {
        double distance = VecOp.dot(point,plane.getNormal())-plane.getDistance();
        Point3D closest_point = new Point3D( VecOp.sub(point,VecOp.mult(distance,plane.getNormal())) );
        return new LineSegment3D(point,closest_point);
    }
    
    /**
     * point - polygon closest approach
     *
     * @param   point
     * @param   polygon
     * @return  shortest linesegment from point to polygon
     */
    public static LineSegment3D lineBetween(Point3D point, Polygon3D polygon)
    {
        double point_plane_distance = distanceBetween(point,polygon.getPlane());
        Point3D projected_point = new Point3D(VecOp.sub(point,VecOp.mult(point_plane_distance,polygon.getPlane().getNormal())));
        
        if (intersects(projected_point,polygon))
        {
            return new LineSegment3D(point,projected_point);
        }
        else
        {
            double distance = Double.POSITIVE_INFINITY;
            LineSegment3D closest_edge = new LineSegment3D();
            for (LineSegment3D edge : polygon.getEdges())
            {
                if (distanceBetween(point,edge) < distance)
                {
                    distance = distanceBetween(point,edge);
                    closest_edge = edge;
                }
            }
            return lineBetween(point,closest_edge);
        }
    }
    
    // Line3D to X
    //----------
    
    /**
     * line - line closest approach (must check first for parallelism)
     *
     * @param   line1
     * @param   line2
     * @return  shortest linesegment from line1 to line2
     */
    public static LineSegment3D lineBetween(Line3D line1, Line3D line2)
    {
        if (parallel(line1,line2))
        {
            throw new RuntimeException("Lines are parallel!  Please check first with parallel(Line line1,Line line2)");
        }
        else
        {
            double pca[] = linesPCA(line1,line2);                         // FIXME (check usage here) (OK?)
            return new LineSegment3D(line1.getEndPoint(pca[0]),line2.getEndPoint(pca[1]));
        }
    }
    
    /**
     * line - linesegment closest approach (must check first for parallelism)
     *
     * @param   line
     * @param   linesegment
     * @return  shortest linesegment from line to linesegment
     */
    public static LineSegment3D lineBetween(Line3D line, LineSegment3D linesegment)
    {
        LineSegment3D line_between;
        
        if (parallel(line,linesegment.getLine()))
        {
            throw new RuntimeException("Line and line segment are parallel!  Please check first with parallel(Line line,LineSegment linesegment)");
        }
        else
        {
            double[] pca = linesPCA(line,linesegment);                         // FIXME (check usage here) (fixed?)
            
            if (pca[1]<0.0)
            {
                line_between = lineBetween(linesegment.getStartPoint(),line);
                line_between.reverse();
            }
            else if (pca[1]>linesegment.getLength())
            {
                line_between = lineBetween(linesegment.getEndPoint(),line);
                line_between.reverse();
            }
            else
            {
                line_between = new LineSegment3D(line.getEndPoint(pca[0]),linesegment.getEndPoint(pca[1]));
            }
           
            return line_between;            
        }
    }
    
    /**
     * line - polygon closest approach (must check for for intersection)
     *
     * @param   line
     * @param   polygon
     * @return  shortest linesegment from line to polygon
     */
    public static LineSegment3D lineBetween(Line3D line, Polygon3D polygon)
    {
        Point3D plane_intersection = intersection(line,polygon.getPlane());
        if (intersects(plane_intersection,polygon))
        {
            throw new RuntimeException("Line intersects polygon  Please check first with intersects(Line line, Polygon polygon)");
        }
        else
        {
            double distance = Double.POSITIVE_INFINITY;
            LineSegment3D closest_edge = new LineSegment3D();
            for (LineSegment3D edge : polygon.getEdges())
            {
                if (distanceBetween(line,edge) < distance)
                {
                    distance = distanceBetween(line,edge);
                    closest_edge = edge;
                }
            }
            return lineBetween(line,closest_edge);
        }
    }
    
    // LineSegment3D to X
    //-----------------
    
    /**
     * linesegment - linesegment closest approach
     *
     * @param   linesegment1
     * @param   linesegment2
     * @return  shortest linesegment from linesegment1 to linesegment2
     */
    public static LineSegment3D lineBetween(LineSegment3D linesegment1, LineSegment3D linesegment2)
    {
        Hep3Vector start_diff = VecOp.sub(linesegment2.getStartPoint(),linesegment1.getStartPoint());
        if (parallel(linesegment1.getLine(),linesegment2.getLine()))
        {
            Hep3Vector end_diff = VecOp.sub(linesegment2.getEndPoint(),linesegment1.getEndPoint());
            
            double start_projection = VecOp.dot(start_diff,linesegment1.getDirection());
            double end_projection = VecOp.dot(end_diff,linesegment1.getDirection());
            if (start_projection < 0 && end_projection < 0)
            {
                if (start_projection < end_projection)
                {
                    return new LineSegment3D(linesegment1.getStartPoint(),linesegment2.getEndPoint());
                }
                else
                {
                    return new LineSegment3D(linesegment1.getStartPoint(),linesegment2.getStartPoint());
                }
            }
            else if (start_projection > linesegment1.getLength() && end_projection > linesegment1.getLength())
            {
                if (start_projection > end_projection)
                {
                    return new LineSegment3D(linesegment1.getEndPoint(),linesegment2.getEndPoint());
                }
                else
                {
                    return new LineSegment3D(linesegment1.getEndPoint(),linesegment2.getEndPoint());
                }
            }
            else
            {
                double startpoint1 = Math.max(0,start_projection);
                double endpoint1 = Math.min(linesegment1.getLength(),end_projection);
                Point3D origin = linesegment1.getEndPoint((startpoint1+endpoint1)/2);
                
                double startpoint2 = (start_projection-startpoint1)/2;
                double endpoint2 = (end_projection-endpoint1)/2;
                Point3D destination = linesegment2.getEndPoint((startpoint2+endpoint2)/2);
                
                return new LineSegment3D(origin,destination);
            }
        }
        else
        {
            double[] pca = linesPCA(linesegment1,linesegment2);                         // FIXME (check usage here) (fixed?)
            pca[0] = Math.max(0.0,pca[0]);
            pca[0] = Math.min(linesegment1.getLength(),pca[0]);
            
            pca[1] = Math.max(0.0,pca[1]);
            pca[1] = Math.min(linesegment2.getLength(),pca[1]);
            
            return new LineSegment3D(linesegment1.getEndPoint(pca[0]),linesegment2.getEndPoint(pca[1]));
        }
        
    }
    
    /**
     * linesegment - plane closest approach (must check first for intersection)
     *
     * @param   linesegment
     * @param   plane
     * @return  shortest linesegment from linesegment to plane
     */
    public static LineSegment3D lineBetween(LineSegment3D linesegment, Plane3D plane)
    {
        if (intersects(linesegment,plane))
        {
            throw new RuntimeException("Line segment intersects plane.  Please check first with intersects(LineSegment linesegment, Plane plane)");
        }
        else if (parallel(linesegment,plane))
        {
            return lineBetween(linesegment.getEndPoint(linesegment.getLength()/2),plane);
        }
        else
        {
            if (distanceBetween(linesegment.getStartPoint(),plane) < distanceBetween(linesegment.getEndPoint(),plane))
            {
                return lineBetween(linesegment.getStartPoint(),plane);
            }
            else
            {
                return lineBetween(linesegment.getEndPoint(),plane);
            }
        }
    }
    
    /**
     * linesegment - polygon closest approach (must check first for intersection)
     *
     * @param   linesegment
     * @param   polygon
     * @return  shortest linesegment from linesegment to polygon
     */
    public static LineSegment3D lineBetween(LineSegment3D linesegment, Polygon3D polygon)
    {
        Line3D line = linesegment.getLine();
        Plane3D plane = polygon.getPlane();
        
        // if not parallel check for intersection of segment with polygon interior
        if (!parallel(line,plane))
        {
            if (intersects(intersection(line,plane),polygon) && intersects(linesegment,polygon.getPlane()))
            {
                throw new RuntimeException("Line segment intersects polygon.  Please check first with intersects(LineSegment linesegment, Polygon polygon)");
            }
        }
        
        // line segments to edges of polygon
        double edge_distance = Double.POSITIVE_INFINITY;
        LineSegment3D closest_edge = new LineSegment3D();
        for (LineSegment3D edge : polygon.getEdges())
        {
            if (distanceBetween(linesegment,edge) < edge_distance)
            {
                edge_distance = distanceBetween(linesegment,edge);
                closest_edge = edge;
            }
        }
        
        double endpoint_distance = edge_distance;
        LineSegment3D closest_planar_approach = new LineSegment3D();
        for (Point3D endpoint : linesegment.getPoints())
        {
            LineSegment3D closest_approach = lineBetween(endpoint,plane);
            if (closest_approach.getLength() < endpoint_distance && intersects(closest_approach.getEndPoint(),polygon))
            {
                endpoint_distance = closest_approach.getLength();
                closest_planar_approach = closest_approach;
            }
        }
        
        if (endpoint_distance < edge_distance)
        {
            return closest_planar_approach;
        }
        else
        {
            return lineBetween(linesegment,closest_edge);
        }
    }
    
    // Plane3D to X
    //-----------
    
    /**
     * plane - polygon closest approach (must check first for intersection)
     *
     * @param   plane
     * @param   polygon
     * @return  shortest linesegment from plane to polygon
     */
    public static LineSegment3D lineBetween(Plane3D plane, Polygon3D polygon)
    {
        if (intersects(plane,polygon))
        {
            throw new RuntimeException("Plane intersects polygon.  Please check first with intersects(Plane plane, Polygon polygon)");
        }
        
        double distance = Double.POSITIVE_INFINITY;
        LineSegment3D closest_edge = new LineSegment3D();
        for (LineSegment3D edge : polygon.getEdges())
        {
            if (distanceBetween(edge,plane) < distance)
            {
                distance = distanceBetween(edge,plane);
                closest_edge = edge;
            }
        }
        return lineBetween(closest_edge,plane);
    }
    
    
    // Polygon3D to X
    //-------------
    
    /**
     * polygon - polygon closest approach
     *
     * @param   polygon1
     * @param   polygon2
     * @return  shortest linesegment from polygon1 to polygon2
     */
    // does not deliver optimal solution for parallel polygons that are adjacent
    public static LineSegment3D lineBetween(Polygon3D polygon1, Polygon3D polygon2)
    {
        double distance = Double.POSITIVE_INFINITY;
        LineSegment3D closest_edge = new LineSegment3D();
        
        Plane3D plane = polygon2.getPlane();
        for (LineSegment3D edge : polygon1.getEdges())
        {
            if (distanceBetween(edge,plane) < distance)
            {
                distance = distanceBetween(edge,plane);
                closest_edge = edge;
            }
        }
        if (distance < DISTANCE_TOLERANCE)
        {
            throw new RuntimeException("Polygons intersect.  Please check first with intersects(Polygon polygon1, Polygon polygon2)");
        }
        
        plane = polygon1.getPlane();
        for (LineSegment3D edge : polygon2.getEdges())
        {
            if (distanceBetween(edge,plane) < distance)
            {
                distance = distanceBetween(edge,plane);
                closest_edge = edge;
            }
        }
        if (distance < DISTANCE_TOLERANCE)
        {
            throw new RuntimeException("Polygons intersect.  Please check first with intersects(Polygon polygon1, Polygon polygon2)");
        }
        
        return lineBetween(closest_edge,plane);
        
    }
    
    
    // Coplanarity and collinearity
    //=============================
    
    /**
     * coplanarity test for a list of points
     *
     * @param   points
     * @return  true if points are coplanar within DISTANCE_TOLERANCE
     */
    public static boolean coplanar(List<Point3D> points)
    {
        if (points.size()<4) return true;
        if (collinear(points)) return true;
        
        Hep3Vector v1 = VecOp.sub(points.get(1),points.get(0));
        Hep3Vector v2 = VecOp.sub(points.get(2),points.get(1));
        
        // normal and distance
        Hep3Vector normal = VecOp.unit(VecOp.cross(v1,v2));
        
        for (int ipoint = 3; ipoint < points.size(); ipoint++)
        {
            Hep3Vector vtest = VecOp.sub(points.get(ipoint),points.get(ipoint-1));
            if (VecOp.dot(vtest,normal) > DISTANCE_TOLERANCE) return false;
        }
        
        return true;
    }
    
    /**
     * coplanarity test for a line and plane
     *
     * @param   line
     * @param   plane
     * @return  true if line is in plane within DISTANCE_TOLERANCE
     */
    public static boolean coplanar(Line3D line, Plane3D plane)
    {
        return (parallel(line,plane) && intersects(line.getStartPoint(),plane));
    }
    
    /**
     * collinearity test for a list of points
     *
     * @param   points
     * @return  true if points are collinear within DISTANCE_TOLERANCE
     */
    public static boolean collinear(List<Point3D> points)
    {
        if (points.size()<3) return true;
        
        Hep3Vector direction = VecOp.unit(VecOp.sub(points.get(1),points.get(0)));
        
        for (int ipoint = 2; ipoint < points.size(); ipoint++)
        {
            Hep3Vector vtest = VecOp.sub(points.get(ipoint),points.get(ipoint-1));
            if (VecOp.cross(vtest,direction).magnitude() > DISTANCE_TOLERANCE) return false;
        }
        
        return true;
    }
    
    
    /**
     * test for parallel lines
     *
     * @param   line1
     * @param   line2
     * @return  true if lines are parallel within ANGULAR_TOLERANCE
     */
    public static boolean parallel(Line3D line1, Line3D line2)
    {
        return VecOp.cross(line1.getDirection(),line2.getDirection()).magnitude() < ANGULAR_TOLERANCE;
    }
    
    /**
     * test whether line is parallel to plane
     *
     * @param   line
     * @param   plane
     * @return  true if line is parallel to plane within ANGULAR_TOLERANCE
     */
    public static boolean parallel(Line3D line, Plane3D plane)
    {
        return Math.abs(VecOp.dot(line.getDirection(),plane.getNormal())) < ANGULAR_TOLERANCE;
    }
    
    /**
     * test for parallel planes
     *
     * @param   plane1
     * @param   plane2
     * @return  true if planes are parallel within ANGULAR_TOLERANCE
     */
    public static boolean parallel(Plane3D plane1, Plane3D plane2)
    {
        return VecOp.cross(plane1.getNormal(),plane2.getNormal()).magnitude() < ANGULAR_TOLERANCE;
    }
    
    /**
     * test for plane normal to a vector
     *
     * @param   plane
     * @param   unit_vector (unchecked)
     * @return  true if plane normal to a unit vector within ANGULAR_TOLERANCE
     */
    public static boolean isNormal(Hep3Vector unit_vector, Plane3D plane)
    {
        if ( VecOp.cross(unit_vector,plane.getNormal()).magnitude() < ANGULAR_TOLERANCE )
        {
            return VecOp.dot(unit_vector,plane.getNormal()) > 0;
        }
        else return false;
    }
    
//========================================
// Private methods
//========================================
    
    // Finds points of closest approach fora pair of lines
    // returns length parameter along each line of PCA
    private static double[] linesPCA(Line3D line1, Line3D line2)
    {
        Hep3Vector s1 = VecOp.sub(line2.getStartPoint(),line1.getStartPoint());
        Hep3Vector s2 = line2.getDirection();
        Hep3Vector s3 = VecOp.cross(line1.getDirection(),line2.getDirection());
        double[][] s_elements = {s1.v(),s2.v(),s3.v()};
        BasicMatrix s = new BasicMatrix(s_elements);
        double s_det = s.det();
        
        Hep3Vector t1 = s1;
        Hep3Vector t2 = line1.getDirection();
        Hep3Vector t3 = s3;
        double[][] t_elements = {t1.v(),t2.v(),t3.v()};
        BasicMatrix t = new BasicMatrix(t_elements); // check orientation... may need to be transposed
        t.transpose();
        double t_det = t.det();
        
        double denominator = t3.magnitudeSquared();
        if (denominator == 0)
        {
            throw new RuntimeException("Lines must be checked for parallelism before calling linesPCA!!");
        }
        
        double s_pca = s_det/denominator;
        double t_pca = t_det/denominator;
        
        return new double[]{s_pca,t_pca};
    }
    
    
}
