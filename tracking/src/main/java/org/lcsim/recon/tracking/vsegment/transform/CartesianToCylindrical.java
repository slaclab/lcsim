package org.lcsim.recon.tracking.vsegment.transform;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

/**
 * Coordinates transformation from cartesian to cylindrical reference frame: 
 * (x,y,z) --> (phi,z,ro).
 *
 * @author D.Onoprienko
 * @version $Id: CartesianToCylindrical.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class CartesianToCylindrical  implements Transformation3D {
  
// -- Constructors :  ----------------------------------------------------------
  
  public CartesianToCylindrical() {
  }
  
// -----------------------------------------------------------------------------
  
  /**
   * Returns coordinates of a point in the transformed reference frame given its 
   * coordinates in the original reference frame. 
   */
  public Hep3Vector transformTo(Hep3Vector point) {
    double x = point.x();
    double y = point.y();
    return new BasicHep3Vector(Math.atan2(y,x), point.z(), Math.hypot(x,y));
  }
  
  /**
   * Returns coordinates of a point in the original reference frame given its 
   * coordinates in the transformed reference frame. 
   */
  public Hep3Vector transformFrom(Hep3Vector point) {
    double r = point.z();
    double phi = point.x();
    return new BasicHep3Vector(r*Math.cos(phi), r*Math.sin(phi), point.y());
  }
  
  /**
   * Returns covariance matrix in the transformed reference frame given the 
   * covariance matrix in the original reference frame. 
   */
  public SymmetricMatrix transformTo(SymmetricMatrix covMatrix) {
    throw new RuntimeException(_em);
  }
  
  /**
   * Returns covariance matrix in the original reference frame given the 
   * covariance matrix in the transformed reference frame. 
   */
  public SymmetricMatrix transformFrom(SymmetricMatrix covMatrix) {
    throw new RuntimeException(_em);
  }
  
  /**
   * Returns covariance matrix in the transformed reference frame given the 
   * covariance matrix and position in the original reference frame. 
   */
  public SymmetricMatrix transformTo(SymmetricMatrix covMatrix, Hep3Vector vector) {
    throw new RuntimeException("Covariance matrix transformation is not yet implemented");    
  }
  
  /**
   * Returns covariance matrix in the original reference frame given the 
   * covariance matrix and position in the transformed reference frame. 
   */
  public SymmetricMatrix transformFrom(SymmetricMatrix covMatrix, Hep3Vector vector) {
    double phi = vector.x();
    double z = vector.y();
    double r = vector.z();
    double cos = Math.cos(phi);
    double sin = Math.sin(phi);
    _f[0][0] = - r * sin;
    _f[0][2] = cos;
    _f[1][0] = r * cos;
    _f[1][2] = sin;
    SymmetricMatrix cCar = new SymmetricMatrix(3);
    for (int i=0; i<3; i++) {
      for (int j=0; j<3; j++) {
        double c = 0.;
        for (int k=0; k<3; k++) {
          for (int l=0; l<3; l++) {
            c += _f[i][k] * _f[j][l] * covMatrix.e(k,l);
          }
        }
        cCar.setElement(i,j,c);
      }
    }
    return cCar;
  }

  // -- Private parts :  -------------------------------------------------------
  
  private String _em = "CartesianToCylindrical: Covariance matrix transformation requires position";
  
  private double[][] _f = {{222.,0.,222.}, {222.,0.,222.}, {0.,1.,0.}};
  
}
