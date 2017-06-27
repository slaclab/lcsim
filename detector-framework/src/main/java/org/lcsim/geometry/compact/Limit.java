package org.lcsim.geometry.compact;

import org.jdom.Element;
import org.jdom.DataConversionException;

/**
 *
 * @author jeremym
 */
public class Limit
{
    private String name;
    private String particles;
    private String unit;
    private double value;
    
    /** Creates a new instance of Limit */
    protected Limit(Element node)
    {
        name = node.getAttributeValue("name");
        particles = node.getAttributeValue("particles");
        
        try
        {
            value = node.getAttribute("value").getDoubleValue();
        }
        catch (DataConversionException dce)
        {
            throw new RuntimeException("invalid value attribute: " + value, dce);
        }
        
        unit = node.getAttributeValue("unit");                
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getParticles()
    {
        return particles;
    }
    
    public String getUnit()
    {
        return unit;
    }
    
    public double getValue()
    {
        return value;
    }
}