package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.EventHeader.LCMetaData;

import org.lcsim.event.SimCalorimeterHit;


/**
 *
 * @author tonyj
 */
class SIOSimCalorimeterHitBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "SimCalorimeterHit"; }
   public Class getClassForType() { return SimCalorimeterHit.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      LCMetaData meta = event.getMetaData(collection);
      for (int i = 0; i < n; i++)
         collection.add(new SIOSimCalorimeterHit(in, collection.getFlags(), version, meta));
      return null;
   }
      
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOSimCalorimeterHit.write((SimCalorimeterHit) element, out, flags);
   }
}
