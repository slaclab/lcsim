package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;

import java.io.IOException;
import org.lcsim.event.TrackerData;

/**
 *
 * @author tonyj
 */
class SIOTrackerDataBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "TrackerData"; }
   public Class getClassForType() { return TrackerData.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOTrackerData(in, collection.getFlags(), version));
      return null;
   }
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOTrackerData.write((TrackerData) element, out, flags);
   }
}
