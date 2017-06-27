package org.lcsim.geometry.subdetector;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;

/**
 * Test of all types in org.lcsim.geometry.subdetector
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class SubdetectorTest extends TestCase
{
    Detector det = null;
    
    public SubdetectorTest(String testName)
    {
        super(testName);
    }
    
    public static Test suite()
    {
        return new TestSuite(SubdetectorTest.class);
    }
    
    protected void setUp() throws Exception
    {
        InputStream in = SubdetectorTest.class.getResourceAsStream("/org/lcsim/geometry/subdetector/subdetectors.xml");
        GeometryReader reader = new GeometryReader();
        det = reader.read(in);
    }
    
    public void testFoo()
    {}
}