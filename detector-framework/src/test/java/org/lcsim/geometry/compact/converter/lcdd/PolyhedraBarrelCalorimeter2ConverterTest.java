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
public class PolyhedraBarrelCalorimeter2ConverterTest extends TestCase
{    
    public void testPolyhedraBarrelCalorimeterConverter() throws Exception
    {
        InputStream in = 
                PolyhedraBarrelCalorimeterConverterTest.class.
                getResourceAsStream("/org/lcsim/geometry/subdetector/PolyhedraBarrelCalorimeter2Test.xml");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new TestOutputFile("PolyhedraBarrelCalorimeter2Test.lcdd")));
        new Main().convert("PolyhedraBarrelCalorimeter2Test",in,out);
    }
}
