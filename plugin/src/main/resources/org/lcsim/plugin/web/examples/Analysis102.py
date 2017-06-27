from org.lcsim.util.aida import AIDA
from hep.physics.vec import VecOp
from org.lcsim.event import MCParticle
from org.lcsim.util import Driver

class Analysis102(Driver):
   def __init__(self):
       self.aida = AIDA.defaultInstance()
   
   def process(self, event):
       # only necessary when adding Drivers to __this__ class.
       # in this case process() shouldn't do anything else
       #Driver.invokeChildren(self, event)
       # Get the list of MCParticles from the event
       particles = event.get(MCParticle.class, event.MC_PARTICLES)
       # Histogram the number of particles per event
       self.aida.cloud1D("nTracks").fill(particles.size())
       # Loop over the particles
       for iParticle in range(particles.size()):
          particle = particles.get(iParticle)
	  self.aida.cloud1D("mass").fill(particle.getMass())
          self.aida.cloud1D("pSquared").fill(particle.getMomentum().magnitudeSquared())
	  
