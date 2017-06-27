package org.lcsim.lcio;

/**
 *
 * @author tonyj
 */
public class LCIOUtil
{
   private LCIOUtil()
   {
      
   }
   public static boolean bitTest(int flag, int bit)
   {
      return (flag & (1<<bit)) != 0;
   }
   public static int bitSet(int flag, int bit, boolean set)
   {
      int mask = 1<<bit;
      if (set) flag |= mask;
      else flag &= ~mask;
      return flag;
   }
   /**
    * Create a bitMask for setting the specified bit
    */
   public static int bitMask(int bit)
   {
      return 1<<bit;
   }  
}
