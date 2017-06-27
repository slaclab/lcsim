package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.DataConversionException;

// FIXME: Hard-coded mult and div by 2 to match GDML convention of dividing inputs by 2.
public class Trap extends Solid
{  
    public Trap(
            String name, 
            double z, 
            double theta, 
            double phi, 
            double y1, 
            double x1, 
            double x2, 
            double alpha1, 
            double y2, 
            double x3, 
            double x4, 
            double alpha2)
    {
        super("trap",name);
                
        setAttribute("z",String.valueOf(z*2));
        setAttribute("theta",String.valueOf(theta));
        setAttribute("phi",String.valueOf(phi));
        setAttribute("y1",String.valueOf(y1*2));
        setAttribute("x1",String.valueOf(x1*2));
        setAttribute("x2",String.valueOf(x2*2));
        setAttribute("alpha1",String.valueOf(alpha1));
        setAttribute("y2",String.valueOf(y2*2));
        setAttribute("x3",String.valueOf(x3*2));
        setAttribute("x4",String.valueOf(x4*2));
        setAttribute("alpha2",String.valueOf(alpha2));
        
        setAttribute("aunit","radian");
        setAttribute("lunit","mm");
    }  
    
    private double p(String dim)
    {
        try 
        {
            return getAttribute(dim).getDoubleValue();
        }
        catch (DataConversionException x)
        {
            throw new RuntimeException(x);
        }
    }
    
    public double getZHalfLength()
    {
        return p("z") / 2;
    }
    
    public double getTheta()
    {
        return p("theta");
    }
    
    public double getPhi()
    {
        return p("phi");
    }
    
    public double getYHalfLength1()
    {
        return p("y1") / 2;
    }
    
    public double getXHalfLength1()
    {
        return p("x1") / 2;
    }
    
    public double getXHalfLength2()
    {
        return p("x2") / 2;
    }
    
    public double getAlpha1()
    {
        return p("alpha1");
    }
    
    public double getYHalfLength2()
    {
        return p("y2") / 2;
    }
    
    public double getXHalfLength3()
    {
        return p("x3") / 2;
    }
    
    public double getXHalfLength4()
    {
        return p("x4") / 2;
    }
    
    public double getAlpha2()
    {
        return p("alpha2") / 2;
    }
}