package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.trfutil.TRFMath;
import Jama.Matrix;
/**
 * Class TrackError is the corresponding symmetric 5x5 error matrix
 * for TrackVector.
 *<p>
 * TrackError should support the following operations:
 * <pre>
 * construct: tv(), te(), td() (should fill with zeros)
 * copy: tv2(tv1), te2(te1), td2(td1)
 * assignment: tv2 = tv2, te2 = te1, td2 = td1
 * indexing: tv(i), te(i,j), td(i,j)
 * minimum element: tv.min(), te.min(), td.min()
 * maximum element: tv.max(), te.max(), td.max()
 * absolute minimum element: tv.amin(), te.amin(), td.amin()
 * absolute maximum element: tv.amax(), te.amax(), td.amax()
 * addition: tv2 += tv1, tv3 = tv1 + tv2,
 *           te2 += te1, te3 = tv1 + te2,
 *           td2 += td1, td3 = td1 + td2,
 * subtraction: tv2 -= tv1, tv3 = tv1 - tv2,
 *              te2 -= te1, te3 = tv1 - te2,
 *              td2 -= td1, td3 = td1 - td2,
 * inversion: int stat = invert( TrackError& te1 ) (0 for success)
 * output stream: cout << tv << te << td
 * equality: tv1==tv2, te1==te2, td1==td2
 * inequality: tv!=tv2, te1!=te2, td1!=td2
 * chisquare difference: chisq_diff(tv,te) = tv_T * te * tv
 * (tv is the difference between two vectors and te is the inverse
 * of the error matrix.)
 * transpose: td.transpose()
 * transform: te.xform(td)
 * </pre>
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class TrackError
{
    private Matrix _mat;
    private int _size;
    
    //
    
    /**
     * Default constructor creates 5x5 matrix of zeros.
     *
     */
    public TrackError( )
    {
        _size = 5;
        _mat = new Matrix(5,5);
    }
    
    //
    
    /**
     * Constructor from array.
     *
     * @param   mat  5x5 array
     */
    public TrackError(double[][] mat)
    {
        _size = mat.length;
        for (int i = 0; i < _size; i++)
        {
            if (mat[i].length != _size)
            {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
        }
        _mat = new Matrix(mat, _size, _size);
        
    }
    
    //
    
    /**
     * Constructor from Jama Matrix.
     *
     * @param   mat  5x5 Jama Matrix of doubles representing the Track error matrix.
     */
    public TrackError( Matrix mat)
    {
        if(mat.getRowDimension()!=5 && mat.getColumnDimension() != 5)
        {
            throw new IllegalArgumentException("TrackError Matrix must be 5x5.");
        }
        _size = mat.getRowDimension();
        _mat = new Matrix(mat.getArrayCopy());
    }
    
    //
    
    /**
     * Convenience method to set the error matrix to the identity matrix.
     *
     */
    public void setIdentity()
    {
        _mat = Matrix.identity(_size, _size);
    }
    
    //
    
    /**
     * Copy Constructor
     *
     * @param   te  TrackError to copy.
     */
    public TrackError( TrackError te)
    {
        _size = 5;
        _mat = te.getMatrix();
    }
    
    //
    
    /**
     * Return the underlying vector.
     *
     * @return  5x5 double array.
     */
    public double[][] matrix()
    { //need to fix this!
        return _mat.getArrayCopy();
    }
    
    
    /**
     * String representation of TrackError.
     *
     * @return     String representation of TrackError.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+"\n");
        for(int i=0; i<_size; ++i)
        {
            for(int j = 0; j<=i; ++j)
            {
                sb.append(_mat.get(i,j)).append(" ");
            }
            sb.append("\n");
        }
        return sb.append("\n").toString();
    }
    
    /**
     * Get the track error matrix.
     *
     * @return Jama Matrix.
     */
    public Matrix getMatrix()
    {
        return _mat.copy();
    }
    
    
    /**
     * Set an element of the error matrix. This should be used
     * with caution, since the error matrix should have qualities
     * which may not be preserved under arbitrary element insertion.
     * Inserting an element (i,j) will also set the element (j,i), since
     * the matrix has to be at least symmetric.
     * @param   i  row index.
     * @param   j  column index.
     * @param   val  value for element (i,j).
     */
    public void set(int i, int j, double val)
    {
        // This is a symmetric matrix, so always set the
        // other element as well.
        _mat.set(i,j,val);
        if(i!=j) _mat.set(j,i,val);
    }
    
    
    /**
     * Get an element of the error matrix.
     *
     * @param   i  row index.
     * @param   j  column index.
     * @return     value of element (i,j).
     */
    public double get(int i, int j)
    {
        return _mat.get(i, j);
    }
    
    //cng what is the signature of this method?
    //	public void Xform( TrackDerivative deriv)
    //	{
    //		_mat = deriv.getMatrix().times(getMatrix().times(deriv.getMatrix().transpose()));
    //	}
    
    
    /**
     * TrackDerivative * TrackError * TrackDerivative(transpose).
     *
     * @param   deriv TrackDerivative by which to transform the TrackError  .
     * @return     TrackError transformed by Trackderivative.
     */
    public TrackError Xform( TrackDerivative deriv)
    {
        return new TrackError(deriv.getMatrix().times(getMatrix().times(deriv.getMatrix().transpose())));
    }
    
    
    /**
     * Math function minus.
     *
     * @param   te  TrackError to be subtracted.
     * @return     <em>this</em> minus TrackError te.
     */
    public TrackError minus(TrackError te)
    {
        return new TrackError(getMatrix().minus(te.getMatrix()));
    }
    
    
    /**
     *Math function plus.
     *
     * @param   te  TrackError to be added.
     * @return     <em>this</em> plus TrackError te.
     */
    public TrackError plus(TrackError te)
    {
        return new TrackError(getMatrix().plus(te.getMatrix()));
    }
    
    
    /**
     * Math function multiply.
     *
     * @param   te  TrackError to be multiplied by.
     * @return     <em>this</em> times TrackError te.
     */
    public TrackError times(TrackError te)
    {
        return new TrackError( _mat.times(te._mat) );
    }
    
    
    /**
     * Matrix inverse.
     *
     * @return Matrix inverse of <em>this</em>.
     */
    public TrackError inverse( )
    {
        return new TrackError(getMatrix().inverse());
    }
    
    
    /**
     * Maximum TrackError element.
     *
     * @return  value of maximum TrackError element.
     */
    public double max()
    {
        double[][] tmp = matrix();
        double max = tmp[0][0];
        for(int i = 0; i<5; ++i)
        {
            for(int j = 0; j<5; ++j)
            {
                if( tmp[i][j]>max) max = tmp[i][j];
            }
        }
        return max;
    }
    
    
    /**
     * Absolute maximum TrackError element.
     *
     * @return  absolute value of absolute maximum TrackError element.
     */
    public double amax()
    {
        double[][] tmp = matrix();
        double amax = Math.abs(tmp[0][0]);
        for(int i = 0; i<5; ++i)
        {
            for(int j = 0; j<5; ++j)
            {
                if( Math.abs(tmp[i][j])>amax) amax = Math.abs(tmp[i][j]);
            }
        }
        return amax;
    }
    
    
    /**
     * Minimum TrackError element.
     *
     * @return  value of minimum TrackError element.
     */
    public double min()
    {
        double[][] tmp = matrix();
        double min = tmp[0][0];
        for(int i = 0; i<5; ++i)
        {
            for(int j = 0; j<5; ++j)
            {
                if( tmp[i][j]<min) min = tmp[i][j];
            }
        }
        return min;
    }
    
    
    /**
     * Absolute minimum TrackError element.
     *
     * @return  absolute value of absolute minimum TrackError element.
     */
    public double amin()
    {
        double[][] tmp = matrix();
        double amin = Math.abs(tmp[0][0]);
        for(int i = 0; i<5; ++i)
        {
            for(int j = 0; j<5; ++j)
            {
                if( Math.abs(tmp[i][j])<amin) amin = Math.abs(tmp[i][j]);
            }
        }
        return amin;
    }
    
    
    
    /**
     * Equality.
     *
     * @param   te  TrackError to check equality against.
     * @return     true if TrackErrors are equal.
     */
    public boolean equals( TrackError te)
    {
        if( _size != te._size ) return false;
        for (int i = 0 ;i < _size ; ++i )
        {
            for (int j = 0; j < _size ; ++j )
            {
                if( _mat.get(i,j) != te._mat.get(i,j) ) return false;
            }
        }
        return true;
    }
    
    
    /**
     * Inequality convenience method.
     *
     * @param   te  TrackError to check equality against.
     * @return     true if TrackErrors are <b> not </b> equal.
     */
    public boolean notEquals(TrackError te)
    {
        return !equals(te);
    }
    
    
    /**
     * Equality within tolerance.
     *
     * @param   te  TrackError to check equality against.
     * @return     true if TrackErrors are equal within tolerance.
     */
    public boolean isEqual( TrackError te)
    {
        if( _size != te._size ) return false;
        for (int i = 0 ;i < _size ; ++i )
        {
            for (int j = 0; j < _size ; ++j )
            {
                if( !TRFMath.isEqual(_mat.get(i,j), te._mat.get(i,j)) ) return false;
            }
        }
        return true;
    }
    
    
    /**
     * Invert matrix in place.
     *
     * @return  nonzero integer if error.
     */
    public int invert()
    {
        int stat = 0;
        _mat = _mat.inverse();
        return stat;
    }
    
    /**
     * Normalize a matrix.
     * Transformation which makes all diagonal elements unity.
     *
     */
    public void normalize()
    {
        
        double[] vec = new double[_size];
        for(int irow=0; irow<_size; irow++ )
        {
            vec[irow] = 1.0/Math.sqrt( _mat.get(irow,irow) );
            double rowfac = Math.sqrt( vec[irow] );
            for ( int icol=0; icol<=irow; icol++ )
            {
                double tmp = _mat.get(irow,icol);
                tmp *= vec[irow]*vec[icol];
                _mat.set(irow,icol, tmp);
                _mat.set(icol,irow, tmp);
            }
        }
    }
}