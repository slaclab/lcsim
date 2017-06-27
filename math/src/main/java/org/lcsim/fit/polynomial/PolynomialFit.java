/*
 * PolynomialFit.java
 *
 * Created on March 20, 2006, 3:40 PM
 *
 *
 */

package org.lcsim.fit.polynomial;

import Jama.Matrix;

/**
 * Encapsulates the behavior of a least-squares polynomial fit to
 * two-dimensional data points.
 * @author Norman A. Graf
 * @version 1.0
 */
public class PolynomialFit
{
    private Matrix _parameters;
    private Matrix _covariance;
    /**
     * Creates a new instance of PolynomialFit
     * @param p The vector of fit parameters.
     * @param c The covariance matrix of the fit parameters.
     */
    public PolynomialFit(Matrix p, Matrix c)
    {
        _parameters = p;
        _covariance = c;
    }
    
    /**
     * Return the fit parameters.
     * @return The vector of fit parameters as a (nX1) Matrix
     */
    public Matrix parameters()
    {
        return _parameters;
    }
    
    /**
     * Return the covariance matrix
     * @return The covariance matrix as an (nxn) Matrix.
     */
    public Matrix covariance()
    {
        return _covariance;
    }
    
}
