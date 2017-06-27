import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 * A example Driver that only processes evenly numbered events.
 * 
 * @author jeremym
 * @version $Id: SkipEvent.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class SkipEvent extends Driver
{    
    protected void process(EventHeader event)
    {
        if (event.getEventNumber() % 2 != 0)
        {
            throw new Driver.NextEventException();
        }
        else {
            super.process(event);
        }
    }

}
