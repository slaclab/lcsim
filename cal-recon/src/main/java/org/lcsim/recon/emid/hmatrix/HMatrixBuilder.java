package org.lcsim.recon.emid.hmatrix;
import java.io.*;
/** A class to construct an HMatrix.
 *
 *@author Norman A. Graf
 *@version 1.0
 *@see HMatrix
 */
public class HMatrixBuilder
{
    private double[][] _cov;
    private double[] _vec;
    private double[] _vals;
    private double[][] _valsq;
    private int _dim;
    private int _key;
    private int _ndata;
    private boolean _isValid;
    private HMatrix _hmx;
    
    /** Construct an <b>n</b> x <b>n</b> HMatrixBuilder.
     * Leaves HMatrix in an invalid state.
     * One must either accumulate entries to build up the HMatrix
     * or read in the HMatrix values from a file.
     *
     * @param   n  The dimension of the measurement space.
     * @param   key The key by which to index this HMatrix.
     */
    public HMatrixBuilder(int n, int key)
    {
        _dim = n;
        _key = key;
        _cov = new double[_dim][_dim];
        _vec = new double[_dim];
        _vals = new double[_dim];
        _valsq = new double[_dim][_dim];
        _ndata = 0;
        _isValid = false;
    }
    
    /**
     * Add the measurement vector to the accumulated HMatrix
     *
     * @param   dat  The array of measurements.
     */
    public void accumulate(double[] dat)
    {
        //if(dat.length !=_dim) throw new InvalidArgumentException();
        for(int i=0; i<_dim; ++i)
        {
            for(int j=0; j<_dim; ++j)
            {
                _valsq[i][j]+=dat[i]*dat[j];
                if(i==j) _vals[i]+=dat[i];
                //System.out.println("i= "+i+", j= "+j+"val= "+_vals[i]+", valsq= "+_valsq[i][j]);
            }
        }
        _ndata++;
    }
    
    /**
     * Validates the HMatrix by performing the averages
     */
    public void validate()
    {
        double[] _covDiag = new double[_dim];
        for(int i=0; i<_dim; ++i)
        {
            _vec[i] = _vals[i]/_ndata;
            for(int j=0; j<_dim; ++j)
            {
                double data = (double)_ndata;
                _cov[i][j] = _valsq[i][j]/(data) - (_vals[i]/(data)*(_vals[j]/(data)));
            }
            _covDiag[i] = _cov[i][i];
        }
        
        _hmx = new HMatrix(_dim, _key, _vec, _covDiag, invert(_cov,_dim));
        _isValid = true;
    }
    
