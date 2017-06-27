package org.lcsim.event.util;

import hep.physics.event.generator.diagnostic.DiagnosticEventGenerator;
import hep.physics.particle.properties.ParticlePropertyManager;
import hep.physics.particle.properties.ParticlePropertyProvider;
import java.util.Random;
import org.lcsim.event.EventHeader;

/**
 *
 * @author tonyj
 */
public class LCSimEventGenerator extends DiagnosticEventGenerator
{
   public LCSimEventGenerator(String detectorName)
   {
      this(detectorName,ParticlePropertyManager.getParticlePropertyProvider());
   }
   public LCSimEventGenerator(String detectorName, ParticlePropertyProvider ppp)
   {
      this(detectorName,ppp,new Random());
   }
   public LCSimEventGenerator(String detectorName, ParticlePropertyProvider ppp, Random random)
   {
      super(ppp,new LCSimFactory(detectorName), random);
   }

   public EventHeader generate()
   {
      return (EventHeader) super.generate();
   }

}