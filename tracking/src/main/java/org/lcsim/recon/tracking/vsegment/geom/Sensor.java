package org.lcsim.recon.tracking.vsegment.geom;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import hep.physics.matrix.SymmetricMatrix;
import org.lcsim.detector.IDetectorElement;

import org.lcsim.recon.tracking.vsegment.transform.Rotation3D;
import org.lcsim.recon.tracking.vsegment.transform.Transformation3D;

/**
 * Representation of a silicon sensor that can be further divided 
 * into strips or pixels. Each sensor has a local reference frame associated with it, 
 * and knows how to transform coordinates between local and global frames. 
 *
 * @author D.Onoprienko
 * @version $Id: Sensor.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class Sensor {
  
// -- Constructors :  ----------------------------------------------------------
  
  public Sensor(IDetectorElement de, int id, SensorType type, Hep3Vector translation, Transformation3D rotation) {
    _de = de;
    _id = id;
    _type = type;
    _translation = translation;
    _rotation = rotation;
  }
  
// -- Getters :  ---------------------------------------------------------------
  
  /**
   * Returns {@link IDetectorElement} object this sensor belongs to.
   */
  public IDetectorElement getDetectorElement() {return _de;}
  
  /**
   * Returns {@link SensorType} object representing the geometry of this sensor.
   */
  public SensorType getType() {return _type;}
  
  /**
   * Returns a unique integer ID associated with this sensor.
   */
  public int getID() {return _id;}
  
  /** 
   * Returns vector in global frame pointing from global to local reference frame origin.
   */
  public Hep3Vector getTranslation() {return _translation;}
  
  /**
   * Returns transformation from translated global to local reference frame.
   */
  public Transformation3D getRotation() {return _rotation;}
  
  /** 
   * Converts vector coordinates from local to global reference frame. 
   */
  public Hep3Vector localToGlobal(Hep3Vector point) {
    return VecOp.add(_translation, _rotation.transformFrom(point));
  }
  
  /**
   * Converts vector coordinates from global to local reference frame.
   */
  public Hep3Vector globalToLocal(Hep3Vector point) {
    return _rotation.transformTo(VecOp.sub(point, _translation));
  }
  
  /** 
   * Converts covariance matrix from local to global reference frame. 
   * Throws <tt>RuntimeException</tt> if transformed covariance matrix cannot be
   * computed based on the original covariance matrix alone.
   */
  public SymmetricMatrix localToGlobal(SymmetricMatrix covMatrix) {
    return _rotation.transformFrom(covMatrix);
  }
  
  /**
   * Converts covariance matrix from global to local reference frame.
   * Throws <tt>RuntimeException</tt> if transformed covariance matrix cannot be
   * computed based on the original covariance matrix alone.
   */
  public SymmetricMatrix globalToLocal(SymmetricMatrix covMatrix) {
    return _rotation.transformTo(covMatrix);
  }
  
  /** 
   * Converts covariance matrix from local to global reference frame. 
   */
  public SymmetricMatrix localToGlobal(SymmetricMatrix covMatrix, Hep3Vector position) {
    return _rotation.transformFrom(covMatrix, position);
  }
  
  /**
   * Converts covariance matrix from global to local reference frame.
   */
  public SymmetricMatrix globalToLocal(SymmetricMatrix covMatrix, Hep3Vector position) {
    return _rotation.transformTo(covMatrix, position);
  }

// -- Private parts :  ---------------------------------------------------------

  private IDetectorElement _de;
  private SensorType _type;
  private int _id;
  private Hep3Vector _translation;
  private Transformation3D _rotation;

}
