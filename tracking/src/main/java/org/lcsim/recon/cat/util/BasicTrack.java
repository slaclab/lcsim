package org.lcsim.recon.cat.util;

import java.util.*;

import org.lcsim.event.*;
import org.lcsim.util.*;
import hep.physics.matrix.SymmetricMatrix;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;
import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsEvent;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.geometry.Detector;
import org.lcsim.util.swim.HelixSwimmer;

/**
 * Basic implementation of {@link org.lcsim.event.Track} interface.
 * Includes method for initializing track parameters from momentum at a given point.
 *
 * @author D. Onoprienko
 * @version $Id: BasicTrack.java,v 1.4 2012/06/18 23:02:14 jeremy Exp $
 */
public class BasicTrack implements Track {

// -- Constructors and static initialization :  --------------------------------
 
  /** 
   * Initialization of static members.
   * Runs automatically once the detector classes have been initialized.
   */
  static public void initialize(Detector detector) {
    _bField = detector.getFieldMap().getField(new double[]{0.,0.,0.})[2];
    _swimmer = new HelixSwimmer(_bField);
  }
  
  /** Default constructor. */
  public BasicTrack() {
    _refPoint = new double[3];
    _refPointIsPCA = false;
    _parameters = new double[5];
    _covMatrix = new SymmetricMatrix(5);
    _momentum = new double[3];
    _charge = 0;
    _fitSuccess = false;
    _chi2 = 0.;
    _ndf = 1;
    _dEdx = 0.;
    _dEdxErr = 0.;
    _innermostHitRadius = 9999.;
    _nHitsSubdet = new int[4];
    _tracks = new ArrayList<Track>(1);
    _hits = new ArrayList<TrackerHit>(1);
    _type = 0;
  }
  
  /** Copy constructor. */
  public BasicTrack(BasicTrack track) {
    _refPoint = new double[3];
    System.arraycopy(track._refPoint, 0, _refPoint, 0, 3);
    _refPointIsPCA = track._refPointIsPCA;
    _parameters = new double[5];
    System.arraycopy(track._parameters, 0, _parameters, 0, 5);
    _covMatrix = new SymmetricMatrix(track._covMatrix);
    _momentum = new double[3];
    System.arraycopy(track._momentum, 0, _momentum, 0, 3);
    _charge = track._charge;
    _fitSuccess = track._fitSuccess;
    _chi2 = track._chi2;
    _ndf = track._ndf;
    _dEdx = track._dEdx;
    _dEdxErr = track._dEdxErr;
    _innermostHitRadius = track._innermostHitRadius;
    _nHitsSubdet = new int[4];
    System.arraycopy(track._nHitsSubdet, 0, _nHitsSubdet, 0, 4);
    _tracks = new ArrayList<Track>(track._tracks);
    _hits = new ArrayList<TrackerHit>(track._hits);
    _type = track._type;
  }

// -- Standard getters (implementing Track interface) :  -----------------------
  
  public int getCharge() {return _charge;}

  public double[] getReferencePoint() {return _refPoint;}
  public double getReferencePointX()  {return _refPoint[0];}
  public double getReferencePointY()  {return _refPoint[1];}
  public double getReferencePointZ()  {return _refPoint[2];}

  public boolean isReferencePointPCA() {return _refPointIsPCA;}

  public double[] getMomentum() {return _momentum;}
  public double getPX() {return _momentum[0];}
  public double getPY() {return _momentum[1];}
  public double getPZ() {return _momentum[2];}

  public double[] getTrackParameters() {return _parameters;}
  public double getTrackParameter(int i) {return _parameters[i];}
  public SymmetricMatrix getErrorMatrix() {return _covMatrix;}

  public boolean fitSuccess() {return _fitSuccess;}
  public double getChi2() {return _chi2;}
  public int getNDF() {return _ndf;}

  public double getdEdx() {return _dEdx;}
  public double getdEdxError() {return _dEdxErr;}

  public double getRadiusOfInnermostHit() {return _innermostHitRadius;}

  public int[] getSubdetectorHitNumbers() {return _nHitsSubdet;}

  public List<Track> getTracks() {return _tracks;}

  public List<TrackerHit> getTrackerHits() {return _hits;}

  public int getType() {return _type;}
  
// -- Additional getters :  ----------------------------------------------------
  
  /** Returns transverse momentum of the track. */
  public double getPt() {
    return Math.hypot(_momentum[0], _momentum[1]);
  }
  
  /** Returns amplitude of the track momentum. */
  public double getP() {
    double[] p = getMomentum();
    return Math.sqrt(_momentum[0]*_momentum[0]+_momentum[1]*_momentum[1]+_momentum[2]*_momentum[2]);
  }
  
// -- Setters :  ---------------------------------------------------------------
  
  /** Add track to the list of tracks associated with this track. */
  public void addTrack(Track track) {_tracks.add(track);}
  
  /** Removes track from the list of tracks associated with this track. */
  public void removeTrack(Track track) {_tracks.remove(track);}
  
  /** Remove all tracks from the list of tracks associated with this track. */
  public void removeTracks() {_tracks.clear();}
  
  public void setHelixParameters(SpacePoint refPoint, SpacePoint position, SpaceVector momentum, int charge) {
    _refPoint = refPoint.v();
    _swimmer.setTrack(momentum, position, charge);
    double alpha = _swimmer.getTrackLengthToPoint(refPoint);
    SpacePoint poca = _swimmer.getPointAtLength(alpha);
    SpaceVector momentumAtPoca = _swimmer.getMomentumAtLength(alpha);
    LCIOParameters parameters = LCIOParameters.SpaceMomentum2Parameters(poca, momentumAtPoca, refPoint, charge, _bField);
    _parameters = parameters.getValues();
    _momentum = momentumAtPoca.v();
    _charge = charge;
  }
  
// -- Private parts :  ---------------------------------------------------------

  protected double[] _refPoint;
  protected boolean _refPointIsPCA;
  protected double[] _parameters;
  protected SymmetricMatrix _covMatrix;
  protected double[] _momentum;
  protected int _charge;
  protected boolean _fitSuccess;
  protected double _chi2;
  protected int _ndf;
  protected double _dEdx;
  protected double _dEdxErr;
  protected double _innermostHitRadius;
  protected int[] _nHitsSubdet;
  protected ArrayList<Track> _tracks;
  protected ArrayList<TrackerHit> _hits;
  protected int _type;
  
  protected static double _bField;
  protected static HelixSwimmer _swimmer;
  
  protected static ConditionsListener _conListener = new ConditionsListener() {
    public void conditionsChanged(ConditionsEvent event) {
      ConditionsManager conMan = (event == null) ? ConditionsManager.defaultInstance() : event.getConditionsManager();
      try {
        Detector det = conMan.getCachedConditions(Detector.class,"compact.xml").getCachedData();
        initialize(det);
      } catch (ConditionsSetNotFoundException x) {}
    }
  };
  static {
    ConditionsManager.defaultInstance().addConditionsListener(_conListener);
    _conListener.conditionsChanged(null);
  }
  
  public List<TrackState> getTrackStates()
  {
      return null;
  }
}
