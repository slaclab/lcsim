import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 * An example that shows how to add points to the WIRED display.
 *
 * @author tonyj
 * @version $Id: HeprepPoints.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class HeprepPoints extends Driver
{
   public void process(EventHeader e) 
   {
      List<Hep3Vector> list = new ArrayList<Hep3Vector>();
      // Make some (example) points and add to event
      for (double p = 0; p < Math.PI*2; p += Math.PI/50)
      {
         double r = 1000;
         double x = r*Math.sin(p);
         double y = r*Math.cos(p);
         double z = 0;
         Hep3Vector v = new BasicHep3Vector(x,y,z);
         list.add(v);
      }
      e.put("MyPoints",list);
   }
}
