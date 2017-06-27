package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.SiliconTrackerHit;
import org.lcsim.event.TPCHit;

/**
 * Simple implementation of LCIO SiliconTrackerHit IO
 * @author tonyj
 */
class SIOSiliconTrackerHit implements SiliconTrackerHit
{
   private final long cellID;
   private final int timestamp;
   private final int adcCounts;
   
   public SIOSiliconTrackerHit(SIOInputStream in, int flags, int version) throws IOException
   {
      cellID = in.readLong();
      timestamp = in.readInt();
      adcCounts = in.readInt();
      in.readPTag(this);
   }
   
   public long getCellID()
   {
      return cellID;
   }
   
   public int getTimestamp()
   {
      return timestamp;
   }
   
   public int getADCCounts()
   {
      return adcCounts;
   }
   
   static void write(SiliconTrackerHit hit, SIOOutputStream out, int flags) throws IOException
   {
      out.writeLong(hit.getCellID());
      out.writeInt(hit.getTimestamp());
      out.writeInt(hit.getADCCounts());
      out.writePTag(hit);
   }
}
