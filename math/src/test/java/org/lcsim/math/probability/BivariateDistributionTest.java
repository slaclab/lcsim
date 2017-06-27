/*
 * BivariateDistributionTest class
 */
package org.lcsim.math.probability;

import junit.framework.TestCase;

/**
 * Test case for BivariateDistribution class
 *
 * @author Richard Partridge
 */
public class BivariateDistributionTest extends TestCase {

    /** Creates a new instance of HelicalTrackFitterTest */
    public void testBivariateDistribution() {

        //  Instantiate the BivariateDistribution class
        BivariateDistribution b = new BivariateDistribution();

        //  Set up the x coordinate binning
        int nx = 140;
        double dx = 0.1;
        double xmin = -0.5 * nx * dx;
        b.xBins(nx, xmin, dx);

        //  Set up the y coordinate binning
        int ny = 140;
        double dy = 0.1;
        double ymin = -0.5 * ny * dy;
        b.yBins(ny, ymin, dy);

        //  Set the bivariate Gaussian parameters to some semi-random values
        double x0 = 0.526;
        double y0 = -0.317;
        double sigx0 = 0.642;
        double sigy0 = 0.784;
        double rho0 = 0.231;

        //  Calculate the bivariate probabilities for our x-y bins
        double[][] bi = b.Calculate(x0, y0, sigx0, sigy0, rho0);

        //  Now calculate our parameter estimates from the binned data
        double xave = 0.;
        double yave = 0.;
        double xysum = 0.;
        double xxsum = 0.;
        double yysum = 0.;
        double psum = 0.;
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                double x = xmin + dx * (i + 0.5);
                double y = ymin + dy * (j + 0.5);
                double prob = bi[i][j];
                xave += prob * x;
                yave += prob * y;
                xxsum += prob * x * x;
                yysum += prob * y * y;
                xysum += prob * x * y;
                psum += prob;
            }
        }

        //  Calculate the measured error matrix
        double sigx = Math.sqrt(xxsum - xave * xave);
        double sigy = Math.sqrt(yysum - yave * yave);
        double rho = (xysum - xave * yave) / (sigx * sigy);

        System.out.println(" x ave: " + xave + " y ave: " + yave);
        System.out.println(" x sd: " + sigx + " y sd: " + sigy + " rho: " + rho);
        System.out.println("PSum: " + psum);

        //  Test that probability is conserved - this is the key test that
        //  the method is working.  Estimate a 5 sigma round-off error from
        //  summing nx*ny bins assuming 1e-16 precision per bin.
        assertEquals("Probability sum failure", 1.0, psum, 5.0e-16*Math.sqrt(nx*ny));

        //  Make some crude tests that the Gaussian parameters are reasonable
        //  These are not precisely measured due to the coarse binning
        assertEquals("x ave failure", x0, xave, 1e-10);
        assertEquals("y ave failure", y0, yave, 1e-10);
        assertEquals("sig x failure", sigx0, sigx, 2e-3);
        assertEquals("sig y failure", sigy0, sigy, 2e-3);
        assertEquals("rho failure", rho0, rho, 2e-3);

    }
}