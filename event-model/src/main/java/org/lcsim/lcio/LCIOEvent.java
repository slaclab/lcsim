package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import java.util.HashMap;
import java.util.Map;

import org.lcsim.event.base.BaseLCSimEvent;

import java.io.IOException;

/**
 *
 * @author tonyj
 */
class LCIOEvent extends BaseLCSimEvent
{
   private Map<String,String> blockMap;
   private SIOLCParameters eventParameters;
   private static final String WEIGHT = "_weight";

   
   /** Creates a new instance of LCIOEvent */
   LCIOEvent(SIOInputStream in, int version) throws IOException
   {
      this(in.readInt(),in.readInt(),in.readLong(),in.readString());
      
      int nBlockNames = in.readInt();
      blockMap = new HashMap<String,String>();
      for (int i = 0; i < nBlockNames; i++)
      {
         String blockName = in.readString();
         String blockType = in.readString();
         
         blockMap.put(blockName, blockType);
      }
      eventParameters = version > 1001 ? new SIOLCParameters(in) : new SIOLCParameters();
   }

   public Map<String, float[]> getFloatParameters() 
   {
      return eventParameters.getFloatMap();
   }

   public Map<String, int[]> getIntegerParameters() 
   {
      return eventParameters.getIntMap();
   }

   public Map<String, String[]> getStringParameters() 
   {
      return eventParameters.getStringMap();
   }
   
   public float getWeight()
   {
      float[] weights = getFloatParameters().get(WEIGHT);
      return weights == null || weights.length == 0 ? 1.0f : weights[0];
   }
   
   private LCIOEvent(int run, int event, long time, String name)
   {
      super(run,event,name,time);
   }
   String getBlockType(String type)
   {
      return blockMap.get(type);
   }
   void put(String name, LCIOCollection collection)
   {
      SIOLCParameters parameters = collection.getParameters();
      super.put(name,collection,collection.getType(),collection.getFlags(),parameters.getIntMap(), parameters.getFloatMap(), parameters.getStringMap());
   }
}
