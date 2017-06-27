package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author jeremym
 */
public class PhysVolID extends Element
{   
   public PhysVolID(String name, int value)
   {
	  super("physvolid");      
	  setAttribute("field_name", name);
	  setAttribute("value", Integer.toString(value));
   }
}
