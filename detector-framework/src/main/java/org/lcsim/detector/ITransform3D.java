/*
 * ITransform3D.java
 */

package org.lcsim.detector;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

/**
 * An interface to a combined rotation and translation in 3D apce.
 * 
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 */
public interface ITransform3D
{
    
    /**
     * Get Hep3Vector corresponding to translation
     * @return translation Hep3Vector
     */
    public ITranslation3D getTranslation();
    
    /**
     * Get IRotation3D corresponding to rotation
     * @return rotation IRotation3D
     */
    public IRotation3D getRotation();
    
    // Transformations in place
    //=========================
    
    /**
     * Transform coordinates in place
     * @param coordinates to transform
     */
    public void transform(Hep3Vector coordinates);
    
    /**
     * Transform SymmetricMatrix in place (e.g. covariance)
     * @param matrix to transform
     */
    public void transform(SymmetricMatrix matrix);
    
    /**
     * Translate coordinates in place
     * @param coordinates to translate
     */
    public void translate(Hep3Vector coordinates);
    
    /**
     * Rotate coordinates in place
     * @param coordinates to rotate
     */
    public void rotate(Hep3Vector coordinates);
    
    /**
     * Rotate SymmetricMatrix in place (e.g. covariance)
     * @param matrix to rotate
     */
    public void rotate(SymmetricMatrix matrix);
    
    // Transformations creating new position vectors
    //==============================================
    
    /**
     * Transform coordinates
     * @param coordinates to transform
     * @return transformed coordinates
     */
    public Hep3Vector transformed(Hep3Vector coordinates);
    
    /**
     * Transform SymmetricMatrix (e.g. covariance)
     * @param matrix to transform
     * @return transformed matrix
     */
    public SymmetricMatrix transformed(SymmetricMatrix matrix);
    
    /**
     * Translated coordinates
     * @param coordinates to translate
     * @return translated coordinates
     */
    public Hep3Vector translated(Hep3Vector coordinates);
    
    /**
     * Rotate coordinates
     * @param coordinates to rotate
     * @return rotated coordinates
     */
    public Hep3Vector rotated(Hep3Vector coordinates);
    
    /**
     * Rotate SymmetricMatrix (e.g. covariance)
     * @param matrix to rotate
     * @return rotated matrix
     */
    public SymmetricMatrix rotated(SymmetricMatrix matrix);
    
    /**
     * Multply this by another transformation in place
     * @param transformation to multiply by
     */
    public void multiplyBy(ITransform3D trans);
    
    /**
     * Invert this transformation in place
     */
    public void invert();
    
    /**
     * Get inverse of this transformation
     * @ return inverted transformation
     *
     */
    public Transform3D inverse();
}