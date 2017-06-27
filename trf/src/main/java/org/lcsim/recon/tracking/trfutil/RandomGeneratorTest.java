package org.lcsim.recon.tracking.trfutil;
public class RandomGeneratorTest extends RandomGenerator
{
    
    private double _min;
    private double _max;
    
    public RandomGeneratorTest(double min, double max)
    {
        _min = min;
        _max = max;
    }
    public RandomGeneratorTest(double min, double max, long seed)
    {
        super(seed);
        _min = min;
        _max = max;
    }
    public RandomGeneratorTest( RandomGeneratorTest rgt)
    {
        super(rgt);
        _min = rgt._min;
        _max = rgt._max;
    }
    public double flat()
    { return super.flat(_min,_max); }
    public double gauss()
    { return super.gauss(); }
}
