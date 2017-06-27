package org.lcsim.lcio;

import hep.io.sio.SIOBlock;
import hep.io.sio.SIOWriter;
import java.io.IOException;
import java.util.List;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 *
 * @author tonyj
 */
interface LCIOBlockHandler
{
   public Class getClassForType();
   public String getType();
   /** Called by the event reader to read a single block.
    * It can return an LCIOCallback which will then be called after the entire
    * event is read, or <code>null</code> if no callback is required.
    */
   public LCIOCallback readBlock(LCIOEvent event, SIOBlock block) throws IOException;
   public void writeBlock(SIOWriter out, List collection, LCMetaData md) throws IOException;
}
