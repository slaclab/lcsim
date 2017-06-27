package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;
import java.io.IOException;
import org.lcsim.event.TrackerData;
import org.lcsim.event.TrackerPulse;

class SIOTrackerPulse implements TrackerPulse
{
   private SIORef data;
   private long cellid;
   private double time;
   private double charge;
   private int quality;
   
   SIOTrackerPulse(SIOInputStream in, int flags, int version) throws IOException
   {
      int cellid0 = in.readInt();
      int cellid1 = 0;
      if (LCIOUtil.bitTest(flags,LCIOConstants.TRAWBIT_ID1))
      {
         cellid1 = in.readInt();
      }
      this.cellid = ((long) cellid1)<<32 | cellid0;
      
      time = in.readFloat();
      charge = in.readFloat();
      quality = in.readInt();
      data = in.readPntr();
      in.readPTag(this);
   }
   static void write(TrackerPulse hit, SIOOutputStream out, int flags) throws IOException
   {
      long cellid = hit.getCellID();
      out.writeInt((int) cellid);
      if (LCIOUtil.bitTest(flags,LCIOConstants.TRAWBIT_ID1))
      {
         out.writeInt((int) (cellid>>32));
      }
      out.writeFloat((float) hit.getTime());
      out.writeFloat((float) hit.getCharge());
      out.writeInt(hit.getQuality());
      out.writePntr(hit.getTrackerData());
      out.writePTag(hit);
   }
   
   public TrackerData getTrackerData()
   {
      return (TrackerData) data.getObject();
   }

   public long getCellID()
   {
      return cellid;
   }

   public double getTime()
   {
      return time;
   }

   public double getCharge()
   {
      return charge;
   }

   public int getQuality()
   {
      return quality;
   }
}
