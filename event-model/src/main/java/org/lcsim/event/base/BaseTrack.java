package org.lcsim.event.base;

import static java.lang.Math.abs;
import static java.lang.Math.signum;
import hep.physics.matrix.SymmetricMatrix;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.constants.Constants;
import org.lcsim.event.LCIOParameters.ParameterName;
import org.lcsim.event.Track;
import org.lcsim.event.TrackState;
import org.lcsim.event.TrackerHit;

/**
 * Base implementation of LCIO Track interface.
 * 
 * Modified for LCIO v2 compatibility: added TrackStates.
 * 
 * @author Norman Graf
 * @author Jeremy McCormick
 * @version $Id: BaseTrack.java,v 1.15 2012/10/18 19:32:29 jeremy Exp $
 */
public class BaseTrack implements Track {

    public enum TrackType {
        Z_FIELD, Y_FIELD
    };

    // These three now stored in TrackStates but kept here for backward compatibility.
    protected double[] _refPoint = new double[3];
    protected double[] _parameters = new double[5];

    protected SymmetricMatrix _covMatrix = new SymmetricMatrix(5);

    protected double[] _momentum = new double[3];
    protected double _chi2;
    protected boolean _refPointIsDCA = true;
    protected int _charge;
    protected boolean _fitSuccess = true;
    protected int _ndf;
    protected double _dEdx;
    protected double _dEdxErr;
    protected double _innermostHitRadius = 9999.;
    protected int _type;
    protected int[] _subdetId = new int[1];

    // References to other objects.
    protected List<Track> _tracks;
    protected List<TrackerHit> _hits;
    protected List<TrackState> _trackStates;

    // Parameter ordering.
    public static final int D0 = ParameterName.d0.ordinal();
    public static final int PHI = ParameterName.phi0.ordinal();
    public static final int OMEGA = ParameterName.omega.ordinal();
    public static final int TANLAMBDA = ParameterName.tanLambda.ordinal();
    public static final int Z0 = ParameterName.z0.ordinal();

    /** Creates a new instance of BaseTrack */
    public BaseTrack() {
        _tracks = new ArrayList<Track>();
        _hits = new ArrayList<TrackerHit>();
        _trackStates = new ArrayList<TrackState>();
    }

    /**
     * This gets the first TrackState as a BaseTrackState, so it can be modified. It will create
     * this TrackState, if it doesn't exist already.
     * @return The first TrackState.
     */
    private BaseTrackState getFirstTrackState() {
        if (_trackStates.size() == 0) {
            _trackStates.add(new BaseTrackState());
        }
        return (BaseTrackState) _trackStates.get(0);
    }

    // add following setters for subclasses.

    public void setTrackParameters(double[] params, double magneticField) {
        // Copy to this object's parameters array.
        System.arraycopy(params, 0, _parameters, 0, 5);

        // Compute momentum from parameters and magnetic field.
        double omega = _parameters[OMEGA];
        if (abs(omega) < 0.0000001)
            omega = 0.0000001;
        double Pt = abs((1. / omega) * magneticField * Constants.fieldConversion);
        _momentum[0] = Pt * Math.cos(_parameters[PHI]);
        _momentum[1] = Pt * Math.sin(_parameters[PHI]);
        _momentum[2] = Pt * _parameters[TANLAMBDA];

        // Compute charge.
        _charge = (int) signum(omega);

        // LCIO v2 ... setup a TrackState with full parameter list.
        getFirstTrackState().setParameters(params, magneticField);
    }

    // TODO replace this with a SpacePoint
    /**
     * Set the reference point for this track. By default it is (0,0,0). By definition, DCA, etc.
     * is measured with respect to this point.
     * @param point The (x,y,z) reference point for this track.
     */
    public void setReferencePoint(double[] point) {
        this._refPoint = point;

        // Set the ref point on the first TrackState.
        getFirstTrackState().setReferencePoint(point);
    }

