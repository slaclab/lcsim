/*
 * Line3D.java
 *
 * Created on October 17, 2007, 8:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import org.lcsim.detector.ITransform3D;

/**
 *
 * @author tknelson
 */
public class Line3D implements Transformable
{
    
    protected Point3D _startpoint;
    protected Hep3Vector _direction;
    
    /**
     * Creates a new instance of Line3D
     */
    public Line3D()
    {
        _startpoint = new Point3D();
        _direction = new BasicHep3Vector();
    }
    
    public Line3D(Point3D startpoint, Hep3Vector direction)
    {
        _startpoint = startpoint;
        _direction = VecOp.unit(direction);
    }
    
    public Point3D getStartPoint()
    {
        return _startpoint;
    }
    
    public Hep3Vector getDirection()
    {
        return _direction;
    }
    
    public Point3D getEndPoint(double length)
    {
        return new Point3D(VecOp.add(_startpoint,VecOp.mult(length,_direction)));
    }
    
    public Inside inside(Hep3Vector point)
    {
        Hep3Vector v1 = VecOp.sub(point,getStartPoint());
        
        double v1mag = v1.magnitude();
        
        if ( Math.abs(VecOp.dot(v1,_direction)) - v1mag < Tolerance.TOLERANCE ) return Inside.INSIDE;
        
        else return Inside.OUTSIDE;
        
    }
    
    public double distanceTo(Line3D line)
    {
        Hep3Vector normal = VecOp.cross(line.getDirection(),this.getDirection());
        Hep3Vector diff = VecOp.sub(line.getStartPoint(),this.getStartPoint());
        return VecOp.dot(normal,diff)/normal.magnitude();
    }
    
    public double distanceTo(Point3D point)
    {
        Hep3Vector diff = VecOp.sub(point,this.getStartPoint());
        return Math.sqrt( diff.magnitudeSquared() - Math.pow(VecOp.dot(diff,this.getDirection()),2) );
    }
    
    public void transform(ITransform3D transform)
    {
        _startpoint.transform(transform);
        transform.rotate(_direction);
    }
    
    public Line3D transformed(ITransform3D transform)
    {
        return new Line3D(_startpoint.transformed(transform),transform.rotated(_direction));
    }
    
}
