package org.lcsim.plugin;

import org.freehep.record.loop.RecordLoop;
import org.freehep.util.FreeHEPLookup;
import org.lcsim.event.EventHeader;

/**
 * The methods in this class are made available to scripts via the <code>lcsim</code>
 * variable.
 * @author tonyj
 */
public class LCSim
{
   private final FreeHEPLookup lookup;
   /** Creates a new instance of LCSim */
   LCSim(FreeHEPLookup lookup)
   {
      this.lookup = lookup;
   }
   /**
    * Get the current event.
    * @return The event, or <code>null</code> if event not available
    */
   public EventHeader getCurrentEvent()
   {
      try
      {
         RecordLoop loop = (RecordLoop) lookup.lookup(RecordLoop.class); 
         Object event = loop.getRecordSource().getCurrentRecord();
         return (event instanceof EventHeader) ? (EventHeader) event : null;
      }
      catch (Exception x)
      {
         return null;
      }
   }
}
