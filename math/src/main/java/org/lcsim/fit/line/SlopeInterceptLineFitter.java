/*
 * SlopeInterceptLineFitter.java
 *
 * Created on March 27, 2006, 3:19 PM
 *
 * $Id: SlopeInterceptLineFitter.java,v 1.1 2006/03/28 04:51:03 ngraf Exp $
 */

package org.lcsim.fit.line;
import static java.lang.Math.sqrt;
/**
 * A least-squares fit to a 2-D line in slope-intercept form y=a+bx. 
 * It assumes that there is no error on the x-component and that all 
 * the error can be projected onto the y-component.
 * @author Norman Graf
 */
public class SlopeInterceptLineFitter
{
    private SlopeInterceptLineFit _fit;
    /** Creates a new instance of SlopeInterceptLineFitter */
    public SlopeInterceptLineFitter()
    {
        
    }
    
    /**
     * Perform the fit.
     * @param x The array of independent variables. 
     * @param y The array of dependent variables.
     * @param sigma_y The array of uncertainties on the dependent variables.
     * @param n The number of points to fit
     * @return true if the fit is successful.
     */
    public boolean fit(double[] x, double[] y, double[] sigma_y, int n)
    {
        double sum,sx,sy,sxx,sxy,syy,det;
        double chisq = 999999.0;
        int i;
        
        if (n < 2)
        {
            return false; //too few points, abort
        }
        
        //initialization
        sum = sx = sy = sxx = sxy = syy = 0.;
        
        //find sum , sumx ,sumy, sumxx, sumxy
        double[] w = new double[n];
        for (i=0; i<n; ++i)
        {
            w[i] = 1/(sigma_y[i]*sigma_y[i]);
            sum +=  w[i];
            sx  += w[i]*x[i];
            sy  += w[i]*y[i];
            sxx += w[i]*x[i]*x[i];
            sxy += w[i]*x[i]*y[i];
            syy += w[i]*y[i]*y[i];
        }
        
        det = sum*sxx-sx*sx;
        if (Math.abs(det) < 1.0e-20) return false; //Zero determinant, abort
        
        //compute the best fitted parameters A,B
        
        double slope = (sum*sxy-sx*sy)/det;
        double intercept = (sy*sxx-sxy*sx)/det;
        
        //calculate chisq-square
        
        chisq = 0.0;
        for (i=0; i<n; ++i)
        {
            chisq += w[i]*((y[i])-slope*(x[i])-intercept)*((y[i])-slope*(x[i])-intercept);
        }
        
        double slopeErr = sqrt(sum/det);
        double interceptErr = sqrt(sxx/det);
        double sigab = -sx/det;
        
        _fit = new SlopeInterceptLineFit(slope, intercept, slopeErr, interceptErr, sigab, chisq, n-2 );
        return true;
    }
    
    /**
     * Return the fit.
     * @return The fit results. Returns null if the fit is not successful.
     */
    public SlopeInterceptLineFit getFit()
    {
        return _fit;
    }
}
