/*
 * ErfTest Class
 */

package org.lcsim.math.probability;

import junit.framework.TestCase;

/**
 *  Test case for Erf methods
 *
 * @author partridge
 */
public class ErfTest extends TestCase {

    public void testErf() {

        //  Value of erf(1) from Abramowitz and Steigun
        double erf1 = 0.8427007929;
        double root2 = Math.sqrt(2.);
        assertEquals("Erf", erf1, Erf.erf(1.), 1e-10);
        assertEquals("Erfc", 1. - erf1, Erf.erfc(1.), 1e-10);
        assertEquals("Phi", 0.5 * (1. + erf1), Erf.phi(root2), 1e-10);
        assertEquals("PhiC", 0.5 * (1. - erf1), Erf.phic(root2), 1e-10);
    }
}
