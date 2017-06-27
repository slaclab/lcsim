package org.lcsim.mc.fast.tracking;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import Jama.util.Maths;

import org.lcsim.mc.fast.tracking.SimpleTables;
import java.util.Random;

/**
 * @author T. Barklow
 */
class SmearTrackSimple {
    /**
     * Smear track parameters according to modified version of track's stored error matrix.
     *
     * @see TrackParameters
     */
    static DocaTrackParameters smearTrackSimple(double bField, TrackParameters noSmear, Random rand, SimpleTables SmTbl, double pt, boolean hist) {

        final double errScale = 0.0001;
        final double eMScale = 1.e14;
        // get copy of error matrix and prepare for modification
        Matrix eM = Maths.toJamaMatrix(noSmear.getErrorMatrix());
        double[] errscale = { SmTbl.getD0ErrorScale(), SmTbl.getPhiErrorScale(), 1., SmTbl.getZ0ErrorScale(), SmTbl.getTanLambdaErrorScale() };
        double[] oldDiagErr = new double[5];
        double[] newDiagErr = new double[5];
        for (int i = 0; i < 5; i++) {
            oldDiagErr[i] = Math.sqrt(noSmear.getErrorMatrix().diagonal(i));
            if (i == 2) {
                double th = Math.atan(1 / (noSmear.getTanL()));
                double a = SmTbl.getConstantTerm();
                double b = SmTbl.getThetaTerm() / (pt * Math.sin(th));
                newDiagErr[i] = Math.abs(noSmear.getOmega()) * pt * Math.sqrt(a * a + b * b);
            } else {
                newDiagErr[i] = errscale[i] * oldDiagErr[i];
            }
            eM.set(i, i, Math.pow(newDiagErr[i], 2.));
            for (int j = 0; j < i; j++) {
                eM.set(i, j, noSmear.getErrorMatrix().e(i, j) * newDiagErr[i] * newDiagErr[j] / oldDiagErr[i] / oldDiagErr[j]);
                eM.set(j, i, eM.get(i, j));
            }
        }
        Matrix M = eM.copy();
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

        // DocaTrackParameters smeared = new DocaTrackParameters(bField, newval, (eM.timesEquals(eMScale)).getArray(), chi2);
        // DocaTrackParameters smeared = new DocaTrackParameters(bField, newval, eM.getArray(), chi2);
        // DocaTrackParameters smeared = new DocaTrackParameters(bField, newval, (Matrix.constructWithCopy(noSmear.getErrorMatrix()).timesEquals(errScale)).getArray(), chi2);
        DocaTrackParameters smeared = new DocaTrackParameters(bField, newval, noSmear.getErrorMatrix(), chi2);
        if (noSmear instanceof DocaTrackParameters) {
            smeared.setL0(((DocaTrackParameters) noSmear).getL0());
        }

        return smeared;
    }
}
