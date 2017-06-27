package org.lcsim.lcio;

import java.util.ArrayList;


/**
 *
 * @author tonyj
 */
class LCIOCollection extends ArrayList
{
   private Class type;
   private int flags;
   private SIOLCParameters parameters;
   
   LCIOCollection(Class type, int flags, int size, SIOLCParameters parameters)
   {
      super(size);
      this.flags = flags;
      this.type = type;
      this.parameters = parameters;
   }  

   public Class getType()
   {
      return type;
   }

   public int getFlags()
   {
      return flags;
   }

   public boolean equals(Object o)
   {
      return o == this;
   }

   public int hashCode()
   {
      return type.hashCode();
   }
   
   public SIOLCParameters getParameters()
   {
      return parameters;
   }
}
