package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.trfutil.TRFMath;
import Jama.Matrix;

/**
 * Class TrackVector is a 5-vector containing the parameters
 * describing a track in a magnetic field.
 *<p>
 * TrackVector tv should support the following operations:
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
public class TrackVector
{
    private Matrix _vec;
    private int _length;
    //
    
    /**
     * Default constructor.
     * Builds vector of 5 zeros.
     *
     */
    public TrackVector( )
    {
        _length = 5;
        _vec = new Matrix(_length,1);
    }
    
    //
    
    /**
     * Constructor from array.
     *
     * @param   vec  5 dimensional vector of doubles.
     */
    public TrackVector( double[] vec)
    {
        _length = vec.length;
        if(_length!=5.) throw new IllegalArgumentException();
        _vec = new Matrix(vec, _length);
    }
    
    //
    
    /**
     * Constructor from Jama Matrix.
     *
     * @param   mat 5 dimensional Jama Column Matrix of doubles.
     */
    public TrackVector( Matrix mat)
    {
        if(mat.getRowDimension()!=5 && mat.getColumnDimension() != 1)
        {
            throw new IllegalArgumentException("TrackVector Matrix must be 5x1.");
        }
        _length = mat.getRowDimension();
        _vec = new Matrix(mat.getArrayCopy());
    }
    
    //
    
    /**
     * Copy Constructor
     *
     * @param   tv  TrackVector to copy.
     */
    public TrackVector( TrackVector tv)
    {
        _length = tv.vector().length;
        _vec  = new Matrix(tv.vector(),_length);
    }
    
    //
    
    /**
     * Return the underlying vector.
     *
     * @return  a 5 dimensional array of doubles.
     */
    public double[] vector()
    {
        return _vec.getColumnPackedCopy();
    }
    
    
    /**
     * Math funtion minus.
     *
     * @param   tv TrackVector to be subtracted.
     * @return  <em>this</em> minus TrackVector.
     */
    public TrackVector minus( TrackVector tv)
    {
        double[] tmp = new double[5];
        for(int i = 0; i<5; ++i)
        {
            tmp[i] = _vec.get(i,0)-tv.get(i);
        }
        return new TrackVector(tmp);
    }
    
    
    /**
     * Math funtion plus.
     *
     * @param   tv  TrackVector to be subtracted.
     * @return   <em>this</em> pluus TrackVector.
     */
    public TrackVector plus( TrackVector tv)
    {
        double[] tmp = new double[5];
        for(int i = 0; i<5; ++i)
        {
            tmp[i] = _vec.get(i,0)+tv.get(i);
        }
        return new TrackVector(tmp);
    }
    
    
    /**
     * String representation of this TrackVector.
     *
     * @return     String representation of this TrackVector.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+" ");
        for(int i=0; i<_length; ++i)
        {
            sb.append(_vec.get(i,0)).append(" ");
        }
        return sb.append("\n").toString();
    }
    
    /**
     * Return the underlying track vector as a Jama Matrix.
     *
     * @return     The track vector as a 5 dimensional Jama Column Matrix.
     */
    public Matrix getMatrix()
    {
        return _vec.copy();
    }
    
    /**
     * Return the underlying track vector as a Jama Matrix.
     *
     * @return The track vector as a 5 dimensional Jama Column Matrix.
     */
    public Matrix matrix()
    {
        return _vec.copy();
    }
    
    /**
     * Set a track vector element.
     *
     * @param   i  The track element to set.
     * @param   val  The value to be assigned to track element i.
     */
    public void set(int i, double val)
    {
        _vec.set(i, 0, val);
    }
    
    /**
     * Get a track vector element.
     *
     * @param   i  The track element to get.
     * @return   The value of track element i.
     */
    public double get(int i)
    {
        return _vec.get(i,0);
    }
    
    
    /**
     * Equality operator.
     *
     * @param   tv TrackVector to check against.
     * @return  true if <em> this</em> equals TrackVector tv.
     */
    public boolean equals( TrackVector tv)
    {
        for(int i = 0; i<5; ++i)
        {
            if( get(i)!=tv.get(i) ) return false;
        }
        return true;
    }
    
    
    /**
     * Inequality operator. Convenience method.
     *
     * @param   tv  TrackVector to check against.
     * @return   true if <em> this</em> <b> does not </b> equal TrackVector tv.
     */
    public boolean notEquals(TrackVector tv)
    {
        return !equals(tv);
    }
    
    
    /**
     * Equality within tolerance.
     *
     * @param   tv  TrackVector to check against.
     * @return  true if <em> this</em> equals TrackVector tv within tolerance.
     */
    public boolean isEqual( TrackVector tv)
    {
        for(int i = 0; i<5; ++i)
        {
            if( !TRFMath.isEqual(get(i),tv.get(i)) ) return false;
        }
        return true;
    }
    
    
    /**
     * Absolute Maximum TrackVector element.
     *
     * @return absolute value of absolute maximum TrackVector element.
     */
    public double amax()
    {
        double amax = Math.abs(get(0));
        for (int i = 1; i<5; ++i)
        {
            if(Math.abs(get(i))>amax) amax = Math.abs(get(i));
        }
        return amax;
    }
    
    
    /**
     * Maximum TrackVector element.
     *
     * @return  value of maximum TrackVector element.
     */
    public double max()
    {
        double max = get(0);
        for (int i = 1; i<5; ++i)
        {
            if(get(i)>max) max = get(i);
        }
        return max;
    }
    
    
    /**
     *Absolute Minimum TrackVector element.
     *
     * @return absolute value of absolute minimum TrackVector element.
     */
    public double amin()
    {
        double amin = Math.abs(get(0));
        for (int i = 1; i<5; ++i)
        {
            if(Math.abs(get(i))<amin) amin = Math.abs(get(i));
        }
        return amin;
    }
    
    
    /**
     * Mimimum TrackVector element.
     *
     * @return  value of minimum TrackVector element.
     */
    public double min()
    {
        double min = get(0);
        for (int i = 1; i<5; ++i)
        {
            if(get(i)<min) min = get(i);
        }
        return min;
    }
    
    
    /**
     * Chi-squared difference between TrackVectors.
     *
     * @param   tv  TrackVector to compare to.
     * @param   te  TrackError of uncertainties.
     * @return     double chi-squared.
     */
    public static double chisqDiff(TrackVector tv, TrackError te)
    {
        // cng needs to be checked...
        return (tv._vec.transpose()).times(te.getMatrix()).times(tv._vec).get(0,0);
    }
    
}
