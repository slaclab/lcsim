package org.lcsim.recon.tracking.trfbase;
import Jama.Matrix;

/** Class HitVector contains the parameters describing a hit.
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public class HitVector
{
    
    private Matrix _vec;
    private int _length;
    
    //
    
    /**
     *constructor from Matrix implementation
     *
     * @param   Matrix vec containing the hit values
     */
    private HitVector(  Matrix vec )
    {
        _length = vec.getRowDimension();
        _vec = vec.copy();
    }
    
    
    
    //
    
    /**
     *default constructor
     * leaves HitVector in invalid state
     */
    public HitVector()
    {
        _length = 0;
        _vec = new Matrix(_length, _length);
    }
    
    //
    
    /**
     *constructor initializing size of hit
     *
     * @param   size  dimensionality of the hit
     */
    public HitVector( int size )
    {
        _length = size;
        _vec = new Matrix(_length, 1);
    }
    
    //
    
    /**
     *constructor from an array
     *
     * @param   size of the array of values
     * @param   vec  double[] containing the values
     */
    public HitVector( int size, double[] vec )
    {
        _length = size;
        //needed until I fix JAMA
        double[] tmp = new double[_length];
        System.arraycopy(vec, 0, tmp, 0, _length);
        _vec = new Matrix(tmp, _length);
    }
    
    //
    
    /**
     *constructor from one value
     *
     * @param   x1  single HitVector value
     */
    public HitVector( double x1 )
    {
        _length = 1;
        _vec = new Matrix(_length,1);
        _vec.set(0,0,x1);
    }
    
    //
    
    /**
     *constructor from two values
     *
     * @param   x1  first value
     * @param   x2  second value
     */
    public HitVector( double x1, double x2 )
    {
        _length = 2;
        _vec = new Matrix(_length,1);
        _vec.set(0,0,x1);
        _vec.set(1,0,x2);
    }
    
    //
    
    /**
     *constructor from three values
     *
     * @param   x1  first value
     * @param   x2  second value
     * @param   x3  third value
     */
    public HitVector( double x1, double x2, double x3 )
    {
        _length = 3;
        _vec = new Matrix(_length,1);
        _vec.set(0,0,x1);
        _vec.set(1,0,x2);
        _vec.set(2,0,x3);
    }
    
    //
    
    /**
     *copy constructor
     *
     * @param  hvec HitVector to replicate
     */
    public HitVector( HitVector hvec )
    {
        _length = hvec.vector().length;
        _vec = new Matrix( hvec.vector(), _length);
    }
    
    // doesn't work in Java
    // assignment
    // left side must be unassigned or have the same length
    //  HitVector& operator=( const HitVector& hvec ) {
    //    assert( size() == 0 || size() == hvec.size() );
    //    _vec = hvec._vec;
    //    return *this;
    //  }
    
    //
    
    /**
     *access the hit vector
     *
     * @return the underlying hit vector.
     */
    public  double[] vector()
    {
        return _vec.getColumnPackedCopy();
    }
    
    //
    
    /**
     * access the hit vector
     *
     * @return  the underlying hit vector as a Matrix.
     */
    public  Matrix matrix()
    {
        return _vec.copy();
    }
    
    
    //
    
    /**
     * hit size
     *
     * @return the dimension of the hit
     */
    public int size()
    {
        return _length;
    }
    
    //
    
    /**
     * accessor
     *
     * @param   i  element to access
     * @return  value of vector element i
     */
    public double get( int i )
    {
        return _vec.get(i,0);
    }
    
    //
    
    /**
     *  set vector element i to value val
     *
     * @param   i  element to set
     * @param   val  value of element i
     */
    public void set(int i, double val)
    {
        _vec.set(i, 0, val);
    }
    
    
    //
    
    /**
     *minimum
     *
     * @return minimum value of vector elements
     */
    public double min( )
    { return _vec.min(); }
    
    //
    
    /**
     *maximum
     *
     * @return maximum value of vector elements
     */
    public double max( )
    { return _vec.max(); }
    
    //
    
    /**
     * absolute minimum
     *
     * @return absolute minimum value of vector elements
     */
    public double amin( )
    { return _vec.amin(); }
    
    
    
    /**
     *absolute maximum
     *
     * @return     absolute maximum value of vector elements
     */
    public double amax( )
    { return _vec.amax(); }
    
    // +=
    
    /** vector addition in place
     * changes value of this to this + hv
     * @param hv HitVector to add
     */
    public void plusEquals( HitVector hv)
    {
        if(_length != hv.size()) throw new IllegalArgumentException("HitVectors have different dimensions!");
        
        _vec.plusEquals(hv.matrix());
    }
    
    
    /** vector subtraction in place
     * changes value of this to this - hv
     * @param hv HitVector to subtract
     */
    
    public void minusEquals( HitVector hv)
    {
        if(_length != hv.size()) throw new IllegalArgumentException("HitVectors have different dimensions!");
        
        _vec.minusEquals(hv.matrix());
    }
    
    
    /** vector addition
     *
     * @return new HitVector equal to this + hv
     * @param hv HitVector to add
     */
    public  HitVector plus(HitVector hv)
    {
        double[] tmp = new double[_length];
        for(int i = 0; i<_length; ++i)
        {
            tmp[i] = _vec.get(i,0) + hv.get(i);
        }
        return new HitVector(_length, tmp);
    }
    
    
    
    /** vector subtraction
     *
     * @return return new HitVector equal to this - hv
     * @param hv HitVector to subtract
     */
    public  HitVector minus(HitVector hv)
    {
        double[] tmp = new double[_length];
        for(int i = 0; i<_length; ++i)
        {
            tmp[i] = _vec.get(i,0) - hv.get(i);
        }
        return new HitVector(_length, tmp);
    }
    
    
    /** equality
     * must have the same length and the same values
     *
     * @return true if HitVectors this and hv are <bf>not</bf> equal
     * @param hv HitVector to compare
     */
    public  boolean equals( HitVector hv)
    {
        if(_length != hv.size()) return false;
        for(int i = 0; i<_length; ++i)
        {
            if (_vec.get(i,0)!=hv.get(i)) return false;
        }
        return true;
    }
    
    
    
    /** inequality
     *
     * @return true if HitVectors this and hv are <bf>not</bf> equal
     * @param hv HitVector to compare
     */
    public  boolean notEquals( HitVector hv)
    {
        return ! equals(hv);
    }
    
    
    // equality with tolerance
    // must have the same length and the same values
    //  public  boolean is_equal(HitVector hv)
    //  {
    //    // for now just fake it.
    //    return equals(hv);
    //  }
    
    
    /**
     *output stream
     *
     * @return String representation of HitVector
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+" \n"+_vec;
    }
}




