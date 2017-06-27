package org.lcsim.geometry.util;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.geometry.util.IDDescriptor.IDException;

/**
 *
 * @author tonyj
 */
public class IDEncoderTest extends TestCase
{
   
   public IDEncoderTest(String testName)
   {
      super(testName);
   }
   
   public static Test suite()
   {
      return new TestSuite(IDEncoderTest.class);
   }
   public void testEncoder() throws IDException
   {
      int[] ids = { 1, 2, 3, 400, 500 };
      long[] lds = { 1, 2, 3, 400, 500 };
      long id = lds[0] | lds[1]<<7 | lds[2]<<10 | lds[3]<<32 | lds[4]<<43;

      String description = "layer:7,system:3,barrel:3,theta:32:11,phi:11";
      IDDescriptor desc = new IDDescriptor(description);
      IDEncoder ring = new IDEncoder(desc);

      long rc = ring.setValues(ids);
      assertEquals(rc,id);

      ring = new IDEncoder(desc);
      for (int i=0; i<ids.length; i++)
      {
         ring.setValues(ids);
      }
      assertEquals(id,ring.getID());
   }
   public void testSignedEncoder() throws IDException
   {
      int[] ids = { 1, 2, 3, -400, 500 };
      long[] lds = { 1, 2, 3, -400, 500 };
      long id = lds[0] | lds[1]<<7 | lds[2]<<10 | (lds[3] & ((1<<11) - 1))<<32 | lds[4]<<43;

      String description = "layer:7,system:-3,barrel:3,theta:32:-11,phi:11";
      IDDescriptor desc = new IDDescriptor(description);
      IDEncoder ring = new IDEncoder(desc);

      long rc = ring.setValues(ids);
      assertEquals(rc,id);

      ring = new IDEncoder(desc);
      for (int i=0; i<ids.length; i++)
      {
         ring.setValues(ids);
      }
      assertEquals(id,ring.getID());
   }
   public void testKiller() throws IDException
   {
      StringBuffer desc = new StringBuffer();
      Random r = new Random();
      for (int i=0; i<100; i++)
      {
         desc.setLength(0);
         for (int j=0,pos=0;;j++)
         {
            int l = r.nextInt(31) + 1;
            if (pos + l > 64) l = 64-pos;
            pos += l;
            desc.append('v').append(j).append(':').append(l);
            if (pos == 64) break;
            desc.append(',');
         }
         IDDescriptor id = new IDDescriptor(desc.toString());
         IDEncoder e = new IDEncoder(id);
         IDDecoder d = new IDDecoder(id);
         int[] buffer = new int[id.fieldCount()];
         for (int k=0; k<100; k++)
         {
            long data = r.nextLong();
            d.setID(data);
            long result = e.setValues(d.getValues(buffer));
            assertEquals(data,result);
         }
      }
   }
}