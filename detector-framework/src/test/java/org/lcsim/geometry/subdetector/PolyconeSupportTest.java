package org.lcsim.geometry.subdetector;

import java.io.IOException;
import junit.framework.*;
import java.io.InputStream;
import org.jdom.JDOMException;
import org.lcsim.util.xml.ElementFactory.ElementCreationException;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.layer.Layering;

/**
 *
 * @author jeremym
 */
public class PolyconeSupportTest extends TestCase
{
   Detector det;
   public PolyconeSupportTest(String testName)
   {
      super(testName);
   }   
   
   public static Test suite()
   {
      return new TestSuite(PolyconeSupportTest.class);
   }   

   protected void setUp() throws Exception
   {
      InputStream in = LayeredSubdetectorTest.class.getResourceAsStream("/org/lcsim/geometry/subdetector/PolyconeSupportTest.xml");
      GeometryReader reader = new GeometryReader();
      det = reader.read(in);
   }
   
   public void testRead() throws IOException, JDOMException, ElementCreationException
   {
       assertTrue(det.getSubdetectors().size() == 1);
   }
   
   public void testPolyconeSupport()
   {
       PolyconeSupport bp = (PolyconeSupport) det.getSubdetectors().get("BeamPipe");       
       assertTrue(bp != null);
       
       assertTrue(bp.getNumberOfZPlanes() == 4);          
       
       for(PolyconeSupport.ZPlane zp : bp.getZPlanes())
       {
           assertTrue(zp.getRMin() != 0);
           assertTrue(zp.getRMax() != 0);
           assertTrue(zp.getZ() != 0);
           
           /* thickness of this zplane should be 1 cm == 10 mm */
           if (zp.getRMin() == 12.34)
           {
               assertTrue(zp.getRMax() - zp.getRMin() == 10.0);
           }
       }
   }
}