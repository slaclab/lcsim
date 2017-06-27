package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.VecOp;

/**
 *
 * @author tknelson
 */
public class Translation3D extends BasicHep3Vector implements ITranslation3D
{
    
    /** Creates a new instance of Translation3D */
    public Translation3D()
    {
    }
    
    public Translation3D(Hep3Vector translation)
    {
        this.setV(translation.x(),translation.y(),translation.z());
    }
    
    public Translation3D(double x, double y, double z)
    {
        this.setV(x,y,z);
    }
    
    public Hep3Vector getTranslationVector()
    {
        return (Hep3Vector)this;
    }
    
    public void setTranslationVector(Hep3Vector translation)
    {
        this.setV(translation.x(),translation.y(),translation.z());
    }
    
    public void translate(Hep3Vector coordinates)
    {
        Hep3Vector new_coordinates = translated(coordinates);
        ((BasicHep3Vector)coordinates).setV(new_coordinates.x(),new_coordinates.y(),new_coordinates.z());
    }
    
    public Hep3Vector translated(Hep3Vector coordinates)
    {
        return VecOp.add(coordinates,this);
    }
    
    public void invert()
    {
        setTranslationVector(inverse().getTranslationVector());
    }
    
    public ITranslation3D inverse()
    {
        return new Translation3D(VecOp.mult(-1.0,this));
    }
    
}
