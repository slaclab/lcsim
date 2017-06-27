package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.io.IOException;

import org.lcsim.event.Track;

/**
 *
 * @author tonyj
 */
class SIOTrackBlockHandler extends AbstractBlockHandler
{
   private static final Hep3Vector origin = new BasicHep3Vector();
   public String getType() { return "Track"; }
   public Class getClassForType() { return Track.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      double bField = event.getDetector().getFieldMap().getField(origin).z();
      for (int i = 0; i < n; i++)
         collection.add(new SIOTrack(in, collection.getFlags(), version, bField));
      return null;
   }
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOTrack.write((Track) element, out, flags);
   }
}
