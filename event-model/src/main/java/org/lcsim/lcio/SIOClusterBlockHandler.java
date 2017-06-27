package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;

import org.lcsim.event.Cluster;

/**
 *
 * @author tonyj
 */
class SIOClusterBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "Cluster"; }
   public Class getClassForType() { return Cluster.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOCluster(in, collection.getFlags(), version));
      return null;
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOCluster.write((Cluster) element, out, flags);
   }
}
