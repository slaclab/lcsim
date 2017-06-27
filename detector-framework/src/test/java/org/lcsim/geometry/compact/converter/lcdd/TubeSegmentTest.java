package org.lcsim.geometry.compact.converter.lcdd;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * Test of LCDD converter for TubeSegment.
 * @author jeremym
 */
public class TubeSegmentTest extends TestCase
{    
    public void testCnv() throws Exception
    {
        InputStream in = TubeSegment.class.getResourceAsStream("/org/lcsim/geometry/subdetector/TubeSegmentTest.xml");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new TestOutputFile("TubeSegmentTest.lcdd")));
        new Main().convert("TubeSegmentTest",in,out);
    }
}
