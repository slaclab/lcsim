package org.lcsim.recon.tracking.trfutil;
import java.util.Random;
/** This class smears a vector of quantities in
 * accordance with a multivariate normal distribution.
 * The constructor takes a covariance matrix as input;
 * only a minimal amount of checking is done.
 * The method 'smear' takes an array and smears its
 * members according to a gaussian distribution, whose
 * width corresponds to the square root of the diagonal
 * element of the covariance matrix. Here is an example
 * of how the covariance terms are handled:
 *<pre>
 *        v11
 * cov =  v12 v22
 *        v13 v23 v33
 *
 *  x1 = x1 + a1*rn1
 *  x2 = x2 + a2*rn1 + b2*rn2
 *  x3 = x3 + a3*rn1 + b3*rn2 + c3*rn3
 *</pre>
 *<p>
 *  where rn1, rn2 and rn3 are independently generated normal random numbers
 *
 *<pre>
 *  a1 = sqrt(v11)
 *  if a1 = 0, then a2 = a3 = 0
 *  otherwise a2 = v12/a1, a3 = v13/a1
 *
 *  b2 = sqrt(v22 - a2*a2)
 *  if b2 = 0, then b3 = 0
 *  otherwise b3 = (v23 - a2*a3)/b2
 *
 *  c3 = sqrt(v33 - a3*a3 - b3*b3)
 *</pre>
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */


public class ArraySmearer
{
    //attributes
    // store the matrix used later for smearing
    private double[][] _smear;
    //store a copy of the covariance matrix for printing
    // could be eliminated for efficiency
    private double[][] _cov;
    // the size of the matrix and arrays to be smeared
    private int _ndim;
    // the random number generator
    private Random _r = new Random();
    
    // methods
    
    /**
     *constructor from the covariance matrix
     *
     * @param   cov The covariance matrix as
     */
    public ArraySmearer( double[][] cov)
    {
        // sanity checks...
        _ndim = cov.length;
        // dimension
        for(int i = 0; i< _ndim; ++i)
        {
            if(cov[i].length != _ndim) throw new IllegalArgumentException("Array Smearer: Bad covariance matrix!");
        }
        // symmetry
        for(int i = 0; i < _ndim; ++i)
        {
            for (int j = 0; j< _ndim ; ++j )
            {
                if( cov[i][j] != cov[j][i] ) throw new IllegalArgumentException("Array Smearer: Bad covariance matrix!");
            }
        }
        // should also check the determinant, punt for now
        
        _smear = new double[_ndim][_ndim];
        // store local copy, could be elimated
        _cov = new double[_ndim][_ndim];
        System.arraycopy(cov,0,_cov,0,_ndim);
        // calculate the elements of the smearing matrix
        double ck = 0.;
        double sum;
        for (int i=0; i<_ndim; i++)
        {
            // Diagonal terms...
            sum = 0;
            for (int j=0; j<i; j++)
            {
                sum += _smear[i][j]*_smear[i][j];
            }
            _smear[i][i] = Math.sqrt(Math.abs(cov[i][i] - sum));
            // Off-Diagonal terms...
            for (int k=i+1; k<_ndim; k++)
            {
                sum = 0;
                for (int j=0; j<i; j++)
                {
                    sum += _smear[k][j]*_smear[i][j];
                }
                _smear[k][i] = (cov[k][i] - sum)/_smear[i][i];
            }
        }
    }
    
    /**
     * Smear a vector according to the covariance matrix.
     * Note overwrite of input argument!
     * @param   vec  The array to be smeared.
     */
    public void smear(double[] vec)
    {
        if(vec.length!=_ndim) throw new IllegalArgumentException("Array Smearer: Bad input vector!");
        double smrfac;
        
        for (int i=0; i<_ndim; i++)
        {
            smrfac = 0.0;
            for (int j=0; j<=i; j++)
            {
                smrfac += _smear[i][j]*_r.nextGaussian();
            }
            vec[i] += smrfac;
        }
    }
    
    // generate random vector according to the covariance matrix
    
    /**
     * Generate a vector of variables distributed according to the covariance matrix.
     *
     * @param   vec  Array to be filled
     */
    public void generate(double[] vec)
    {
        if(vec.length!=_ndim) throw new IllegalArgumentException("Array Generator: Bad input vector!");
        double[] z = new double[_ndim];
        // generate a vector of uncorrelated gaussians
        for (int i=0; i<_ndim; i++)
        {
            z[i] = _r.nextGaussian();
        }
        // add correlations
        for (int i=0; i<_ndim; i++)
        {
            vec[i]=0.;
            for (int j=0; j<=i; j++)
            {
                vec[i] += _smear[i][j]*z[j];
            }
            
        }
    }
    
    
    /**
     *Output Stream
     *
     * @return  String representation of object
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        StringBuffer sb = new StringBuffer(className+" of dimension "+_ndim+"\n");
        for ( int i = 0; i<_ndim ; ++i )
        {
            for (int j = 0 ; j<_ndim ; ++j )
            {
                sb.append( _cov[i][j] +" ");
            }
            sb.append("\n");
        }
        sb.append("\n smear= \n");
        for ( int i = 0; i<_ndim ; ++i )
        {
            for (int j = 0 ; j<_ndim ; ++j )
            {
                sb.append( _smear[i][j] +" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}