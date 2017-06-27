package org.lcsim.geometry.compact.converter.lcdd;

import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.util.xml.DefaultElementFactory;
import org.lcsim.geometry.compact.Field;
import org.lcsim.geometry.compact.Segmentation;
import org.lcsim.geometry.compact.CompactElementFactory;

/**
 *
 * @author tonyj
 */
class LCDDElementFactory extends CompactElementFactory
{
   LCDDElementFactory()
   {
      super();
      register(LCDDDetector.class);
      register(Subdetector.class,"org.lcsim.geometry.compact.converter.lcdd");
      register(Field.class,"org.lcsim.geometry.compact.converter.lcdd");
      register(Segmentation.class,"org.lcsim.geometry.compact.converter.lcdd");
   }
/*
    public <T> T createElement(Class<T> c, org.jdom.Element node, String type) throws org.jdom.JDOMException, org.lcsim.geometry.compact.ElementFactory.ElementCreationException 
    {
        T retValue;        
        retValue = super.createElement(c, node, type);
        System.out.println("Got "+retValue+" for type "+type);
        return retValue;
    }
 */
}
