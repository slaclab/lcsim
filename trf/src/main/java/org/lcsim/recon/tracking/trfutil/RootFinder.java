package org.lcsim.recon.tracking.trfutil;

/**
 * Abstract interface for classes which find a root of an
 * equation,  i.e. x for which f(x) = 0.
 *
 * Different root-finding algorithms inherit from this base and
 * implement methods solve(double,double) and status().
 *
 * Equations are solved by inheriting from those methods and
 * implementing method evaluate(double).  It may also optionally
 * implement method no_solution() which should return a value that
 * the function cannot reach.
 *
 * @version 1.0
 * @author  Norman A. Graf
 */
public interface RootFinder
{
    // Find solution starting with two guesses.
    // Return no_solution() for failure.
    public StatusDouble solve(double x1, double x2);
    
    // Evaluate the function.
    public StatusDouble evaluate(double x);
    
    
}
