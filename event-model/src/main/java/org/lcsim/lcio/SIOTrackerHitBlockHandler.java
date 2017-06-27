package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;

import java.io.IOException;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.TrackerHit;

/**
 *
 * @author tonyj
 */
class SIOTrackerHitBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "TrackerHit"; }
   public Class getClassForType() { return TrackerHit.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOTrackerHit(in, collection.getFlags(), version));
      return null;
   }
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOTrackerHit.write((TrackerHit) element, out, flags);
   }
}
