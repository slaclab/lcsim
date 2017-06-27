package org.lcsim.geometry.compact;

import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * The class created when a constant definition is found in the XML file.
 * @author tonyj
 */
public class Constant
{
   private String name;
   private double value;
   
   public Constant(String name, double value)
   {
	   this.name = name;
	   this.value = value;
   }
   
   /**
    * Construct a new Constant
    * @param constant The JDOM element corresponding to the constant definition.
    * @throws org.jdom.DataConversionException If an XML error occurs while handling the node.
    */
   protected Constant(Element constant) throws DataConversionException
   {
      name = constant.getAttributeValue("name");
      value = constant.getAttribute("value").getDoubleValue();
   }
   /**
    * Get the name of this constant
    * @return The name.
    */
   public String getName()
   {
      return name;
   }
   /**
    * The value of this constant (after expression evaluation).
    * @return The value.
    */
   public double getValue()
   {
      return value;
   }
}
