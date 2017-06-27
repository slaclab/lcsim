package org.lcsim.geometry.util;

/**
 * Does the opposite of IDDecoder
 * @author tonyj
 */
public class IDEncoder
{
   private IDDescriptor desc;
   private long id;
   /** Creates a new instance of IIDecoder */
   public IDEncoder(IDDescriptor desc)
   {
      this.desc = desc;
   }
   public long setValue(String name, int value)
   {
      return setValue(desc.indexOf(name),value);  
   }
   public long setValue(int index, int value)
   {
      int start = desc.fieldStart(index);
      int length = desc.fieldLength(index);
      long mask = ((1L<<length) - 1) << start;
      id &= ~mask;
      id |= (((long) value)<<start) & mask;
      
      return id;
   }
   public long setValues(int[] values)
   {
      if (values.length != desc.fieldCount()) throw new IllegalArgumentException("Invalid array length");
      long result = 0;
      for (int i=0; i<values.length; i++)
      {
         int start = desc.fieldStart(i);
         int length = desc.fieldLength(i);
         long mask = ((1L<<length) - 1);
         result |= (mask & values[i]) << start;
      }
      return id = result;      
   }
   public long getID()
   {
      return id;
   }
}
