import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 * An example showing how to set and retrieve a boolean value from the EventHeader.
 * 
 * @author Jeremy McCormick
 * @version $Id: BooleanCondition.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class BooleanCondition extends Driver
{
    public BooleanCondition()
    {
        add(new SetBoolean());
        add(new GetBoolean());
    }

    public class SetBoolean extends Driver
    {        
        protected void process(EventHeader event)
        {            
            // Put a random boolean value into the EventHeader.
            event.put("MyBool", getRandom().nextBoolean());  
        }
    }
    
    public class GetBoolean extends Driver
    {
        protected void process(EventHeader event)
        {
            // Get the boolean from EventHeader added by SetBoolean.
            Boolean bool = (Boolean)event.get("MyBool");
            
            // Condition based on the boolean value.
            if (bool)
                System.out.println("MyBool is TRUE");
            else
                System.out.println("MyBool is FALSE");                
        }
    }    
}
