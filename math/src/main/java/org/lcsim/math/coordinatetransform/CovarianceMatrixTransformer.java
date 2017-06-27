/*
 * CovarianceMatrixTransformer.java
 *
 * Created on March 30, 2006, 3:35 PM
 *
 * $Id: CovarianceMatrixTransformer.java,v 1.1.1.1 2010/11/30 21:32:00 jeremy Exp $
 */

package org.lcsim.math.coordinatetransform;
import static java.lang.Math.sqrt;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Utility class to handle transformations of covariance matrices
 * Inspired by Nick Sinev's CMTransform.java.
 * @author Norman Graf
 */
public class CovarianceMatrixTransformer
{
    
    /** No need to create, all methods static*/
    private CovarianceMatrixTransformer()
    {
    }
    
    /**
     * Convert covariance matrix elements in cartesian coordinates (x,y) to cylindrical (r,phi).
     * @param x The cartesian x coordinate.
     * @param y The cartesian y coordinate.
     * @param sxx The covariance matrix term for x at (x,y).
     * @param syy The covariance matrix term for y at (x,y).
     * @param sxy The covariance matrix term for xy at (x,y).
     * @return The packed r-phi covariance matrix terms:
     * <table>
     * <tr><td> cov[0] </td><td> r-r </td><tr>
     * <tr><td> cov[1] </td><td> phi-phi </td><tr>
     * <tr><td> cov[2] </td><td> r-phi</td><tr>
     * </table>
     */
    public static double[] xy2rphi(double x, double y, double sxx, double syy, double sxy)
    {
        double[] cov = new double[3];
        double xx = x*x;
        double xy = x*y;
        double yy = y*y;
        double rr = xx+yy;
        double r = sqrt(rr);
        double oneOverR2 = 1/rr;
        
        // srr
        cov[0] = oneOverR2*(sxx*xx + syy*yy + 2.*sxy*xy);
        
        // sphiphi
        cov[1] = oneOverR2*oneOverR2*(sxx*yy - 2.*sxy*xy + syy*xx);
        
        // srphi
        cov[2] = oneOverR2*(-sxx*xy + sxy*(xx-yy) + syy*xy)/r;
        
        return cov;
    }
    
    /**
     *Convert covariance matrix elements in cylindrical (r,phi) to cartesian coordinates (x,y).
     * @param r The cylindrical radius.
     * @param phi The cylindrical angle.
     * @param srr The covariance matrix term for r at (r, phi).
     * @param sff The covariance matrix term for phi at (r, phi).
     * @param srf The covariance matrix term for r-phi at (r, phi).
     * @return The packed x-y covariance matrix terms:
     * <table>
     * <tr><td> cov[0] </td><td> x-x </td><tr>
     * <tr><td> cov[1] </td><td> y-y </td><tr>
     * <tr><td> cov[2] </td><td> x-y</td><tr>
     * </table>
     */
     
    public static double[] rphi2xy(double r, double phi, double srr, double sff, double srf)
    {
        double[] cov = new double[3];
        // cosine^2(phi)
        double cf = cos(phi);
        double cc = cf*cf;
        //sine^2(phi)
        double sf = sin(phi);
        double ss = sf*sf;
        
        // cosine(phi)*sine(phi)
        double cs = cf*sf;
        
        // sxx
        cov[0] = srr*cc - 2.*srf*r*cs + sff*r*r*ss;
        
        // syy
        cov[1] = srr*ss + 2.*srf*r*cs + sff*r*r*cc;
        
        // sxy
        cov[2] = srr*cs + srf*r*(cc - ss) - sff*r*r*cs;
        
        return cov;
    }
}
