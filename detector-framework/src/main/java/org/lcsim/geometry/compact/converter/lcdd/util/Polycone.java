/*
 * Polycone.java
 *
 * Created on May 3, 2005, 11:55 AM
 */

package org.lcsim.geometry.compact.converter.lcdd.util;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jdom.DataConversionException;
import org.jdom.JDOMException;

/**
 *
 * @author jeremym
 */
public class Polycone extends Solid 
{
        
	public Polycone(String name)
	{
		super("polycone", name);
		setStartPhi(0);
		setDeltaPhi(2.*Math.PI);
	}
	
    /** Creates a new instance of Polycone */
    public Polycone(String name, double startPhi, double deltaPhi, Element zplanesNode) throws JDOMException
    {
        super("polycone", name);        
        setStartPhi(startPhi);
        setDeltaPhi(deltaPhi);        
        addZPlanes(zplanesNode);
    }        

    void addZPlanes(Element zplanesNode) throws JDOMException, DataConversionException
    {
        //System.out.println("Polycone::addZPlanes - begin");
        
        int num_zplanes = 0;
        for ( Iterator i = zplanesNode.getChildren("zplane").iterator(); i.hasNext(); num_zplanes++)
        {
            Element e = (Element) i.next();            
            
            ZPlane zp = new ZPlane(e.getAttribute("rmin").getDoubleValue(),
                                   e.getAttribute("rmax").getDoubleValue(),
                                   e.getAttribute("z").getDoubleValue() );                        
            addZPlane(zp);
        }
        
        if ( num_zplanes < 2)
        {
            throw new JDOMException("Not enough zplanes -- minimum is 2.");
        }
        
        //System.out.println("Polycone::addZPlanes - end");
    }   
    
    final void setStartPhi(double startPhi)
    {
        setAttribute("startphi", String.valueOf(startPhi) );
    }
    
    final void setDeltaPhi(double deltaPhi)
    {
        setAttribute("deltaphi", String.valueOf(deltaPhi) );
    }
    
    public void addZPlane(ZPlane zplane)
    {
        addContent(zplane);
    }
    
    public void addZPlane(double rmin, double rmax, double z)
    {
        addZPlane( new ZPlane(rmin, rmax, z) );
    }           
}
