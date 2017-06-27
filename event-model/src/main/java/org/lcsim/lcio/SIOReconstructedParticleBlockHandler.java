package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.ReconstructedParticle;

/**
 *
 * @author tonyj
 */
class SIOReconstructedParticleBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "ReconstructedParticle"; }
   public Class getClassForType() { return ReconstructedParticle.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOReconstructedParticle(in, collection.getFlags(), version));
      return null;
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOReconstructedParticle.write((ReconstructedParticle) element, out, flags);
   }
}
