package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;

import org.lcsim.event.FloatVec;

/**
 *
 * @author tonyj
 */
class SIOFloatVecBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "LCFloatVec"; }
   public Class getClassForType() { return FloatVec.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOFloatVec(in, collection.getFlags(), version));
      return null;
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOFloatVec.write((FloatVec) element, out, flags);
   }
}
