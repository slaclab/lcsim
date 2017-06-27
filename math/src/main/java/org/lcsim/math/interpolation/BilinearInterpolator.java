/*
 * BilinearInterpolator.java
 *
 * Created on June 3, 2008, 4:04 PM
 *
 * $Id: BilinearInterpolator.java,v 1.1.1.1 2010/11/30 21:32:00 jeremy Exp $
 */
//
package org.lcsim.math.interpolation;

import static java.lang.Math.abs;

/**
 * A class to provide interpolated values for values determined at discrete points
 * on a 2D grid
 *
 * @author Norman Graf
 */
public class BilinearInterpolator
{
    private double[] _x;
    private double _xmin;
    private double _xmax;
    private int _xDim;
    private double[] _y;
    private double _ymin;
    private double _ymax;
    private int _yDim;
    private double[][] _val;
    /**
     * Creates a new instance of BilinearInterpolator
     * @param x Array of first independent variable at which values are known
     * @param y Array of second independent variable at which values are known
     * @param z Array of values at the (x,y) points
     */
    public BilinearInterpolator(double[] x, double[] y, double[][] z)
    {
        _x = new double[x.length];;
        System.arraycopy(x,0,_x,0, x.length);
        _xDim = _x.length;
        _xmin = _x[0];
        _xmax = _x[_xDim-1];
        
        _y = new double[y.length];
        System.arraycopy(y,0,_y,0, y.length);
        _yDim = _y.length;
        _ymin = _y[0];
        _ymax = _y[_yDim-1];
        
 
        
        _val = new double[z.length][z[0].length];
        for(int i=0; i< z.length; ++i)
        {
            System.arraycopy(z[i],0,_val[i],0,z[i].length);
        }
    }
    
    //TODO add protection for values out of range
    /**
     * Return the value at an arbitrary x,y point, using bilinear interpolation
     * @param x the first independent variable
     * @param y the second independent variable
     * @return the interpolated value at (x,y)
     */
    public double interpolateValueAt(double x, double y)
    {
        return trueBilinear( x, y );
//        return polin2(_x, _y, _val, x, y);
    }
    
    /*
     * Given arrays x1a[1..m] and x2a[1..n] of independent variables, and a submatrix of function
     *  values ya[1..m][1..n], tabulated at the grid points defined by x1a and x2a; and given values
     * x1 and x2 of the independent variables; this routine returns an interpolated function value y.
     */
    double  polin2(double[] x1a, double[] x2a, double[][] ya, double x1, double x2)
    {
        int m = x1a.length;
        int n = x2a.length;
        double[] ymtmp = new double[m];
        double[] yntmp = new double[n];
        for (int j=0; j<m; ++j) //Loop over rows.
        {
            // copy the row into temporary storage
            for(int k = 0; k<n; ++k)
            {
                yntmp[k] = ya[j][k];
            }
            ymtmp[j] = polint(x2a, yntmp, x2 ); //Interpolate answer into temporary storage.
        }
        return polint(x1a, ymtmp, x1); //Do the final interpolation.
    }
    
    /*
     * Given arrays xa[1..n] and ya[1..n], and given a value x, this routine returns a value y.
     * If P(x) is the polynomial of degree N -1 such that P(xai) = ya,  i ; i = 1... n, then
     * the returned value y = P(x).
     */
    double polint( double[] xa, double[] ya, double x)
    {
        int ns=0;
        int n = xa.length;
        double den,dif,dift,ho,hp,w;
        double dy;
        double[] c = new double[n];
        double[] d = new double[n];
        dif=abs(x-xa[0]);
        
        for (int i=0;i<n;++i)
        {	//Here we find the index ns of the closest table entry,
            dift = abs(x-xa[i]);
            if ( dift < dif)
            {
                ns=i;
                dif=dift;
            }
            c[i]=ya[i];	// and initialize the table of c's and d's.
            d[i]=ya[i];
        }
        double y = ya[ns--];	// This is the initial approximation to y.
//        ns = ns-1;
        for (int m=1; m<n ; ++m)
        {
            //For each column of the table,
            for (int i=0; i<n-m;++i)
            {	//we loop over the current c's and d's and update them.
                ho=xa[i]-x;
                hp=xa[i+m]-x;
                w=c[i+1]-d[i];
                den = ho-hp;
                if (den == 0.0) System.err.println("Error in routine polint");
                //This error can occur only if two input xa's are (to within roundo) identical.
                den=w/den;
                d[i]=hp*den;	//Here the c's and d's are updated.
                c[i]=ho*den;
            }
            if(2*ns < (n-m))
            {
                dy = c[ns+1];
            }
            else
            {
                dy = d[ns];
                ns = ns-1;
            }
            y += dy;
            //After each column in the tableau is completed, we decide which correction, c or d,
            //we want to add to our accumulating value of y, i.e., which path to take through the
            //tableau|forking up or down. We do this in such a way as to take the most \straight
            //line" route through the tableau to its apex, updating ns accordingly to keep track of
            //where we are. This route keeps the partial approximations centered (insofar as possible)
            //on the target x. The last dy added is thus the error indication.
        }
        return y;
    }
    
    double trueBilinear(double x, double y)
    {
        // find bin for x
        int ixlo = 0;
        int ixhi = 0;
        for(int i=0; i<_xDim-1; ++i)
        {
            if(x>=_x[i] && x<=_x[i+1])
            {
                ixlo = i;
                ixhi = i+1;
                break;
            }
        }
        
        // find bin for y
        int iylo = 0;
        int iyhi = 0;
        for(int i=0; i<_yDim-1; ++i)
        {
            if(y>=_y[i] && y<=_y[i+1])
            {
                iylo = i;
                iyhi = i+1;
                break;
            }
        }
        double v1 = _val[ixlo][iylo];
        double v2 = _val[ixhi][iylo];
        double v3 = _val[ixhi][iyhi];
        double v4 = _val[ixlo][iyhi];
        
        double t = (x - _x[ixlo])/(_x[ixhi] - _x[ixlo]);
        double u = (y - _y[iylo])/(_y[iyhi] - _y[iylo]);
        
        return (1-t)*(1-u)*v1 +t*(1-u)*v2+t*u*v3+(1-t)*u*v4;
    }
}