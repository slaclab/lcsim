package org.lcsim.detector;

import hep.physics.matrix.Matrix;
import hep.physics.matrix.MatrixOp;
import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Matrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Matrix;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.io.PrintStream;

/**
 * Implementation of the @see IRotation3D interface.
 *
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: Rotation3D.java,v 1.14 2008/03/26 20:33:53 tknelson Exp $
 */
public class Rotation3D implements IRotation3D
{
    /**
     * The 3x3 rotation _matrix representing the state of this Rotation3D.
     */
    protected Hep3Matrix _matrix = BasicHep3Matrix.identity();
    
    /**
     * Construct a Rotation3D with the identity _matrix.
     */
    public Rotation3D()
    {}
    
    /**
     * Construct a Rotation3D from a Hep3Matrix interface.
     *
     * @param _matrix
     */
    public Rotation3D(Hep3Matrix matrix)
    {
        setRotationMatrix(matrix);
    }
    
    /**
     * Construct a copy from a Rotation3D.
     *
     * @param _matrix
     */
    public Rotation3D(IRotation3D rotation)
    {
        setRotationMatrix(rotation.getRotationMatrix());
    }
    
    /**
     * Set the rotation _matrix from a Hep3Matrix.
     */
    public void setRotationMatrix(Hep3Matrix matrix)
    {
        this._matrix = (BasicHep3Matrix)matrix;
    }
    
    public void invert()
    {
        ((BasicHep3Matrix)_matrix).transpose();
    }
    
    public IRotation3D inverse()
    {
        BasicHep3Matrix inversematrix = new BasicHep3Matrix(_matrix);
        inversematrix.transpose();
        return new Rotation3D(inversematrix);
    }
    
    public void printOut(PrintStream ps)
    {
        ps.print("[");
        ps.println();
        
        BasicHep3Matrix m = (BasicHep3Matrix)getRotationMatrix();
        
        for (int i=0; i<3; i++)
        {
            ps.printf("%.5f %.5f %.5f", m.e(i,0), m.e(i,1), m.e(i,2));
            ps.println();
        }
        
        ps.print("]");
        ps.println('\n');
    }
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        buff.append("[");
        buff.append('\n');
        buff.append(_matrix.e(0,0) + " " + _matrix.e(0,1) + " " + _matrix.e(0,2) + '\n');
        buff.append(_matrix.e(1,0) + " " + _matrix.e(1,1) + " " + _matrix.e(1,2) + '\n');
        buff.append(_matrix.e(2,0) + " " + _matrix.e(2,1) + " " + _matrix.e(2,2) + '\n');
        buff.append('\n');
        buff.append("]");
        return buff.toString();
    }
    
    public void resetToIdentity()
    {
        _matrix = BasicHep3Matrix.identity();
    }
    
    public Hep3Matrix getRotationMatrix()
    {
        return this._matrix;
    }
    
    public double getComponent(int row, int col)
    {
        return _matrix.e(row,col);
    }
    
    public void multiplyBy(IRotation3D rotation)
    {
        this.setRotationMatrix(VecOp.mult(this.getRotationMatrix(), rotation.getRotationMatrix()));
    }
    
    public void rotate(Hep3Vector coordinates)
    {
        Hep3Vector new_coordinates = rotated(coordinates);
        ((BasicHep3Vector)coordinates).setV(new_coordinates.x(),new_coordinates.y(),new_coordinates.z());
    }
    
    public Hep3Vector rotated(Hep3Vector coordinates)
    {
        return VecOp.mult(_matrix,coordinates);
    }
    
    public void rotate(SymmetricMatrix matrix)
    {
        SymmetricMatrix rotated_matrix = rotated(matrix);
        for (int irow = 0; irow < matrix.getNRows(); irow++)
        {
            for (int icol = 0; icol < matrix.getNColumns(); icol++)
            {
                matrix.setElement(irow,icol,rotated_matrix.e(irow,icol));
            }
        }
        
    }
    
    public SymmetricMatrix rotated(SymmetricMatrix matrix)
    {
        Matrix rotation_matrix = getRotationMatrix();
        Matrix rotation_matrix_transposed = inverse().getRotationMatrix();
        return new SymmetricMatrix(MatrixOp.mult(rotation_matrix,MatrixOp.mult(matrix,rotation_matrix_transposed)));
    }
    
    public static Rotation3D multiply(IRotation3D rot1, IRotation3D rot2)
    {
        return new Rotation3D(VecOp.mult(rot1.getRotationMatrix(), rot2.getRotationMatrix()));
    }
    
    public boolean equals(IRotation3D rotation)
    {
        boolean result=true;
        for (int row=XRow; row<3; row++)
        {
            for (int col=YCol; col<3; col++)
            {
                if (this.getComponent(row,col) != rotation.getComponent(row,col))
                {
                    result=false;
                    break;
                }
            }
        }
        return result;
    }
    
    public boolean isIdentity()
    {
        return equals(BasicHep3Matrix.identity());
    }
    
    // Static Methods
    //===============
    public static IRotation3D passiveXRotation(double angle)
    {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        BasicHep3Matrix rotation_matrix = BasicHep3Matrix.identity();
        rotation_matrix.setElement(1,1,cos);
        rotation_matrix.setElement(1,2,sin);
        rotation_matrix.setElement(2,1,-sin);
        rotation_matrix.setElement(2,2,cos);
        return new Rotation3D(rotation_matrix);
    }
    
    public static IRotation3D passiveYRotation(double angle)
    {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        BasicHep3Matrix rotation_matrix = BasicHep3Matrix.identity();
        rotation_matrix.setElement(0,0,cos);
        rotation_matrix.setElement(0,2,-sin);
        rotation_matrix.setElement(2,0,sin);
        rotation_matrix.setElement(2,2,cos);
        return new Rotation3D(rotation_matrix);
    }
    
    public static IRotation3D passiveZRotation(double angle)
    {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        BasicHep3Matrix rotation_matrix = BasicHep3Matrix.identity();
        rotation_matrix.setElement(0,0,cos);
        rotation_matrix.setElement(0,1,sin);
        rotation_matrix.setElement(1,0,-sin);
        rotation_matrix.setElement(1,1,cos);
        return new Rotation3D(rotation_matrix);
    }
    
    public static IRotation3D passiveAxisRotation(double angle, Hep3Vector axis)
    {
        if (axis.magnitude() == 0.0)
        {
            throw new RuntimeException("Rotation3D: cannot define a rotation around a null vector!");
        }
        
        Hep3Vector unit_axis = VecOp.unit(axis);
        double dx = unit_axis.x();
        double dy = unit_axis.y();
        double dz = unit_axis.z();
        
        double sa = Math.sin(angle);
        double ca = Math.cos(angle);
        
        BasicHep3Matrix rotation_matrix = new BasicHep3Matrix
                (
                ca+(1-ca)*dx*dx,        (1-ca)*dx*dy+sa*dz,     (1-ca)*dx*dz-sa*dy,
                (1-ca)*dy*dx-sa*dz,     ca+(1-ca)*dy*dy,        (1-ca)*dy*dz+sa*dx,
                (1-ca)*dz*dx+sa*dy,     (1-ca)*dz*dy-sa*dx,     ca+(1-ca)*dz*dz
                );
        
        return new Rotation3D(rotation_matrix);
        
    }
    
}