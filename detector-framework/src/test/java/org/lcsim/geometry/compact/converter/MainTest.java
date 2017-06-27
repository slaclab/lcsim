package org.lcsim.geometry.compact.converter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.util.test.TestUtil.TestOutputFile;


/**
 *
 * @author tonyj
 */
public class MainTest extends TestCase
{
   private File tempFile;
   
   public MainTest(String testName)
   {
      super(testName);
   }
   
   protected void setUp() throws Exception
   {
      InputStream in = MainTest.class.getResourceAsStream("/org/lcsim/geometry/compact/sid01_compact.xml");
      tempFile = File.createTempFile("xyz","xml");
      OutputStream out = new FileOutputStream(tempFile);
      byte[] buffer = new byte[8096];
      for (;;)
      {
         int l = in.read(buffer);
         if (l < 0) break;
         out.write(buffer, 0, l);
      }
      out.close();
      in.close();
   }
   
   protected void tearDown() throws Exception
   {
      tempFile.delete();
   }
   
   public static Test suite()
   {
      return new TestSuite(MainTest.class);
   }
   
   /**
    * Test of main method, of class org.lcsim.geometry.compact.converter.Main.
    */
   public void testMain() throws Throwable
   {
      String[] args = new String[]{tempFile.getAbsolutePath(), new TestOutputFile("temp.heprep").getAbsolutePath()};
      Main.main(args);
   }
   
   /**
    * Test of getConverters method, of class org.lcsim.geometry.compact.converter.Main.
    */
/*
   public void testGetConverters()
   {
      List<Converter> converters = Main.getConverters();
      assertEquals(3,converters.size());
      for (Converter c : converters)
      {
         assertTrue(c instanceof Converter);
      }
   }
*/
}
