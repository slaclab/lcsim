package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.Field;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;

/**
 *
 * @author tonyj
 */
abstract class LCDDField extends Field
{
   
   /** Creates a new instance of LCDDField */
   public LCDDField(Element node)
   {
      super(node);
   }
   abstract void addToLCDD(LCDD lcdd) throws JDOMException;
}
