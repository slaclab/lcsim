package org.lcsim.geometry.compact.converter.lcdd;

import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author jeremym
 */
public class TPCConverterTest extends TestCase
{    
    public TPCConverterTest()
    {
    }
    
    public static TestSuite suite()
    {
        return new TestSuite(TPCConverterTest.class);
    }
    
    public void test_TPCConverter() throws Exception
    {
        InputStream in = TPCConverterTest.class.getResourceAsStream("/org/lcsim/geometry/subdetector/TPCTest.xml");
        new Main().convert("TPCTest",in,null);
    }
}
