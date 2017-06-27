package org.lcsim.recon.tracking.trfutil;
/**
 * A linear root finder.
 * @version 1.0
 * @author  Norman A. Graf
 */
public abstract class RootFindLinear implements RootFinder
{
    // maximum # iterations
    private int _max_iter;
    
    final public static int OK = 0;
    final public static int INVALID_LIMITS=1;
    final public static int INVALID_FUNCTION_CALL=2;
    final public static int OUT_OF_RANGE=3;
    final public static int TOO_MANY_ITERATIONS=4;
    
    /** Create a new instance of RootFindLinear */
    public RootFindLinear()
    {
        _max_iter = 100;
    }
    
    // Find a root.
    public StatusDouble solve(double x1, double x2)
    {
        // Value to return for failure.
        double xfail = 0.0;
        
        // Save limits.
        // We require solution to be in the range (xmin,xmax).
        double xmin = x1;
        double xmax = x2;
        
        if ( xmin >= xmax ) return new StatusDouble(INVALID_LIMITS,xfail);
        
        // Evaluate the starting difference.
        double adif = Math.abs(x2-x1);
        
        // Set the precision.
        // Solution is said to be close if it changes by less than this value.
        double xclose = 1.e-14 * Math.abs(x2-x1);
        double yclose = 1.e-14;
        
        StatusDouble sd1 = evaluate(x1);
        if ( sd1.status() != 0 ) return new StatusDouble(INVALID_FUNCTION_CALL,xfail);
        double f1 = sd1.value();
        StatusDouble sd2 = evaluate(x2);
        if ( sd2.status() != 0 ) return new StatusDouble(INVALID_FUNCTION_CALL,xfail);
        double f2 = sd2.value();
        
        // Loop.
        for ( int count=0; count<_max_iter; ++count )
        {
            // interpolate for new solution
            Assert.assertTrue( f1 != f2 );
            if ( f1 == f2 ) return new StatusDouble(OUT_OF_RANGE,xfail);
            double x0 = (f2*x1 - f1*x2) / (f2 - f1);
            if ( x0 < xmin ) return new StatusDouble(OUT_OF_RANGE,xfail);
            if ( x0 > xmax ) return new StatusDouble(OUT_OF_RANGE,xfail);
            // Evaluate the function at the new solution.
            StatusDouble sd0 = evaluate(x0);
            if ( sd0.status() != 0 ) return new StatusDouble(INVALID_FUNCTION_CALL,xfail);
            double f0 = sd0.value();
            // Replace the most distant guess with x0.
            double adif1 = Math.abs(x1-x0);
            double adif2 = Math.abs(x2-x0);
            if ( adif1 < adif2 )
            {
                adif = adif1;
                x2 = x0;
                f2 = f0;
            }
            else
            {
                adif = adif2;
                x1 = x0;
                f1 = f0;
            }
            // Return if solution is found.
            if ( f0 == 0.0 ) return new StatusDouble(0,x0);
            // If we are close in y, exit.
            if ( Math.abs(f2-f1) < yclose ) return new StatusDouble(0,x0);
            // If we are close in x, exit.
            if ( adif < xclose ) return new StatusDouble(0,x0);
        }
        
        // Too many iterations.
        return new StatusDouble(TOO_MANY_ITERATIONS,xfail);
        
    }
    
}
