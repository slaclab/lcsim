import org.lcsim.event.util.LCSimEventGenerator;

/*
 * A basic example of event generation using the diagnostic particle generator.
 * 
 * @author Norman Graf
 * @version $Id: EventGenerator.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class EventGenerator extends LCSimEventGenerator
{
   public EventGenerator()
   {
      super("sdjan03");
      
      setMomentumRange(10,20);
      setNumberOfParticles(10);
      setParticleType(getParticlePropertyProvider().get(22));
   }
}
