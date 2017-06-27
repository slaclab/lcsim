package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;

import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.FloatVec;
import org.lcsim.event.base.BaseFloatVec;

/**
 *
 * @version $Id: SIOFloatVec.java,v 1.1 2009/03/04 08:40:11 tonyj Exp $
 */
class SIOFloatVec extends BaseFloatVec
{   
   SIOFloatVec(SIOInputStream in, int flag, int version) throws IOException
   {
      int size = in.readInt();
      float[] data = new float[size];
      for (int i = 0; i < size; i++)
         data[i] = in.readFloat();
      if(version>1002) in.readPTag(this);
      setVec(data);
   }

   static void write(FloatVec vec, SIOOutputStream out, int flags) throws IOException
   {
         float[] data = vec.toFloatArray();
         out.writeInt(data.length);
         for (int i = 0; i < data.length; i++)
            out.writeFloat(data[i]);
         out.writePTag(vec) ;
   }
}
