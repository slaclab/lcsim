package org.lcsim.recon.tracking.vsegment.hit.base;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Matrix;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpacePointVector;
import org.lcsim.recon.tracking.vsegment.transform.Transformation3D;

import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;

/**
 * An adapter to be extended by classes implementing {@link TrackerHit} interface.
 * Methods returning position and covariance matrix in global frame are defined using
 * methods returning position and covariance matrix in local frame, and vice versa.
 *
 * @author D.Onoprienko
 * @version $Id: TrackerHitAdapter.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
abstract public class TrackerHitAdapter implements TrackerHit {
  
// -- Constructors :  ----------------------------------------------------------
  
  protected TrackerHitAdapter(TrackerCluster cluster) {
    _cluster = cluster;
    _signal = cluster.getSignal();
    _time = cluster.getTime();
  }
  
  protected TrackerHitAdapter(TrackerCluster cluster, double signal, double time) {
    _cluster = cluster;
    _signal = signal;
    _time = time;
  }
  
// -- Position and covariance matrix in local coordinates :  -------------------
  
  /** 
   * Returns position of the hit in local reference frame of the {@link Sensor}.
   * For a segment-like hit, this is the center of the segment.
   * Default implementation relies on {@link #getPosition()} method.
   */
  public Hep3Vector getLocalPosition() {
    return getSensor().globalToLocal(getPosition());
  }
  
  /** 
   * Returns covariance matrix in local frame. 
   * Default implementation relies on {@link #getCovMatrix()} method.
   */
  public SymmetricMatrix getLocalCovMatrix() {
    return getSensor().globalToLocal(getCovMatrix(), getPosition());
  }
  
  /** 
   * Returns <tt>SpacePointVector</tt> pointing from start to end of the segment 
   * defining the hit in the local reference frame.
   * Default implementation relies on {@link #getSegment()} method.
   */
  public SpacePointVector getLocalSegment() {
    SpacePointVector glob = getSegment();
    Sensor sensor = getSensor();
    return new SpacePointVector(new SpacePoint(sensor.globalToLocal(glob.getStartPoint())), 
                                new SpacePoint(sensor.globalToLocal(glob.getEndPoint())));
  }
  
// -- Position and covariance matrix in global coordinates :  ------------------
  
  /**
   * 
   * Returns position of the hit in global reference frame.
   * For a segment-like hit, this is the center of the segment.
   * Default implementation relies on {@link #getLocalPosition()} method.
   */
  public Hep3Vector getPosition() {
    return getSensor().localToGlobal(getLocalPosition());
  }
  
  /** 
   * Returns covariance matrix of the hit in global reference frame. 
   * Default implementation relies on {@link #getLocalCovMatrix()} method.
   */
  public SymmetricMatrix getCovMatrix() {
    return getSensor().localToGlobal(getLocalCovMatrix(), getLocalPosition());
  }

  /** 
   * Returns <tt>SpacePointVector</tt> pointing from start to end of the segment 
   * defining the hit in the global reference frame.
   * Default implementation relies on {@link #getLocalSegment()} method.
   */
  public SpacePointVector getSegment() {
    SpacePointVector local = getLocalSegment();
    Sensor sensor = getSensor();
    return new SpacePointVector(new SpacePoint(sensor.localToGlobal(local.getStartPoint())), 
                                new SpacePoint(sensor.localToGlobal(local.getEndPoint())));
  }
  
// -- Length of segment-like hit :  --------------------------------------------
  
  /**
   * 
   * Returns length of the segment defining the hit. 
   * Default implementation relies on {@link #getSegment()} method.
   */
  public double getLength() {
    return getSegment().magnitude();
  }

// -- Signal and time :  -------------------------------------------------------
  
  /** Returns signal amplitude associated with this hit. */
  public double getSignal() {return _signal;}
  
  /** Set signal value associated with this hit. */
  public void setSignal(double signal) {_signal = signal;}
  
  /** Returns time associated with this hit. */
  public double getTime() {return _time;}
  
  /** Set time associated with this hit. */
  public void setTime(double time) {_time = time;}

// -- Access to underlying Sensor and TrackerCluster objects :  ----------------
  
  /** Returns {@link Sensor} object for this hit. */
  public Sensor getSensor() {
    return _cluster.getSensor();
  }
  
  /** Points back to <tt>TrackerCluster</tt> that produced this hit. */
  public TrackerCluster getCluster() {return _cluster;}
  
// -- Private parts :  ---------------------------------------------------------

  protected TrackerCluster _cluster;
  protected double _signal;
  protected double _time;
  
}
