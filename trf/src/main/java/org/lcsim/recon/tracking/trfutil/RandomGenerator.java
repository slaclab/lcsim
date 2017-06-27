package org.lcsim.recon.tracking.trfutil;

import java.util.Random;

/**
 * A generator produces objects with random parameters.
 *
 * This class is abstract and contains the interface for
 * the random number generator.  Each instance will maintain
 * its own sequence of random numbers.
 *
 * We use the default Java random number generator.
 *
 * The calls to rand are encapsulated so that we can keep count of
 * the number of calls to reproduce the state.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class RandomGenerator
{
    
    // attributes
    
    // Random number generator.
    private Random _rand;
    
    // Number of calls since seed was set.
    private int _ncall;
    
    // The seed
    private long _seed;
    
    // methods
    
    /**
     * Set the state = seed and number of calls.
     *
     * @param   seed  Random number seed
     * @param   ncall Number of calls to this generator
     */
    private void setState(long seed, int ncall)
    {
        setSeed(seed);
        for ( int i=0; i<ncall; ++i ) flat(0.0,1.0);
    }
    
    
    /**
     * Return a flat value in range (min.max).
     *
     * @param   min  The minimum of the range.
     * @param   max  The maximum of the range.
     * @return  A double value flat between <em>min</em> and <em >max </em>.
     */
    protected double flat(double min, double max)
    {
        ++_ncall;
        return min + (max-min)*_rand.nextDouble();
    }
    
    
    /**
     * Return a random number according to a Gaussian distribution
     * with sigma = 1.0.
     * We do it this way to ensure that we can reproduce the same sequence.
     *
     * @return A double value gaussian distributed with mean 0 and sigma 1.
     */
    protected double gauss()
    {
        double v1, v2, r;
        do
        {
            v1 = flat( -1.0, 1.0 );
            v2 = flat( -1.0, 1.0 );
            r = v1*v1 + v2*v2;
        } while ( r > 1.0 );
        double fac = Math.sqrt( -2.0*Math.log(r)/r);
        return v2*fac;
    }
    
    /**
     * Default constructor.
     *
     */
    public RandomGenerator()
    {
        _rand = new Random();
        setSeed(19970729);
    }
    
    /**
     * Constructor from seed.
     *
     * @param   seed long value seeding the random number generator.
     */
    public RandomGenerator(long seed)
    {
        _rand = new Random();
        setSeed(seed);
    }
    
    /**
     * Copy constructor.
     * The new generator is independent from the original and is in the
     * same state.
     * Set the original seed and skip through the same number of
     * values to obtain the same state.
     *
     * @param   rgen The RandomGenerator to copy.
     */
    public RandomGenerator( RandomGenerator rgen)
    {
        _rand = new Random();
        setState(rgen);
    }
    
    /**
     * Set the seed.
     * Generator is positioned at the beginning of the sequence.
     * This reinitializes the generator engine, positioning it at the
     *  first value in the sequence.
     *
     * @param   seed  long value seeding the random number generator.
     */
    public void setSeed(long seed)
    {
        _ncall = 0;
        _seed = seed;
        _rand.setSeed(seed);
    }
    
    
    /**
     * Get the seed.
     *
     * @return  long value which seeded this random number generator.
     */
    public long seed()
    {
        return _seed;
    }
    
    /**
     * Set the state (seed + ncall) to be the same as that of the argument.
     *
     * @param   rgen RandomGenerator used to set the state of this RandomGenerator.
     */
    public void setState(RandomGenerator rgen)
    {
        setState(rgen.seed(),rgen._ncall);
    }
    
    
    /**
     * String representation of this RandomGenerator.
     *
     * @return String representation of this RandomGenerator.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+"\nSeed: " + seed() + "\n # calls: " + _ncall;
    }
    
}
