package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author tonyj
 */
public abstract class Field extends RefElement
{
   /** Creates a new instance of Field */
   public Field(String type, String name)
   {
      super(type,name);
      setAttribute("lunit","mm");
      setAttribute("funit","tesla");
   }
}
