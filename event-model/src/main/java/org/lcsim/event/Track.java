package org.lcsim.event;

import hep.physics.matrix.SymmetricMatrix;

import java.util.List;


/**
 * Represents a found (reconstructed) track
 * 
 * @author tonyj
 * @version $Id: Track.java,v 1.13 2012/09/05 18:23:20 jeremy Exp $
 */

public interface Track 
{
    int getCharge();

    /**
     * @deprecated Use TrackState method instead.
     * @return
     */
    @Deprecated
    double[] getReferencePoint();

    /**
     * @deprecated Use TrackState method instead.
     * @return
     */
    @Deprecated
    double getReferencePointX();

    /**
     * @deprecated Use TrackState method instead.
     * @return
     */
    @Deprecated
    double getReferencePointY();

    /**
     * @deprecated Use TrackState method instead.
     * @return
     */
    @Deprecated
    double getReferencePointZ();

    boolean isReferencePointPCA();

    /**
     * @deprecated Use TrackState method instead.
     * @return
     */
    @Deprecated
    double[] getMomentum();

    // Hep3Vector momentum();
    // SpacePoint referencePoint();
    /**
     * @deprecated Use TrackState method instead.
     * @return
     */
    @Deprecated
    double getPX();

    /**
     * @deprecated Use TrackState method instead.
     * @return
     */
    @Deprecated
    double getPY();

    /**
     * @deprecated Use TrackState method instead.
     * @return
     */
    @Deprecated
    double getPZ();

    // MCParticle getMCParticle();

    /**
     * Returns true if the track has been successfully fitted
     */
    boolean fitSuccess();

    /**
     * Get an individual track parameter
     * 
     * The track parameters for LCD are defined as follows <table>
     * <tr>
     * <th>Index</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td> 0 </td>
     * <td> d0 = XY impact parameter </td>
     * <tr>
     * <tr>
     * <td> 1 </td>
     * <td> phi0 </td>
     * <tr> </td>
     * <tr>
     * <tr>
     * <td> 2 </td>
     * <td> omega = 1/curv.radius (negative for negative tracks) </td>
     * <tr>
     * <tr>
     * <td> 3 </td>
     * <td> z0 = z of track origin (z impact parameter) </td>
     * <tr>
     * <tr>
     * <td> 4 </td>
     * <td> s = tan lambda </td>
     * <tr> </table> Parameters according to <a
     * href="../util/swim/doc-files/L3_helix.pdf">the L3 conventions</a><br />
     * 
     * @param i
     *                The index of the track parameter
     * @return The track parameter with the specified index
     * @deprecated Use TrackState method instead.
     */
    @Deprecated
    double getTrackParameter(int i);

    /**
     * Get the track parameters as an array
     * 
     * @see #getTrackParameter
     * 
     * @deprecated Use TrackState method instead.
     */
    @Deprecated
    double[] getTrackParameters();

    /**
     * Get the error matrix
     * 
     * @see #getTrackParameter
     * 
     * @deprecated Use TrackState method instead.
     */
    @Deprecated
    SymmetricMatrix getErrorMatrix();

    /**
     * Get the Chi Squared for the track fit
     * 
     * @see #getNDF
     * @return Chi Squared
     */
    double getChi2();

    /**
     * Get the numbers of degrees of freedom for the fit
     * 
     * @see #getChi2
     * @return The number of degrees of freedom
     */
    int getNDF();

    double getdEdx();

    double getdEdxError();

    double getRadiusOfInnermostHit();

    int[] getSubdetectorHitNumbers();

    List<Track> getTracks();

    List<TrackerHit> getTrackerHits();

    int getType();
    
    List<TrackState> getTrackStates();
}
