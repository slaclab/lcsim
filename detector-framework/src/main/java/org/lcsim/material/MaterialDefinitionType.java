/*
 * MaterialDefinitionType.java
 *
 * Created on June 29, 2005, 12:40 PM
 */

package org.lcsim.material;

import org.jdom.JDOMException;

/**
 *
 * @author jeremym
 */
public class MaterialDefinitionType
{
    String _tagname;
    
    public static final MaterialDefinitionType COMPOSITE = new MaterialDefinitionType("composite");
    public static final MaterialDefinitionType FRACTION = new MaterialDefinitionType("fraction");
    public static final MaterialDefinitionType ATOM = new MaterialDefinitionType("atom");
    public static final MaterialDefinitionType INVALID = new MaterialDefinitionType("");
    
    /** Creates a new instance of MaterialDefinitionType */
    public MaterialDefinitionType(String tagname)
    {
        _tagname = tagname;
    }
    
    public String getTagName()
    {
        return _tagname;
    }
    
    static MaterialDefinitionType getMaterialDefinitionType(org.jdom.Element materialElement) throws JDOMException
    {
        boolean gotOne = false;
        MaterialDefinitionType mdt = MaterialDefinitionType.INVALID;
        
        if ( materialElement.getChild("atom") != null )
        {
            mdt = MaterialDefinitionType.ATOM;
            gotOne = true;
        }
        
        if ( materialElement.getChild("composite") != null )
        {
            if ( gotOne )
            {
                throw new JDOMException("Already got atom tag for material: " + materialElement.getAttributeValue("name"));
            }
            else
            {
                gotOne = true;
                mdt = MaterialDefinitionType.COMPOSITE;
            }
        }
        
        if ( materialElement.getChild("fraction") != null )
        {
            if ( gotOne )
            {
                throw new JDOMException("Already got atom or composite tag for material: " + materialElement.getAttributeValue("name"));
            }
            else
            {
                mdt = MaterialDefinitionType.FRACTION;
                gotOne = true;
            }
        }
        
        if ( !gotOne )
        {
            throw new JDOMException("One of atom, composite or fraction was not found for material: " + materialElement.getAttributeValue("name"));
        }
        
        return mdt;
    }
}
