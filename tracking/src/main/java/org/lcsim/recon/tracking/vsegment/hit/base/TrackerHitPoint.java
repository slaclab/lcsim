package org.lcsim.recon.tracking.vsegment.hit.base;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpacePointVector;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;

/**
 * Implementation of {@link TrackerHit} suitable for representing point-like hits.
 * Position and covariance matrix can be supplied to the constructor in either
 * global reference frame or <tt>Sensor</tt> local frame. Conversion will only be
 * done if needed, and the results will be cached.
 *
 * @author D.Onoprienko
 * @version $Id: TrackerHitPoint.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class TrackerHitPoint extends TrackerHitAdapter {
  
// -- Constructors :  ----------------------------------------------------------
  
  /**
   * Construct a new TrackerHit.
   * Vector and matrix supplied to the constructor will be owned by the created hit.
   * Signal and time associated with this hit will be set to those of the <tt>TrackerCluster</tt>.
   *
   * @param cluster    {@link TrackerCluster} from which this hit was created.
   * @param position   Position of the hit.
   * @param covMatrix  Covariance matrix
   * @param isLocal    <tt>true</tt> if position and covariance matrix are given
   *                   in the <tt>Sensor</tt> local reference frame, <tt>false</tt>
   *                   if they are given in the global frame.
   */
  public TrackerHitPoint(TrackerCluster cluster, Hep3Vector position, SymmetricMatrix covMatrix, boolean isLocal) {
    super(cluster);
    if (isLocal) {
      _posLocal = position;
      _covLocal = covMatrix;
    } else {
      _posGlobal = position;
      _covGlobal = covMatrix;      
    }
  }
  
  /**
   * Construct a new TrackerHit.
   * Vector and matrix supplied to the constructor will be owned by the created hit.
   *
   * @param cluster    {@link TrackerCluster} from which this hit was created.
   * @param position   Position of the hit.
   * @param covMatrix  Covariance matrix
   * @param isLocal    <tt>true</tt> if position and covariance matrix are given
   *                   in the <tt>Sensor</tt> local reference frame, <tt>false</tt>
   *                   if they are given in the global frame.
   * @param signal     Signal amplitude to be associated with this hit.
   * @param time       Time to be associated with this hit.
   */
  public TrackerHitPoint(TrackerCluster cluster, Hep3Vector position, SymmetricMatrix covMatrix, boolean isLocal,
                         double signal, double time) {
    super(cluster, signal, time);
    if (isLocal) {
      _posLocal = position;
      _covLocal = covMatrix;
    } else {
      _posGlobal = position;
      _covGlobal = covMatrix;      
    }
  }
  
// -- Position and covariance matrix in local coordinates :  -------------------
  
  /** 
   * Returns position of the hit in local reference frame of the {@link Sensor}.
   */
  public Hep3Vector getLocalPosition() {
    if (_posLocal == null) _posLocal = super.getLocalPosition();
    return _posLocal;
  }
  
  /** 
   * Returns covariance matrix in local frame. 
   */
  public SymmetricMatrix getLocalCovMatrix() {
    if (_covLocal == null) _covLocal = super.getLocalCovMatrix();
    return _covLocal;
  }
  
  /** 
   * Returns <tt>SpacePointVector</tt> pointing from start to end of the segment 
   * defining the hit in the local reference frame.
   */
  public SpacePointVector getLocalSegment() {
    if (_posLocal == null) _posLocal = super.getLocalPosition();
    SpacePoint loc = new SpacePoint(_posLocal);
    return new SpacePointVector(loc, loc);
  }
  
// -- Position and covariance matrix in global coordinates :  ------------------
  
  /**
   * 
   * Returns position of the hit in global reference frame.
   */
  public Hep3Vector getPosition() {
    if (_posGlobal == null) _posGlobal = super.getPosition();
    return _posGlobal;
  }
  
  /** 
   * Returns covariance matrix of the hit in global reference frame. 
   */
  public SymmetricMatrix getCovMatrix() {
    if (_covGlobal == null) _covGlobal = super.getCovMatrix();
    return _covGlobal;
  }

  /** 
   * Returns <tt>SpacePointVector</tt> pointing from start to end of the segment 
   * defining the hit in the global reference frame.
   */
  public SpacePointVector getSegment() {
    if (_posGlobal == null) _posGlobal = super.getPosition();
    SpacePoint glob = new SpacePoint(_posGlobal);
    return new SpacePointVector(glob, glob);
  }
  
// -- Length of segment-like hit :  --------------------------------------------
  
  /**
   * 
   * Returns zero (length of the segment defining the hit). 
   */
  public double getLength() {
    return 0.;
  }
  
// -- Private parts :  ---------------------------------------------------------

  Hep3Vector _posLocal;
  Hep3Vector _posGlobal;
  SymmetricMatrix _covLocal;
  SymmetricMatrix _covGlobal;
}
