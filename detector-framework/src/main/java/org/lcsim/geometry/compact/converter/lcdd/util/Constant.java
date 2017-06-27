package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.DataConversionException;


/**
 *
 * @author tonyj
 */
public class Constant extends RefElement
{
    public Constant(String name, double value)
    {
        this(name,String.valueOf(value));
    }
    public Constant(String name, String value)
    {
        super("constant",name);
        setAttribute("value",value);
    }
    
    public double getConstantValue() throws DataConversionException
    {
        return getAttribute("value").getDoubleValue();
    }
}
