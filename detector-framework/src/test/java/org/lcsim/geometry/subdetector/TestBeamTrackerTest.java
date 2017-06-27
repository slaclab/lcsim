/*
 * CylindricalBarrelCalorimeterTest.java
 *
 * Created on June 15, 2005, 12:00 PM
 */

package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRepProvider;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.JDOMException;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.util.xml.ElementFactory.ElementCreationException;

import org.lcsim.geometry.layer.Layering;

/**
 *
 * @author jeremym
 */
public class TestBeamTrackerTest extends TestCase
{
    Detector detector;
    
    /** Creates a new instance of CylindricalBarrelCalorimeterTest */
    public TestBeamTrackerTest(String name)
    {
        super(name);
    }
    
    protected void setUp() throws java.lang.Exception
    {
        InputStream in = this.getClass().getResourceAsStream("/org/lcsim/geometry/subdetector/TestBeamTrackerTest.xml");
        GeometryReader reader = new GeometryReader();
        Detector det = reader.read(in);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(TestBeamTrackerTest.class);
    }
    
    public void testDummy()
    {
        return;
    }    
}
