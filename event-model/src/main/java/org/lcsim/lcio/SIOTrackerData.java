package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;
import org.lcsim.event.TrackerData;

class SIOTrackerData implements TrackerData
{
   private long cellid;
   private double time;
   private double[] chargeValues;
   
   SIOTrackerData(SIOInputStream in, int flags, int version) throws IOException
   {
      int cellid0 = in.readInt();
      int cellid1 = 0;
      if (LCIOUtil.bitTest(flags,LCIOConstants.TRAWBIT_ID1))
      {
         cellid1 = in.readInt();
      }
      this.cellid = ((long) cellid1)<<32 | cellid0;
      
      time = in.readFloat();
      int n = in.readInt();
      if (n > 0)
      {
         chargeValues = new double[n];
         for (int i=0; i<n; i++) chargeValues[i] = in.readFloat();
      }
      in.readPTag(this);
   }
   static void write(TrackerData hit, SIOOutputStream out, int flags) throws IOException
   {
      long cellid = hit.getCellID();
      out.writeInt((int) cellid);
      if (LCIOUtil.bitTest(flags,LCIOConstants.TRAWBIT_ID1))
      {
         out.writeInt((int) (cellid>>32));
      }
      out.writeFloat((float) hit.getTime());
      double[] c = hit.getChargeValues();
      if (c == null)
      {
         out.writeInt(0);
      }
      else
      {
         out.writeInt(c.length);
         for (int i=0; i<c.length; i++) out.writeFloat((float) c[i]);
      }
      out.writePTag(hit);
   }
   
   
   public long getCellID()
   {
      return cellid;
   }
   
   public double getTime()
   {
      return time;
   }
   
   public double[] getChargeValues()
   {
      return chargeValues;
   }
   
}
