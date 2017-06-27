package org.lcsim.geometry.field;

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jdom.JDOMException;

import org.lcsim.geometry.Detector;
import org.lcsim.geometry.FieldMap;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.GeometryReaderTest;
import org.lcsim.util.test.OneTimeDetectorSetup;

/**
 *
 * @author jeremym
 */
public class FieldOverlayTest extends FieldTest
{
    Detector det;
    static final String detLoc = "/org/lcsim/geometry/compact/sdjan03_compact.xml";
	
    /** Creates a new instance of FieldOverlayTest */
    public FieldOverlayTest(String name)
    {
        super(name);
    }
    
    public static Test suite()
    {
        TestSuite ts = new TestSuite();
        ts.addTestSuite(FieldOverlayTest.class);
        return new OneTimeDetectorSetup(ts, detLoc);
    }

    protected void setUp() throws Exception
    {
        if (det == null)
            det = OneTimeDetectorSetup.getDetector();
    }
    	
    public void testFieldOverlay() throws Exception
    {
    	FieldMap field = det.getFieldMap();
        testFieldAt(field,0,0,0,1,0,5);
    }   
    
    public void testEmptyOverlay()
    {
       FieldOverlay overlay = new FieldOverlay();
       testFieldAt(overlay,0,0,0,0,0,0);
    }
    
    public void testSingleOverlay() throws JDOMException
    {
       SolenoidTest test = new SolenoidTest("SolenoidTest");
       FieldOverlay overlay = new FieldOverlay();
       overlay.addField(test.createMap());
       test.checkMap(overlay);
    }
}