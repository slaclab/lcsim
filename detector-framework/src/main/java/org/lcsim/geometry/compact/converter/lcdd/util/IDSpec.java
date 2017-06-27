package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author tonyj
 */
public class IDSpec extends RefElement
{
   
   /** Creates a new instance of IDSpec */
   public IDSpec(String name)
   {
      super("idspec",name);
      setAttribute("length","64");
   }
   public void addIDField(IDField field)
   {
      addContent(field);
   }
   public void setLength(int length)
   {
      setAttribute("length",String.valueOf(length));
   }
}
