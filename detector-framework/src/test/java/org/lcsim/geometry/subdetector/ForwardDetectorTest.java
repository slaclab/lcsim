/*
 * ForwardDetectorTest.java
 *
 * Created on June 16, 2005, 1:50 PM
 */

package org.lcsim.geometry.subdetector;

import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.compact.Detector;

/**
 *
 * @author jeremym
 */
public class ForwardDetectorTest extends TestCase
{
    Detector det;
    ForwardDetector fdet;
    
    /** Creates a new instance of ForwardDetectorTest */
    public ForwardDetectorTest()
    {}
    
    protected void setUp() throws java.lang.Exception
    {
        InputStream in = this.getClass().getResourceAsStream("/org/lcsim/geometry/subdetector/ForwardDetectorTest.xml");
        GeometryReader reader = new GeometryReader();
        det = reader.read(in);
        
        assertTrue( det.getSubdetectors().get("LuminosityMonitor") != null );
        
        try
        {
            fdet = (ForwardDetector) det.getSubdetectors().get("LuminosityMonitor");
        }
        catch ( ClassCastException cce )
        {
            throw new RuntimeException("Failed cast to ForwardDetector.");
        }
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(ForwardDetectorTest.class);
    }
    
    public void test_dimensions()
    {
        double zmax = fdet.getZMax();
        double zmin = fdet.getZMin();
        double orad = fdet.getOuterRadius();
        double irad = fdet.getInnerRadius();  
                                
        assertEquals(zmin, 2000.0);
        assertEquals(zmax, zmin + fdet.getLayering().getLayers().getTotalThickness() );
        assertEquals(orad, 800.0);
        assertEquals(irad, 0.0);
    }
    
    public void test_beampipe()
    {
        double crossing_angle = fdet.getCrossingAngle();
        double incoming_r = fdet.getIncomingRadius();
        double outgoing_r = fdet.getOutgoingRadius();
        
        assertEquals(crossing_angle, 0.2);
        assertEquals(incoming_r, 10.0);
        assertEquals(outgoing_r, 20.0);
    }
}