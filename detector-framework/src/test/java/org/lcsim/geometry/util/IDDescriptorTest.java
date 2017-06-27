package org.lcsim.geometry.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.geometry.util.IDDescriptor.IDException;

/**
 *
 * @author tonyj
 */
public class IDDescriptorTest extends TestCase
{
   
   public IDDescriptorTest(String testName)
   {
      super(testName);
   }
   
   
   public static Test suite()
   {
      return new TestSuite(IDDescriptorTest.class);
   }
   
   
   public void testDescriptor() throws IDException
   {
      IDDescriptor desc = new IDDescriptor("layer:7,system:3,barrel:3,theta:32:11,phi:11");
      assertEquals(5,desc.fieldCount());
      
      assertEquals(0,desc.fieldStart(0));
      assertEquals(7,desc.fieldLength(0));
      
      assertEquals(7,desc.fieldStart(1));
      assertEquals(3,desc.fieldLength(1));
      
      assertEquals(32,desc.fieldStart(3));
      assertEquals(11,desc.fieldLength(3));
      
      assertEquals(43,desc.fieldStart(4));
      assertEquals(11,desc.fieldLength(4));  
      
      assertEquals("layer",desc.fieldName(0));
      assertEquals("phi",desc.fieldName(4));
      
      assertEquals(0,desc.indexOf("layer"));
      assertEquals(4,desc.indexOf("phi"));
   }
   
   public void testSignedDescriptor() throws IDException
   {
      IDDescriptor desc = new IDDescriptor("layer:7,system:-3,barrel:3,theta:32:-11,phi:11");
      assertEquals(5,desc.fieldCount());
      
      assertEquals(0,desc.fieldStart(0));
      assertEquals(7,desc.fieldLength(0));
      assertFalse(desc.isSigned(0));
      
      assertEquals(7,desc.fieldStart(1));
      assertEquals(3,desc.fieldLength(1));
      assertTrue(desc.isSigned(1));
      
      assertEquals(32,desc.fieldStart(3));
      assertEquals(11,desc.fieldLength(3));
      assertTrue(desc.isSigned(3));
      
      assertEquals(43,desc.fieldStart(4));
      assertEquals(11,desc.fieldLength(4));  
      assertFalse(desc.isSigned(4));
      
      assertEquals("layer",desc.fieldName(0));
      assertEquals("phi",desc.fieldName(4));
      
      assertEquals(0,desc.indexOf("layer"));
      assertEquals(4,desc.indexOf("phi"));
   }
}
