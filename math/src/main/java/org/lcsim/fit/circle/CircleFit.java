package org.lcsim.fit.circle;
/**
 * Encapsulates the behavior of a circle fit to data points.
 * Used in high-energy physics for a fit to hits left by
 * charged particles originating near (0,0) in a constant
 * magnetic field.
 * @author Norman A. Graf
 * @version 1.0
 */
public class CircleFit
{
    private double _xref; // x reference position;
    private double _yref; // y reference position;
    
    private double _rho; // curvature
    private double _phi; // phi angle at (_xref, _yref)
    private double _dca; // distance of closest approach to (_xref, _yref)
    private double _chisq; // chi-squared of circle fit
    
    private double[] _covrfd; // covariance matrix of fit
    // parameters in lower triangular form
    //Constructor
    public CircleFit(double x, double y, double rho, double phi, double dca, double chi2, double[] cov)
    {
        _xref = x;
        _yref = y;
        _rho = rho;
        _phi = phi;
        _dca = dca;
        _chisq = chi2;
        _covrfd = cov;
    }
    
    /**
     * The phi angle at the point of reference.
     * @return phi
     */
    public double  phi()
    {
        return _phi;
    }
    
    /**
     * The curvature of the fit.
     * @return the fit curvature.
     */
    public double  curvature()
    {
        return _rho;
    }
    
    /**
     * The x position of the reference point
     * @return x reference point.
     */
    public double  xref()
    {
        return _xref;
    }
    
    /**
     * The y position of the reference point.
     * @return y reference point.
     */
    public double  yref()
    {
        return _yref;
    }
    
    /**
     * The distance of closest approach to the reference point.
     * @return distance of closest approach to (xref, yref)
     */
    public double  dca()
    {
        return _dca;
    }
    
    /**
     * The chi-squared for the fit.
     * @return chi-squared for the circle fit.
     */
    public double  chisq()
    {
        return _chisq;
    }
    
    /**
     * The covariance matrix of fit
     * @return covariance matrix of fit
     */
    public double [] cov()
    {
        double[] tmp = new double[6];
        System.arraycopy(_covrfd, 0, tmp, 0, 6);
        return tmp;
    }
    
    /**
     * String representation of this object
     * @return string representation of this object.
     */
    public String toString()
    {
        return "CircleFit at x= "+_xref+", y= "+_yref+System.getProperty("line.separator")+"      with dca= "+_dca+", curvature= "+_rho+" and phi= "+_phi;
        //return "CircleFit at x= "+_xref+", y= "+_yref+"\n      with dca= "+_dca+", curvature= "+_rho+", phi= "+_phi+", chsq= "+_chisq;
        
    }
    /*
    public static void main(String[] args)
    {
        double[] cov = {1.,0.,0.,1.,0.,1.};
        CircleFit cf = new CircleFit(0.,0.,.01,.3,0.1,12., cov);
        System.out.println(cf);
        double[] covmat = cf.cov();
        for(int i=0;i<covmat.length;++i)
        {
            System.out.println(covmat[i]);
        }
    }
     */
}