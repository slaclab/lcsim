package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;

import java.io.IOException;
import org.lcsim.event.RawCalorimeterHit;

/**
 * SIO-based I/O implementation of the RawCalorimeterHit interface
 *
 * @author Guilherme Lima
 * @version $Id: SIORawCalorimeterHit.java,v 1.3 2007/10/17 02:06:23 tonyj Exp $
 */
class SIORawCalorimeterHit implements RawCalorimeterHit
{
   private int cellId0;
   private int cellId1;
   private int amplitude;
   private int timeStamp;

   SIORawCalorimeterHit(SIOInputStream in, int flags, int version) throws IOException
   {
      cellId0 = in.readInt();
      if (LCIOUtil.bitTest(flags,LCIOConstants.RCHBIT_ID1) || version==8) cellId1 = in.readInt();
      else cellId1 = 0;

      amplitude = in.readInt();
      if (LCIOUtil.bitTest(flags,LCIOConstants.RCHBIT_TIME)) timeStamp = in.readInt();
      if (!LCIOUtil.bitTest(flags,LCIOConstants.RCHBIT_NO_PTR)) in.readPTag(this);
   }

   /**
    * Raw energy deposited in Calorimeter Cell
    */
   public int getAmplitude() {
      return amplitude;
   }

   public long getCellID() {
      return ((long) cellId1)<<32 | cellId0;
   }

   public int getTimeStamp() {
       return timeStamp;
   }

   static void write(RawCalorimeterHit hit, SIOOutputStream out, int flags) throws IOException
   {
      long cellID = hit.getCellID();
      out.writeInt((int) cellID);
      if( LCIOUtil.bitTest( flags, LCIOConstants.RCHBIT_ID1)) out.writeInt((int) (cellID>>32));

      out.writeInt( hit.getAmplitude() );
      if (LCIOUtil.bitTest(flags,LCIOConstants.RCHBIT_TIME)) out.writeInt( hit.getTimeStamp() );
      if (!LCIOUtil.bitTest(flags,LCIOConstants.RCHBIT_NO_PTR)) out.writePTag(hit);
   }
}
