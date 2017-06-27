package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class IDField extends Element
{
   /** Creates a new instance of IDField */
   public IDField()
   {
      super("idfield");
      setAttribute("signed","false");
   }
   public void setLabel(String label)
   {
      setAttribute("label",label);
   }
   public void setStart(int start)
   {
      setAttribute("start",String.valueOf(start));
   }
   public void setLength(int length)
   {
      setAttribute("length",String.valueOf(length));
   }
   public void setSigned(boolean signed)
   {
      setAttribute("signed",String.valueOf(signed));      
   }
}
