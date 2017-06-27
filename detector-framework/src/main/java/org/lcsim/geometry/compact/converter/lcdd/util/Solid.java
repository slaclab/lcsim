package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Attribute;


/**
 *
 * @author tonyj
 */
public abstract class Solid extends RefElement
{
   
   /** Creates a new instance of Solid */
   public Solid(String type, String name)
   {
      super(type,name);
   }
   
   public String getSolidName()
   {
	   return getAttributeValue("name");
   }
   
   public double getDim(String name)
   {
	   Attribute attrib = getAttribute(name);
	   double val = 0.0;
	   
	   if ( attrib == null )
	   {
		   throw new IllegalArgumentException("Solid " + getName() + " has no attribute called " + name);
	   }
	   
	   try {
		   val = attrib.getDoubleValue();
	   }
	   catch (Exception e)
	   {
		   throw new RuntimeException("Problem converting " + attrib.getValue() + " to double.");
	   }
	   
	   return val;
   }   
}
