package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.trfutil.TRFMath;
import Jama.Matrix;

/** Class TrackDerivative is the derivative of one track vector with
 * respect to another; i.e. a non-symmetric 5x5 matrix.
 *<p>
 * TrackDerivative should support the following operations:
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

public class TrackDerivative
{
    private Matrix _mat;
    private int _size;
    //
    
    /**
     * Default constructor creates 5x5 matrix of zeros
     *
     */
    public TrackDerivative( )
    {
        _size = 5;
        Matrix tmp = Matrix.identity(5,5);
        _mat = tmp;
    }
    
    //
    
    /**
     * Constructor from array
     *
     * @param   mat 5x5 array of doubles.
     */
    public TrackDerivative(double[][] mat)
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
     * @param   mat  5x5 Jama Matrix.
     */
    public TrackDerivative(Matrix mat)
    {
        _size = mat.getColumnDimension();
        if( _size!= mat.getRowDimension()) throw new IllegalArgumentException("Matrix must be square!");
        if( _size != 5) throw new IllegalArgumentException("Matrix must be 5x5!");
        _mat = mat.copy();
    }
    
    //
    
    /**
     * Copy constructor
     *
     * @param   td  TrackDerivative to copy.
     */
    public TrackDerivative( TrackDerivative td)
    {
        _size = td._size;
        _mat = td._mat.copy();
    }
    
    //
    
    /**
     * Return the underlying matrix.
     *
     * @return  5x5 array of doubles.
     */
    public double[][] matrix()
    {
        return _mat.getArrayCopy();
    }
    
    
    /**
     * String representation of TrackDerivative.
     *
     * @return String representation of TrackDerivative.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+"\n");
        for(int i=0; i<_size; ++i)
        {
            for(int j = 0; j<_size; ++j)
            {
                sb.append(_mat.get(i,j)).append(" ");
            }
            sb.append("\n");
        }
        return sb.append("\n").toString();
    }
    
    
    /**
     * Return the TrackDerivative as a Jama Matrix.
     *
     * @return  5x5 Jama Matrix.
     */
    public Matrix getMatrix()
    {
        return _mat.copy();
    }
    
    
    /**
     * Set an element of the TrackDerivative.
     *
     * @param   i  Row index.
     * @param   j  Column index.
     * @param   val  Value of element (i,j).
     */
    public void set(int i, int j, double val)
    {
        _mat.set(i,j,val);
    }
    
    
    /**
     * Set the elements equal to those of an existing TrackDerivative.
     *
     * @param   td  TrackDerivative to be equivalent to.
     */
    public void set(TrackDerivative td)
    {
        for (int i = 0 ; i < _size ; ++i )
        {
            for (int j = 0 ; j < _size ;++j )
            {
                set(i,j,td.get(i,j));
            }
        }
    }
    
    
    /**
     * Get an element of the TrackDerivative.
     *
     * @param   i  Row index.
     * @param   j  Column index.
     * @return     Value of element (i,j).
     */
    public double get(int i, int j)
    {
        return _mat.get(i, j);
    }
    
    
    /**
     * Convenience method to set TrackDerivative to the identity.
     *
     */
    public void setIdentity()
    {
        _mat = Matrix.identity(_size, _size);
    }
    
    
    /**
     * Matrix multiply
     *
     * @param   td  TrackDerivative by which to multiply.
     * @return     <em>this</em> times TrackDerivative td.
     */
    public TrackDerivative times(TrackDerivative td)
    {
        return new TrackDerivative(_mat.times(td._mat));
    }
    
    
    
    /**
     * Absolute maximum TrackDerivative element.
     *
     * @return  absolute value of absolute maximum TrackDerivative element.
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
     * Maximum TrackDerivative element.
     *
     * @return  Maximum TrackDerivative element.
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
     * Minimum TrackDerivative element.
     *
     * @return  minimum TrackDerivative element.
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
     *Absolute minimum TrackDerivative element.
     *
     * @return  absolute value of absolute minimum TrackDerivative element.
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
     * Math function minus.
     *
     * @param   td  TrackDerivative to be subtracted.
     * @return     <em> this </em> minus TrackDerivative td.
     */
    public TrackDerivative minus(TrackDerivative td)
    {
        return new TrackDerivative(getMatrix().minus(td.getMatrix()));
    }
    
    
    /**
     * Math function plus.
     *
     * @param   td  TrackDerivative to be added.
     * @return     <em> this </em> plus TrackDerivative td.
     */
    public TrackDerivative plus(TrackDerivative td)
    {
        return new TrackDerivative(getMatrix().plus(td.getMatrix()));
    }
    
    
    /** Equality.
     *
     * @return true if TrackDerivatives are equal.
     * @param td TrackDerivative to compare
     */
    public boolean equals( TrackDerivative td)
    {
        if( _size != td._size ) return false;
        for (int i = 0 ;i < _size ; ++i )
        {
            for (int j = 0; j < _size ; ++j )
            {
                if( _mat.get(i,j) != td._mat.get(i,j) ) return false;
            }
        }
        return true;
    }
    
    
    /** Inequality convenience method.
     *
     * @return true if TrackDerivatives are <b> not </b> equal.
     * @param td TrackDerivative to compare
     */
    public boolean notEquals(TrackDerivative td)
    {
        return !equals(td);
    }
    
    
    /** Equality within tolerance.
     *
     * @return true if TrackDerivatives are equal within tolerance.
     * @param td TrackDerivative to compare
     */
    public boolean isEqual( TrackDerivative td)
    {
        if( _size != td._size ) return false;
        for (int i = 0 ;i < _size ; ++i )
        {
            for (int j = 0; j < _size ; ++j )
            {
                if( ! TRFMath.isEqual(_mat.get(i,j),td._mat.get(i,j) ) ) return false;
            }
        }
        return true;
    }
    
    
    /**
     * Matrix transpose.
     * Transposes rows and columns of TrackDerivative in place.
     */
    public void transpose()
    {
        _mat = _mat.transpose();
    }
    
}
