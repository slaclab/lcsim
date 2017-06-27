package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import org.lcsim.detector.ITransform3D;

/**
 *
 * @author tknelson
 */
public class Point3D extends BasicHep3Vector implements Transformable
{
    
    /**
     * Creates a new instance of Point3D
     */
    public Point3D()
    {        
    }
    
    public Point3D(Hep3Vector point)
    {
        this.setV(point.x(),point.y(),point.z());
    }
    
    public Point3D(double x, double y, double z)
    {
        this.setV(x,y,z);
    }
    
    public Hep3Vector getHep3Vector()
    {
        return (Hep3Vector)this;
    }
    
    public void transform(ITransform3D transform)
    {
        transform.transform(this);
    }
    
    public Point3D transformed(ITransform3D transform)
    {
        return new Point3D(transform.transformed(this));
    }
    
}
