/*
 * LineSegment3D.java
 *
 * Created on October 16, 2007, 10:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector.solids;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tknelson
 */
public class LineSegment3D extends Line3D
{
 
    private double _length;
    
    /**
     * Creates a new instance of LineSegment3D
     */
    public LineSegment3D()
    {
        _length = 0;
    }
    
    public LineSegment3D(Point3D startpoint, Hep3Vector direction, double length)
    {
        super(startpoint, direction);
        _length = length;
    }
    
    public LineSegment3D(Point3D startpoint, Point3D endpoint)
    {
        super(startpoint,VecOp.sub(endpoint,startpoint));        
        _length = VecOp.sub(endpoint,startpoint).magnitude();
    }
    
    public Inside inside(Hep3Vector point)
    {
        Hep3Vector v1 = VecOp.sub(point,getStartPoint());
        Hep3Vector v2 = VecOp.sub(point,getEndPoint());
        
        double v1mag = v1.magnitude();
        double v2mag = v2.magnitude();
        
        if (v1mag == 0 || v2mag == 0) return Inside.SURFACE;
        
        if (VecOp.dot(v1,v2)+(v1mag*v2mag) < Tolerance.TOLERANCE) return Inside.INSIDE;
        
        else return Inside.OUTSIDE;
    }

    public double getLength()
    {
        return _length;
    }
    
    public Point3D getEndPoint()
    {
        return super.getEndPoint(_length);
    }
    
    public Line3D getLine()
    {
        return (Line3D)this;
    }
    
    public List<Point3D> getPoints()
    {
        List<Point3D> points = new ArrayList<Point3D>();
        points.add(getStartPoint());
        points.add(getEndPoint());
        return points;
    }       
    
    public void reverse()
    {
        _startpoint = getEndPoint();
        _direction = VecOp.mult(-1,getDirection());
    }
    
}
