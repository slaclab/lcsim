package org.lcsim.detector.solids;

import java.util.ArrayList;
import java.util.List;
import org.lcsim.detector.ITransform3D;

/**
 *
 * @author tknelson
 */
public class Polygon3D extends Plane3D
{
    
    private List<Point3D> _points; // ordered list of points bounding the polygon
    
    /**
     * Creates a new instance of Polygon3D
     */
    public Polygon3D(List<Point3D> points)
    {
        super(points);
        _points = points;
        checkNormal();
    }
    
    public int nSides()
    {
        return _points.size();
    }
    
    public Plane3D getPlane()
    {
        return (Plane3D)this;
    }
    
    public List<Point3D> getVertices()
    {
        return _points;
    }
    
    public List<Point3D> getClosedVertices()
    {
        List<Point3D> closed_points = new ArrayList<Point3D>(_points);
        closed_points.add(_points.get(0));
        return closed_points;
    }
    
    public void checkNormal()
    {
        Plane3D test_plane = new Plane3D(_points);
        if (!GeomOp3D.equals(test_plane,getPlane()))
        {
            System.out.println("Normal to points: "+test_plane.getNormal());
            System.out.println("Normal to plane: "+getNormal());
            throw new RuntimeException("Normal to points does not match plane");
        }
    }
  
//    public void reverseNormal()
//    {
//        super.reverseNormal();
//        checkNormal();
//    }  
    
//    public void faceOutward()
//    {
//        if (getDistance() < 0) 
//        {
//            reverseNormal();
//        }
//    }
//   
    
    public void reverseNormal()
    {
        
//        System.out.println("Reversing points!!!!!!!!!!!!!!!!!");
//        if (1 == 1) throw new RuntimeException();
        
        super.reverseNormal();

        List<Point3D> reversed_points = new ArrayList<Point3D>();
        for (int ipoint = _points.size()-1; ipoint >= 0; ipoint--)
        {
            reversed_points.add(_points.get(ipoint));
        }
        
        _points = reversed_points;
        
        checkNormal();
        
    }
    
    public List<LineSegment3D> getEdges()
    {
        List<LineSegment3D> edges = new ArrayList<LineSegment3D>();
        
        List<Point3D> points = getClosedVertices();
        for (int ipoint = 0; ipoint<points.size()-1; ipoint++)
        {
            edges.add(new LineSegment3D(points.get(ipoint),points.get(ipoint+1)));
        }
        
        return edges;
    }
    
    public void transform(ITransform3D transform)
    {
        
        // can these get out of sync?
        
        super.transform(transform);
        for (Point3D point : _points)
        {
            point.transform(transform);
        }
        
        checkNormal();
        
//        Plane3D new_plane = new Plane3D(_points);
//        this._distance = new_plane.getDistance();
//        this._normal = new_plane.getNormal();
    }
    
    public Polygon3D transformed(ITransform3D transform)
    {
        
//        System.out.println("Polygon before transformation: "+this);
        
        List<Point3D> transformed_points = new ArrayList<Point3D>();
        for (Point3D point : _points)
        {
            transformed_points.add(point.transformed(transform));
        }
        
        Polygon3D transformed_polygon = new Polygon3D(transformed_points);
        
//        System.out.println("Polygon after transformation: "+transformed_polygon);
        
        checkNormal();
        
        return transformed_polygon;
        
//        return new Polygon3D(transformed_points);
    }
    
    public String toString()
    {
        String newline = System.getProperty("line.separator");
        String output = "Polygon3D: "+newline+
                "   Normal:     "+this.getNormal()+newline+
                "   Distance:   "+this.getDistance()+newline+
                "   Vertices:"+newline;
        for (Point3D vertex : this.getVertices())
        {
            output += vertex.getHep3Vector()+newline;
        }
        return output;
    }
    
}
