/*
 *  Class BivariateDistribution
 */
package org.lcsim.math.probability;

/**
 * Calculate the probability integral for a set of bins in the x-y plane
 * of a bivariate normal distribution (i.e., a 2D Gaussian probability).
 *<p>
 * The evaluation of the probability integrals is described in:
 *<p>
 * Alan Genz, "Numerical Computation of Rectangular Bivariate and Trivariate
 * Normal and t Probabilities" in Statistics and Computing 14, 151 (2004).
 *<p>
 * The integration code is adapted from the FORTRAN source at:
 *<p>
 *   http://www.math.wsu.edu/faculty/genz/homepage
 *<p>
 * @author Richard Partridge
 */
public class BivariateDistribution {

    private int _nx;
    private int _ny;
    private double _xmin;
    private double _ymin;
    private double _dx;
    private double _dy;
    private double[] _h;
    private double[] _k;

    //  Weights and coordinates for 6 point Gauss-Legendre integration
    private double[] _w6 = {0.1713244923791705, 0.3607615730481384, 0.4679139345726904};
    private double[] _x6 = {0.9324695142031522, 0.6612093864662647, 0.2386191860831970};

    //  Weights and coordinates for 12 point Gauss-Legendre integration
    private double[] _w12 = {.04717533638651177, 0.1069393259953183, 0.1600783285433464,
        0.2031674267230659, 0.2334925365383547, 0.2491470458134029};
    private double[] _x12 = {0.9815606342467191, 0.9041172563704750, 0.7699026741943050,
        0.5873179542866171, 0.3678314989981802, 0.1252334085114692};

    //  Weights and coordinates for 20 point Gauss-Legendre integration
    private double[] _w20 = {.01761400713915212, .04060142980038694, .06267204833410906,
        .08327674157670475, 0.1019301198172404, 0.1181945319615184,
        0.1316886384491766, 0.1420961093183821, 0.1491729864726037,
        0.1527533871307259};
    private double[] _x20 = {0.9931285991850949, 0.9639719272779138, 0.9122344282513259,
        0.8391169718222188, 0.7463319064601508, 0.6360536807265150,
        0.5108670019508271, 0.3737060887154196, 0.2277858511416451,
        0.07652652113349733};

    /**
     * Set the locations of the x-coordinate bins
     *
     * @param nx number of x coordinate bins
     * @param xmin minimum x coordinate
     * @param dx width of x coordinate bins
     */
    public void xBins(int nx, double xmin, double dx) {
        _nx = nx;
        _xmin = xmin;
        _dx = dx;
        _h = new double[_nx + 1];
    }

    /**
     * Set the locations of the y-coordinate bins
     *
     * @param ny number of y coordinate bins
     * @param ymin minimum y coordinate
     * @param dy width of y coordinate bins
     */
    public void yBins(int ny, double ymin, double dy) {
        _ny = ny;
        _ymin = ymin;
        _dy = dy;
        _k = new double[_ny + 1];
    }

    /**
     * Integrate the Gaussian probability distribution over each x-y bins,
     * which must be defined before calling this method.
     * <p>
     * The output is a double array that gives the binned probability
     * distribution.  The first array index is used to indicate the bin in x
     * and the second array index is used to indicate the bin in y.
     * <p>
     * @param x0 mean x coordinate of Gaussian distribution
     * @param y0 mean y coordinate of Gaussian distribution
     * @param sigx x coordinate standard deviation
     * @param sigy y coordinate standard deviation
     * @param rho x-y correlation coefficient
     * @return probability distribution
     */
    public double[][] Calculate(double x0, double y0, double sigx, double sigy,
            double rho) {

        //  Calculate the scaled x coordinate for each bin edge
        for (int i = 0; i < _nx + 1; i++) {
            _h[i] = (_xmin + i * _dx - x0) / sigx;
        }

        //  Calculate the scaled y coordinate for each bin edge
        for (int j = 0; j < _ny + 1; j++) {
            _k[j] = (_ymin + j * _dy - y0) / sigy;
        }

        //  Create the array that will hold the binned probabilities
        double[][] bi = new double[_nx][_ny];

        //  Loop over the bin vertices
        for (int i = 0; i < _nx + 1; i++) {
            for (int j = 0; j < _ny + 1; j++) {

                //  Calculate the probability for x>h and y>k for this vertex
                double prob = GenzCalc(_h[i], _k[j], rho);

                //  Add or subtract this probability from the affected bins.
                //  The bin probability for bin (0,0) is the sum of the Genz
                //  probabilities for the (0,0) and (1,1) vertices MINUS the
                //  sum of the probabilities for the (0,1) and (1,0) vertices
                if (i > 0 && j > 0) {
                    bi[i - 1][j - 1] += prob;
                }
                if (i > 0 && j < _ny) {
                    bi[i - 1][j] -= prob;
                }
                if (i < _nx && j > 0) {
                    bi[i][j - 1] -= prob;
                }
                if (i < _nx && j < _ny) {
                    bi[i][j] += prob;
                }
            }
        }

        return bi;
    }

