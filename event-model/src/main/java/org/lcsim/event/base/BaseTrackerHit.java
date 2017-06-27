/*
 * BaseTrackerHit.java
 *
 * Created on March 24, 2006, 9:22 AM
 *
 * $Id: BaseTrackerHit.java,v 1.7 2011/08/24 18:51:17 jeremy Exp $
 */

package org.lcsim.event.base;

import java.util.ArrayList;
import java.util.List;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.TrackerHit;

/**
 * // TODO add methods to add hits to this object. // TODO decide what these hits should be.
 * 
 * @author Norman Graf
 */
public class BaseTrackerHit implements TrackerHit {
    protected double[] _pos = new double[3];
    protected double[] _covMatrix = new double[6];
    protected double _time;
    protected double _dedx;
    // TODO set up an enumeration to replace the integer type
    protected int _type;
    // TODO decide what this is a list of
    // TODO decide whether this should be a Set
    protected List _rawHits = new ArrayList();
    protected long id;

    /** Creates a new instance of BaseTrackerHit */
    public BaseTrackerHit() {
    }

    /**
     * fully qualified constructor
     * 
     * @param pos
     *            the position of this hit (x,y,z) in mm
     * @param cov
     *            the covariance matrix for the position measurement, packed as 6 elements.
     * @param t
     *            the time for this measurement in ns
     * @param e
     *            the energy deposit associated with this measurement, in GeV
     * @param type
     *            the type of this measurement. not yet defined.
     */
    public BaseTrackerHit(double[] pos, double[] cov, double t, double e, int type) {
        _pos = pos;
        _covMatrix = cov;
        _time = t;
        _dedx = e;
        _type = type;
    }

    // include these setters since I don't know what the final inheriting classes will
    // look like.
    /**
     * The (x,y,z) position of this measurement.
     * 
     * @param pos
     *            the position of this hit (x,y,z) in mm
     */
    public void setPosition(double[] pos) {
        _pos = pos;
    }

    /**
     * The covariance matrix for the position measurement.
     * 
     * @param cov
     *            Packed array representing the symmetric covariance matrix (6 elements).
     */
    public void setCovarianceMatrix(double[] cov) {
        _covMatrix = cov;
    }

    /**
     * The time at which this measurement was made.
     * 
     * @param t
     *            the time in ns.
     */
    public void setTime(double t) {
        _time = t;
    }

    /**
     * The energy deposit associated with this measurement.
     * 
     * @param e
     *            The energy in GeV.
     */
    public void setEnergy(double e) {
        _dedx = e;
    }

    /**
     * The type of this measurement.
     * 
     * @param type
     *            Not yet defined.
     */
    public void setType(int type) {
        _type = type;
    }

    /**
     * Add the RawTrackerHit from which this TrackerHit originates
     * 
     * @param hit
     */
    public void addRawTrackerHit(RawTrackerHit hit) {
        _rawHits.add(hit);
    }

    /**
     * Add the list of RawTrackerHits from which this TrackerHit originates
     * 
     * @param hits
     */
    public void addRawTrackerHits(List<RawTrackerHit> hits) {
        _rawHits.addAll(hits);
    }

    // TODO consider customizing based on hit type.
    public String toString() {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if (lastDot != -1)
            className = className.substring(lastDot + 1);
        StringBuffer sb = new StringBuffer(className + ": Type: " + _type + "\n");
        sb.append("(x,y,z): " + _pos[0] + " " + _pos[1] + " " + _pos[2] + "\n");
        // TODO add in covariance matrix
        sb.append("dEdx: " + _dedx + " t: " + _time + "\n");
        return sb.toString();
    }

    // TODO add convenience methods which extend the base interface.
    // TODO return position as SpacePoint
    // TODO return covariance matrix as Matrix

    // TrackerHit interface
    /**
     * The (x,y,z) hit position in [mm].
     * 
     * @return the cartesian position of this point.
     */
    public double[] getPosition() {
        return _pos;
    }

    /**
     * Covariance of the position (x,y,z) as a 6 element array.
     * 
     * @return the packed covariance matrix
     */
    public double[] getCovMatrix() {
        return _covMatrix;
    }

    /**
     * The energy deposited by this hit in [GeV].
     * 
     * @return the energy deposit associated with this hit.
     */
    public double getdEdx() {
        return _dedx;
    }

    /**
     * The time of the hit in [ns]. By convention, the earliest time of energy deposition is used if this is a composite hit.
     * 
     * @return the time of this hit.
     */
    public double getTime() {
        return _time;
    }

    public double getEdepError() {
        return 0.;
    }

    public int getQuality() {
        return 0;
    }

    /**
     * Type of hit. Mapping of integer types to type names through collection parameters "TrackerHitTypeNames" and "TrackerHitTypeValues".
     * 
     * @return the integer type of this hit.
     */
    // TODO define what this type is.
    public int getType() {
        return _type;
    }

    // TODO fix the covariant return type.
    /**
     * The raw data hits. Check getType() to get actual data type.
     * 
     * @return the list of raw hits which contribute to this hit.
     */
    public List getRawHits() {
        return _rawHits;
    }

    public long getCellID() {
        return id;
    }
}