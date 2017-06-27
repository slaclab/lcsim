package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.lcsim.event.EventHeader.LCMetaData;


/**
 *
 * @author Frank Gaede
 * @version $Id: SIOLCParameters.java,v 1.4 2007/10/17 02:06:23 tonyj Exp $
 */
class SIOLCParameters
{
   private Map<String,int[]>     _intMap = new HashMap<String,int[]>();
   private Map<String,float[]>   _floatMap = new HashMap<String,float[]>();
   private Map<String,String[]>  _stringMap = new HashMap<String,String[]>();
   
   
   Map<String,int[]> getIntMap()
   {
      return _intMap;
   }
   Map<String,float[]> getFloatMap()
   {
      return _floatMap;
   }
   Map<String,String[]> getStringMap()
   {
      return _stringMap;
   }
   SIOLCParameters()
   {
      
   }
   SIOLCParameters(SIOInputStream in) throws IOException
   {
      int nIntParameters =  in.readInt();
      for (int i = 0; i < nIntParameters; i++)
      {
         String key = in.readString();
         int nInt = in.readInt() ;
         int[] iv = new int[nInt] ;
         for (int j = 0; j < nInt; j++)
         {
            iv[j] = in.readInt() ;
         }
         _intMap.put( key , iv ) ;
      }
      int nFloatParameters =  in.readInt();
      for (int i = 0; i < nFloatParameters; i++)
      {
         String key = in.readString();
         int nFloat = in.readInt() ;
         float[] fv = new float[nFloat] ;
         for (int j = 0; j < nFloat; j++)
         {
            fv[j] = in.readFloat() ;
         }
         _floatMap.put( key , fv ) ;
      }
      
      int nStringParameters =  in.readInt();
      for (int i = 0; i < nStringParameters; i++)
      {
         String key = in.readString();
         int nString = in.readInt() ;
         String[] sv = new String[nString] ;
         for (int j = 0; j < nString; j++)
         {
            sv[j] = in.readString() ;
         }
         _stringMap.put( key , sv ) ;
      }
   }
   static void write(Map<String,int[]> intMap, Map<String,float[]> floatMap, Map<String,String[]> stringMap, SIOOutputStream out) throws IOException
   {
      out.writeInt(intMap.size());
      for (Map.Entry<String,int[]> entry : intMap.entrySet())
      {
         out.writeString(entry.getKey());
         int[] values = entry.getValue();
         out.writeInt(values.length) ;
         for (int v : values)
         {
            out.writeInt(v);
         }
      }
      out.writeInt(floatMap.size());
      for (Map.Entry<String,float[]> entry : floatMap.entrySet())
      {
         out.writeString(entry.getKey());
         float[] values = entry.getValue();
         out.writeInt(values.length) ;
         for (float v : values)
         {
            out.writeFloat(v);
         }
      }
      out.writeInt(stringMap.size());
      for (Map.Entry<String,String[]> entry : stringMap.entrySet())
      {
         out.writeString(entry.getKey());
         String[] values = entry.getValue();
         out.writeInt(values.length) ;
         for (String v : values)
         {
            out.writeString(v);
         }
      }
   }
}
