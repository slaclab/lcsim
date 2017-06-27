package org.lcsim.util.loop;

import org.lcsim.geometry.Detector;

/**
 * A "dummy" detector loaded when no geometry info is available.
 * @author tonyj
 */
public class DummyDetector extends Detector
{
   private String name;

   public DummyDetector(String name)
   {
      super(null);
      this.name = name;
   }
   
   public String getName()
   {
      return name;
   }
}
