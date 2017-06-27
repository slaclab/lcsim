package org.lcsim.mc.fast.tracking;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import Jama.util.Maths;

import java.util.Random;

/**
 * @author W.Walkowiak, 08/00
 */
class SmearTrack {
    /**
     * Smear track parameters according to the track's stored error matrix.
     *
     * @see TrackParameters
     */
    static DocaTrackParameters smearTrack(double bField, TrackParameters noSmear, Random rand) {

        // get error matrix and do a sanity check
        Matrix M = Maths.toJamaMatrix(noSmear.getErrorMatrix());
        if (M.det() <= 0.) {
            throw new RuntimeException("Error matrix not positive definite!");
        }

        // run Eigenvalue decomposition and get matrices and vectors
        EigenvalueDecomposition eig = M.eig();

        Matrix T = eig.getV();
        double[] er = eig.getRealEigenvalues();
        double[] ei = eig.getImagEigenvalues();

        // sanity check: det(T) != 0
        if (T.det() == 0.) {
            throw new RuntimeException("Non orthogonal basis!");
        }

        // sanity check: no imaginary eigenvalues
        for (int i = 0; i < ei.length; i++)
            if (ei[i] != 0.) {
                throw new RuntimeException("Imaginary Eigenvalues seen!");
            }

        // now do the real smearing
        double[] dev = new double[5];
        for (int i = 0; i < er.length; i++) {
            if (er[i] <= 0) {
                throw new RuntimeException("Non-positive Eigenvalue seen!");
            }
            dev[i] = Math.sqrt(er[i]) * rand.nextGaussian();
        }

        Matrix shift = T.times(new Matrix(dev, 5));
        Matrix val = new Matrix(noSmear.getTrackParameters(), 5);
        double[] newval = (val.plus(shift)).getColumnPackedCopy();

        // adjust new phi value to [-pi,pi] if necessary
        if (newval[1] > Math.PI) {
            newval[1] -= (2. * Math.PI);
        }
        if (newval[1] < -Math.PI) {
            newval[1] += (2. * Math.PI);
        }

        // Chi2 calculation
        double chi2 = ((shift.transpose()).times((M.inverse()).times(shift))).get(0, 0);

        DocaTrackParameters smeared = new DocaTrackParameters(bField, newval, noSmear.getErrorMatrix(), chi2);
        if (noSmear instanceof DocaTrackParameters) {
            smeared.setL0(((DocaTrackParameters) noSmear).getL0());
        }

        return smeared;
    }
}
