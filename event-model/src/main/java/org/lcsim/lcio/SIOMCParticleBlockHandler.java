package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.MCParticle;

/**
 *
 * @author tonyj
 */
class SIOMCParticleBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "MCParticle"; }
   public Class getClassForType() { return MCParticle.class; }
   LCIOCallback addCollectionElements(LCIOEvent event, final LCIOCollection collection, SIOInputStream in, final int n, final int version) throws IOException
   {
      for (int i = 0; i < n; i++)
         collection.add(new SIOMCParticle(in, collection.getFlags(), version));

      return new LCIOCallback()
      {
         public void callback()
         {
             for (int i = 0; i < n; i++)
               ((SIOMCParticle) collection.get(i)).resolve(version);
         }
      };
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOMCParticle.write((MCParticle) element, out, flags);
   }
}
