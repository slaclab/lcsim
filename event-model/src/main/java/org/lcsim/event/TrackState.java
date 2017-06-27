package org.lcsim.event;

/** 
 * The LCIO TrackState interface.
 * 
 * @author gaede, engels
 * @author Jeremy McCormick
 * @version $Id: TrackState.java,v 1.2 2012/06/18 23:02:14 jeremy Exp $
 */
public interface TrackState 
{
    // TrackState location codes.
    public final static int AtOther = 0;  // Any location other than the ones defined below. 
    public final static int AtIP = 1;
    public final static int AtFirstHit = 2;
    public final static int AtLastHit = 3;
    public final static int AtCalorimeter = 4;
    public final static int AtVertex = 5;
    public final static int LastLocation = AtVertex;
    
    /** 
     * The location of the track state.
     * Location can be set to: AtIP, AtFirstHit, AtLastHit, AtCalorimeter, AtVertex, AtOther
     */
    public int getLocation();

    /** 
     * Impact paramter of the track in (r-phi).
     */
    public double getD0();

    /** 
     * Phi of the track at the reference point.
     * @see getReferencePoint
     */
    public double getPhi();

    /** 
     * Omega is the signed curvature of the track in [1/mm].
     * The sign is that of the particle's charge.
     */
    public double getOmega();

    /** 
     * Impact paramter of the track in (r-z).
     */
    public double getZ0();

    /** 
     * Lambda is the dip angle of the track in r-z at the reference point. 
     * @see getReferencePoint
     */
    public double getTanLambda();
    
    /**
     * Get the ordered list of 5 LCIO track parameters.
     * @return The track parameters as a double array of size 5.
     */
    public double[] getParameters();

    /** 
     * Covariance matrix of the track parameters. Stored as lower triangle matrix where
     * the order of parameters is:   d0, phi, omega, z0, tan(lambda).
     * So we have cov(d0,d0), cov( phi, d0 ), cov( phi, phi), ...
     * @return A double array of size 15 containing the covariance matrix of the track parameters.
     */
    public double[] getCovMatrix();

    /** 
     * Reference point of the track parameters.
     * The default for the reference point is the point of closest approach.
     * @return The reference point of the track parameters as a double array of size 3.
     */
    public double[] getReferencePoint();
    
    public double[] getMomentum();
    
    /**
     * Get an individual track parameter.
     * They are returned by the following keys: 
     * 0 = d0
     * 1 = phi
     * 2 = omega
     * 3 = z0
     * 4 = tanLambda
     * This order is defined in BaseTrack. 
     * @return The track parameters.
     */
    public double getParameter(int iparam);
}