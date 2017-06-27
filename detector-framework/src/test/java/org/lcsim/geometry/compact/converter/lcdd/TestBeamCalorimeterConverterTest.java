/*
 * TestBeamCalorimeterConverterTest.java
 *
 * Created on June 16, 2005, 3:23 PM
 */

package org.lcsim.geometry.compact.converter.lcdd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.lcsim.util.test.TestUtil.TestOutputFile;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class TestBeamCalorimeterConverterTest extends TestCase {

    final String resource = "/org/lcsim/geometry/subdetector/TestBeamCalorimeterTest.xml";
    
    public TestBeamCalorimeterConverterTest(String name) {
        super(name);
    }

    public static TestSuite suite() {
        return new TestSuite(TestBeamCalorimeterConverterTest.class);
    }

    public void testTestBeamCalorimeterConverter() throws Exception {
        InputStream in = TestBeamCalorimeterConverterTest.class.getResourceAsStream(resource);
        File file = new TestOutputFile("TestBeamCalorimeterTest.lcdd");
        OutputStream out = new FileOutputStream(file);
        new Main().convert("TestBeamCalorimeterConverterTest", in, out);
    }
}
