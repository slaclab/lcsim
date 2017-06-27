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
public class TPCTest extends TestCase
{
    Detector det;
    TPC tpc;
    
    /** Creates a new instance of ForwardDetectorTest */
    public TPCTest()
    {}
    
    protected void setUp() throws java.lang.Exception
    {
        InputStream in = this.getClass().getResourceAsStream("/org/lcsim/geometry/subdetector/TPCTest.xml");
        GeometryReader reader = new GeometryReader();
        det = reader.read(in);
        
        assertTrue( det.getSubdetectors().get("TPCTest") != null );
        
        try
        {
            tpc = (TPC) det.getSubdetectors().get("TPCTest");
        }
        catch ( ClassCastException cce )
        {
            throw new RuntimeException("Failed cast to TPC.");
        }
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(TPCTest.class);
    }
    
    public void test_TPC()
    {
        double zmax = tpc.getZMax();
        double zmin = tpc.getZMin();
        double orad = tpc.getOuterRadius();
        double irad = tpc.getInnerRadius();
        
        /* dimensions */
        assertEquals(zmax, 1500.0);
        assertEquals(zmin, -1500.0);
        assertEquals(irad, 1000.0);
        assertEquals(orad, 1100.0);

        /* layering */
        assertTrue(tpc.getLayering() != null);
        assertEquals(tpc.getLayering().getLayerCount(), 10);
        assertEquals(tpc.getLayering().getLayerStack().getLayer(0).getThickness(), 10.0);
        assertEquals(tpc.getLayering().getDistanceToLayerSensorMid(0), irad + 5.0);
    }
}