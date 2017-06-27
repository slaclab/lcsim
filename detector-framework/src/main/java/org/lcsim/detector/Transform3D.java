package org.lcsim.detector;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

/**
 * A class for representing a 3D coordinate transformation
 * using a @see Rotation3D for the rotation and a
 * @see hep.physics.vec.Hep3Vector for the translation.
 *
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class Transform3D 
implements ITransform3D
{
    
    // Fields
    ITranslation3D _translation = new Translation3D(); 
    IRotation3D _rotation = new Rotation3D();
            
    /**
     * Creates a new instance of Transform3D
     * with the identity matrix.
     */
    public Transform3D()
    {}
    
    public Transform3D(IRotation3D rotation)
    {
    	this._rotation = rotation;
    }
    
    public Transform3D(ITranslation3D translation)
    {
    	this._translation = translation;
    }
    
    public Transform3D(ITranslation3D translation, IRotation3D rotation)
    {
        _translation = translation;
        _rotation = rotation;
    }

    // Access to translation and rotation
    public ITranslation3D getTranslation()
    {
        return _translation;
    }
    
    private void setTranslation(ITranslation3D translation)
    {
        _translation = translation;
    }
    
    public IRotation3D getRotation()
    {
        return _rotation;
    }
    
    private void setRotation(IRotation3D rotation)
    {
        _rotation = rotation;
    }
    
    // Transformations in place
    public void transform(Hep3Vector coordinates)
    {
        rotate(coordinates);
        translate(coordinates);
    }
    
    public void transform(SymmetricMatrix matrix)
    {
        rotate(matrix);
    }
    
    public void translate(Hep3Vector coordinates)
    {
        _translation.translate(coordinates);
    }
    
    public void rotate(Hep3Vector coordinates)
    {
        _rotation.rotate(coordinates);
    }
    
    public void rotate(SymmetricMatrix matrix)
    {
        _rotation.rotate(matrix);
    }
     
    // Return transformed vectors
    public Hep3Vector transformed(Hep3Vector coordinates)
    {    
        return translated(rotated(coordinates));
    }
    
    public SymmetricMatrix transformed(SymmetricMatrix matrix)
    {
        return rotated(matrix);
    }
    
    public Hep3Vector translated(Hep3Vector coordinates)
    {
        return _translation.translated(coordinates);
    }
    
    public Hep3Vector rotated(Hep3Vector coordinates)
    {
        return _rotation.rotated(coordinates);
    }
    
    public SymmetricMatrix rotated(SymmetricMatrix matrix)
    {
        return _rotation.rotated(matrix);
    }
    
    // Invert the transformation
    public void invert()
    {
        this.setTranslation(inverse().getTranslation());
        this.setRotation(inverse().getRotation());
    }
    
    public Transform3D inverse()
    {
        Transform3D transform = new Transform3D(
                ( new Translation3D(_rotation.inverse().rotated(_translation)).inverse()),
                _rotation.inverse()
                );
        return transform;
    }
    
    // multiply in place
    public void multiplyBy(ITransform3D transformation_first)
    {
        this.setTranslation(multiply(this,transformation_first).getTranslation());
        this.setRotation(multiply(this,transformation_first).getRotation());
    }
    
    public static Transform3D multiply(ITransform3D transformation_second, ITransform3D transformation_first)
    {
        ITranslation3D translation = new Translation3D(transformation_second.translated( transformation_second.rotated(transformation_first.getTranslation()) ) );
        IRotation3D rotation = Rotation3D.multiply(transformation_second.getRotation(),transformation_first.getRotation());

        return new Transform3D(translation,rotation);   
    }
    
    public String toString()
    {
    	return _translation.toString() + '\n' + _rotation.toString();
    }
    
    public static Transform3D copy(ITransform3D ci)
    {
    	Transform3D c = (Transform3D)ci; 
    	try { return (Transform3D)c.clone(); } catch (Throwable x) {}
    	return null;
    }
    
    public static Transform3D copy(Transform3D c)
    {
    	try {
    		return (Transform3D)c.clone();
    	}
    	catch (Exception x)
    	{}
    	return null;
    }
}