package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;

import org.lcsim.event.TPCHit;

/**
 *
 * @author tonyj
 */
class SIOTPCHitBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "TPCHit"; }
   public Class getClassForType() { return TPCHit.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOTPCHit(in, collection.getFlags(), version));
      return null;
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOTPCHit.write((TPCHit) element, out, flags);
   }
}