    /**
     * Set the covariance matrix for the track parameters.
     * @param cov The covariance matrix as a SymetricMatrix.
     */
    public void setCovarianceMatrix(SymmetricMatrix cov) {
        _covMatrix = cov;

        // Set the covariance matrix on the TrackState, converting to a double array.
        getFirstTrackState().setCovMatrix(_covMatrix.asPackedArray(true));
    }

    /**
     * Sets whether the reference point is the distance of closest approach to the origin.
     * @param isDCA true if the reference point is the dca.
     */
    // TODO clarify this.
    public void setRefPointIsDCA(boolean isDCA) {
        _refPointIsDCA = isDCA;
    }

    /**
     * If the track has been successfully set, this should be set. Should only be set if the
     * results of the fit are also set. Should encapsulate all this in a Fit object. False by
     * default.
     * @param success true if the track has successfully been set.
     */
    public void setFitSuccess(boolean success) {
        _fitSuccess = success;
    }

    /**
     * Set the chi-squared for the track fit. Not defined whether this is the full or reduced
     * chi-squared.
     * @param chisq The value of the track fit chi-squared.
     */
    // TODO verify if this is full or reduced chi-squared.
    public void setChisq(double chisq) {
        _chi2 = chisq;
    }

    /**
     * Set the number of degrees of freedom for this track fit.
     * @param n The number of degrees of freedom for this track fit.
     */
    public void setNDF(int n) {
        _ndf = n;
    }

    /**
     * Set the track type. Note that this is still undefined.
     * @param type The track type
     */
    // TODO define this. replace int by enumeration.
    public void setTrackType(int type) {
        _type = type;
    }

    /**
     * Add a hit to this track.
     * @param hit The TrackerHit to add to this track.
     */
    public void addHit(TrackerHit hit) {
        _hits.add(hit);
        double[] pos = hit.getPosition();
        double radius = pos[0] * pos[0] + pos[1] * pos[1];
        if (radius < _innermostHitRadius * _innermostHitRadius) {
            _innermostHitRadius = Math.sqrt(radius);
        }
        _dEdx += hit.getdEdx();
    }

    /**
     * Add a list of hits to this track.
     * @param hits The list of TrackerHits to add to this track.
     */
    public void addHits(List<TrackerHit> hits) {
        _hits.addAll(hits);
        for (TrackerHit hit : hits) {
            double[] pos = hit.getPosition();
            double radius = pos[0] * pos[0] + pos[1] * pos[1];
            if (radius < _innermostHitRadius * _innermostHitRadius) {
                _innermostHitRadius = Math.sqrt(radius);
            }
            _dEdx += hit.getdEdx();
        }
    }

    // TODO finish this...
    public String toString() {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if (lastDot != -1)
            className = className.substring(lastDot + 1);
        StringBuffer sb = new StringBuffer(className + ": Type: " + _type + " charge: " + _charge + "\n");
        sb.append("d0= " + _parameters[D0] + "\n");
        sb.append("phi0= " + _parameters[PHI] + "\n");
        sb.append("curvature: " + _parameters[OMEGA] + "\n");
        sb.append("z0= " + _parameters[Z0] + "\n");
        sb.append("tanLambda= " + _parameters[TANLAMBDA] + "\n");
        sb.append(" px=" + getPX() + " py= " + getPY() + " pz= " + getPZ());
        return sb.toString();
    }

    // TODO add convenience methods to replace clunky interface
    // Track interface

    /**
     * The charge of the particle creating this track in units of the electron charge.
     * @return The charge of the track.
     */
    public int getCharge() {
        return _charge;
    }

    /**
     * Return the reference point of this track. Need to clarigy whether this is a point on the
     * track or not.
     * @return The reference point for this track.
     */
    // TODO augment this with a SpacePoint.
    public double[] getReferencePoint() {
        return getFirstTrackState().getReferencePoint();
    }

    /**
     * Return the x position of the reference point for this track.
     * @return The x position of the reference point for this track.
     */
    public double getReferencePointX() {
        return getFirstTrackState().getReferencePoint()[0];
    }

    /**
     * Return the y position of the reference point for this track.
     * @return The y position of the reference point for this track.
     */
    public double getReferencePointY() {
        return getFirstTrackState().getReferencePoint()[1];
    }

