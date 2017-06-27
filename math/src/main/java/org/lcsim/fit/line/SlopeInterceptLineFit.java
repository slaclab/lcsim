/*
 * SlopeInterceptLineFit.java
 *
 * Created on March 27, 2006, 3:19 PM
 *
 * $Id: SlopeInterceptLineFit.java,v 1.1 2006/03/28 04:51:03 ngraf Exp $
 */

package org.lcsim.fit.line;
/**
 * The result of a fit to a 2-D line in slope-intercept form y=a+bx .
 * @author Norman Graf
 */
public class SlopeInterceptLineFit
{
    private double _slope;
    private double _intercept;
    private double _sigA;
    private double _sigB;
    private double _sigAB;
    private double _chisq;
    private int _ndf;
    /**
     * Creates a new instance of SlopeInterceptLineFit
     * 
     * 
     * @param slope The slope of the line.
     * @param intercept The intercept of the line.
     * @param slopeUncertainty The uncertainty on the slope
     * @param interceptUncertainty The uncertainty on the intercept
     * @param sigAB The covariance term.
     * @param chisq The chi-squared of the fit
     * @param ndf The number of degrees of freedom for the fit
     */
    public SlopeInterceptLineFit(double slope, double intercept, double slopeUncertainty, double interceptUncertainty, double sigAB, double chisq, int ndf)
    {
        _slope = slope;
        _intercept = intercept;
        _sigA = interceptUncertainty;        
        _sigB = slopeUncertainty;
        _sigAB = sigAB;
        _chisq = chisq;
        _ndf = ndf;
    }
    
    /**
     *
     * @return The fit slope of the line.
     */
    public double slope()
    {
        return _slope;
    }
    
    /**
     * 
     * @return The uncertainty on the fit slope of the line.
     */
    public double slopeUncertainty()
    {
        return _sigB;
    }
    
    /**
     *  
     * @return The fit intercept of the line.
     */
    public double intercept()
    {
        return _intercept;
    }
    
    /**
     * 
     * @return The uncertainty on the fit intercept of the line.
     */
    public double interceptUncertainty()
    {
        return _sigA;
    }
    
    /**
     * 
     * @return The covariance of the slope-intercept uncertainties.
     */
    public double covariance()
    {
        return _sigAB;
    }
    
    /**
     * 
     * @return The chi-squared of the fit.
     */
    public double chisquared()
    {
        return _chisq;
    }
    
    /**
     * 
     * @return The number of degrees of freedom in the fit.
     */
    public int ndf()
    {
        return _ndf;
    }
 
    public String toString()
    {
        StringBuffer sb = new StringBuffer("SlopeInterceptLineFit: \n");
        sb.append("slope= "+_slope+" +/- "+_sigB+" intercept= "+_intercept+" +/- "+_sigA+" cov: "+_sigAB+ "\n");
        sb.append("chi-squared for "+_ndf+" degrees of freedom is "+_chisq);
        return sb.toString();
    }
}
