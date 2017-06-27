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
public class SiTrackerEndcapTest extends TestCase
{    
    public SiTrackerEndcapTest(String name)
    {
        super(name);
    }
    
    public static TestSuite suite()
    {
        return new TestSuite(SiTrackerEndcapTest.class);
    }
    
    public void test_converter() throws Exception
    {
        InputStream in = SiTrackerBarrel.class.getResourceAsStream("/org/lcsim/detector/converter/compact/SiTrackerEndcapTest.xml");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new TestOutputFile("SiTrackerEndcapTest.lcdd")));
        new Main().convert("SiTrackerEndcapTest",in,out);
    }
}
