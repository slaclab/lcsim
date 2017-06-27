/*
 * PolynomialFitter.java
 *
 * Created on March 20, 2006, 3:40 PM
 *
 *
 */

package org.lcsim.fit.polynomial;

import Jama.Matrix;
import Jama.QRDecomposition;

/**
 * A least-squares fitter for fitting polynomials to 2-D points.
 * @author Norman Graf
 */
public class PolynomialFitter
{

    private PolynomialFit _fit;
    
    /** Creates a new instance of PolynomialFitter */
    public PolynomialFitter()
    {
    }
    
    /**
     * Perform the least-squares fit.
     * @param x An array of the independent variable, assumed to have no uncertainty.
     * @param y The array of dependent variables, assumed to depend on x via a polynomial.
     * @param sigma_y The array of uncertainties in the dependent variable.
     * @param NP The number of points to fit.
     * @param order The order of the polynomial to fit to the data.
     * @return True if the fit is successful.
     */
    public boolean fit(double[] x, double[] y, double[] sigma_y, int NP, int order)
    {
        double [][] alpha  = new double[order][order];
        double [] beta = new double[order];
        double term = 0;
        
        for (int k=0; k < order; k++)
        {
            // Only need to calculate the diagonal and upper half
            // of symmetric matrix.
            for (int j=k; j < order; j++)
            {
                // Calculate the matrix terms over the data points
                term = 0.0;
                alpha[k][j] = 0.0;
                for (int i=0; i < NP; i++)
                {
                    
                    double prod1 = 1.0;
                    // Calculate x^k
                    if ( k > 0) for (int m=0; m < k; m++) prod1 *= x[i];
                    
                    double prod2 = 1.0;
                    // Calculate x^j
                    if ( j > 0) for (int m=0; m < j; m++) prod2 *= x[i];
                    
                    // Calculate x^k * x^j
                    term =  (prod1*prod2);
                    
                    if (sigma_y != null && sigma_y[i] != 0.0) term /=  (sigma_y[i]*sigma_y[i]);
                    
                    alpha[k][j] += term;
                }
                alpha[j][k] = alpha[k][j];// C will need to be inverted.
            }
            
            for (int i=0; i < NP; i++)
            {
                double prod1 = 1.0;
                if (k > 0) for ( int m=0; m < k; m++) prod1 *= x[i];
                term =  (y[i] * prod1);
                if (sigma_y != null  && sigma_y[i] != 0.0)
                    term /=  (sigma_y[i]*sigma_y[i]);
                beta[k] +=term;
            }
        }
        
        // Use the Jama QR Decomposition classes to solve for
        // the parameters.
        Matrix alpha_matrix = new Matrix(alpha);
        QRDecomposition alpha_QRD = new QRDecomposition(alpha_matrix);
        Matrix beta_matrix = new Matrix(beta,order);
        Matrix parameters;
        try
        {
            parameters = alpha_QRD.solve(beta_matrix);
        }
        catch (Exception e)
        {
            System.out.println("QRD solve failed: "+ e);
            return false;
        }
        
        // The inverse provides the covariance matrix.
        Matrix covariance = alpha_matrix.inverse();
        
        _fit = new PolynomialFit(parameters, covariance);
        return true;
    }
    
    /**
     * Return the result of the fit.
     * @return A PolynomialFit object representing the result of the fit.
     */
    public PolynomialFit getFit()
    {
        return _fit;
    }
}
