/*
 * InterpolatedHMatrix.java
 *
 * Created on June 5, 2008, 10:34 AM
 *
 * $Id: InterpolatedHMatrix.java,v 1.1 2008/06/06 07:27:32 ngraf Exp $
 */

package org.lcsim.recon.emid.hmatrix;

import hep.physics.matrix.SymmetricMatrix;
import org.lcsim.math.interpolation.BilinearInterpolator;

/**
 * This class interpolates the average values and inverse covariance matrix elements
 * for photon showers based on derivations at fixed energy and angle.
 * @author Norman Graf
 */
public class InterpolatedHMatrix
{
    //Interpolation for the vector of measurement averages
    private BilinearInterpolator[] _valInterpolators;
    //Interpolation for the inverse covariance matrix elements represented as a
    //packed lower-diagonal matrix
    private BilinearInterpolator[] _covInterpolators;
    
    private int _dim;
    private int _covDim;
    
    /** Creates a new instance of InterpolatedHMatrix */
    // TODO check robustness, use deep copy if necessary.
    public InterpolatedHMatrix(int dim, BilinearInterpolator[] valInterpolators, BilinearInterpolator[] covInterpolators)
    {
        _dim = dim;
        _covDim = (_dim*(_dim+1))/2;
        _valInterpolators = valInterpolators;
        _covInterpolators = covInterpolators; 
    }
       
    /**
     * Returns the closeness of the vector dat to the expectations based on an 
     * interpolation between discrete values of angle and energy at which showers
     * were simulated.
     *
     * @param theta  the polar angle
     * @param energy the energy of the electromagnetic shower
     * @param dat the vector of fractional energies by layer
     * @return the distance of this shower to that expected, chi-squared for normal distributions of layer energies.
     */
    public double zeta(double theta, double energy, double[] dat)
    {
        double[] means = new double[_dim];
        double[] cov = new double[_covDim];
        double[] tmp = new double[_dim];
        double[] tmp2 = new double[_dim];

        for(int i=0; i<_dim; ++i)
        {
            means[i] = _valInterpolators[i].interpolateValueAt(theta, energy);
        }

        for(int i=0; i<_covDim; ++i)
        {
            cov[i] = _covInterpolators[i].interpolateValueAt(theta, energy);
        }

        // vector of measured-predicted values
        for(int i=0; i<_dim; ++i)
        {
            tmp[i] = dat[i] - means[i];
        }

        double zeta = 0.;
        // reexpand lower-diagonal into full double array
        // could this be optimized?
        double[][] invcov = new double[_dim][_dim];
        int count = 0;
        for(int i=0; i<_dim; ++i)
        {
            for(int j=0; j<i+1; ++j)
            {
                invcov[i][j] = cov[count];
                invcov[j][i] = cov[count];
                count++;
            }
        }
        // covariance matrix times difference vector
        for(int i=0; i<_dim; ++i)
        {
            tmp2[i] = 0.;
            for(int j=0; j<_dim; ++j)
            {
                tmp2[i]+=invcov[j][i]*tmp[j];
            }
            zeta += tmp[i]*tmp2[i];
        }
        return zeta;
    }   
}