    /**
     * Output Stream
     *
     * @return  String representation of the object
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("HMatrixBuilder: dimension "+_dim+" ");
        if(!_isValid) return (sb.append("INVALID").toString());
        sb.append(_hmx);
/*		sb.append("\n\nvector: \n");
                for(int i = 0; i<_dim; ++i)
                {
                        sb.append(_vec[i]+" ");
                }
                sb.append("\n\ncovariance matrix: \n");
                for(int i = 0; i<_dim; ++i)
                {
                        for(int j = 0; j<_dim; ++j)
                        {
                                sb.append(_cov[i][j]+" ");
                        }
                        sb.append("\n");
                }
 */
        return sb.toString();
    }
    
    /**
     * Writes the HMatrix to a Serialized file
     *
     *  @param   filename  The file to which to write.
     */
    /*
    public void writeSerialized(String filename)
    {
        if (_isValid)
        {
            _hmx.writeSerialized(filename);
        }
        else
        {
            //		throw new Exception();
            System.out.println("HMatrix not valid! Cannot write!");
        }
    }
    */
    /**
     * Reads the HMatrix from a Serialized file
     *
     * @param   filename The Serialized file from which to read.
     * @return  The HMatrix
     */
    /*
    public HMatrix readSerialized(String filename)
    {
        return HMatrix.readSerialized(filename);
    }
    */
    /**
     * Reads the HMatrix from an ASCII file
     *
     * @param   filename The ASCII file from which to read.
     * @return  The HMatrix
     */
    public HMatrix read(String filename)
    {
        return HMatrix.read(filename);
    }
    
    /**
     *  Writes out the HMatrix to an ASCII file
     *
     * @param   filename  The file to which to write.
     * @param   comment   A comment for the header.
     */
    public void write(String filename, String comment)
    {
        _hmx.write(filename, comment);
    }
    
    /**
     * Invert the matrix
     */
    private double[][] invert(double[][] mat, int dim)
    {
        
        int i = dim;
        double[][] matrix = new double[i][2 * i];
        double[][] matrix1 = new double[i][1];
        double[][] matrix2 = new double[i][1];
        for(int j = 0; j < i; j++)
        {
            for(int i2 = 0; i2 < i; i2++)
                matrix[j][i2]= mat[j][i2];
            
        }
        
        for(int k = 0; k < i; k++)
        {
            for(int j2 = i; j2 < 2 * i; j2++)
                matrix[k][j2]= 0.0D;
            
            matrix[k][i + k]=1.0D;
        }
        
        for(int i4 = 0; i4 < i; i4++)
        {
            for(int l = i4; l < i; l++)
            {
                matrix2[l][0]=0.0D;
                for(int k2 = i4; k2 < i; k2++)
                    matrix2[l][0]=matrix2[l][0] + matrix[l][k2];
                
                matrix1[l][0]= Math.abs(matrix[l][i4]) / matrix2[l][0];
            }
            
            int k4 = i4;
            for(int i1 = i4 + 1; i1 < i; i1++)
                if(matrix1[i1][0] > matrix1[i1 - 1][0])
                    k4 = i1;
            
            if(matrix1[k4][0] == 0.0D)
            {
                System.err.println("Matrix::inverse -> Matrix is singular.");
                System.exit(0);
            }
            if(k4 != i4)
            {
                for(int l2 = 0; l2 < 2 * i; l2++)
                {
                    double d = matrix[i4][l2];
                    matrix[i4][l2]=matrix[k4][l2];
                    matrix[k4][l2]=d;
                }
                
            }
            for(int i3 = 2 * i - 1; i3 >= i4; i3--)
                matrix[i4][i3]=matrix[i4][i3] / matrix[i4][i4];
            
            for(int j1 = i4 + 1; j1 < i; j1++)
            {
                for(int j3 = 2 * i - 1; j3 >= i4 + 1; j3--)
                    matrix[j1][j3] =matrix[j1][j3] - matrix[i4][j3] * matrix[j1][i4];
                
            }
            
        }
        
        for(int j4 = i - 1; j4 >= 0; j4--)
        {
            for(int k1 = j4 - 1; k1 >= 0; k1--)
            {
                for(int k3 = i; k3 < 2 * i; k3++)
                    matrix[k1][k3] =matrix[k1][k3] - matrix[j4][k3] * matrix[k1][j4];
                
            }
            
        }
        
        double[][] matrix3 = new double[i][i];
        for(int l1 = 0; l1 < i; l1++)
        {
            for(int l3 = 0; l3 < i; l3++)
                matrix3[l1][l3] =matrix[l1][l3 + i];
            
        }
        
        return matrix3;
    }
    
/*
// Inversion in place requires larger matrix.
        private double[][] invert( double[][] D, int n)
        {
                double alpha;
                double beta;
                int i;
                int j;
                int k;
                int error;
 
                error = 0;
                int n2 = 2*n;
 
                // initialize the reduction matrix
                for( i = 1; i <= n; i++ )
                {
                        for( j = 1; j <= n; j++ )
                        {
                                D[i][j+n] = 0.;
                        }
                        D[i][i+n] = 1.0;
                }
 
                // perform the reductions
                for( i = 1; i <= n; i++ )
                {
                        alpha = D[i][i];
                        if( alpha == 0.0 ) // error - singular matrix
                        {
                                error = 1;
                                break;
                        }
                        else
                        {
                                for( j = 1; j <= n2; j++ )
                                {
                                        D[i][j] = D[i][j]/alpha;
                                }
                                for( k = 1; k <= n; k++ )
                                {
                                        if( (k-i) != 0 )
                                        {
                                                beta = D[k][i];
                                                for( j = 1; j <= n2; j++ )
                                                {
                                                        D[k][j] = D[k][j] - beta*D[i][j];
                                                }
                                        }
                                }
                        }
                }
                return D;
        }
 */
}
