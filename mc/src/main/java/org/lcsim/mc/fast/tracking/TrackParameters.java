package org.lcsim.mc.fast.tracking;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

interface TrackParameters extends Hep3Vector {
    double getD0();

    /**
     * Get the error matrix
     * @see #getTrackParameter
     */
    SymmetricMatrix getErrorMatrix();

    double[] getMomentum();

    double getOmega();

    double getPX();

    double getPY();

    double getPZ();

    double getPhi0();

    double getPtot();

    double getTanL();

    /**
     * Get an individual track parameter. <br>
     *
     * The track parameters for LCD are defined as follows
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>d0 = XY impact parameter</td>
     * <tr>
     * <tr>
     * <td>1</td>
     * <td>phi0</td>
     * <tr>
     * </td>
     * <tr>
     * <tr>
     * <td>2</td>
     * <td>omega = 1/curv.radius (negative for q/abs(q) > 0)</td>
     * <tr>
     * <tr>
     * <td>3</td>
     * <td>z0 = z of track (z impact parameter)</td>
     * <tr>
     * <tr>
     * <td>4</td>
     * <td>s = tan lambda</td>
     * <tr>
     * </table>
     * @param i The index of the track parameter
     * @return The track parameter with the specified index
     */
    double getTrackParameter(int i);

    /**
     * Get the track parameters as an array
     * @see #getTrackParameter
     */
    double[] getTrackParameters();

    int getUnitCharge();

    double getZ0();
}
