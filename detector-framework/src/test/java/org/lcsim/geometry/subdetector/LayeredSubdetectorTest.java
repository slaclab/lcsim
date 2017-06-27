package org.lcsim.geometry.subdetector;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.JDOMException;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.util.test.OneTimeDetectorSetup;
import org.lcsim.util.xml.ElementFactory.ElementCreationException;

/**
 * 
 * @author jeremym
 */
public class LayeredSubdetectorTest extends TestCase
{
    Detector det;
    static final String detLoc = "/org/lcsim/geometry/subdetector/LayeredSubdetectorTest.xml";

    public LayeredSubdetectorTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        TestSuite ts = new TestSuite();
        ts.addTestSuite(LayeredSubdetectorTest.class);
        return new OneTimeDetectorSetup(ts, detLoc);
    }

    protected void setUp() throws Exception
    {
        if (det == null)
            det = OneTimeDetectorSetup.getDetector();
    }

    public void testRead() throws IOException, JDOMException, ElementCreationException
    {
        assertTrue(det.getSubdetectors().size() == 4);
    }

    public void testCalorimeterBarrel()
    {
        CylindricalBarrelCalorimeter calBarr = (CylindricalBarrelCalorimeter)det.getSubdetectors().get("CalorimeterBarrel");
        assertTrue(calBarr != null);
        Layering layering = calBarr.getLayering();
        assertTrue(layering != null);
        assertTrue(layering.getOffset() == 100.0);
    }
}