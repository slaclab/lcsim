/**
 * @version $Id: FastMCTrack.java,v 1.3 2012/07/26 16:46:14 grefe Exp $
 */
package org.lcsim.mc.fast.tracking.fix;

import static java.lang.Math.sqrt;
import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.LCIOParameters;
import org.lcsim.event.MCParticle;
import org.lcsim.event.Track;
import org.lcsim.event.TrackState;
import org.lcsim.event.TrackerHit;
import org.lcsim.event.LCIOParameters.ParameterName;
import org.lcsim.event.base.BaseTrackState;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.CartesianVector;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;

/**
 * @author jstrube The class to store the measurement information of the track of a charged particle in a magnetic field. This class represents the FastMC simulation. An invariant of this class is
 *         that the "origin" is the point of closest approach to the reference point.
 */
public class FastMCTrack implements Track {
    protected LCIOParameters _parameters;
    protected SymmetricMatrix _errorMatrix;
    protected SpacePoint _referencePoint;
    protected int _charge;
    protected MCParticle _particle = null;
    protected List<TrackState> _trackStates;

    protected FastMCTrack(SpacePoint refPoint, LCIOParameters parameters, SymmetricMatrix errorMatrix, int charge) {
        _referencePoint = refPoint;
        _parameters = parameters;
        _charge = charge;
        _errorMatrix = errorMatrix;
        _trackStates = new ArrayList<TrackState>();
        _trackStates.add(new BaseTrackState(parameters.getValues(), errorMatrix.asPackedArray(true), refPoint.v(), 0));
    }

    protected FastMCTrack(SpacePoint refPoint, LCIOParameters parameters, SymmetricMatrix errorMatrix, int charge, MCParticle part) {
        this(refPoint, parameters, errorMatrix, charge);
        _particle = part;
    }

    public FastMCTrack(Track t) {
        double[] p = t.getMomentum();
        double pt = sqrt(p[0] * p[0] + p[1] * p[1]);
        _parameters = new LCIOParameters(t.getTrackParameters(), pt);
        _errorMatrix = t.getErrorMatrix();
        _referencePoint = new CartesianPoint(t.getReferencePoint());
        _charge = t.getCharge();
        _trackStates = new ArrayList<TrackState>();
        _trackStates.add(new BaseTrackState(_parameters.getValues(), _errorMatrix.asPackedArray(true), _referencePoint.v(), 0));
    }

    public boolean fitSuccess() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.lcsim.contrib.JanStrube.tracking.Track#getCharge()
     */
    public int getCharge() {
        return _charge;
    }

    public double getChi2() {
        return -1;
    }

    public double getdEdx() {
        return 0;
    }

    public double getdEdxError() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.lcsim.contrib.JanStrube.tracking.Track#getErrorMatrix()
     */
    public SymmetricMatrix getErrorMatrix() {
        return _errorMatrix;
    }

    public MCParticle getMCParticle() {
        return _particle;
    }

    public double[] getMomentum() {
        return momentum().v();
    }

    public int getNDF() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.lcsim.contrib.JanStrube.tracking.Track#getParameter(org.lcsim.contrib.JanStrube.tracking.FastMCTrack.ParameterName)
     */
    public double getParameter(ParameterName name) {
        return _parameters.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.lcsim.contrib.JanStrube.tracking.Track#getParameters()
     */
    public LCIOParameters getParameters() {
        return _parameters;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.lcsim.contrib.JanStrube.tracking.Track#getPt()
     */
    public double getPt() {
        return _parameters.getPt();
    }

    public double getPX() {
        return momentum().x();
    }

    public double getPY() {
        return momentum().y();
    }

    public double getPZ() {
        return momentum().z();
    }

    public double getRadiusOfInnermostHit() {
        return -1;
    }

    public double[] getReferencePoint() {
        return _referencePoint.v();
    }

    public double getReferencePointX() {
        return referencePoint().x();
    }

    public double getReferencePointY() {
        return referencePoint().y();
    }

    public double getReferencePointZ() {
        return referencePoint().z();
    }

    public int[] getSubdetectorHitNumbers() {
        return null;
    }

    public List<TrackerHit> getTrackerHits() {
        return null;
    }

    public double getTrackParameter(int i) {
        return _parameters.getValues()[i];
    }

    public double[] getTrackParameters() {
        return _parameters.getValues();
    }

    public List<Track> getTracks() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public boolean isReferencePointPCA() {
        return true;
    }

    public Hep3Vector momentum() {
        return new CartesianVector(LCIOParameters.Parameters2Momentum(_parameters).v());
    }

    public SpacePoint position() {
        return LCIOParameters.Parameters2Position(_parameters, _referencePoint);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.lcsim.contrib.JanStrube.tracking.Track#getReferencePoint()
     */
    public SpacePoint referencePoint() {
        return _referencePoint;
    }

    public List<TrackState> getTrackStates() {
        return _trackStates;
    }
}