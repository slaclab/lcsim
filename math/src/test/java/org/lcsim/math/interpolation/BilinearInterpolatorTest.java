/*
 * BilinearInterpolatorTest.java
 * JUnit based test
 *
 * Created on June 3, 2008, 4:06 PM
 *
 * $Id: BilinearInterpolatorTest.java,v 1.1 2010/12/01 01:25:26 jeremy Exp $
 */

package org.lcsim.math.interpolation;

import junit.framework.*;

/**
 *
 * @author Norman Graf
 */
public class BilinearInterpolatorTest extends TestCase
{
    
    /**
     * Test of interpolateValueAt method, of class org.lcsim.math.BilinearInterpolator.
     */
    public void testInterpolateValueAt()
    {
        double[] a = {-2, -1, 0, 1, 2};
        double[] b = {-2, -1, 0, 1, 2};
        double[][] vals = new double[a.length][b.length];
        
        for(int i=0; i<a.length; ++i)
        {
            for(int j =0; j<b.length; ++j)
            {
                vals[i][j] = a[i]*a[i]+b[j]*b[j];
            }
        }
        
        BilinearInterpolator instance = new BilinearInterpolator(a, b, vals);
        
        double x = 0.0;
        double y = 0.0;
        
        for(int i=0; i<a.length; ++i)
        {
            for(int j=0; j<b.length; ++j)
            {
//                System.out.println("a= "+a[i]+", b= "+b[j]+" ,  interpolates to "+instance.interpolateValueAt(a[i],b[j]));
//                System.out.println("should be "+vals[i][j]);
                assertEquals(instance.interpolateValueAt(a[i],b[j]), vals[i][j]);
            }
        }
        
        for(double i=a[0]; i<a[a.length-1]; i+=.2)
        {
            for(double j=b[0]; j<b[b.length-1]; j+=.2)
            {
                double pred = instance.interpolateValueAt(i,j);
                double real = i*i+j*j;
//                System.out.format("( %4.2f , %4.2f ) interpolates to %4.2f%n",i, j, pred);
//                System.out.println("should be "+real);
                assertEquals(real, pred ,.5);
            }
        }
    }
}