    private double GenzCalc(double dh, double dk, double rho) {

        double twopi = 2. * Math.PI;

        //  Declare the Gauss-Legendre constants
        int ng;
        double[] w;
        double[] x;

        if (Math.abs(rho) < 0.3) {
            //  for rho < 0.3 use 6 point Gauss-Legendre integration
            ng = 3;
            w = _w6;
            x = _x6;
        } else if (Math.abs(rho) < 0.75) {
            //  for 0.3 < rho < 0.75 use 12 point Gauss-Legendre integration
            ng = 6;
            w = _w12;
            x = _x12;
        } else {
            //  for rho > 0.75 use 20 point Gauss-Legendre integration
            ng = 10;
            w = _w20;
            x = _x20;
        }

        //  Initialize the probability and some local variables
        double bvn = 0.;
        double h = dh;
        double k = dk;
        double hk = h * k;

        //  For rho < 0.925, integrate equation 3 in the Genz paper
        if (Math.abs(rho) < 0.925) {

            //  More or less direct port of Genz code follows
            //  It is fairly easy to match this calculation against equation 3 of
            //  Genz's paper if you take into account that you need to change
            //  variables so the integration argument spans the range -1 to 1
            double hs = (h * h + k * k) / 2.;
            double asr = Math.asin(rho);
            double sn;
            for (int i = 0; i < ng; i++) {
                sn = Math.sin(asr * (1 - x[i]) / 2.);
                bvn += w[i] * Math.exp((sn * hk - hs) / (1 - sn * sn));
                sn = Math.sin(asr * (1 + x[i]) / 2.);
                bvn += w[i] * Math.exp((sn * hk - hs) / (1 - sn * sn));
            }
            //  The factor of asr/2 comes from changing variables so the
            //  integration is over the range -1 to 1 instead of 0 - asin(rho)
            bvn = bvn * asr / (2. * twopi) + Erf.phi(-h) * Erf.phi(-k);

        } else {
            //  rho > 0.925 - integrate equation 6 in Genz paper with the
            //  extra term in the Taylor expansion given in equation 7.
            //  The rest of this code is pretty dense and is a pretty direct
            //  port of Genz's code.

            if (rho < 0.) {
                k = -k;
                hk = -hk;
            }

            if (Math.abs(rho) < 1.) {

                double as = (1 - rho) * (1 + rho);
                double a = Math.sqrt(as);
                double bs = (h - k) * (h - k);
                double c = (4. - hk) / 8.;
                double d = (12. - hk) / 16.;
                double asr = -(bs / as + hk) / 2.;

                if (asr > -100.) {
                    bvn = a * Math.exp(asr) *
                            (1. - c * (bs - as) * (1. - d * bs / 5.) / 3. +
                            c * d * as * as / 5.);
                }

                if (-hk < 100.) {
                    double b = Math.sqrt(bs);
                    bvn -= Math.exp(-hk / 2.) * Math.sqrt(twopi) * Erf.phi(-b / a) *
                            b * (1 - c * bs * (1 - d * bs / 5.) / 3.);
                }

                a = a / 2.;
                for (int i = 0; i < ng; i++) {
                    for (int j = 0; j < 2; j++) {
                        int is = -1;
                        if (j > 0) {
                            is = 1;
                        }
                        double xs = Math.pow(a * (is * x[i] + 1), 2);
                        double rs = Math.sqrt(1 - xs);
                        asr = -(bs / xs + hk) / 2;

                        if (asr > -100) {
                            double sp = (1 + c * xs * (1 + d * xs));
                            double ep = Math.exp(-hk * (1 - rs) / (2 * (1 + rs))) / rs;
                            bvn += a * w[i] * Math.exp(asr) * (ep - sp);
                        }
                    }
                }

                bvn = -bvn / twopi;
            }

            if (rho > 0) {
                bvn = bvn + Erf.phi(-Math.max(h, k));
            } else {
                bvn = -bvn;
                if (k > h) {
                    bvn += Erf.phi(k) - Erf.phi(h);
                }
            }
        }
         
        return Math.max(0, Math.min(1, bvn));
    }
}