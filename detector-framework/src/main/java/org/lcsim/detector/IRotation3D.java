package org.lcsim.detector;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Matrix;
import hep.physics.vec.Hep3Vector;

import java.io.PrintStream;

/**
 * An interface to rotations in 3D space, using interfaces and base classes from
 * <a href="http://java.freehep.org/freehep-physics/">freehep physics</a>.
 *
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 * @version $Id: IRotation3D.java,v 1.11 2010/04/14 17:52:32 jeremy Exp $
 */
public interface IRotation3D
{
    /**
     * Numerical constants for the X and Y column indices.
     */
    public static final int XCol=0, YCol=1, ZCol=2,XRow=0,YRow=1,ZRow=2;
    
    /**
     * The number of rows and columns in the matrix.
     */
    public static final int NRows=3,NCols=3;
    
    /**
     * The rotation in standard rotation matrix form.
     * @return The BasicHep3Matrix representing this rotation.
     */
    public Hep3Matrix getRotationMatrix();
    
    /**
     * Set the matrix from a Hep3Matrix interface.
     * @param matrix
     */
    public void setRotationMatrix(Hep3Matrix matrix);
    
    /**
     * Multiply this rotation in place with another IRotation3D,
     * modifying this IRotation3D in place.
     * @param rotation
     */
    public void multiplyBy(IRotation3D rotation);
    
    /**
     * Rotate a Hep3Vector in place
     * @param coordinates
     */
    public void rotate(Hep3Vector coordinates);
    
    /**
     * Rotate a Hep3Vector
     * @param coordinates
     * @return rotated Hep3Vector
     */
    public Hep3Vector rotated(Hep3Vector coordinates);
    
    /**
     * Rotate a SymmetricMatrix in place (e.g. covariance matrix)
     * @param matrix
     */
    public void rotate(SymmetricMatrix matrix);
    
    /**
     * Rotate a SymmetricMatrix (e.g. covariance matrix)
     * @param matrix
     * @return rotated SymmetricMatrix
     */
    public SymmetricMatrix rotated(SymmetricMatrix matrix);
    
    /**
     * Compare this IRotation3D with another.
     * @param rotation
     * @return True if all components of the matrices are equal.
     */
    public boolean equals(IRotation3D rotation);
    
    /**
     * Apply inverse transformation in place.
     */
    public void invert();
    
    /**
     * Apply inverse transformation, returning
     * a new IRotation3D, not altering this matrix.
     * @return A new matrix which is the inverse of this one.
     */
    public IRotation3D inverse();
    
    /**
     * Get matrix component by row and column.
     * @param row
     * @param col
     * @return Matrix component at row and col.
     */
    public double getComponent(int row, int col);
    
    /**
     * Reset this IRotation3D to the identity matrix.
     */
    public void resetToIdentity();
    
    /**
     * True if this IRotation3D is equivalent to the identity matrix.
     * @return True if this rotation is equal to identity.
     */
    public boolean isIdentity();
    
    /**
     * Print out rotation
     * @param PrintStream for output
     */
    public void printOut(PrintStream ps);
    
}
