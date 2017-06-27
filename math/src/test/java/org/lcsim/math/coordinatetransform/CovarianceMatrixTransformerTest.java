/*
 * CovarianceMatrixTransformerTest.java
 *
 * Created on March 30, 2006, 3:57 PM
 *
 * $Id: CovarianceMatrixTransformerTest.java,v 1.1 2010/12/01 01:25:26 jeremy Exp $
 */

package org.lcsim.math.coordinatetransform;

import junit.framework.TestCase;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
/**
 *
 * @author Norman Graf
 */
public class CovarianceMatrixTransformerTest extends TestCase
{
    
    /** Creates a new instance of CovarianceMatrixTransformerTest */
    public  void testCovarianceMatrixTransformer()
    {
        double eps = 1E-8;
        double x = 1.0;
        double y = 0.0;
        double sx = .1;
        double sy = 0.;
        double sxy = 0.;
        
        double[] cov = CovarianceMatrixTransformer.xy2rphi(x, y, sx*sx, sy*sy, sxy);
//        System.out.println("srr= "+cov[0]);
//        System.out.println("sphiphi= "+cov[1]);
//        System.out.println("srphi= "+cov[2]);
        
        assertEquals(cov[0], sx*sx, eps);
        assertEquals(cov[1], sy*sy, eps);
        assertEquals(cov[2], sxy, eps);
        
//        System.out.println(" ");
        sx = 0.;
        sy = .1;
        cov = CovarianceMatrixTransformer.xy2rphi(x, y, sx*sx, sy*sy, sxy);
//        System.out.println("srr= "+cov[0]);
//        System.out.println("sphiphi= "+cov[1]);
//        System.out.println("srphi= "+cov[2]);
        
        assertEquals(cov[0], sx*sx, eps);
        assertEquals(cov[1], sy*sy, eps);
        assertEquals(cov[2], sxy, eps);
        
//        System.out.println(" ");
        x = .5;
        y = .5;
        sx = .1;
        sy = .1;
        sxy = -.1;
        cov = CovarianceMatrixTransformer.xy2rphi(x, y, sx*sx, sy*sy, sxy);
//        System.out.println("srr= "+cov[0]);
//        System.out.println("sphiphi= "+cov[1]);
//        System.out.println("srphi= "+cov[2]);
        
        // now let's round-trip...
        double srr = cov[0];
        double sphiphi = cov[1];
        double srphi = cov[2];
        double r = sqrt(x*x+y*y);
        double phi = atan2(y,x);
        
        
        cov = CovarianceMatrixTransformer.rphi2xy(r, phi, srr, sphiphi, srphi);
//        for(int i=0; i<3; ++i) System.out.println("cov["+i+"]= "+cov[i]);
        
        
        assertEquals(sqrt(cov[0]), sx, eps);
        assertEquals(sqrt(cov[1]), sy, eps);
        assertEquals(cov[2], sxy, eps);
        
        
//        System.out.println(" ");
        // start from rPhi...
        r = 1.;
        double dr = 0.;
        phi= PI/4.;
        double dPhi = .1;
        srr = dr*dr;
        sphiphi = dPhi*dPhi;
        srphi = 0.;
        
        cov = CovarianceMatrixTransformer.rphi2xy(r, phi, srr, sphiphi, srphi);
//        for(int i=0; i<3; ++i) System.out.println("cov["+i+"]= "+cov[i]);
        
        
        // dx should equal dy
        assertEquals(cov[0], cov[1], eps);
        // cov term should be neg
        assertTrue(cov[2]<0);
        // normalized cov term should be -1
        assertEquals(cov[2]/cov[0], -1, eps);
        
        // now round-trip...
        x = r*cos(phi);
        y = r*sin(phi);
        cov = CovarianceMatrixTransformer.xy2rphi(x, y, cov[0], cov[1], cov[2]);
//        for(int i=0; i<3; ++i) System.out.println("cov["+i+"]= "+cov[i]);
        assertEquals(cov[0], srr, eps);
        assertEquals(cov[1],sphiphi, eps);
        assertEquals(cov[2], srphi, eps);
    }
}
