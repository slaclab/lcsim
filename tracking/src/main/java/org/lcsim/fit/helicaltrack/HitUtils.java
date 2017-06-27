
/*
 * HitUtils.java
 *
 * Created on July 2, 2008, 9:31 AM
 *
 */

package org.lcsim.fit.helicaltrack;

import hep.physics.matrix.BasicMatrix;
import hep.physics.matrix.Matrix;
import hep.physics.matrix.MatrixOp;
import hep.physics.matrix.MutableMatrix;
import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.util.Map;

import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 *
 * @author Richard Partridge
 * @version 1.0
 */
public class HitUtils {
    private static double _eps = 1.0e-6;
    private  boolean _debug = false;

    /** Creates a new instance of HitUtils */
    public HitUtils() {
    }

    /**
     * Turn a pixel hit into a pseudo-strip hit.  Used for ZSegment hits that
     * include a pixel hit.  If the pixel is in an endcap disk, we need to
     * calculate an effective z uncertainty by calling the zres method.
     * @param hit pixel hit
     * @param smap map of x-y path lengths
     * @param msmap map of multiple scatterings
     * @param helix approximate helix
     * @return strip hit
     */
    public static HelicalTrack2DHit PixelToStrip(HelicalTrackHit hit, Map<HelicalTrackHit, Double> smap,
            Map<HelicalTrackHit, MultipleScatter> msmap, HelicalTrackFit helix, double tolerance) {

        //  Take the strip to be span the interval +- tolerance * dz about the pixel hit position
        double dz = zres(hit, msmap, helix);
        double zmin = hit.z() - tolerance * dz;
        double zmax = hit.z() + tolerance * dz;

        //  Make a new strip hit using the pixel's corrected position and covariance matrix
        HelicalTrack2DHit striphit = new HelicalTrack2DHit(hit.getCorrectedPosition(),
                hit.getCorrectedCovMatrix(), hit.getdEdx(), hit.getTime(), hit.getRawHits(),
                hit.Detector(), hit.Layer(), hit.BarrelEndcapFlag(), zmin, zmax);
        //  Save the path length for the strip hit
        smap.put(striphit, smap.get(hit));

        return striphit;
    }

    public static Hep3Vector StripCenter(HelicalTrackStrip strip) {
        return  VecOp.add(strip.origin(), VecOp.mult(strip.umeas(), strip.u()));
    }

    public static SymmetricMatrix StripCov(HelicalTrackStrip strip) {
        SymmetricMatrix cov = new SymmetricMatrix(3);
        Hep3Vector pos = StripCenter(strip);
        double x = pos.x();
        double y = pos.y();
        double r2 = x*x + y*y;
        double du = strip.du();
        cov.setElement(0, 0, y*y * du*du / r2);
        cov.setElement(0, 1, -x * y * du*du / r2);
        cov.setElement(1, 1, x*x * du*du / r2);
        return cov;
    }

    public static SymmetricMatrix PixelCov(double x, double y, double drphi, double dz) {
        SymmetricMatrix cov = new SymmetricMatrix(3);
        double r2 = x*x + y*y;
        cov.setElement(0, 0, y*y * drphi*drphi / r2);
        cov.setElement(0, 1, -x * y * drphi*drphi / r2);
        cov.setElement(1, 1, x*x * drphi*drphi / r2);
        cov.setElement(2, 2, dz);
        return cov;
    }

    /**
     * Find the effective z uncertainty to use in the s-z line fit.  Include
     * both resolution and multiple scattering contributions.  Endcap disk
     * hits require converting an uncertainty in r to an effective uncertainty
     * in z using dz = dr * slope.  If a helix is supplied, it is used to
     * calculate the slope.  Otherwise, the track is assumed to travel along
     * a straight line from the origin.
     * @param hit the hit we want dz for
     * @param msmap map of the multiple scattering uncertainties
     * @param helix approximate helix for the track (or null)
     * @return effective z uncertainty
     */
    public static double zres(HelicalTrackHit hit, Map<HelicalTrackHit, MultipleScatter> msmap,
            HelicalTrackFit helix) {

        //  Get the multiple scattering uncertainty (if any)
        double dz_ms = 0.;
        if (msmap.containsKey(hit)) dz_ms = msmap.get(hit).dz();

        //  Barrels and disks are treated differently
        if (hit.BarrelEndcapFlag() == BarrelEndcapFlag.BARREL) {
            //  For barrel hits, take the resolution uncertainty from the hit covariance matrix
            double dz_res2 = hit.getCorrectedCovMatrix().diagonal(2);
            //  Combine resolution and multiple scattering uncertainties in quadrature
            return Math.sqrt(dz_res2 + dz_ms*dz_ms);
        } else {
            //  For endcap disks, take dz = dr * |slope|
            //  First find the slope - default to the slope for a straight-line track from the origin
            double slope = hit.z() / hit.r();
            //  If we have a helix, see if we can use the slope from the helix
            if (helix != null) {
                //  Don't use the helix slope if the magnitude of the slope is smaller than its uncertainty
                if (Math.abs(helix.slope()) > helix.getSlopeError()) slope = helix.slope();

            }
            //  Take the resolution uncertainty to be dr * |slope|
            double dzres = hit.dr() * Math.abs(slope);
            //  Combine resolution and multiple scattering uncertainties in quadrature
            return Math.sqrt(dzres*dzres + dz_ms*dz_ms);
        }
    }

