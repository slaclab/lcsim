/*
 * TestBeamCalorimeterConverterTest.java
 *
 * Created on June 16, 2005, 3:23 PM
 */

package org.lcsim.geometry.compact.converter.lcdd;

import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author jeremym
 */
public class TestBeamTrackerConverterTest extends TestCase
{
    public TestBeamTrackerConverterTest(String name)
    {
        super(name);
    }
    
    public static TestSuite suite()
    {
        return new TestSuite(TestBeamCalorimeterConverterTest.class);
    }
    
    public void testTestBeamCalorimeterConverter() throws Exception
    {
      InputStream in = TestBeamCalorimeterConverterTest.class.getResourceAsStream("/org/lcsim/geometry/subdetector/TestBeamTrackerTest.xml");
      new Main().convert("TestBeamTrackerConverterTest",in,null);      
    }  
}
