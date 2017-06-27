package org.lcsim.math.distribution;

import java.util.Random;

/**
 * Generate random numbers according to an empirical distribution provided by 
 * the user as an array of positive real numbers.
 * Based on RandomGeneral from CLHEP.
 *
 * @author Norman A. Graf
 *
 * @version $Id: 
 */
public class EmpiricalDistribution
{
    private Random _rand;
    protected double[] _cdf; // cumulative distribution function
    protected int _interpolationType;

    /**
     * Create an EmpiricalDistribution from an input array of positive values.
     * The array value i should be thought of as the central value of bin i in
     * an equally binned distribution. The values will be normalized internally.
     * This constructor will return doubles from the usual interval between 
     * 0 and 1.
     * @param pdf an array of positive values representing the empirical
     *            distribution from which random numbers should be drawn.
     */
    public EmpiricalDistribution(double[] pdf)
    {
        _rand = new Random();
        setState(pdf);
    }
    
    //TODO implement method which allows random number range to be
    //set by user beforehand
//    public EmpiricalDistribution(double[] pdf, double lowEdge, double interval)
//    {
//        _rand = new Random();
//        setState(pdf);
//    }

    /**
     * Allows the random number generator seed to be set
     * @param seed
     */
    public void setSeed(long seed)
    {
        _rand.setSeed(seed);
    }
    
    /**
     * Returns a random number from the distribution.
     * @return the next double value drawn from this distribution
     */
    public double nextDouble()
    {
        double rand = _rand.nextDouble();
        if (this._cdf == null)
        {
            return rand; // Non-existing pdf
        }

        int nBins = _cdf.length - 1;
        int nbelow = 0;     // largest k such that I[k] is known to be <= rand
        int nabove = nBins; // largest k such that I[k] is known to be >  rand

        while (nabove > nbelow + 1)
        {
            int middle = (nabove + nbelow + 1) >> 1; // div 2
            if (rand >= _cdf[middle])
            {
                nbelow = middle;
            } else
            {
                nabove = middle;
            }
        }
        // nabove is always nbelow+1 and they straddle rand:
        double binMeasure = _cdf[nabove] - _cdf[nbelow];
        if (binMeasure == 0.0)
        {
            // rand lies right in a bin of measure 0. 
            // Return the center of the range of that bin.  
            // (Any value between k/N and (k+1)/N is equally good, 
            // in this rare case.)
            return (nbelow + 0.5) / nBins;
        }
        double binFraction = (rand - _cdf[nbelow]) / binMeasure;
        return (nbelow + binFraction) / nBins;
    }

    /**
     * Returns the probability distribution function.
     */
    public double pdf(int k)
    {
        if (k < 0 || k >= _cdf.length - 1)
        {
            return 0.0;
        }
        return _cdf[k] - _cdf[k - 1];
    }

    /**
     * Creates and normalizes the cumulative probability distribution
     */
    public void setState(double[] pdf)
    {
        if (pdf == null || pdf.length == 0)
        {
            this._cdf = null;
            //throw new IllegalArgumentException("Non-existing pdf");
            return;
        }
        // compute cumulative distribution function (cdf) from probability 
        // distribution function (pdf)
        int nBins = pdf.length;
        this._cdf = new double[nBins + 1];

        _cdf[0] = 0;
        for (int ptn = 0; ptn < nBins; ++ptn)
        {
            double prob = pdf[ptn];
            if (prob < 0.0)
            {
                throw new IllegalArgumentException("Negative probability");
            }
            _cdf[ptn + 1] = _cdf[ptn] + prob;
        }
        if (_cdf[nBins] <= 0.0)
        {
            throw new IllegalArgumentException("At least one probability must be > 0.0");
        }
        //normalize to 1.
        for (int ptn = 0; ptn < nBins + 1; ++ptn)
        {
            _cdf[ptn] /= _cdf[nBins];
        }
       
    }

    /**
     * Returns a String representation of the receiver.
     */
    public String toString()
    {
        return this.getClass().getName();
    }
}
