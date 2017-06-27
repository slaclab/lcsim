package org.lcsim.recon.tracking.vsegment.hit;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;
import org.lcsim.spacegeom.SpacePointVector;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.transform.Transformation3D;

/**
 * Tracker hit object to be used by a fitter.
 * <tt>TrackerHit</tt> can represent either a point-like (pixel) or a segment-like 
 * (strip) object. Each hit has a local reference frame (u,v,w) associated with it
 * (reference frame of the {@link Sensor} object the hit belongs to).
 * U is the measurement direction, V is along the length of the strip, 
 * <nobr>W = U x V.</nobr>
 *
 * @author D.Onoprienko
 * @version $Id: TrackerHit.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public interface TrackerHit {
  
// -- Position and covariance matrix in local coordinates :  -------------------
  
  /** 
   * Returns position of the hit in local reference frame of the {@link Sensor}.
   * For a segment-like hit, this is the center of the segment.
   */
  public Hep3Vector getLocalPosition();
  
  /** Returns covariance matrix in local frame of the {@link Sensor}. */
  public SymmetricMatrix getLocalCovMatrix();
  
  /** 
   * Returns <tt>SpacePointVector</tt> pointing from start to end of the segment 
   * defining the hit in the local reference frame.
   */
  public SpacePointVector getLocalSegment();
  
// -- Position and covariance matrix in global coordinates :  ------------------
  
  /** 
   * Returns position of the hit in global reference frame.
   * For a segment-like hit, this is the center of the segment.
   */
  public Hep3Vector getPosition();
  
  /** Returns covariance matrix of the hit in global reference frame. */
  public SymmetricMatrix getCovMatrix();
  
  /** 
   * Returns <tt>SpacePointVector</tt> pointing from start to end of the segment 
   * defining the hit in the global reference frame.
   */
  public SpacePointVector getSegment();
  
// -- Length of segment-like hit :  --------------------------------------------
  
  /** Returns length of the segment defining the hit. */
  public double getLength();

// -- Signal and time :  -------------------------------------------------------
  
  /** Returns signal amplitude associated with this hit. */
  public double getSignal();
  
  /** Returns time associated with this hit. */
  public double getTime();
  
// -- Access to underlying Sensor and TrackerCluster objects :  ----------------
  
  /** Returns {@link Sensor} object for this hit. */
  public Sensor getSensor();
  
  /** Points back to <tt>TrackerCluster</tt> from which this hit was produced. */
  public TrackerCluster getCluster();
  
}