    /**
     * Return the z position of the reference point for this track.
     * @return The z position of the reference point for this track.
     */
    public double getReferencePointZ() {
        return getFirstTrackState().getReferencePoint()[2];
    }

    /**
     * Is the reference point for this track the DCA? This needs clarification
     * @return true if the reference point is the dca.
     */
    // TODO clarify what this means.
    public boolean isReferencePointPCA() {
        return false;
    }

    /**
     * The cartesian momentum for this track (px, py, pz)
     * @return The momentum of this track.
     */
    public double[] getMomentum() {
        return getFirstTrackState().getMomentum();
    }

    /**
     * The x component of the momentum of this track.
     * @return The x component of the momentum of this track.
     */
    public double getPX() {
        return getFirstTrackState().getMomentum()[0];
    }

    /**
     * The y component of the momentum of this track.
     * @return The y component of the momentum of this track.
     */
    public double getPY() {
        return getFirstTrackState().getMomentum()[1];
    }

    /**
     * The z component of the momentum of this track.
     * @return The z component of the momentum of this track.
     */
    public double getPZ() {
        return getFirstTrackState().getMomentum()[2];
    }

    /**
     * Return whether the track was successfully fit.
     * @return true if this track was successfully fir.
     */
    public boolean fitSuccess() {
        return _fitSuccess;
    }

    /**
     * Return an individual track parameter
     * @see Track.Parameter
     * @param i the index of the track parameter desired
     * @return The value of the ith track parameter
     */
    public double getTrackParameter(int i) {
        return getFirstTrackState().getParameter(i);
    }

    /**
     * Return the track parameters.
     * @see Track.Parameter
     * @return The track parameters.
     */
    public double[] getTrackParameters() {
        return getFirstTrackState().getParameters();
    }

    /**
     * Return the track covariance matrix.
     * @return the track covariance matrix as an array.
     */
    public SymmetricMatrix getErrorMatrix() {
        return _covMatrix;
    }

    /**
     * The track fit chi-squared.
     * @return The chi-squared of the track fit.
     */
    public double getChi2() {
        return _chi2;
    }

    /**
     * The number of degrees of freedom in the track fit.
     * @return The number of degrees of freedom in the track fit.
     */
    public int getNDF() {
        return _ndf;
    }

    /**
     * The ionization associated with this track.
     * @return the energy deposited along this track in GeV.
     */
    public double getdEdx() {
        return _dEdx;
    }

    /**
     * The uncertainty on the ionization associated with this track.
     * @return The uncertainty on the ionization associated with this track.
     */
    public double getdEdxError() {
        return _dEdxErr;
    }

    /**
     * The innermost radius of a hit on this track. For values smaller than this the track must be,
     * or has been, extrapolated.
     * @return The innermost radius of a hit on this track.
     */
    // TODO verify that this is in global coordinates, not wrt reference point.
    public double getRadiusOfInnermostHit() {
        return _innermostHitRadius;
    }

    /**
     * The ids of the subdetectors hit by this track. Not yet defined.
     * @return a list of integers representing the subdetector ids hit by this track.
     */
    // TODO establish what this means.
    public int[] getSubdetectorHitNumbers() {
        return _subdetId;
    }

    public void setSubdetectorHitNumbers(int[] subdetId) {
        this._subdetId = subdetId;
    }

    /**
     * If this is a composite track, return a list of constituent tracks.
     * @return the list of individual tracks of which this track is composed.
     */
    public List<Track> getTracks() {
        return _tracks;
    }

    /**
     * Return the list of tracker hits of which this track is composed.
     * @return the list of hits on this track.
     */
    public List<TrackerHit> getTrackerHits() {
        return _hits;
    }

    /**
     * Return the type of this track. Not yet defined.
     * @return an integer representation of the type of this track.
     */
    public int getType() {
        return _type;
    }

    /**
     * Get the list of associated <code>TrackState</code> objects.
     * @return The list of TrackStates.
     */
    public List<TrackState> getTrackStates() {
        return this._trackStates;
    }
}