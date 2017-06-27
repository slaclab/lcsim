package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class RefElement extends Element
{
   
   /** Creates a new instance of GDMLElement */
   public RefElement(String type, String name)
   {
      super(type);
      setAttribute("name",name);       
   }
   public String getRefName()
   {
      return getAttributeValue("name").toString();
   }
}
