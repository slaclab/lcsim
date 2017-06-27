package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;

import org.lcsim.event.StringVec;

/**
 *
 * @author tonyj
 */
class SIOStringVecBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "LCStrVec"; }
   public Class getClassForType() { return StringVec.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOStringVec(in, collection.getFlags(), version));
      return null;
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOStringVec.write((StringVec) element, out, flags);
   }
}
