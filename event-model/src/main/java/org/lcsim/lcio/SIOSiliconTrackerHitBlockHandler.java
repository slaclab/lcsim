package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.SiliconTrackerHit;
import org.lcsim.event.base.SiliconRawHit;

/**
 *
 * @author tonyj
 */
class SIOSiliconTrackerHitBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "SiliconRawHit"; }
   public Class getClassForType() { return SiliconRawHit.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOSiliconTrackerHit(in, collection.getFlags(), version));
      return null;
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOSiliconTrackerHit.write((SiliconTrackerHit) element, out, flags);
   }
}