    /**
     * Return the hit position assuming the track originated at the origin.
     * @return hit position
     */
    public static Hep3Vector PositionFromOrigin(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {

        //  Assume the particle is coming from the origin, so x2 = gamma * x1
        //  gamma = Origin2 . w_hat / Origin1 . w_hat
        double gamma = VecOp.dot(strip2.origin(), strip2.w()) / NonZeroDotProduct(strip1.origin(), strip1.w());

        //  Calculate v1hat . u2hat, which is equivalent to sin(alpha) where alpha is the stereo angle
        double salpha = getSinAlpha(strip1, strip2);
        //  Calculate the hit locations for v = 0:  p = Origin + u * u_hat
        Hep3Vector p1 = StripCenter(strip1);
        Hep3Vector p2 = StripCenter(strip2);

        //  dp = (p2 - gamma * p1)
        Hep3Vector dp = VecOp.sub(p2, VecOp.mult(gamma, p1));

        //  Unmeasured coordinate v1:  v1 = (dp . u2_hat) / (gamma * sin(alpha))
        double v1 = VecOp.dot(dp, strip2.u()) / (gamma * salpha);
        if (v1 < strip1.vmin()) v1 = strip1.vmin();
        if (v1 > strip1.vmax()) v1 = strip1.vmax();

        //  Position of hit on strip 1:  r1 = p1 + v1 * v1_hat
        Hep3Vector r1 = VecOp.add(p1, VecOp.mult(v1, strip1.v()));
        //  Take position to be the midpoint of r1 and r2: r = 0.5 * (1 + gamma) * r1
     return VecOp.mult(0.5 * (1 + gamma), r1);
    }

    /**
     * Return the covariance matrix assuming the track originated from the
     * origin.
     * @return covariance matrix
     */
    public static SymmetricMatrix CovarianceFromOrigin(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {
        //  Assume the particle is coming from the origin, so x2 = gamma * x1
        //  gamma = Origin2 . w_hat / Origin1 . w_hat
        double gamma = VecOp.dot(strip2.origin(), strip2.w()) / NonZeroDotProduct(strip1.origin(), strip1.w());
        //  Calculate the seperation between the sensor planes
        double separation = SensorSeperation(strip1, strip2);
        //  Calculate v1hat . u2hat, which is equivalent to sin(alpha) where alpha is the stereo angle
        double salpha = getSinAlpha(strip1, strip2);
        //  Calculate the scale factor:  factor = (1 + gamma)^2 / 4 * sin^2(alpha)
        double factor = (1 + gamma)*(1 + gamma) / (4. * salpha*salpha);
        //  Calculate v * v^T for strips 1 and 2
        Matrix v1 = v2m(strip1.v());
        Matrix v2 = v2m(strip2.v());
        Matrix v1v1t = MatrixOp.mult(v1, MatrixOp.transposed(v1));
        Matrix v2v2t = MatrixOp.mult(v2, MatrixOp.transposed(v2));
        //  Find measurement uncertainties for strips 1 and 2
        double du1 = strip1.du();
        double du2 = strip2.du();
        //  Calculate the uncertainty in the unmeasured coordinate due to not knowing the track direction
        //  by assuming phat . u has an uncertainty of 2/sqrt(12) so dv = 2 / sqrt(12) * seperation / sin(alpha)
        double dv = Math.abs(2. * separation / (Math.sqrt(12) * salpha));
        //  Don't let dv by greater than the strip length / sqrt(12)
        double dv1 = Math.min(dv, (strip1.vmax()-strip1.vmin()) / Math.sqrt(12.));
        double dv2 = Math.min(dv, (strip2.vmax()-strip2.vmin()) / Math.sqrt(12.));
        //  Calculate the covariance matrix.       
        //    From resolution:  cov = factor * (v2 * v2^T * du1^2 + v1 * v1^T * du2^2)
        //    From direction:                + (v1 * v1^T * (dv1/2)^2 + v2 * v2^T * (dv2/2)^2
        Matrix cov1 = MatrixOp.mult(factor * du2*du2 + 0.25 * dv1*dv1, v1v1t);
        Matrix cov2 = MatrixOp.mult(factor * du1*du1 + 0.25 * dv2*dv2, v2v2t);
        Matrix cov = MatrixOp.add(cov1, cov2);
        return new SymmetricMatrix(cov);
    }

    /**
     * Return the hit position given the track direction.
     * @param trkdir TrackDirection object containing direction and derivatives
     * @return Corrected hit position
     */
    public static Hep3Vector PositionOnHelix(TrackDirection trkdir, HelicalTrackStrip strip1,
            HelicalTrackStrip strip2) {
        //  Get the track direction unit vector
        Hep3Vector dir = trkdir.Direction();
        double salpha = getSinAlpha(strip1, strip2);
        //  Gamma is the distance the particle travels between the two sensors:  gamma = separation / (what . dir)
        double gamma = SensorSeperation(strip1, strip2) / NonZeroDotProduct(strip1.w(), dir);
        Hep3Vector p1 = StripCenter(strip1);
        Hep3Vector p2 = StripCenter(strip2);
        //  dp = p2 - (p1 + gamma * dir)
        Hep3Vector dp = VecOp.sub(p2, VecOp.add(p1, VecOp.mult(gamma, dir)));
        //  Unmeasured coordinate v1: v1 = (dp . u2hat) / sin(alpha)
        double v1 = VecOp.dot(dp, strip2.u()) / salpha;
        //  Position of hit on strip 1:  r1 = p1 + v1 * v1_hat
        Hep3Vector r1 = VecOp.add(p1, VecOp.mult(v1, strip1.v()));
        //  Take position to be the midpoint of x1 and x2: r = r1 + 0.5 * gamma * dir
        return VecOp.add(r1, VecOp.mult(0.5 * gamma, dir));
    }

    
    /**
     * Return the covariance matrix given a track direction and helix
     * covariance matrix.
     * @param trkdir TrackDirection object containing direction and derivatives
     * @param hcov covariance matrix for helix parameters
     * @return corrected covariance matrix
     */
    public static SymmetricMatrix CovarianceOnHelix(TrackDirection trkdir, SymmetricMatrix hcov,
            HelicalTrackStrip strip1, HelicalTrackStrip strip2) {
        //  Get the track direction and direction derivatives with respect to the helix parameters
        Hep3Vector dir = trkdir.Direction();
        Matrix dirderiv = trkdir.Derivatives();
        //  Calculate phat x v1
        Matrix pcrossv1 = v2m(VecOp.cross(dir, strip1.v()));
        //  Calculate phat x v2
        Matrix pcrossv2 = v2m(VecOp.cross(dir, strip2.v()));
        //  Calculate phat . w
        double pdotw = NonZeroDotProduct(dir, strip1.w());
        //  salpha is the sin of the stereo angle
        double salpha = getSinAlpha(strip1, strip2);
        //  Calculate the scale factor:  _separation / 2 * sin(alpha) * (phat . w)^2
        double factor = SensorSeperation(strip1, strip2) / (2 * salpha * pdotw*pdotw);
        //  Create the matrix of position derivatives:  d_i,j = dr_i / dphat_j
        //  The matrix d is given by factor * (v1 * (phat x v2)^T + v2 * (phat x v1)^T  where ^T means transpose
        Matrix v1 = v2m(strip1.v());
        Matrix v2 = v2m(strip2.v());
        Matrix d = MatrixOp.mult(factor, MatrixOp.add(MatrixOp.mult(v1, MatrixOp.transposed(pcrossv2)),
                MatrixOp.mult(v2, MatrixOp.transposed(pcrossv1))));
        Matrix dh = MatrixOp.mult(d, dirderiv);
        //  Construct the transpose of dh
        Matrix dht = MatrixOp.transposed(dh);
        //  Calculate the covariance contributions from the direction uncertainty:  cov = dh * hcov * dh^T
        Matrix cov_dir = MatrixOp.mult(dh, MatrixOp.mult(hcov, dht));
        //  Calculate the contributions from measurement errors:  cov += (v2 * v2^T * du1^2 + v1 * v1^T * du2^2) / sin(alpha)^2
        double du1 = strip1.du();
        double du2 = strip2.du();
        Matrix cov1 = MatrixOp.mult(du1*du1 / (salpha*salpha), MatrixOp.mult(v2, MatrixOp.transposed(v2)));
        Matrix cov2 = MatrixOp.mult(du2*du2 / (salpha*salpha), MatrixOp.mult(v1, MatrixOp.transposed(v1)));
        //  Sum all the contributions
        Matrix cov = MatrixOp.add(cov_dir, MatrixOp.add(cov1, cov2));

        //  Convert to a symmetric matrix
        return new SymmetricMatrix(cov);
    }

    public static double UnmeasuredCoordinate(TrackDirection trkdir, HelicalTrackStrip strip1,
            HelicalTrackStrip strip2) {
        //  Get the track direction unit vector
        Hep3Vector dir = trkdir.Direction();
        //  Gamma is the distance the particle travels between the two sensors:  gamma = separation / (what . dir)
        double gamma = SensorSeperation(strip1, strip2) / NonZeroDotProduct(strip1.w(), dir);
        double salpha = getSinAlpha(strip1, strip2);
        Hep3Vector p1 = StripCenter(strip1);
        Hep3Vector p2 = StripCenter(strip2);
        //  dp = p2 - (p1 + gamma * dir)
        Hep3Vector dp = VecOp.sub(p2, VecOp.add(p1, VecOp.mult(gamma, dir)));
        //  Unmeasured coordinate v1: v1 = (dp . u2hat) / sin(alpha)
        return VecOp.dot(dp, strip2.u()) / salpha;
    }

    /**
     * Calculate the uncertainty in the unmeasured coordinate v1.
     * @param trkdir track direction
     * @param hcov helix covariance matrix
     * @return uncertainty in v1
     */
    public static double dv(TrackDirection trkdir, SymmetricMatrix hcov, HelicalTrackStrip strip1,
            HelicalTrackStrip strip2) {
        //  Get the track direction and the direction derivatives with respect to the helix parameters
        Hep3Vector dir = trkdir.Direction();
        Matrix dirderiv = trkdir.Derivatives();
        //  Calculate u1 . u2
        double u1dotu2 = VecOp.dot(strip1.u(), strip2.u());
        //  Calculate phat . w
        double pdotw = NonZeroDotProduct(dir, strip1.w());
        //  Calculate phat x v2
        Hep3Vector pcrossv2 = VecOp.cross(dir, strip2.v());
        double salpha = getSinAlpha(strip1, strip2);
        //  Calculate the scale factor:  separation / sin(alpha) * (phat . w)^2
        double factor = SensorSeperation(strip1, strip2) / (salpha * pdotw*pdotw);
        //  The matrix d^T is a row vector given by factor * (phat x v2)
        MutableMatrix dT = new BasicMatrix(1, 3);
        for (int i = 0; i < 3; i++) {
            dT.setElement(0, i, factor * pcrossv2.v()[i]);
        }
        //  Construct the matrix dh = d^T * dirderiv
        Matrix dh = MatrixOp.mult(dT, dirderiv);
        //  Construct the transpose of dh
        Matrix dhT = MatrixOp.transposed(dh);
        //  Calculate the uncertainty in v1 from the direction uncertainty:  cov = dh * hcov * dh^T
        MutableMatrix cov = (MutableMatrix) MatrixOp.mult(dh, MatrixOp.mult(hcov, dhT));
        //  Return the uncertainty in v1: dv1^2 = ((u1 . u2)^2 * du1^2 + du2^2) / sin^2(alpha) + cov
        double dvsq = (Math.pow(u1dotu2 * strip1.du(), 2) + Math.pow(strip2.du(), 2))/ (salpha*salpha);
        return Math.sqrt(dvsq + cov.e(0, 0));
    }

    public static double SensorSeperation(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {
        //  Calculate the seperation between the sensor planes
        return VecOp.dot(strip1.w(), VecOp.sub(strip2.origin(), strip1.origin()));
    }

    public static double v1Dotu2(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {
        //  Calculate v1hat . u2hat, which is equivalent to sin(alpha) where alpha is the stereo angle
        return VecOp.dot(strip1.v(), strip2.u());
    }

    public static double getSinAlpha(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {
        //  Calculate v1hat . u2hat, which is equivalent to sin(alpha) where alpha is the stereo angle
        double salpha = v1Dotu2(strip1, strip2);

//get cos(alpha) and check if the meaurement directions are ~parallel or flipped
//mg..5/23/2012...this is wrong for some reason...go back to original
        //        double calpha = VecOp.dot(strip1.u(), strip2.u());
//        if (calpha < 0)
//            salpha = -salpha;
        return salpha;
    }

    private static double NonZeroDotProduct(Hep3Vector v1, Hep3Vector v2) {
        double cth = VecOp.dot(v1, v2);
        if (Math.abs(cth) < _eps) {
          if (cth < 0.) cth = -_eps;
          else cth = _eps;
        }
        return cth;
    }

    private static Matrix v2m(Hep3Vector v) {
        BasicMatrix mat = new BasicMatrix(3, 1);
        mat.setElement(0, 0, v.x());
        mat.setElement(1, 0, v.y());
        mat.setElement(2, 0, v.z());
        return mat;
    }
}