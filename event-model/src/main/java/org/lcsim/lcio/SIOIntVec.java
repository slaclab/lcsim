package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;

import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.IntVec;
import org.lcsim.event.base.BaseIntVec;

/**
 *
 * @version $Id: SIOIntVec.java,v 1.1 2009/03/05 02:44:17 tonyj Exp $
 */
class SIOIntVec extends BaseIntVec
{   
   SIOIntVec(SIOInputStream in, int flag, int version) throws IOException
   {
      int size = in.readInt();
      int[] data = new int[size];
      for (int i = 0; i < size; i++)
         data[i] = in.readInt();
      if(version>1002) in.readPTag(this);
      setVec(data);
   }

   static void write(IntVec vec, SIOOutputStream out, int flags) throws IOException
   {
         int[] data = vec.toIntArray();
         out.writeInt(data.length);
         for (int i = 0; i < data.length; i++)
            out.writeFloat(data[i]);
         out.writePTag(vec) ;
   }
}