package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class Tube extends Solid
{
    public Tube(String name, double innerR, double outerR, double zHalf)
    {
        super("tube",name);
        setRMin(innerR);
        setRMax(outerR);
        // FIXME: GDML will divide this by 2 so need to double it!
        setZ(zHalf*2);
        setAttribute("deltaphi", String.valueOf(Math.PI * 2) );
    }
    
    public Tube(String name, Element element)
    {
        super("tube",name);
        try {
            setRMin(element.getAttribute("rmin").getDoubleValue());
            setRMax(element.getAttribute("rmax").getDoubleValue());
            // FIXME: GDML will divide this by 2 so need to double it!
            setZ(element.getAttribute("zhalf").getDoubleValue() * 2);
        }
        catch (DataConversionException x)
        {
            throw new RuntimeException(x);
        }
        setAttribute("deltaphi", String.valueOf(Math.PI * 2) );
    }

    /** Creates a new instance of Tube */
    public Tube(String name)
    {
        super("tube",name);

        /** 
         * Set default deltaphi to 360 deg, i.e. full tube segment.        
         */
        setAttribute("deltaphi", String.valueOf(Math.PI * 2) );

        /** 
         * Set default rmin to 0.0.      
         */
        setAttribute("rmin", "0.0");
    }
    public void setZ(double z)
    {
        setAttribute("z",String.valueOf(z));
    }
    public void setRMin(double r)
    {
        setAttribute("rmin",String.valueOf(r));
    }
    public void setRMax(double r)
    {
        setAttribute("rmax",String.valueOf(r));
    }
    public void setDeltaPhi(double phi)
    {
        setAttribute("deltaphi",String.valueOf(phi));
    }
}