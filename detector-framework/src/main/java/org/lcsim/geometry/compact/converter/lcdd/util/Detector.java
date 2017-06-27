package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class Detector extends Element
{
   
   /** Creates a new instance of Detector */
   public Detector()
   {
      super("detector");
   }

   public void setTitle(String name)
   {
      setAttribute("name",name);
   }

   public void setVersion(String version)
   {
      setAttribute("version",version);
   }

   public void setURL(String url)
   {
      setAttribute("url",url);
   }
   
}
