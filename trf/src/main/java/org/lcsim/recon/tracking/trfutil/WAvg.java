package org.lcsim.recon.tracking.trfutil;
//**********************************************************************

// Return a weighted average.
public class WAvg
{
    
    private double _sum0;
    private double _sum1;
    private double _sum2;
    private int _count;
    
    public WAvg()
    {
        _sum0= 0.0;
        _sum1=0.0;
        _sum2=0.0;
        _count=0;
    }
    
    // input is value and square of the uncertainty
    public void addPair(double val, double err)
    {
        double weight = 1.0/err;
        _sum0 += weight;
        _sum1 += weight*val;
        _sum2 += weight*val*val;
        ++_count;
    }
    public int count()
    {
        return _count;
    }
    public double average()
    {
        return _sum1/_sum0;
    }
    public double error()
    {
        return 1.0/_sum0;
    }
    public double chiSquared()
    {
        return _sum2 - _sum1*_sum1/_sum0;
    }
    
    
    public String toString()
    {
        return "Weighted average = " + average()
        + " +/- " + Math.sqrt(error())
        + " (err = " + error() + ")"
                + "\n Chi-square = " + chiSquared();
        
    }
}
