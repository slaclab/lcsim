/*
 * ZPlane.java
 *
 * Created on May 3, 2005, 4:50 PM
 */
package org.lcsim.geometry.compact.converter.lcdd.util;

import java.util.Iterator;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 *
 * @author jeremym
 */
public class ZPlane extends Element {
                        
    public ZPlane(double rmin, double rmax, double z) 
    {
        super("zplane");
        setRmin(rmin);
        setRmax(rmax);
        setZ(z);
    }
        
    void setRmin(double rmin) 
    {
        setAttribute("rmin", String.valueOf(rmin) );
    }
        
    void setRmax(double rmax) 
    {
        setAttribute("rmax", String.valueOf(rmax) );
    }          
                
    void setZ(double z) 
    {         
        setAttribute("z", String.valueOf(z) );            
    }
}   
