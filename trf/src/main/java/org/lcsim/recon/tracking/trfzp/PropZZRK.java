package org.lcsim.recon.tracking.trfzp;

import org.lcsim.recon.tracking.magfield.AbstractMagneticField;
import org.lcsim.recon.tracking.trfbase.*;

import static java.lang.Math.sqrt;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import org.lcsim.recon.tracking.spacegeom.CartesianPoint;
import org.lcsim.recon.tracking.spacegeom.SpacePointTensor;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class PropZZRK extends PropDirected
{

    private AbstractMagneticField _bfield;
    // Assign track parameter indices.
    private static final int IX = SurfZPlane.IX;
    private static final int IY = SurfZPlane.IY;
    private static final int IDXDZ = SurfZPlane.IDXDZ;
    private static final int IDYDZ = SurfZPlane.IDYDZ;
    private static final int IQP = SurfZPlane.IQP;
    private double _precision;
    private double _derivprec;

    public PropZZRK(AbstractMagneticField bfield, double precision, double derivprecision)
    {
        _bfield = bfield;
        _precision = precision;
        _derivprec = derivprecision;
        assert (_precision > 0. && _precision <= 0.01);
        assert (_derivprec > 0. && _derivprec <= 0.1);
    }
    
    public PropZZRK(AbstractMagneticField bfield)
    {
        _precision = 1.e-7;
        _derivprec = 1.e-7;
        _bfield = bfield;
    }

    @Override
    public Propagator newPropagator()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Equations of motion in terms of track parameters x, y, dxdz, dydz,
    // curvature (q/p), and path length and the magnetic field.
    double[] motion(double[] par, double z, double crv, double sign_dz,
                    AbstractMagneticField bfield)
    {

        double[] result = new double[par.length];
        boolean deriv = par.length > 5;

        // Extract track parameters.

        double x = par[0];
        double y = par[1];
        double xv = par[2];
        double yv = par[3];
        double s = par[4];

        // Declare derivative matrix.

        double dxdx0 = 0.;
        double dxdy0 = 0.;
        double dxdxv0 = 0.;
        double dxdyv0 = 0.;
        double dxdcrv = 0.;

        double dydx0 = 0.;
        double dydy0 = 0.;
        double dydxv0 = 0.;
        double dydyv0 = 0.;
        double dydcrv = 0.;

        double dxvdx0 = 0.;
        double dxvdy0 = 0.;
        double dxvdxv0 = 0.;
        double dxvdyv0 = 0.;
        double dxvdcrv = 0.;

        double dyvdx0 = 0.;
        double dyvdy0 = 0.;
        double dyvdxv0 = 0.;
        double dyvdyv0 = 0.;
        double dyvdcrv = 0.;

        if (deriv) {

            // Extract derivative matrix.

            dxdx0 = par[5];
            dxdy0 = par[6];
            dxdxv0 = par[7];
            dxdyv0 = par[8];
            dxdcrv = par[9];

            dydx0 = par[10];
            dydy0 = par[11];
            dydxv0 = par[12];
            dydyv0 = par[13];
            dydcrv = par[14];

            dxvdx0 = par[15];
            dxvdy0 = par[16];
            dxvdxv0 = par[17];
            dxvdyv0 = par[18];
            dxvdcrv = par[19];

            dyvdx0 = par[20];
            dyvdy0 = par[21];
            dyvdxv0 = par[22];
            dyvdyv0 = par[23];
            dyvdcrv = par[24];
        }
        double dmax = 1.e30;
        if (!(xv < dmax && xv > -dmax)) {
            throw new RuntimeException("Floating point exception");
        }
        if (!(yv < dmax && yv > -dmax)) {
            throw new RuntimeException("Floating point exception");
        }
        double dsdz2 = 1. + xv * xv + yv * yv;
        double dsdz = sign_dz * sqrt(dsdz2);

        // Get Cartesian components of magnetic field & derivatives.

        double bx = 0.;
        double by = 0.;
        double bz = 0.;
        double dbxdx = 0.;
        double dbxdy = 0.;
        double dbydx = 0.;
        double dbydy = 0.;
        double dbzdx = 0.;
        double dbzdy = 0.;
        SpacePointVector bf;
        if (deriv) {
            SpacePointTensor t = new SpacePointTensor();
            bf = bfield.field(new CartesianPoint(x, y, z), t);
            dbxdx = t.t_x_x();
            dbxdy = t.t_x_y();
            dbydx = t.t_y_x();
            dbydy = t.t_y_y();
            dbzdx = t.t_z_x();
            dbzdy = t.t_z_y();
        } else {
            bf = bfield.field(new CartesianPoint(x, y, z));
        }
        bx = bf.v_x();
        by = bf.v_y();
        bz = bf.v_z();

        // dx/dz.

        result[0] = xv;

        // dy/dz.

        result[1] = yv;

        // dxv/dz.

        double dxvdz_nocrv = TRFMath.BFAC * dsdz * (xv * yv * bx - (1. + xv * xv) * by + yv * bz);
        result[2] = dxvdz_nocrv * crv;

        // dyv/dz.

        double dyvdz_nocrv = TRFMath.BFAC * dsdz * ((1. + yv * yv) * bx - xv * yv * by - xv * bz);
        result[3] = dyvdz_nocrv * crv;

        // ds/dz.

        result[4] = dsdz;

        if (deriv) {

            // d(dx/d x0)/dz.

            result[5] = dxvdx0;

            // d(dx/d y0)/dz.

            result[6] = dxvdy0;

            // d(dx/d xv0)/dz.

            result[7] = dxvdxv0;

            // d(dx/d yv0)/dz.

            result[8] = dxvdyv0;

            // d(dx/d crv)/dz.

            result[9] = dxvdcrv;

            // d(dy/d x0)/dz.

            result[10] = dyvdx0;

            // d(dy/d y0)/dz.

            result[11] = dyvdy0;

            // d(dy/d xv0)/dz.

            result[12] = dyvdxv0;

            // d(dy/d yv0)/dz.

            result[13] = dyvdyv0;

            // d(dy/d crv)/dz.

            result[14] = dyvdcrv;

            // d(d xv/d x0)/dz.

            result[15] = result[2] * (xv * dxvdx0 + yv * dyvdx0) / dsdz2
                    + TRFMath.BFAC * crv * dsdz
                    * (dxvdx0 * yv * bx + xv * dyvdx0 * bx + xv * yv * (dbxdx * dxdx0 + dbxdy * dydx0)
                    - 2. * xv * dxvdx0 * by - (1. + xv * xv) * (dbydx * dxdx0 + dbydy * dydx0)
                    + dyvdx0 * bz + yv * (dbzdx * dxdx0 + dbzdy * dydx0));

            // d(d xv/d y0)/dz.

            result[16] = result[2] * (xv * dxvdy0 + yv * dyvdy0) / dsdz2
                    + TRFMath.BFAC * crv * dsdz
                    * (dxvdy0 * yv * bx + xv * dyvdy0 * bx + xv * yv * (dbxdx * dxdy0 + dbxdy * dydy0)
                    - 2. * xv * dxvdy0 * by - (1. + xv * xv) * (dbydx * dxdy0 + dbydy * dydy0)
                    + dyvdy0 * bz + yv * (dbzdx * dxdy0 + dbzdy * dydy0));

            // d(d xv/d xv0)/dz.

            result[17] = result[2] * (xv * dxvdxv0 + yv * dyvdxv0) / dsdz2
                    + TRFMath.BFAC * crv * dsdz
                    * (dxvdxv0 * yv * bx + xv * dyvdxv0 * bx + xv * yv * (dbxdx * dxdxv0 + dbxdy * dydxv0)
                    - 2. * xv * dxvdxv0 * by - (1. + xv * xv) * (dbydx * dxdxv0 + dbydy * dydxv0)
                    + dyvdxv0 * bz + yv * (dbzdx * dxdxv0 + dbzdy * dydxv0));

            // d(d xv/d yv0)/dz.

            result[18] = result[2] * (xv * dxvdyv0 + yv * dyvdyv0) / dsdz2
                    + TRFMath.BFAC * crv * dsdz
                    * (dxvdyv0 * yv * bx + xv * dyvdyv0 * bx + xv * yv * (dbxdx * dxdyv0 + dbxdy * dydyv0)
                    - 2. * xv * dxvdyv0 * by - (1. + xv * xv) * (dbydx * dxdyv0 + dbydy * dydyv0)
                    + dyvdyv0 * bz + yv * (dbzdx * dxdyv0 + dbzdy * dydyv0));

            // d(d xv/d crv)/dz.

            result[19] = dxvdz_nocrv * (1. + crv * (xv * dxvdcrv + yv * dyvdcrv) / dsdz2)
                    + TRFMath.BFAC * crv * dsdz
                    * (dxvdcrv * yv * bx + xv * dyvdcrv * bx + xv * yv * (dbxdx * dxdcrv + dbxdy * dydcrv)
                    - 2. * xv * dxvdcrv * by - (1. + xv * xv) * (dbydx * dxdcrv + dbydy * dydcrv)
                    + dyvdcrv * bz + yv * (dbzdx * dxdcrv + dbzdy * dydcrv));


            // d(d yv/d x0)/dr

            result[20] = result[3] * (xv * dxvdx0 + yv * dyvdx0) / dsdz2
                    + TRFMath.BFAC * crv * dsdz
                    * (2. * yv * dyvdx0 * bx + (1. + yv * yv) * (dbxdx * dxdx0 + dbxdy * dydx0)
                    - dxvdx0 * yv * by - xv * dyvdx0 * by - xv * yv * (dbydx * dxdx0 + dbydy * dydx0)
                    - dxvdx0 * bz - xv * (dbzdx * dxdx0 + dbzdy * dydx0));

            // d(d yv/d y0)/dr

            result[21] = result[3] * (xv * dxvdy0 + yv * dyvdy0) / dsdz2
                    + TRFMath.BFAC * crv * dsdz
                    * (2. * yv * dyvdy0 * bx + (1. + yv * yv) * (dbxdx * dxdy0 + dbxdy * dydy0)
                    - dxvdy0 * yv * by - xv * dyvdy0 * by - xv * yv * (dbydx * dxdy0 + dbydy * dydy0)
                    - dxvdy0 * bz - xv * (dbzdx * dxdy0 + dbzdy * dydy0));

            // d(d yv/d xv0)/dr

            result[22] = result[3] * (xv * dxvdxv0 + yv * dyvdxv0) / dsdz2
                    + TRFMath.BFAC * crv * dsdz
                    * (2. * yv * dyvdxv0 * bx + (1. + yv * yv) * (dbxdx * dxdxv0 + dbxdy * dydxv0)
                    - dxvdxv0 * yv * by - xv * dyvdxv0 * by - xv * yv * (dbydx * dxdxv0 + dbydy * dydxv0)
                    - dxvdxv0 * bz - xv * (dbzdx * dxdxv0 + dbzdy * dydxv0));

            // d(d yv/d yv0/dr

            result[23] = result[3] * (xv * dxvdyv0 + yv * dyvdyv0) / dsdz2
                    + TRFMath.BFAC * crv * dsdz
                    * (2. * yv * dyvdyv0 * bx + (1. + yv * yv) * (dbxdx * dxdyv0 + dbxdy * dydyv0)
                    - dxvdyv0 * yv * by - xv * dyvdyv0 * by - xv * yv * (dbydx * dxdyv0 + dbydy * dydyv0)
                    - dxvdyv0 * bz - xv * (dbzdx * dxdyv0 + dbzdy * dydyv0));

            // d(d yv/d crv)/dr

            result[24] = dyvdz_nocrv * (1. + crv * (xv * dxvdcrv + yv * dyvdcrv) / dsdz2)
                    + TRFMath.BFAC * crv * dsdz
                    * (2. * yv * dyvdcrv * bx + (1. + yv * yv) * (dbxdx * dxdcrv + dbxdy * dydcrv)
                    - dxvdcrv * yv * by - xv * dyvdcrv * by - xv * yv * (dbydx * dxdcrv + dbydy * dydcrv)
                    - dxvdcrv * bz - xv * (dbzdx * dxdcrv + dbzdy * dydcrv));
        }
        return result;
    }

    // Function to evolve track paramters using a single fourth order 
    // Runge-Kutta step from z1 to z2.
    void rk4(double[] par, double[] z, double h, double crv, double sign_dz,
             AbstractMagneticField bfield, double[] k1, boolean reuse)
    {
        int size = par.length;
        double[] k2 = new double[size];
        double[] k3 = new double[size];
        double[] k4 = new double[size];
        if (!reuse) //    k1 = h * motion(par, z[0], crv, sign_dz, bfield);
        //    k2 = h * motion(par + 0.5*k1, z + 0.5*h, crv, sign_dz, bfield);
        //    k3 = h * motion(par + 0.5*k2, z + 0.5*h, crv, sign_dz, bfield);
        //    k4 = h * motion(par + k3, z + h, crv, sign_dz, bfield);
        {
            scale(motion(par, z[0], crv, sign_dz, bfield), h, k1);
        }
        scale(motion(add(par, 0.5, k1), z[0] + 0.5 * h, crv, sign_dz, bfield), h, k2);
        scale(motion(add(par, 0.5, k2), z[0] + 0.5 * h, crv, sign_dz, bfield), h, k3);
        scale(motion(add(par, 1.0, k3), z[0] + h, crv, sign_dz, bfield), h, k4);
//        par += (1. / 6.) * (k1 + 2. * k2 + 2. * k3 + k4);
        sum(k1, k2, k3, k4, par);
        z[0] += h;
        double dmax = 1.e30;
        for (int i = 0; i < par.length; ++i) {
            double x = par[i];
            if (!(x < dmax && x > -dmax)) {
                throw new RuntimeException("Floating point exception");
            }
        }
    }

    // Function to calculate the relative difference between two sets
    // of track parameters.  The result is scaled so that a difference
    // of less than one is "good."
    double pardiff(double[] par1, double[] par2,
                   double derivscale)
    {
        int n = par1.length;
        assert (par2.length == n);
        double epsmax = 0.;
        for (int i = 0; i < n; ++i) {
            double p1 = par1[i];
            double p2 = par2[i];
            double eps = abs(p2 - p1) / max(max(abs(p1), abs(p2)), 10.);
            if (i >= 5) {
                eps *= derivscale;
            }
            if (eps > epsmax) {
                epsmax = eps;
            }
        }
        return epsmax;
    }

    // Function to evolve track parameters using fourth order Runge-Kutta
    // with a variable step size.  The starting step size is as specified,
    // which may be reduced until the error is estimated to be small enough.
    // The z coordinate is updated to reflect the actual step size.  The 
    // estimated next step size is returned as the value of the function.
    double rkv(double[] par, double[] z, double h, double crv, double sign_dz,
               AbstractMagneticField bfield, double precision,
               double derivprec)
    {

        double derivscale = precision / derivprec;

        // Calculate the minimum allowed step, which is the lesser of 
        // either 1 cm or the initial step.

        double hmin = abs(h) / h;
        if (abs(hmin) > abs(h)) {
            hmin = h;
        }
        double hnext = h;
        for (;;) {

            // Reduce the target precision for short steps to control the 
            // accumulation of roundoff error.

            double maxdiff = precision * sqrt(0.1 * abs(h));
            double[] par1 = new double[par.length];
            System.arraycopy(par,0,par1,0,par.length);
            double[] par2 = new double[par.length];
            System.arraycopy(par,0,par2,0,par.length);;
            

            // Try step using h.

            double[] z1 = new double[1];
            z1[0] = z[0];
            double[] k1 = new double[par.length];
            rk4(par1, z1, h, crv, sign_dz, bfield, k1, false);

            // In short hop situations, quit here.

            if (abs(hmin) <= 0.01) {
                System.arraycopy(par1, 0, par, 0, par1.length);
                z[0] = z1[0];
                break;
            }

            // Try two steps using hh.

            double[] z2 = new double[1];
            z2[0] = z[0];
            double hh = 0.5 * h;
//      k1 = 0.5 * k1;
            for (int i = 0; i < k1.length; ++i) {
                k1[i] = 0.5 * k1[i];
            }
            rk4(par2, z2, hh, crv, sign_dz, bfield, k1, true);
            rk4(par2, z2, hh, crv, sign_dz, bfield, k1, false);

            // Calculate difference and get the next step size.

            double eps = pardiff(par1, par2, derivscale);
            if (eps <= maxdiff || abs(h) <= abs(hmin)) {

                // Check for catastrophic loss of accuracy.

                //	if(eps > 100000.*maxdiff)
                //	  throw new RuntimeException("Catastrophic loss of accuracy");

                // Current step is accurate enough.  Adjust the step size.
                // and return current propagation.

                if (eps != 0) {
                    hnext = 0.8 * h * pow(maxdiff / eps, 0.25);
                    if (abs(hnext) > 4. * abs(h)) {
                        hnext = 4. * h;
                    }
                } else {
                    hnext = 4. * h;
                }
//                par = (16. / 15.) * par2 - (1. / 15.) * par1;
                subtract(par, par2, par1);
                z[0] = z1[0];
                break;
            } else {

                // Not accurate enough.  Shrink the step size and try again.
                // Don't let the step get too small.

                hnext = 0.8 * h * pow(maxdiff / eps, 0.25);
                if (abs(hnext) < abs(hmin)) {
                    hnext = hmin;
                }
                h = hnext;
            }
        }

        // Final check on minimum step size.

        if (abs(hnext) < abs(hmin)) {
            hnext = hmin;
        }
        return hnext;
    }
//
    // This routine uses adaptive step size control to make the specified
    // step using one or several steps.

    void rka(double[] par, double[] z, double h, double crv, double sign_dz,
             AbstractMagneticField bfield, double precision,
             double derivprec)
    {
        assert (h != 0);
        double htry = h;
        double hnext;
        double z2 = z[0] + h;
        while (h > 0 && z[0] < z2 || h < 0 && z[0] > z2) {
            double hmax = abs(z2 - z[0]);
            if (abs(htry) > hmax) {
                htry = z2 - z[0];
            }
            hnext = rkv(par, z, htry, crv, sign_dz, bfield, precision, derivprec);
            assert (hnext * htry > 0);
            assert (z[0] + hnext != z[0]);
            htry = hnext;
        }
    }

// 
    private void scale(double[] a, double s, double[] b)
    {
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i] * s;
        }
    }

    private double[] add(double[] a, double s, double[] b)
    {
        double[] f = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            f[i] = a[i] + s * b[i];
        }
        return f;
    }

    private void sum(double[] k1, double[] k2, double[] k3, double[] k4, double[] p)
    {
        for (int i = 0; i < p.length; ++i) {
            p[i] += (1. / 6.) * (k1[i] + 2. * k2[i] + 2. * k3[i] + k4[i]);
        }
    }

    private void subtract(double[] par, double[] par2, double[] par1)
    {
        for (int i = 0; i < par.length; ++i) {
            par[i] = (16. / 15.) * par2[i] - (1. / 15.) * par1[i];
        }
    }

