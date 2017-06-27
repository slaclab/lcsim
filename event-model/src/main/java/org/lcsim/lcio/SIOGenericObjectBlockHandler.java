package org.lcsim.lcio;

import hep.io.sio.SIOBlock;
import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIOWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.lcsim.event.EventHeader.LCMetaData;

import org.lcsim.event.GenericObject;

/**
 *
 * @author tonyj
 */
class SIOGenericObjectBlockHandler extends AbstractBlockHandler
{
   public String getType() { return "LCGenericObject"; }
   public Class getClassForType() { return GenericObject.class; }

   @Override
   LCIOCallback readCollection(SIOInputStream in, int flags, SIOLCParameters colParameters, LCIOEvent event, SIOBlock block, int version) throws IOException {
      LCIOCollection collection = new LCIOCollection(getClassForType(), flags, 0, colParameters);
      event.put(block.getBlockName(), collection);
      return addCollectionElements(event, collection, in, 0, version);
   }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      int flags = collection.getFlags();
      if (LCIOUtil.bitTest(flags,LCIOConstants.GOBIT_FIXED))
      {
         int nInt = in.readInt();
         int nFloat = in.readInt();
         int nDouble = in.readInt();
         n = in.readInt();
         for (int i = 0; i < n; i++)
            collection.add(new SIOGenericObject(in, flags, version, nInt, nFloat, nDouble));
      }
      else
      {
         n = in.readInt();
         for (int i = 0; i < n; i++)
            collection.add(new SIOGenericObject(in, flags, version));
      }
      return null;
   }

    @Override
    public void writeBlock(SIOWriter writer, List collection, LCMetaData md) throws IOException {
      SIOOutputStream out = writer.createBlock(md.getName(), LCIOConstants.MAJORVERSION, LCIOConstants.MINORVERSION);

      List<GenericObject> goCollection = collection;
      // Note: treating empty collections as having variable size saves space
      boolean isFixedSize = !goCollection.isEmpty();
      if (isFixedSize) {
          int nInt = goCollection.get(0).getNInt();
          int nFloat = goCollection.get(0).getNFloat();
          int nDouble = goCollection.get(0).getNDouble();
          for (GenericObject object : goCollection) {
              isFixedSize &= object.getNInt()==nInt && object.getNFloat()==nFloat && object.getNDouble()==nDouble;
          }
      }
      int flags = md.getFlags();
      flags = LCIOUtil.bitSet(flags, LCIOConstants.GOBIT_FIXED, isFixedSize);
      out.writeInt(flags);
      Map<String,int[]> intMap = md.getIntegerParameters();
      Map<String,float[]> floatMap = md.getFloatParameters();
      Map<String,String[]> stringMap = md.getStringParameters();
      SIOLCParameters.write(intMap,floatMap,stringMap,out);
      if (isFixedSize) {
          // We know collection is therefore not empty
          out.writeInt(goCollection.get(0).getNInt());
          out.writeInt(goCollection.get(0).getNFloat());
          out.writeInt(goCollection.get(0).getNDouble());
      }
      out.writeInt(collection.size());
      for (Object element : collection)
      {
         writeCollectionElement(element,out,flags);
      }
      out.close();    }
   
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOGenericObject.write((GenericObject) element, out, flags);
   }
}
