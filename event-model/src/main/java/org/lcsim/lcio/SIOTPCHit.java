package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;

import java.io.IOException;
import org.lcsim.event.TPCHit;


/**
 *
 * @author Tony Johnson
 * @version $Id: SIOTPCHit.java,v 1.2 2007/10/17 02:06:23 tonyj Exp $
 */
class SIOTPCHit implements TPCHit
{
   private int cellID;
   private float time;
   private float charge;
   private int quality;
   private int[] rawDataArray;
   
   SIOTPCHit(SIOInputStream in, int flags, int version) throws IOException
   {
      cellID = in.readInt();
      time = in.readFloat();
      charge = in.readFloat();
      quality = in.readInt();
      
      if (LCIOUtil.bitTest(flags,LCIOConstants.TPCBIT_RAW))
      {
         int size = in.readInt() ;
         rawDataArray = new int[ size ] ;
         for (int i = 0; i < size; i++)
         {
            rawDataArray[i] = in.readInt() ;
         }
      }
      
      if( version > 1002 )
      {
         if ( LCIOUtil.bitTest(flags,LCIOConstants.TPCBIT_NO_PTR)) in.readPTag(this) ;
      }
      else
      {
         if ( !LCIOUtil.bitTest(flags,LCIOConstants.TPCBIT_NO_PTR)) in.readPTag(this) ;
      }
      
   }
   public int getRawDataWord(int i)
   {
      return rawDataArray[i];
   }

   public double getTime()
   {
      return time;
   }

   public int getQuality()
   {
      return quality;
   }

   public int getNRawDataWords()
   {
      return rawDataArray.length;
   }

   public double getCharge()
   {
      return charge;
   }

   public int getCellID()
   {
      return cellID;
   }
   
   static void write(TPCHit hit, SIOOutputStream out, int flags) throws IOException
   {
      out.writeInt(hit.getCellID());
      out.writeFloat((float) hit.getTime());
      out.writeFloat((float) hit.getCharge());
      out.writeInt(hit.getQuality());
      
      if (LCIOUtil.bitTest(flags,LCIOConstants.TPCBIT_RAW))
      {
         out.writeInt( hit.getNRawDataWords() );
         for (int i = 0; i < hit.getNRawDataWords() ; i++)
         {
            out.writeInt( hit.getRawDataWord(i) ) ;
         }
      }
      if (!LCIOUtil.bitTest(flags,LCIOConstants.TPCBIT_NO_PTR)) out.writePTag(hit) ;
   }
}
