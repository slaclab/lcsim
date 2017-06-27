package org.lcsim.geometry.compact;

import org.jdom.Element;

/**
 * Represents a magnetic field.
 * @author tonyj
 */
public class Field
{
   private String name;
   protected Field(Element field)
   {
      name = field.getAttributeValue("name");
   }
   public String getName()
   {
      return name;
   }
}
