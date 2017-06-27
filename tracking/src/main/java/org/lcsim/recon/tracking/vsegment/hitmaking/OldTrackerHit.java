package org.lcsim.recon.tracking.vsegment.hitmaking;

import java.util.*;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;

/**
 * Implementation of org.lcsim.event.TrackerHit for converting hit objects used by
 * this package into old <tt>TrackerHits</tt>.
 *
 * @author D. Onoprienko
 * @version $Id: OldTrackerHit.java,v 1.3 2011/08/24 18:51:18 jeremy Exp $
 */
public class OldTrackerHit implements org.lcsim.event.TrackerHit {
  
// -- Constructors :  ----------------------------------------------------------
  
  public OldTrackerHit(Hep3Vector position, SymmetricMatrix covMatrix, 
                       double signal, double time, int type, 
                       List<TrackerCluster> parentClusters) {
    _pos = position.v();
    _cov = covMatrix.asPackedArray(true);
    _dedx = signal;
    _time = time;
    _type = type;
    _clusters = parentClusters;
  }
  
  public OldTrackerHit(Hep3Vector position, double[] covMatrix, 
                       double signal, double time, int type, 
                       List<TrackerCluster> parentClusters) {
    _pos = position.v();
    _cov = covMatrix;
    _dedx = signal;
    _time = time;
    _type = type;
    _clusters = parentClusters;
  }
  
// -- Implementing org.lcsim.event.TrackerHit :  -------------------------------
  
  /** 
   * The hit position in [mm].
   */
  public double[] getPosition() {return _pos;}
  
  /**
   * Covariance of the position (x,y,z)
   */
  public double[] getCovMatrix() {return _cov;}
  
  /** 
   * The dE/dx of the hit in [GeV].
   */
  public double getdEdx() {return _dedx;}
  
  /** 
   * The  time of the hit in [ns].
   */
  public double getTime() {return _time;}
  
  /** 
   * Type of hit. Mapping of integer types to type names
   * through collection parameters "TrackerHitTypeNames"
   * and "TrackerHitTypeValues".
   */
  public int getType() {return _type;}
  
  /** 
   * The raw data hits.
   * Check getType() to get actual data type.
   */
  public List getRawHits() {return _rawHits;}
  
// -- Additional getters :  ----------------------------------------------------
  
  /**
   * Returns a list of <tt>TrackerClusters</tt> from which this hit was produced.
   */
  public List<TrackerCluster> getClusters() {return _clusters;}
  
  /**
   * Returns <tt>true</tt> if this hit is a cross between segment-like hits in
   * stereo layers.
   */
  public boolean isStereo() {return _clusters.size() > 1;}
  
// -- Setters :  ---------------------------------------------------------------
  
  /**
   * Set the list of raw hits.
   * The list supplied to this method will be owned by this <tt>OldTrackerHit</tt> object.
   */
  public void setRawHits(List rawHits) {_rawHits = rawHits;}
  
// -- Private parts :  ---------------------------------------------------------
   
   protected double[] _pos;
   protected double[] _cov;
   protected double _dedx;
   protected double _time;
   protected int _type;
   protected List _rawHits;
   
   protected List<TrackerCluster> _clusters;
   
   public double getEdepError()
   {
       return 0;
   }
   
   public int getQuality()
   {
       return 0;
   }
   
   public long getCellID()
   {
       if (true) throw new UnsupportedOperationException("This method is not implemented in this class.");
       return 0;
   }    
}