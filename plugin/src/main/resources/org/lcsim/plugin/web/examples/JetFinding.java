import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.util.JetDriver;
import org.lcsim.mc.fast.MCFast;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;
/**
 * An example showing how to use the Jet Finder.
 * 
 * @author Tony Johnson
 * @version $Id: JetFinding.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 * 
 */
public class JetFinding extends Driver
{
   private AIDA aida = AIDA.defaultInstance();
   public JetFinding()
   {
      add(new MCFast());
      add(new JetDriver());
   }
   
   protected void process(EventHeader event)
   {
      super.process(event);
      
      List<ReconstructedParticle> jets = event.get(ReconstructedParticle.class,"Jets");
      aida.cloud1D("nJets").fill(jets.size());
      for (ReconstructedParticle jet : jets)
      {
         List<ReconstructedParticle> particlesInJet = jet.getParticles();
         aida.cloud1D("nParticles").fill(particlesInJet.size());         
      }
   }
}
