import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 * A simple example that prints the EventHeader to the JAS console.
 * 
 * @see org.lcsim.event.EventHeader
 * 
 * @author Norman Graf
 * @version $Id: PrintEventHeader.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class PrintEventHeader extends Driver
{
   // This method will be called for each event
   protected void process(EventHeader event)
   {
      // Just print the event header to the JAS console
      System.out.println(event);
   }
}
