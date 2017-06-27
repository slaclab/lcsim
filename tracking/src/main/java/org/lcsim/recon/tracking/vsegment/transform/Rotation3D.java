package org.lcsim.recon.tracking.vsegment.transform;

import hep.physics.matrix.MatrixOp;
import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Matrix;
import hep.physics.vec.Hep3Matrix;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import static org.lcsim.recon.tracking.vsegment.transform.Axis.*;

/**
 * An object of this type defines a reference frame rotation in 3D space.
 *
 * @author D.Onoprienko
 * @version $Id: Rotation3D.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class Rotation3D implements Transformation3D {
  
// -- Constructors :  ----------------------------------------------------------
  
  /** Create identity transformation. */
  public Rotation3D() {
    _from = new BasicHep3Matrix(1., 0., 0.,
                                0., 1., 0.,
                                0., 0., 1);
  }
  
  /**
   * Create rotation given the matrix elements.
   * Multiplying a vector of coordinates by this matrix converts coordinates from
   * transformed to original reference frame.
   */
  public Rotation3D(double e11, double e12, double e13,
                    double e21, double e22, double e23,
                    double e31, double e32, double e33) {
      _from = new BasicHep3Matrix(e11, e12, e13,
                                  e21, e22, e23,
                                  e31, e32, e33);
  }
  
  /**
   * Create rotation given the matrix.
   * Multiplying a vector of coordinates by this matrix converts coordinates from
   * transformed to original reference frame.
   */
  public Rotation3D(Hep3Matrix matrix) {
    _from = matrix;
  }

  /**
   * Create transformation to a reference frame rotated by <tt>angle</tt> around <tt>axis</tt>.
   */
  public Rotation3D(Axis axis, double angle) {
    double sin = Math.sin(angle);
    double cos = Math.cos(angle);
    BasicHep3Matrix active = new BasicHep3Matrix();
    active.setElement(axis.index(), axis.index(), 1.);
    active.setElement((axis.index()+1)%3, (axis.index()+1)%3, cos);
    active.setElement((axis.index()+1)%3, (axis.index()+2)%3, -sin);
    active.setElement((axis.index()+2)%3, (axis.index()+1)%3, sin);
    active.setElement((axis.index()+2)%3, (axis.index()+2)%3, cos);
    _from = active;
  }
  
// -- Operations :  ------------------------------------------------------------
  
  /**
   * Returns coordinates of a point in the transformed reference frame given its 
   * coordinates in the original reference frame. 
   */
  public Hep3Vector transformTo(Hep3Vector vector) {
    if (_to == null) _to = VecOp.inverse(_from);
    return VecOp.mult(_to, vector);
  }
  
  /**
   * Returns coordinates of a point in the original reference frame given its 
   * coordinates in the transformed reference frame. 
   */
  public Hep3Vector transformFrom(Hep3Vector vector) {
    if (_from == null) _from = VecOp.inverse(_to);
    return VecOp.mult(_from, vector);
  }
  
  /**
   * Returns covariance matrix in the transformed reference frame given the 
   * covariance matrix in the original reference frame. 
   */
  public SymmetricMatrix transformTo(SymmetricMatrix covMatrix) {
    if (_to == null) _to = VecOp.inverse(_from);
    double[] loc = new double[6];
    int index = -1;
    for (int l = 0; l <3; l++) {
      for (int k = 0; k <= l; k++) {
        loc[++index] = 0.;
        for (int i = 0; i<3; i++) {
          for (int j = 0; j<= i; j++) {
            double term = _to.e(l,i)*_to.e(k,j)*covMatrix.e(i,j);
            loc[index] += (i==j) ? term : 2.*term;
          }
        }
      }
    }
    return new SymmetricMatrix(3, loc, true);
  }
  
  /**
   * Returns covariance matrix in the original reference frame given the 
   * covariance matrix in the transformed reference frame. 
   */
  public SymmetricMatrix transformFrom(SymmetricMatrix covMatrix) {
    if (_from == null) _from = VecOp.inverse(_to);
    double[] glob = new double[6];
    int index = -1;
    for (int l = 0; l <3; l++) {
      for (int k = 0; k <= l; k++) {
        glob[++index] = 0.;
        for (int i = 0; i<3; i++) {
          for (int j = 0; j<= i; j++) {
            double term = _from.e(l,i)*_from.e(k,j)*covMatrix.e(i,j);
            glob[index] += (i==j) ? term : 2.*term;
          }
        }
      }
    }
    return new SymmetricMatrix(3, glob, true);
  }
  
  /**
   * Returns covariance matrix in the transformed reference frame given the 
   * covariance matrix and position in the original reference frame. 
   */
  public SymmetricMatrix transformTo(SymmetricMatrix covMatrix, Hep3Vector vector) {
    return transformTo(covMatrix);
  }
  
  /**
   * Returns covariance matrix in the original reference frame given the 
   * covariance matrix and position in the transformed reference frame. 
   */
  public SymmetricMatrix transformFrom(SymmetricMatrix covMatrix, Hep3Vector vector) {
    return transformFrom(covMatrix);
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  Hep3Matrix _to;
  Hep3Matrix _from;
}
