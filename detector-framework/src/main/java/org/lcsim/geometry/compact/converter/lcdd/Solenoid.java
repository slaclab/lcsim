package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;

/**
 * 
 * @author tonyj
 */
class Solenoid extends LCDDField
{
    private Element node;
           
    boolean setExplicitly = false;

    Solenoid(Element element)
    {
        super(element);
        this.node = element;
    }    

    void addToLCDD(LCDD lcdd) throws JDOMException
    {
        org.lcsim.geometry.compact.converter.lcdd.util.Solenoid sol = new org.lcsim.geometry.compact.converter.lcdd.util.Solenoid(getName());
        
        sol.setInnerField(node.getAttribute("inner_field").getDoubleValue());
        sol.setOuterField(node.getAttribute("outer_field").getDoubleValue());

        sol.setZMax(node.getAttribute("zmax").getDoubleValue());

        // Inner radius is set explicitly in compact.
        if (node.getAttribute("inner_radius") != null)
        {
            sol.setInnerRadius(node.getAttribute("inner_radius").getDoubleValue());
            sol.setOuterRadius(node.getAttribute("outer_radius").getDoubleValue());
        }
        // Inner radius not set so use default assumption.
        else
        {        
            // My inner radius is Jeremy's outer radius.  TJ
            sol.setInnerRadius(node.getAttribute("outer_radius").getDoubleValue());
        }
        
        lcdd.add(sol);
    }
}
