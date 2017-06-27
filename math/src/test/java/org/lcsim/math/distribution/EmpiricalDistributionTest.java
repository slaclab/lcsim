package org.lcsim.math.distribution;

import junit.framework.TestCase;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: 
 */
public class EmpiricalDistributionTest extends TestCase
{

    /**
     * Test of class EmpiricalDistribution.
     */
    public void testEmpiricalDistribution()
    {
        double[] input =
        {
            0., 1., 2., 1., 0.
        };
        EmpiricalDistribution ed = new EmpiricalDistribution(input);
//        for (int i = 0; i < input.length; ++i)
//        {
//            System.out.println(i);
//            System.out.println("input[" + i + "]= " + input[i] + " pdf[" + (i + 1) + "]= " + ed.pdf(i + 1));
//        }
        double sum = 0.;
        int nTrials = 100000;
        for (int i = 0; i < nTrials; ++i)
        {
            double ran = ed.nextDouble();
            sum += ran;
            assertTrue(ran > 0.2);
            assertTrue(ran < 0.8);            
        }
        double average = sum / nTrials;
        assertEquals(0.5, average, .002);
        
//        System.out.println("\n*********\n");
        double[] bimodal =
        {
            0., 1., 0., 1., 0.
        };
        ed = new EmpiricalDistribution(bimodal);
//        for (int i = 0; i < bimodal.length; ++i)
//        {
//            System.out.println(i);
//            System.out.println("bimodal[" + i + "]= " + bimodal[i] + " pdf[" + (i + 1) + "]= " + ed.pdf(i + 1));
//        }        
        nTrials = 100000;
        ed.setSeed(12345);
        double oldRand = ed.nextDouble();
        for (int i = 0; i < nTrials; ++i)
        {
            double ran = ed.nextDouble();
            sum += ran;
            assertTrue((0.2 < ran && ran < 0.4) || (0.6 < ran && ran < 0.8));
        }
        ed.setSeed(12345);
        double newRand = ed.nextDouble();
        assertEquals(oldRand, newRand);
        
    }
}
