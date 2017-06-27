package org.lcsim.geometry.compact;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.util.test.OneTimeDetectorSetup;

/**
 *
 * @author tonyj
 */
public class CompactReaderTest extends TestCase
{
    Detector det = null;
    static final String detLoc = "/org/lcsim/geometry/compact/sdjan03_compact.xml";
    
    public CompactReaderTest(String testName)
    {
        super(testName);
    }
    
    public static Test suite()
    {
        TestSuite ts = new TestSuite();
        ts.addTestSuite(CompactReaderTest.class);
        return new OneTimeDetectorSetup(ts, detLoc);
    }
    
    protected void setUp()
    {
        if (det == null)
            det = OneTimeDetectorSetup.getDetector();
    }
       
    /**
     * Make sure count of subdetectors is correct. 
     */
    public void testDetectorCount()
    {
        assertEquals(12, det.getSubdetectors().size());
    }
    
    /** 
     * Test that limits were created correctly. 
     */
    public void testLimits()
    {
        LimitSet limitset = det.getLimitSet("MyLimits");        
        assertTrue(limitset != null);               
        
        Limit limit = limitset.getLimit("step_length_max");
        assertTrue(limit != null);
        
        assertEquals(limit.getValue(), 1.0);
        assertEquals(limit.getUnit(), "mm");
        assertEquals(limit.getParticles(), "*");
    }
    
    /** 
     * Test that a region was created correctly. 
     */
    public void testRegions()
    {
        Region region = det.getRegion("MyRegion");
        assertTrue(region != null);
    }
    
    /** 
     * Make sure that multiple subdetectors on same readout return the correct readout. 
     */
    public void testMultiSubdetectorReadout()
    {
        assertTrue( det.getSubdetector("HADBarrel").getReadout().getName().compareTo("HcalBarrHits") == 0 );
        assertTrue( det.getSubdetector("HADBarrel2").getReadout().getName().compareTo("HcalBarrHits") == 0 );
    }
}