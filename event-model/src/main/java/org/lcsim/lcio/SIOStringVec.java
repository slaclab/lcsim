package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;

import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.StringVec;
import org.lcsim.event.base.BaseStringVec;

/**
 *
 * @version $Id: SIOStringVec.java,v 1.1 2009/03/05 02:44:17 tonyj Exp $
 */
class SIOStringVec extends BaseStringVec
{   
   SIOStringVec(SIOInputStream in, int flag, int version) throws IOException
   {
      int size = in.readInt();
      String[] data = new String[size];
      for (int i = 0; i < size; i++)
         data[i] = in.readString();
      if(version>1002) in.readPTag(this);
      setVec(data);
   }

   static void write(StringVec vec, SIOOutputStream out, int flags) throws IOException
   {
         String[] data = vec.toStringArray();
         out.writeInt(data.length);
         for (int i = 0; i < data.length; i++)
            out.writeString(data[i]);
         out.writePTag(vec) ;
   }
}
