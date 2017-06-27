package org.lcsim.recon.tracking.trfutil;
/**
 * The component TRFMath includes some mathematical constants (PI, TWOPI and PI2),
 * some physical constants (CLIGHT and BFAC) and some utility functions.
 * These include two modulus-related functions, fmod1 and fmod2, and asin(x)/x .
 * There are also functions <it> is_equal </it> and <it>is_zero </it>which provide
 * for the comparison of two floating point values allowing for some numerical roundoff.
 * Modelled on the C++ class, so contains some superfluous functionality which is
 * preserved to maintain a similarity to the C++ code.
 * @author Norman A. Graf
 * @version 1.0
 */
public final class TRFMath
{
    
    // ensure that no instances of this class can be created
    private TRFMath()
    {
    }
    //**********************************************************************
    
    // Mathematical constants
    
    // pi
    /**
     * 2*PI
     */
    public static double TWOPI = 2.*Math.PI;
    
    /**
     * PI/2
     */
    public static double PI2   = 0.5*Math.PI;
    
    //**********************************************************************
    
    // Physical constants.
    
    /**
     * Speed of light. 2.99792458e10 cm/sec.
     */
    public static double CLIGHT = 2.99792458e10;
    
    /**
     * Factor connecting curvature to momentum.
     * C = 1/Rc = BFAC * B * q/pT
     * where B = magnetic field in Tesla, q = charge in natural units
     * (electron has -1) and pT = transverse momentum in GeV/c
     * Numerical value is 1.0e-13 * CLIGHT .
     */
    public static double BFAC = 1.0e-13 * CLIGHT;
    
    //**********************************************************************
    
    // Modulus functions.
    
    /**
     * Similar to fmod but returns value in positive range only.
     *
     * @param   value  double value to convert.
     * @param   range  double range for modulus.
     * @return  double value in range (0,range).
     */
    public static double fmod1( double value, double range )
    {
        //double tmp = fmod(value,range);
        double tmp = value%range;
        if ( tmp < 0.0 ) return tmp + Math.abs(range);
        return tmp;
    }
    
    
    /**
     * Similar to fmod1 but returns range (-R/2,R/2) instead of (0,R).
     *
     * @param   value  double value to convert.
     * @param   range  double half-range for modulus.
     * @return   double value in range (-range/2,range/2).
     */
    public static double fmod2( double value, double range )
    {
        return fmod1( value+0.5*range, range ) - 0.5*Math.abs(range);
    }
    
    //**********************************************************************
    
    //**********************************************************************
    
    /**
     * asinrat = asin(x)/x
     *
     * @param   x  value for which to calculate asin(x)/x
     * @return  arcsin(x)/x using series expansion for small x.
     */
    public static double asinrat(double x)
    {
        double small = 1.e-3;
        if ( Math.abs(x) < small )
        {
            double x2 = x*x;
            return 1.0 + 1.0/6.0*x2 + 3./40.*x2*x2;
        }
        else
        {
            return Math.asin(x)/x;
        }
    }
    
    //**********************************************************************
    //**********************************************************************
    
    // Zero check
    
    /**
     * Function <em>is_zero</em> returns true if the value is within epsilon
     * of zero.  By definition, 1.0+epsilon is the closest value to 1.0.
     * Thus, this function is sensible for variables whose natural scale
     * or range is 1.0.
     *
     * @param   x  value to check against zero
     * @return  approximation to zero accounting for roundoff.
     */
    public static boolean isZero(double x)
    {
        //  double eps = std::numeric_limits<double>::epsilon();
        double eps = 2.2e-16;
        return Math.abs(x) < eps;
    }
    
    // Equality check
    
    /**
     * Function <em>is_equal</em> returns true if values are exactly the same
     * or differ by a very small amount.  We set that small amount to
     * be twice the machine precision.  Values differing by one tick
     * will return true, those differing by three or more will return
     * false.  The behavior for those differing by two depends on the value
     * and the platform.
     *
     * @param   x1  value 1
     * @param   x2  value 2
     * @return  true if equal or equals within precision.
     */
    public static boolean isEqual(double x1, double x2)
    {
        if ( x1 == 0.0 ) return x2 == 0.0;
        if ( x2 == 0.0 ) return false;
        //double eps = std::numeric_limits<double>::epsilon();
        double eps = 2.2e-16;
        // 1 tick = 0.5, 2tick = 1, ...
        // Choose an intermediate value
        double maxdif = 0.7*eps;
        double num = x1 - x2;
        double den = x1 + x2;
        double rat = Math.abs(num/den);
        return rat < maxdif;
    }
}
