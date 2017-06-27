package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;
import java.io.IOException;
import java.util.ListIterator;
import org.lcsim.event.MCParticle;

/**
 *
 * @author tonyj
 */
class SIOReferencesBlockHandler extends AbstractBlockHandler
{
   private String type;
   private Class classForType;
   SIOReferencesBlockHandler(String type, Class classForType)
   {
      this.type = type;
      this.classForType = classForType;
   }
   public String getType() { return type; }
   public Class getClassForType() { return classForType; }
   
   LCIOCallback addCollectionElements(LCIOEvent event, final LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      for (int i = 0; i < n; i++) collection.add(in.readPntr());
      return new LCIOCallback()
      {
         public void callback()
         {
            for (ListIterator iter = collection.listIterator(); iter.hasNext(); )
            {
               SIORef ref = (SIORef) iter.next();
               iter.set(ref.getObject());
            }
         }
      };
   }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      out.writePntr(element);
   }
}
