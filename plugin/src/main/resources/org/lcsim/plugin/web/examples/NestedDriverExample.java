import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 * An example showing how to nest drivers inside a parent driver.
 * 
 * @author Jan Strube
 * @version $Id: NestedDriverExample.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class NestedDriverExample extends Driver
{
   public NestedDriverExample()
   {
      add(new A());
      add(new B());
   }
   private class A extends Driver
   {
      protected void process(EventHeader event)
      {
         super.process(event);
         System.out.println("Hello from a");
      }
   }
   private class B extends Driver
   {
      protected void process(EventHeader event)
      {
         super.process(event);
         System.out.println("Hello from b");
      }
   }
}