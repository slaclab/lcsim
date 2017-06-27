package org.lcsim.recon.tracking.trfutil;
// Test interface -- this is not a true root finder.

public class RootFinderTest implements RootFinder
{
    
    public StatusDouble evaluate(double x)
    {
        return new StatusDouble(1,x);
    }
    public StatusDouble solve(double x1, double x2)
    {
        return new StatusDouble(2,x1*x2);
    }
}