package org.lcsim.recon.tracking.trfeloss;
public class DeDxTest extends DeDx
{
    
    public double dEdX(double energy)
    {
        return 0.1*energy;
    }
    
    public  double sigmaEnergy(double energy, double x)
    {
        return 0.01*energy*x;
    }
    
    public   double loseEnergy(double energy, double x)
    {
        return -energy*x;
    }
    
    public String toString()
    {
        return "DeDxTest";
    }
    
}