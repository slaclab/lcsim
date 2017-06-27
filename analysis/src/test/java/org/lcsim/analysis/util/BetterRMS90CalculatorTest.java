package org.lcsim.analysis.util;

import junit.framework.TestCase;

/**
 *
 * @author tonyj
 */
public class BetterRMS90CalculatorTest extends TestCase {

    public BetterRMS90CalculatorTest(String testName) {
        super(testName);
    }

    /**
     * Test of calculateRMS90 method, of class BetterRMS90Calculator.
     */
    public void testCalculateRMS90() {
        BetterRMS90Calculator rms90 = new BetterRMS90Calculator();
        double[] data = {100, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Result result = rms90.calculate(data);
        assertEquals(0, result.getMean(), 1e-16);
        assertEquals(0, result.getRms(), 1e-16);

        data = new double[]{-100, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        result = rms90.calculate(data);
        assertEquals(0, result.getMean(), 1e-16);
        assertEquals(0, result.getRms(), 1e-16);

        data = new double[]{-100, -100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        result = rms90.calculate(data);
        assertEquals(0, result.getMean(), 1e-16);
        assertEquals(0, result.getRms(), 1e-16);

        data = new double[]{-100, -100, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        result = rms90.calculate(data);
        assertEquals(1, result.getMean(), 1e-16);
        assertEquals(0, result.getRms(), 1e-16);


        data = new double[]{-100, -100, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2};
        result = rms90.calculate(data);
        assertEquals(1.5, result.getMean(), 1e-16);
        assertEquals(0.5, result.getRms(), 1e-16);
    }
}