// Propagator interface here
// Propagate a track without error in the specified direction.
//
// The track parameters for a z-plane are:
// x y dx/dz dy/dz curvature=q/p
//
// If pder is nonzero, return the derivative matrix there.
    public PropStat vecDirProp(VTrack trv, Surface srf, PropDir dir,
                               TrackDerivative deriv)
    {

        // construct return status
        PropStat pstat = new PropStat();

        // fetch the originating surface.
        Surface srf1 = trv.surface();

        // Check origin is a z plane.
        Assert.assertTrue(srf1.pureType().equals(SurfZPlane.staticType()));
        if (!srf1.pureType().equals(SurfZPlane.staticType())) {
            return pstat;
        }

        SurfZPlane sz1 = (SurfZPlane) srf1;

        // Check destination is a z plane.
        Assert.assertTrue(srf.pureType().equals(SurfZPlane.staticType()));
        if (!srf.pureType().equals(SurfZPlane.staticType())) {
            return pstat;
        }
        SurfZPlane sz2 = (SurfZPlane) srf;

        // Separate the move status from the direction.
        PropDir rdir = dir; // need to check constness
        boolean move = Propagator.reduceDirection(rdir);

        // This flag indicates the new surface is only a short hop away.
        boolean short_hop = false;

        // If surfaces are the same and move is not set, then we leave the
        // track where it is.
        if (srf.pureEqual(srf1) && !move) {
            if (deriv != null) {
                deriv.setIdentity();
            }
            pstat.setSame();
            return pstat;
        }

        // Fetch the z coordinates.
        int iz = SurfZPlane.ZPOS;
        double z1 = sz1.parameter(iz);
        double z2 = sz2.parameter(iz);

        // Fetch the starting track vector.
        TrackVector vec = trv.vector();
        double x1 = vec.get(IX);                   // x
        double y1 = vec.get(IY);                   // y
        double xv1 = vec.get(IDXDZ);               // dx/dz
        double yv1 = vec.get(IDYDZ);               // dy/dz
        double crv = vec.get(IQP);                 // q/p
        double sign_dz = 0.;
        if (trv.isForward()) {
            sign_dz = 1.;
        } else if (trv.isBackward()) {
            sign_dz = -1.;
        }
        if (sign_dz == 0.) {
            return pstat;
        }

        // Check the consistency of the initial and final z positions, the track 
        // direction and the propagation direction.  Four of the eight possible
        // forward/backward combinations are illegal.  

        if (z2 == z1) {
            if (move) {
                System.out.println("PropZZRK: Invalid move");
                return pstat;
            }
        } else if (z2 > z1) {
            if (rdir == PropDir.FORWARD && trv.isBackward()
                    || rdir == PropDir.BACKWARD && trv.isForward()) {
                System.out.println("PropZZRK: Wrong direction");
                return pstat;
            }
        } else {  // z2 < z1
            if (rdir == PropDir.FORWARD && trv.isForward()
                    || rdir == PropDir.BACKWARD && trv.isBackward()) {
                System.out.println("PropZZRK: Wrong direction");
                return pstat;
            }
        }

        // We have all non-trivial mathematical operations in a try block
        // so we can catch floating point exceptions.

        {

            // Fill initial parameter vector.

            int size = 5;
            if (deriv != null) {
                size = 25;
            }
            double[] par = new double[size];
            par[0] = x1;       // x
            par[1] = y1;       // y
            par[2] = xv1;      // dx/dz
            par[3] = yv1;      // dy/dz
            par[4] = 0.;       // Path length.
            if (deriv != null) {
                par[5] = 1.;     // d x / d x0
                par[11] = 1.;    // d y / d y0
                par[17] = 1.;    // d xv / d xv0
                par[23] = 1.;    // d yv / d yv0
            }

            // Propagate parameter vector.

            double[] z = new double[1];
            z[0] = z1;
            double h = z2 - z1;
            if (abs(h) < 1.e-10) {
                z[0] = z2;
            } else {
                rka(par, z, h, crv, sign_dz, _bfield, _precision, _derivprec);
            }

            // Calculate final parameters and put them back into vec.

            double dmax = 1.e30;
            double x2 = par[0];
            double y2 = par[1];
            double xv2 = par[2];
            double yv2 = par[3];
            double s = par[4];
            vec.set(IX, x2);
            vec.set(IY, y2);
            vec.set(IDXDZ, xv2);
            vec.set(IDYDZ, yv2);
            vec.set(IQP, crv);
            if (!(x2 < dmax && x2 > -dmax)) {
                throw new RuntimeException("Floating point exception");
            }
            if (!(y2 < dmax && y2 > -dmax)) {
                throw new RuntimeException("Floating point exception");
            }
            if (!(xv2 < dmax && xv2 > -dmax)) {
                throw new RuntimeException("Floating point exception");
            }
            if (!(yv2 < dmax && yv2 > -dmax)) {
                throw new RuntimeException("Floating point exception");
            }
            if (!(s < dmax && s > -dmax)) {
                throw new RuntimeException("Floating point exception");
            }
            if (deriv != null) {

                // Calculate final derivative matrix.


                deriv.set(0, 0, par[5]);                // dx / d x0
                deriv.set(0, 1, par[6]);                // dx / d y0
                deriv.set(0, 2, par[7]);                // dx / d xv0
                deriv.set(0, 3, par[8]);                // dx / d yv0
                deriv.set(0, 4, par[9]);                // dx / d crv
                deriv.set(1, 0, par[10]);               // dy / d x0
                deriv.set(1, 1, par[11]);               // dy / d y0
                deriv.set(1, 2, par[12]);               // dy / d xv0
                deriv.set(1, 3, par[13]);               // dy / d yv0
                deriv.set(1, 4, par[14]);               // dy / d crv
                deriv.set(2, 0, par[15]);               // d xv / d x0
                deriv.set(2, 1, par[16]);               // d xv / d y0
                deriv.set(2, 2, par[17]);               // d xv / d xv0
                deriv.set(2, 3, par[18]);               // d xv / d yv0
                deriv.set(2, 4, par[19]);               // d xv / d crv
                deriv.set(3, 0, par[20]);               // d yv / d x0
                deriv.set(3, 1, par[21]);               // d yv / d y0
                deriv.set(3, 2, par[22]);               // d yv / d xv0
                deriv.set(3, 3, par[23]);               // d yv / d yv0
                deriv.set(3, 4, par[24]);               // d yv / d crv
                deriv.set(4, 0, 0.);                    // d crv / d x0
                deriv.set(4, 1, 0.);                    // d crv / d y0
                deriv.set(4, 2, 0.);                    // d crv / d xv0
                deriv.set(4, 3, 0.);                    // d crv / d yv0
                deriv.set(4, 4, 1.);                    // d crv / d crv
                for (int i = 0; i < 4; ++i) {
                    for (int j = 0; j < 5; ++j) {
                        double x = deriv.get(i, j);
                        if (!(x < dmax && x > -dmax)) {
                            throw new RuntimeException("Floating point exception");
                        }
                    }
                }
            }

            // Update trv
            boolean forward = trv.isForward();
            boolean backward = trv.isBackward();
            assert (forward || backward);
            assert (!(forward && backward));
            trv.setSurface(srf.newPureSurface());
            trv.setVector(vec);
            if (forward) {
                trv.setForward();
            } else if (backward) {
                trv.setBackward();
            }

            // Set the return status.
            pstat.setPathDistance(s);
        }
        return pstat;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer( "Z Plane RK propagation with magnetic field: \n");
         sb.append(" "+_bfield+"\n");
	 sb.append(" precision = " + _precision);
	 sb.append(", derivprec = " + _derivprec+"\n");
         return sb.toString();
    }
}
