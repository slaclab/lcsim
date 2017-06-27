import org.lcsim.util.aida.AIDA;
import hep.physics.vec.VecOp;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.util.Driver;

/*
 * An example showing how to access MCParticles from the EventHeader and
 * make some simple histograms from the data.
 * 
 * @author Norman Graf
 * @version $Id: Analysis101.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 * 
 */
public class Analysis101 extends Driver
{
   private AIDA aida = AIDA.defaultInstance();
   
   protected void process(EventHeader event)
   {
      // Get the list of MCParticles from the event
      List<MCParticle> particles = event.get(MCParticle.class,event.MC_PARTICLES);
      // Histogram the number of particles per event
      aida.cloud1D("nTracks").fill(particles.size());
      // Loop over the particles
      for (MCParticle particle : particles)
      {
         aida.cloud1D("energy").fill(particle.getEnergy());
         aida.cloud1D("cosTheta").fill(VecOp.cosTheta(particle.getMomentum()));
         aida.cloud1D("phi").fill(VecOp.phi(particle.getMomentum()));
      }
   }
}