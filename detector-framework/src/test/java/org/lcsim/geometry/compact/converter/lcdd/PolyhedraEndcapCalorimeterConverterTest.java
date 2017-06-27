package org.lcsim.geometry.compact.converter.lcdd;

import java.io.FileOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 *
 * @author jeremym
 */
public class PolyhedraEndcapCalorimeterConverterTest extends TestCase
{    
    public PolyhedraEndcapCalorimeterConverterTest()
    {}
    
    public static TestSuite suite()
    {
        return new TestSuite(PolyhedraEndcapCalorimeterConverterTest.class);
    }
    
    public void test_PolyhedraEndcapCalorimeterConverter() throws Exception
    {
        InputStream in = 
                PolyhedraEndcapCalorimeterConverterTest.class.
                getResourceAsStream(
                "/org/lcsim/geometry/subdetector/PolyhedraEndcapCalorimeterTest.xml");
        new Main().convert("PolyhedraEndcapCalorimeterTest",in,new FileOutputStream(new TestOutputFile("PolyhedraEndcapCalorimeterTest.lcdd")));
    }
}
