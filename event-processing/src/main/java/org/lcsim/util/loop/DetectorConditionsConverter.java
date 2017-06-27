package org.lcsim.util.loop;

import java.io.InputStream;

import org.lcsim.conditions.ConditionsConverter;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.conditions.RawConditions;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;


/**
 * 
 * @author tonyj
 */
public class DetectorConditionsConverter implements ConditionsConverter<Detector>
{
   public Detector getData(ConditionsManager manager, String name)
   {
      RawConditions conditions = manager.getRawConditions(name);
      try
      {
         InputStream in = conditions.getInputStream();
         GeometryReader reader = new GeometryReader();
         Detector detector = reader.read(in);
                       
         return detector;
      }
      catch (Exception x)
      {
         throw new ConditionsSetNotFoundException("Error reading detector condition item '"+name+"' for detector '"+manager.getDetector()+"'",x);
      }
   }  

   public Class<Detector> getType()
   {
      return Detector.class;
   }   
}
