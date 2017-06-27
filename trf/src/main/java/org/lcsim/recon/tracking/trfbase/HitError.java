package org.lcsim.recon.tracking.trfbase;
import Jama.Matrix;
/** Class HitError contains the corresponding (symmetric) error matrix.
 *
 *@author Norman A. Graf
 *@version 1.0
 */


public class HitError
{
    private Matrix _mat;
    private int _size;
    
    // constructor from implementation
    private HitError(  Matrix err )
    {
        if(err.getRowDimension() != err.getColumnDimension() )
            throw new IllegalArgumentException("Error Matrix must be symmetric!");
        _size = err.getRowDimension();
        _mat = (Matrix) err.clone();
    }
    
    
    // default constructor
    public HitError()
    {
        _size = 0;
        _mat = new Matrix(_size, _size);
    }
    // constructor
    /**
     * @param size
     */
    public HitError( int size )
    {
        _size = size;
        _mat = new Matrix(_size, _size);
    }
    
    // constructor from an array
    // order is lower triangle (00, 10, 11, 20, ...)
    // This is a symmetric matrix, so always set both terms
    /**
     * @param size
     * @param arr
     */
    public HitError( int size, double[] arr )
    {
        _size = size;
        _mat = new Matrix(_size, _size);
        int i=0;
        int j=0;
        int k=0;
//        System.out.println(_size+" "+arr.length);
        while(k<(_size*_size+_size)/2)
        {
            _mat.set(i,j,arr[k]);
            _mat.set(j,i,arr[k]);
            k++;
            for (j = 1; j<=i; ++j)
            {
                _mat.set(i,j,arr[k]);
                _mat.set(j,i,arr[k]);
                k++;
            }
            i++;
            j=0;
        }
    }
    
    // constructor for 1D from values
    /**
     * @param e11
     */
    public HitError( double e11 )
    {
        _size = 1;
        _mat = new Matrix(_size,_size);
        _mat.set(0,0,e11);
    }
    
    // constructor for 2D from values
    /**
     * @param e11
     * @param e12
     * @param e22
     */
    public HitError( double e11, double e12, double e22 )
    {
        _size = 2;
        _mat = new Matrix(_size,_size);
        _mat.set(0,0,e11);
        _mat.set(1,0,e12);
        _mat.set(0,1,e12);
        _mat.set(1,1,e22);
    }
    
    // constructor for 3D from values
    /**
     * @param e11
     * @param e21
     * @param e22
     * @param e31
     * @param e32
     * @param e33
     */
    public HitError( double e11, double e21, double e22,
            double e31, double e32, double e33 )
    {
        _size = 3;
        _mat = new Matrix(_size,_size);
        _mat.set(0,0,e11);
        _mat.set(1,0,e21);
        _mat.set(1,1,e22);
        _mat.set(2,0,e31);
        _mat.set(2,1,e32);
        _mat.set(2,2,e33);
        // now symmetric terms...
        _mat.set(0,1,e21);
        _mat.set(0,2,e31);
        _mat.set(1,2,e32);
    }
    
    // constructor from a track error and a track derivative:
    // E_hit = dhit_dtrack * E_track * dhit_dtrack_transpose
    
    public HitError( HitDerivative dhit_dtrack,
            TrackError trkerr )
    {
        _size = dhit_dtrack.size();
        _mat = dhit_dtrack.matrix().times(trkerr.getMatrix().times(dhit_dtrack.matrix().transpose()));
    }
    
    // copy constructor
    public HitError( HitError herr )
    {
        _size = herr.size();
        _mat = herr.matrix();
    }
    
    // assignment
    // left side must be unassigned or have the same length
    //  HitError& operator=( const HitError& herr ) {
    //    assert( size() == 0 || size() == herr.size() );
    //    _err = herr._err;
    //    return *this;
    //  }
    
    // Return the underlying matrix.
    public  Matrix matrix()
    {
        return _mat.copy();
    }
    
    // return the dimension of the matrix
    public int size()
    {
        return _size;
    }
    
    // accessor
    public double get( int i, int j )
    {
        return _mat.get(i,j);
    }
    
    // set
    // This is a symmetric matrix, so always set the
    // other element as well.
    public void set(int i, int j, double val)
    {
        _mat.set(i, j, val);
        _mat.set(j, i, val);
    }
    
    // minimum
    public double min( )
    { return _mat.min(); }
    
    // maximum
    public double max( )
    { return _mat.max(); }
    
    // absolute minimum
    public double amin( )
    { return _mat.amin(); }
    
    // absolute maximum
    public double amax( )
    { return _mat.amax(); }
    
    // invert -- return 0 for success
    public int invert()
    {
        // Count on Matrix class to throw an exception for no inverse
        // need to check
        _mat.inverse();
        return 0;
    }
    
    // +=
    public HitError plusEquals( HitError herr)
    {
        if(_size != herr.size()) throw new IllegalArgumentException("HitVectors have different dimensions!");
        
        return new HitError( _mat.plusEquals(herr.matrix()) );
    }
    
    
    // -=
    public HitError minusEquals( HitError herr)
    {
        if(_size != herr.size()) throw new IllegalArgumentException("HitVectors have different dimensions!");
        
        return new HitError( _mat.minusEquals(herr.matrix()) );
    }
    
    // error + error
    public HitError
            plus( HitError he)
    {
        return new HitError( _mat.plus(he.matrix()) );
    }
    
    // error - error
    public HitError
            minus( HitError he)
    {
        return new HitError( _mat.minus(he.matrix())  );
    }
    
    // equality
    // must have the same dimension and the same values
    public boolean equals( HitError he)
    {
        if ( _size != he.size() ) return false;
        Matrix tmp = he.matrix();
        for(int i =0; i<_size; ++i)
        {
            for (int j=0; j<_size; ++j)
            {
                if(_mat.get(i,j)!=tmp.get(i,j)) return false;
            }
        }
        return true;
    }
    
    // inequality
    public boolean notEquals( HitError he)
    {
        return ! equals(he);
    }
    
    //  // equality with tolerance
    //  // must have the same dimension and the same values
    //  friend bool is_equal( HitError& lhs,  HitError& rhs) {
    //    if ( lhs.size() != rhs.size() ) return false;
    //    return is_equal( lhs._err, rhs._err );
    //  }
    
    // output stream
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+"\n"+_mat;
    }
}
