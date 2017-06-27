/*
 * CentralMomentsCalculatorTest.java
 *
 * Created on April 5, 2006, 1:41 PM
 *
 * $Id: CentralMomentsCalculatorTest.java,v 1.1 2010/12/01 01:25:26 jeremy Exp $
 */

package org.lcsim.math.moments;

import java.util.Random;
import junit.framework.TestCase;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.SpacePoint;

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

/**
 *
 * @author Norman Graf
 */
public class CentralMomentsCalculatorTest extends TestCase
{
    
    /** Creates a new instance of CentralMomentsCalculatorTest */
    public void testCentralMomentsCalculator()
    {
        double[] x = {0., 1., 2.};
        double[] y = {0., 0., 0.};
        double[] z = {0., 0., 0.};
        double[] w = {1., 1., 1.};
        
        CentralMomentsCalculator mc = new CentralMomentsCalculator();
        
        mc.calculateMoments(x, y, z, w);
        
        double[] c = mc.centroid();
        assertEquals(c[0],1.);
        assertEquals(c[1],0.);
        assertEquals(c[2],0.);
        
        w[2] = 2.;
        mc.calculateMoments(x, y, z, w);
        SpacePoint p = new CartesianPoint(mc.centroid());
//        System.out.println(p);
        
        int nPoints = 1000;
        x = new double[nPoints];
        y = new double[nPoints];
        z = new double[nPoints];
        w = new double[nPoints];
        
        // try a sphere
        // a = b = c = 10.
//        System.out.println("");
//        System.out.println("");
//        System.out.println("Sphere");
        generateEvents(10., 10., 10., nPoints, x, y, z, w);
        mc.calculateMoments(x, y, z, w);
        p = new CartesianPoint(mc.centroid());
//        System.out.println(p);
        double[] inv = mc.invariants();
//        System.out.println("inv: "+inv[0]+" "+inv[1]+" "+inv[2]);
        
        
        // try an ellipsoid
        // a = 5
        // b = 17
        // c = 32
//        System.out.println("");
//        System.out.println("");
//        System.out.println("Ellipsoid");
        generateEvents(5., 17., 32., nPoints, x, y, z, w);
        mc.calculateMoments(x, y, z, w);
        p = new CartesianPoint(mc.centroid());
//        System.out.println(p);
        inv = mc.invariants();
//        System.out.println("inv: "+inv[0]+" "+inv[1]+" "+inv[2]);
        
        // try a cigar
        // a = 5
        // b = 5
        // c = 32
//        System.out.println("");
//        System.out.println("");
//        System.out.println("Cigar");
        generateEvents(5., 5., 32., nPoints, x, y, z, w);
        mc.calculateMoments(x, y, z, w);
        p = new CartesianPoint(mc.centroid());
//        System.out.println(p);
        inv = mc.invariants();
//        System.out.println("inv: "+inv[0]+" "+inv[1]+" "+inv[2]);        
        
        // try a plate
        // a = 5
        // b = 20
        // c = 20
//        System.out.println("");
//        System.out.println("");
//        System.out.println("Plate");
        generateEvents(5., 20., 20., nPoints, x, y, z, w);
        mc.calculateMoments(x, y, z, w);
        p = new CartesianPoint(mc.centroid());
//        System.out.println(p);
        inv = mc.invariants();
//        System.out.println("inv: "+inv[0]+" "+inv[1]+" "+inv[2]);        
        
    }
    
    // generate nPoints events according to the ellipsoid equation:
    // x = a cos(phi) sin(theta)
    // y = b sin(phi) sin(theta)
    // z = c cos(theta)
    //
    void generateEvents(double a, double b, double c, int nPoints, double[] x, double[] y, double[] z, double[] w)
    {
        Random r = new Random();
        for(int i=0; i<nPoints; ++i)
        {
            double t = PI*r.nextDouble();
            double p = 2.*PI*r.nextDouble();
            x[i] = a*cos(p)*sin(t);
            y[i] = b*sin(p)*sin(t);
            z[i] = c*cos(t);
            w[i] = 1.;
        }
    }
}
