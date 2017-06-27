/*
 * ThreePointCircleFitter.java
 *
 * Created on March 23, 2006, 3:41 PM
 *
 *
 */

package org.lcsim.fit.threepointcircle;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
/**
 * A class to return the circle which passes through three points.
 * @author Norman Graf
 */
public class ThreePointCircleFitter
{
    private CircleFit _fit;
    private static double[][] a = {{0, 0, 0},{0, 0, 0},{0, 0, 0}};
    private static double[][] m = {{0, 0, 0},{0, 0, 0},{0, 0, 0}};
    
    /** Creates a new instance of ThreePointCircleFitter */
    public ThreePointCircleFitter()
    {
    }
    
    /**
     * Find the circle which passes through three points
     * @param p1 The first point.
     * @param p2 The second point.
     * @param p3 The third point.
     * @return true if a circle can be found which passes through the points.
     */
    public boolean fit(double[] p1, double[] p2, double[] p3)
    {
        int i;
        double r, m11, m12, m13, m14;

        double[][] P = {p1, p2, p3};
        
        for (i = 0; i < 3; i++)                    // find minor 11
        {
            a[i][0] = P[i][0];
            a[i][1] = P[i][1];
            a[i][2] = 1;
        }
        m11 = determinant( a, 3 );
        if(m11==0) 
        {
            _fit = null;
            return false;
        }
        
        for (i = 0; i < 3; i++)                    // find minor 12
        {
            a[i][0] = P[i][0]*P[i][0] + P[i][1]*P[i][1];
            a[i][1] = P[i][1];
            a[i][2] = 1;
        }
        m12 = determinant( a, 3 );
        
        for (i = 0; i < 3; i++)                    // find minor 13
        {
            a[i][0] = P[i][0]*P[i][0] + P[i][1]*P[i][1];
            a[i][1] = P[i][0];
            a[i][2] = 1;
        }
        m13 = determinant( a, 3 );
        
        for (i = 0; i < 3; i++)                    // find minor 14
        {
            a[i][0] = P[i][0]*P[i][0] + P[i][1]*P[i][1];
            a[i][1] = P[i][0];
            a[i][2] = P[i][1];
        }
        m14 = determinant( a, 3 );
              
        double x0 =  0.5 * m12 / m11;                 // center of circle
        double y0 = -0.5 * m13 / m11;
        r  = sqrt( x0*x0 + y0*y0 + m14/m11 );
        
        _fit = new CircleFit(x0, y0, r);
        
        return true;
    }
    
    /**
     * Get the result of the fit.
     * @return The Circle which passes through the points.
     */
    public CircleFit getFit()
    {
        return _fit;
    }
    
    // Recursive definition of determinant using expansion by minors.
    private double determinant(double[][] a, int n)
    {
        int i, j, j1, j2;
        double d = 0.;
   
        if (n == 2)                                // terminate recursion
        {
            d = a[0][0]*a[1][1] - a[1][0]*a[0][1];
        }
        else
        {
            d = 0;
            for (j1 = 0; j1 < n; ++j1 )            // do each column
            {
                for (i = 1; i < n; ++i)            // create minor
                {
                    j2 = 0;
                    for (j = 0; j < n; ++j)
                    {
                        if (j == j1) continue;
                        m[i-1][j2] = a[i][j];
                        j2++;
                    }
                }
                // sum (+/-)cofactor * minor
                d += pow(-1.0, j1)*a[0][j1]*determinant( m, n-1 );
            }
        }
        return d;
    }
}