/*
 * RZFieldMapConverterTest.java
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
public class RZFieldMapConverterTest extends TestCase
{  
    public RZFieldMapConverterTest(String name)
    {
        super(name);
    }
    
    public static TestSuite suite()
    {
        return new TestSuite(RZFieldMapConverterTest.class);
    }
    
    public void testRZFieldMapConverter() throws Exception
    {
      InputStream in = RZFieldMapConverterTest.class.getResourceAsStream("/org/lcsim/geometry/field/RZFieldMapTest.xml");
      new Main().convert("RZFieldMapTest",in,null);      
    }  
}
