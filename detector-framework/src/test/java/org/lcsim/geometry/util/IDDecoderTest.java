package org.lcsim.geometry.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.geometry.util.IDDescriptor.IDException;

/**
 *
 * @author tonyj
 */
public class IDDecoderTest extends TestCase
{
   
   public IDDecoderTest(String testName)
   {
      super(testName);
   }

   public static Test suite()
   {
      return new TestSuite(IDDecoderTest.class);
   }

   public void testDecoder() throws IDException
   {
      String description = "layer:7,system:3,barrel:3,theta:32:11,phi:11";
      IDDescriptor desc = new IDDescriptor(description);
      IDDecoder ring = new IDDecoder(desc);
      
      long[] ids = { 1, 2, 3, 400, 500 };
      long id = ids[0] | ids[1]<<7 | ids[2]<<10 | ids[3]<<32 | ids[4]<<43;
      ring.setID(id);
      
      for (int i=0; i<ids.length; i++)
      {
         assertEquals(ids[i],ring.getValue(i));
      }
      
      int[] rids = new int[ids.length];
      ring.getValues(rids);
      for (int i=0; i<ids.length; i++) assertEquals(ids[i],rids[i]);
   }
   public void testSignedDecoder() throws IDException
   {
      String description = "layer:7,system:-3,barrel:3,theta:32:-11,phi:11";
      IDDescriptor desc = new IDDescriptor(description);
      IDDecoder ring = new IDDecoder(desc);
      
      long[] ids = { 1, 2, 3, -400, 500 };
      long id = ids[0] | ids[1]<<7 | ids[2]<<10 | (ids[3] & ((1<<11) - 1))<<32 | ids[4]<<43;
      ring.setID(id);
      
      for (int i=0; i<ids.length; i++)
      {
         assertEquals(ids[i],ring.getValue(i));
      }
      
      int[] rids = new int[ids.length];
      ring.getValues(rids);
      for (int i=0; i<ids.length; i++) assertEquals(ids[i],rids[i]);
   }
   
}
