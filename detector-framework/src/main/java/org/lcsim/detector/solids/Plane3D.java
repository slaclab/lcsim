package org.lcsim.detector.solids;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import java.util.List;
import org.lcsim.detector.ITransform3D;

/**
 *
 * @author tknelson
 */
public class Plane3D implements Transformable
{
    
    Hep3Vector _normal; // normal to plane
    double _distance; // distance from origin
    
    /**
     * Creates a new instance of Plane3D
     */
    public Plane3D(Hep3Vector normal, double distance)
    {
        _normal = normal;
        _distance = distance;
    }
    
    public Plane3D(Hep3Vector normal, Point3D point)
    {
        _normal = normal;
        _distance = VecOp.dot(point,normal);
    }
    
    public Plane3D(List<Point3D> points)
    {
        if (points.size()<3)
        {
            throw new RuntimeException("Cannot make a plane from less than three points!");
        }
        
        if (!GeomOp3D.coplanar(points))
        {
            throw new RuntimeException("Cannot make a single plane from non-coplanar points!");
        }
        
        Hep3Vector v1 = VecOp.sub(points.get(1),points.get(0));
        Hep3Vector v2 = VecOp.sub(points.get(2),points.get(1));
        
        // normal and distance
        _normal = VecOp.unit(VecOp.cross(v1,v2));
        _distance = VecOp.dot(_normal,points.get(0));
    }
    
    public Hep3Vector getNormal()
    {
        return _normal;
    }
    
    public double getDistance()
    {
        return _distance;
    }
    
    public void faceOutward()
    {
        if (_distance < 0) reverseNormal();
    }
    
    public void reverseNormal()
    {
        _normal = VecOp.mult(-1,_normal);
        _distance *= -1;
    }
    
    public void transform(ITransform3D transform)
    {
        Point3D closest_point = (Point3D)VecOp.mult(_distance,_normal);
        _distance = closest_point.transformed(transform).magnitude();
        transform.rotate(_normal);       
    }
    
    public Plane3D transformed(ITransform3D transform)
    {
        Point3D closest_point = (Point3D)VecOp.mult(_distance,_normal);
        return new Plane3D(transform.rotated(_normal),closest_point.transformed(transform).magnitude());
    }
    
}
