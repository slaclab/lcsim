package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class Generator extends Element
{
   
   /** Creates a new instance of Generator */
   public Generator()
   {
      super("generator");
   }

   public void setTitle(String title)
   {
      setAttribute("name",title);
   }

   public void setVersion(String version)
   {
      setAttribute("version",version);
   }

   public void setFile(String file)
   {
      setAttribute("file",file);
   }
   
   public void setChecksum(long checksum)
   {
      setAttribute("checksum",String.valueOf(checksum));
   }  
}
