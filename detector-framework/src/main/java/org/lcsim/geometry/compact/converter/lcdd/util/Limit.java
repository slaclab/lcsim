/*
 * Limit.java
 *
 * Created on October 26, 2005, 5:38 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.lcsim.geometry.compact.converter.lcdd.util;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 *
 * @author jeremym
 */
public class Limit extends Element
{   
    /** Creates a new instance of Limit */
    public Limit(String name)
    {
        super("limit");    
        setAttribute("name", name);
        setUnit("mm");
        setParticles("*");       
    }
 
    /** 
     * Set comma delimited list of particles to which limit applies.  These are
     * the Geant4 particle names.  "*" for all particles.
     */
    public void setParticles(String particleNames)
    {
        setAttribute("particles", particleNames);
    }
    
    public void setValue(double value)
    {
        setAttribute("value", String.valueOf(value));
    }
    
    public void setUnit(String unit)
    {
        setAttribute("unit", unit);
    }
}