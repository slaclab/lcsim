package org.lcsim.util.loop;

import org.lcsim.conditions.ConditionsConverter;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.geometry.Detector;

/**
 * Used to return the dummy HepRepOnlyDetector
 * @author tonyj
 */
public class DummyConditionsConverter implements ConditionsConverter<Detector>
{
   private Detector detector;
   public DummyConditionsConverter(Detector detector)
   {
      this.detector = detector;
   }
   public Detector getData(ConditionsManager manager, String name)
   {
      return detector;
   }  

   public Class<Detector> getType()
   {
      return Detector.class;
   }   
}
