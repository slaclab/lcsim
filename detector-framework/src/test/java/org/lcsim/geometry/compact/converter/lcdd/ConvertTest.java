package org.lcsim.geometry.compact.converter.lcdd;
import junit.framework.*;
import java.io.InputStream;


/**
 *
 * @author tonyj
 */
public class ConvertTest extends TestCase
{
   public ConvertTest(String testName)
   {
      super(testName);
   }
   
   public static TestSuite suite()
   {
      return new TestSuite(ConvertTest.class);
   }
   
   /**
    * Test of main method, of class org.lcsim.geometry.compact.converter.lcdd.Main.
    */
   /* FIXME: Does not appear to actually run the converter, because output stream is null! */
   public void testMain() throws Exception
   {
      InputStream in = ConvertTest.class.getResourceAsStream("/org/lcsim/geometry/compact/sidloi3.xml");
      new Main().convert("sdjan03",in,null);
   }
}
