package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * 
 * LCDD Position element.
 * 
 * @author tonyj
 * @author jeremym
 */
public class Position extends RefElement
{
    /** Creates a new instance of Position at (0,0,0). */
    public Position(String name)
    {
        super("position", name);
        setAttribute("x", "0.0");
        setAttribute("y", "0.0");
        setAttribute("z", "0.0");
        setAttribute("unit", "mm");
    }
    
    public Position(String name, double x, double y, double z)
    {
    	super("position",name);
    	setX(x);
    	setY(y);
    	setZ(z);
    	setAttribute("unit", "mm");
    }

    /**
     * Creates a new instance of Position from an {@link org.jdom.Element}.
     * @param element The XML element which must be called position.
     */
    public Position(String name, Element element)
    {   
        super("position", name);

        if (!element.getName().equals("position"))
            throw new IllegalArgumentException("expected position element but got " + element.getName());

        try {
            
            double x,y,z;
            x = y = z = 0.;

            if (element.getAttribute("x") != null)
            {
                x = element.getAttribute("x").getDoubleValue();
            }

            if (element.getAttribute("y") != null)
            {
                y  = element.getAttribute("y").getDoubleValue();
            }

            if (element.getAttribute("z") != null)
            {
                z = element.getAttribute("z").getDoubleValue();
            }
            
            setX(x);
            setY(y);
            setZ(z);
        }
        catch (DataConversionException except)
        {
            throw new RuntimeException(except);
        }
    }   

    public void setX(double d)
    {
        setAttribute("x", String.valueOf(d));
    }

    public void setY(double d)
    {
        setAttribute("y", String.valueOf(d));
    }

    public void setZ(double d)
    {
        setAttribute("z", String.valueOf(d));
    }

    public double x()
    {
        return dim("x");
    }

    public double y()
    {
        return dim("y");
    }

    public double z()
    {
        return dim("z");
    }

    private double dim(String name)
    {
        try {
            return getAttribute(name).getDoubleValue();
        }
        catch (DataConversionException x)
        {
            throw new RuntimeException(x);
        }
    }
}
