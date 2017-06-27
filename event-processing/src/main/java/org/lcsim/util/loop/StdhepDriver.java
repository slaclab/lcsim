package org.lcsim.util.loop;

import hep.io.stdhep.StdhepEvent;
import hep.io.stdhep.StdhepWriter;
import java.io.File;
import java.io.IOException;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 * A driver for writing out Stdhep events.
 * This driver only works if the event was orignially read in from a stdhep file.
 * The driver writes out the original event, as read from the input file. Thus
 * it is useful for filtering events from stdhep files, but not for modifying
 * events in stdhep files.
 * @author tonyj
 */
public class StdhepDriver extends Driver
{
   private StdhepWriter writer;
   private String file;
   private String title;
   private String comment;
   private int expectedEvents;
   
   public StdhepDriver(String file, String title, String comment, int expectedEvents)
   {
      this.file = file;
      this.title = title;
      this.comment = comment;
      this.expectedEvents = expectedEvents;
   }
   protected void startOfData()
   {
      try
      {
         writer = new StdhepWriter(file, title, comment, expectedEvents);
      }
      catch (IOException x)
      {
         throw new RuntimeException("Error opening Stdhep file",x);
      }
   } 
   public StdhepDriver(File file, String title, String comment, int expectedEvents)
   {
      try
      {
         writer = new StdhepWriter(file.getAbsolutePath(), title, comment, expectedEvents);
      }
      catch (IOException x)
      {
         throw new RuntimeException("Error opening Stdhep file",x);
      }
   }  

   protected void process(EventHeader event)
   {
      try
      {
         StdhepEvent se = (StdhepEvent) event.get("StdhepEvent");
         writer.writeRecord(se);
      }
      catch (IOException x)
      {
         throw new RuntimeException("Error writing Stdhep file",x);
      }
   }   

   protected void endOfData()
   {
      try
      {
         if (writer != null)
         {
            writer.close();
            writer = null;
         }
      }
      catch (IOException x)
      {
         throw new RuntimeException("Error closing Stdhep file",x);
      }
   }
   
   protected void suspend()
   {
// We can't do this yet, the flush method is not in the current version of stdhep/MCFIO
//      try
//      {
//         writer.flush();
//      }
//      catch (IOException x)
//      {
//         throw new RuntimeException("Error flushing Stdhep file",x);
//      }
   }
}