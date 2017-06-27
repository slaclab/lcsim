package org.lcsim.lcio;

import hep.io.sio.SIOBlock;
import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIOWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 * A default implementation of LCIOBlockHandler.
 * This implementation assumes that the block corresponds to a LCIOCollection
 * @author tonyj
 */
abstract class AbstractBlockHandler implements LCIOBlockHandler
{
   public LCIOCallback readBlock(LCIOEvent event, SIOBlock block) throws IOException
   {
      int major = block.getMajorVersion();
      int minor = block.getMinorVersion();
      int version = major*1000 + minor;
      if (version < 8)
         throw new IOException("Sorry: files created with versions older than v00-08" + " are no longer supported !");
      
      SIOInputStream in = block.getData();
      
      int flags = in.readInt();
      
      SIOLCParameters colParameters = version > 1001 ? new SIOLCParameters(in) : new SIOLCParameters();
      return readCollection(in,flags,colParameters,event,block, version);
   }

   LCIOCallback readCollection(SIOInputStream in, int flags, SIOLCParameters colParameters, LCIOEvent event, SIOBlock block, int version) throws IOException {
      int n = in.readInt();
      LCIOCollection collection = new LCIOCollection(getClassForType(), flags, n, colParameters);
      event.put(block.getBlockName(), collection);
      return addCollectionElements(event, collection, in, n, version);
   }
   abstract LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException;
   
   public void writeBlock(SIOWriter writer, List collection, LCMetaData md) throws IOException
   {
      SIOOutputStream out = writer.createBlock(md.getName(), LCIOConstants.MAJORVERSION, LCIOConstants.MINORVERSION);
      int flags = md.getFlags();
      out.writeInt(flags);
      Map<String,int[]> intMap = md.getIntegerParameters();
      Map<String,float[]> floatMap = md.getFloatParameters();
      Map<String,String[]> stringMap = md.getStringParameters();
      SIOLCParameters.write(intMap,floatMap,stringMap,out);
      out.writeInt(collection.size());
      for (Object element : collection)
      {
         writeCollectionElement(element,out,flags);
      }
      out.close();
   }
   abstract void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException;
}
