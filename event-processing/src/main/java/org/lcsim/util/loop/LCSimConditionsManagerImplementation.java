package org.lcsim.util.loop;

import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManagerImplementation;

/**
 *
 * @author tonyj
 */
public class LCSimConditionsManagerImplementation extends ConditionsManagerImplementation
{   
   /** Creates a new instance of LCSimConditionsImplementaton */
   public LCSimConditionsManagerImplementation()
   {
      registerConditionsConverter(new DetectorConditionsConverter());
   }
   public static void register()
   {
      ConditionsManager.setDefaultConditionsManager(new LCSimConditionsManagerImplementation());
   }
}
