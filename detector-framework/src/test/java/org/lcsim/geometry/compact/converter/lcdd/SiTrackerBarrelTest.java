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
public class SiTrackerBarrelTest extends TestCase
{    
    public SiTrackerBarrelTest(String name)
    {
    	super(name);
    }
    
    public static TestSuite suite()
    {
        return new TestSuite(SiTrackerBarrelTest.class);
    }
    
    public void test_converter() throws Exception
    {
        InputStream in = SiTrackerBarrel.class.getResourceAsStream("/org/lcsim/geometry/subdetector/SiTrackerBarrelTest.xml");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new TestOutputFile("SiTrackerBarrelTest.lcdd")));
        new Main().convert("SiTrackerBarrelTest",in,out);
    }
}
