package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;

import java.io.IOException;
import org.lcsim.event.TrackerPulse;

/**
 *
 * @author tonyj
 */
class SIOTrackerPulseBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "TrackerPulse"; }
   public Class getClassForType() { return TrackerPulse.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOTrackerPulse(in, collection.getFlags(), version));
      return null;
   }
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOTrackerPulse.write((TrackerPulse) element, out, flags);
   }
}
