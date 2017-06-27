/*
 * SubtractionSolid.java
 *
 * Created on June 16, 2005, 4:37 PM
 */

package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author jeremym
 */
public class BooleanSolid extends Solid
{
    
    /** Creates a new instance of SubtractionSolid */
    BooleanSolid(String type, String name)
    {
        super(type, name);
                
        Element first = new Element("first");
        Element second = new Element("second");
        Element positionref = new Element("positionref");
        Element rotationref = new Element("rotationref");
        
        addContent(first);
        addContent(second);
        addContent(positionref);
        addContent(rotationref);
    }
    
    public void setFirstSolid(Solid s)
    {
        getChild("first").setAttribute("ref", s.getRefName() );
    }
    
    public void setSecondSolid(Solid s)
    {
        getChild("second").setAttribute("ref", s.getRefName() );
    }
    
    public void setPosition(Position p)
    {
        getChild("positionref").setAttribute("ref", p.getRefName() );
    }
    
    public void setRotation(Rotation r)
    {
        getChild("rotationref").setAttribute("ref", r.getRefName() );
    }
}
