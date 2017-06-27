package org.lcsim.geometry.compact.converter.lcdd;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 *
 * @author jeremym
 */
public class PolyhedraBarrelCalorimeterConverterTest extends TestCase
{    
    public PolyhedraBarrelCalorimeterConverterTest()
    {}
    
    public static TestSuite suite()
    {
        return new TestSuite(PolyhedraBarrelCalorimeterConverterTest.class);
    }
    
    public void test_PolyhedraBarrelCalorimeterConverter() throws Exception
    {
        InputStream in = 
                PolyhedraBarrelCalorimeterConverterTest.class.
                getResourceAsStream(
                "/org/lcsim/geometry/subdetector/PolyhedraBarrelCalorimeterTest.xml");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new TestOutputFile("PolyhedraBarrelCalorimeterTest.lcdd")));
        new Main().convert("PolyhedraBarrelCalorimeterTest",in,out);
    }
}
