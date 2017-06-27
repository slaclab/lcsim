package org.lcsim.recon.tracking.trfbase;
import Jama.Matrix;
/**  Class HitDerivative is the derivative with respect to a track
 * vector, i.e. is an nx5 matrix where n = hit dimension.
 *
 *@author Norman A. Graf
 *@version 1.0
 */

public class HitDerivative
{
    private Matrix _mat;
    private int _size;
    
    /** constructor from implementation
     *
     *@param mtx explicit Matrix representation of the HitDerivative
     */
    private HitDerivative( Matrix mtx )
    {
        _size = mtx.getRowDimension();
        _mat = (Matrix) mtx.clone();
    }
    
    /** Default constructor
     *
     */
    public HitDerivative()
    {
        _size = 0;
        _mat = new Matrix(_size, 5);
    }
    
    //
    /** Constructor
     * @param size  Size of the Hit measurement
     */
    public HitDerivative( int size)
    {
        _size = size;
        _mat = new Matrix(_size, 5);
    }
    
    //
    // order is by completed rows (00, 01, 02, ..., 10, 12, ...)
    /** constructor from an array
     * @param size Size of the Hit measurement
     * @param arr array of hit derivatives, by completed rows (00, 01, 02, ..., 10, 12, ...)
     */
    public HitDerivative( int size, double[] arr )
    {
        
        _size = size;
        _mat = new Matrix(_size, 5);
        int i=0;
        int j=0;
        int k=0;
        while(k<_size*5)
        {
            for (j = 0; j<5; ++j)
            {
                _mat.set(i,j,arr[k++]);
            }
            i++;
            j=0;
        }
    }
    
    
    //
    /** copy constructor
     * @param hder HitDerivative to replicate
     */
    public HitDerivative( HitDerivative hder )
    {
        _size = hder.size();
        _mat = hder.matrix();
    }
    
    // assignment
    // left side must be unassigned or have the same length
    //  HitDerivative& operator=( const HitDerivative& hder ) {
    //    assert( size() == 0 || size() == hder.size() );
    //    _mtx = hder._mtx;
    //    return *this;
    //  }
    
    //
    /** Return the underlying matrix.
     * @return Matrix representation of the derivative
     */
    public Matrix matrix()
    {
        return _mat.copy();
    }
    
    //
    /** return the # rows in the matrix
     * @return the size of the hit measurement
     */
    public int size()
    {
        return _size;
    }
    
    //
    /** access a derivatie element
     * @param i first index
     * @param j second index
     * @return the (i, j)th derivative
     */
    public double get( int i, int j )
    {
        return _mat.get(i,j);
    }
    
    //
    /** set a dervative element
     * @param i first index
     * @param j second index
     * @param val value of (i,j)th element to set
     */
    public void set(int i, int j, double val)
    {
        _mat.set(i, j, val);
    }
    
    
    //
    /** minimum derivative element
     * @return the minimum derivative element
     */
    public double min( )
    { return _mat.min(); };
    
    //
    /** maximum  derivative element
     * @return the maximum derivative element
     */
    public double max( )
    { return _mat.max(); };
    
    //
    /** absolute minimum
     * @return the derivative absolute value minimum
     */
    public double amin( )
    { return _mat.amin(); };
    
    //
    /** absolute maximum
     * @return the derivative absolute value maximum
     */
    public double amax( )
    { return _mat.amax(); };
    
    // +=
    /** Addition in place, shouldn't this be void?
     * fix
     * @param hder HitDerivative to add
     * @return result of addition
     */
    public HitDerivative plusEquals( HitDerivative hder)
    {
        if(_size != hder.size()) throw new IllegalArgumentException("HitVectors have different dimensions!");
        
        return new HitDerivative( _mat.plusEquals(hder.matrix()) );
    }
    
    // -=
    /** subtraction
     * @param hder HitDerivative to subtract
     * @return result of subtraction
     */
    public HitDerivative minusEquals( HitDerivative hder)
    {
        if(_size != hder.size()) throw new IllegalArgumentException("HitVectors have different dimensions!");
        
        return new HitDerivative( _mat.minusEquals(hder.matrix()) );
    }
    
    // derivative + derivative
    /** Addition
     * @param hd HitDerivative to add
     * @return result of addition
     */
    public HitDerivative plus( HitDerivative hd)
    {
        return new HitDerivative( _mat.plus(hd.matrix()) );
    }
    
    // derivative - derivative
    /** Subtraction
     * @param hd HitDerivative to subtract
     * @return result of subtraction
     */
    public  HitDerivative minus(HitDerivative hd)
    {
        return new HitDerivative( _mat.minus(hd.matrix()) );
    }
    
    //
    // must have the same dimension and the same values
    /** equality
     * @param hd HitDerivative to compare
     * @return true if HitDerivatives are of the same dimension and the same values
     */
    public boolean equals( HitDerivative hd )
    {
        if ( _size != hd.size() ) return false;
        Matrix tmp = hd.matrix();
        for(int i =0; i<_size; ++i)
        {
            for (int j=0; j<5; ++j)
            {
                if(_mat.get(i,j)!=tmp.get(i,j)) return false;
            }
        }
        return true;
    }
    //
    /** inequality
     * @param hd HitDerivative to compare
     * @return rue if HitDerivatives are not equal
     */
    public boolean notEquals( HitDerivative hd)
    {
        return ! equals(hd);
    }
    
    // equality with tolerance
    //  friend bool
    //  is_equal( HitDerivative& lhs,  HitDerivative& rhs) {
    //    if ( lhs.size() != rhs.size() ) return false;
    //    return is_equal( lhs._mat, rhs._mat );
    //  }
    
    //
    /** output stream
     * @return String representation of this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+"\n"+_mat.toString();
    }
    
}