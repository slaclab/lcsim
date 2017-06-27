package org.lcsim.recon.tracking.vsegment.transform;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

/**
 * An object of this type defines a reference frame transformation in 3D space.
 * The origins of the two frames are assumed to be at the same point.
 *
 * @author D.Onoprienko
 * @version $Id: Transformation3D.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public interface Transformation3D {
  
  /**
   * Returns coordinates of a point in the transformed reference frame given its 
   * coordinates in the original reference frame. 
   */
  public Hep3Vector transformTo(Hep3Vector vector);
  
  /**
   * Returns coordinates of a point in the original reference frame given its 
   * coordinates in the transformed reference frame. 
   */
  public Hep3Vector transformFrom(Hep3Vector vector);
  
  /**
   * Returns covariance matrix in the transformed reference frame given the 
   * covariance matrix in the original reference frame. 
   * Throws <tt>RuntimeException</tt> if transformed covariance matrix cannot be
   * computed based on the original covariance matrix alone.
   */
  public SymmetricMatrix transformTo(SymmetricMatrix covMatrix);
  
  /**
   * Returns covariance matrix in the original reference frame given the 
   * covariance matrix in the transformed reference frame. 
   * Throws <tt>RuntimeException</tt> if original covariance matrix cannot be
   * computed based on the transformed covariance matrix alone.
   */
  public SymmetricMatrix transformFrom(SymmetricMatrix covMatrix);
  
  /**
   * Returns covariance matrix in the transformed reference frame given the 
   * covariance matrix and position in the original reference frame. 
   */
  public SymmetricMatrix transformTo(SymmetricMatrix covMatrix, Hep3Vector vector);
  
  /**
   * Returns covariance matrix in the original reference frame given the 
   * covariance matrix and position in the transformed reference frame. 
   */
  public SymmetricMatrix transformFrom(SymmetricMatrix covMatrix, Hep3Vector vector);
  
}
