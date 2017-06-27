package org.lcsim.lcio;

import hep.io.sio.SIOBlock;
import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOReader;
import hep.io.sio.SIORecord;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.lcsim.event.EventHeader;

/**
 *
 * @author tonyj
 */
public class LCIOReader
{
   private SIOReader reader;
   private Logger log = Logger.getLogger(LCIOWriter.class.getName());
   private HandlerManager manager = HandlerManager.instance();
   
   public LCIOReader(File file) throws IOException
   {
      reader = new SIOReader(new FileInputStream(file));
   }
   public void close() throws IOException
   {
      reader.close();
   }
   public void skipEvents(int numberToSkip) throws IOException
   {
      for (int i=0; i<numberToSkip; )
      {
         SIORecord record = reader.readRecord();
         String name = record.getRecordName();
         if (LCIOConstants.eventRecordName.equals(name)) i++; 
      }
   }
   public EventHeader read() throws IOException
   {
      for (;;)
      {
         SIORecord record = reader.readRecord();
         String name = record.getRecordName();
         if (!LCIOConstants.eventHeaderRecordName.equals(name)) continue;
         
         SIOBlock block = record.getBlock();
         int major = block.getMajorVersion() ;
         int minor = block.getMinorVersion() ;
         int version = major*1000 + minor;
         if (version < 8)
            throw new IOException("Sorry: files created with versions older than v00-08" + " are no longer supported !");
         
         SIOInputStream in = block.getData();
         LCIOEvent event = new LCIOEvent(in, version);
         
         record = reader.readRecord();
         name = record.getRecordName();
         if (!LCIOConstants.eventRecordName.equals(name)) throw new IOException("LCIO record order problem");
         
         List<LCIOCallback> callbacks = new ArrayList<LCIOCallback>();
         for (;;)
         {
            block = record.getBlock();
            if (block == null) break;
            String blockName = block.getBlockName();
            String type = event.getBlockType(blockName);
            if (type != null)
            {
               LCIOBlockHandler handler = manager.handlerForType(type);
               if (handler != null) 
               {
                  LCIOCallback callback = handler.readBlock(event,block);
                  if (callback != null) callbacks.add(callback);
               }
               else log.warning("No handler found for "+type);
            }
         }
         // Give block handlers chance to clean up after entire event is read
         for (LCIOCallback callback : callbacks) callback.callback();
         
         return event;
      }
   }
   public int skipEventsChecked(int numberToSkip) throws IOException
   {
      int i=0;
      while(i < numberToSkip)
      {
        try {
         SIORecord record = reader.readRecord();
         String name = record.getRecordName();
         if (LCIOConstants.eventRecordName.equals(name)) i++;
        } catch (EOFException x) {
          break;
        }
      }
      return i;
   }
}
