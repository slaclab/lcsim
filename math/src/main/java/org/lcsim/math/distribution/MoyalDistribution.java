package org.lcsim.math.distribution;

import java.util.Random;

public class MoyalDistribution
{
    
    private String[] parameters = {"N", "x0", "sigma"};
    private double[] parValues = {1, 1, 1};
    private String title = "MoyalDistribution";
    private String[] variables = {"x"};
    
    public double value(double x)
    {
        double var = x;
        double n = parValues[0];
        double x0 = parValues[1];
        double s  = parValues[2];
        double lambda = (var-x0)/s;
        double arg = lambda + Math.exp( -1.*lambda );
        double num = Math.exp( -1.*arg );
        return n*Math.sqrt( num/(2*Math.PI) );
    }
    
}
