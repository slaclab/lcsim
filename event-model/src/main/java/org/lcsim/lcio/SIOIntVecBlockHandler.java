package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;

import org.lcsim.event.IntVec;

/**
 *
 * @author tonyj
 */
class SIOIntVecBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "LCIntVec"; }
   public Class getClassForType() { return IntVec.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOFloatVec(in, collection.getFlags(), version));
      return null;
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOIntVec.write((IntVec) element, out, flags);
   }
}
