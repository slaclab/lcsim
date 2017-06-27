/*
 * ForwardDetectorTest.java
 *
 * Created on June 16, 2005, 1:50 PM
 */

package org.lcsim.geometry.subdetector;

import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 *
 * @author jeremym
 */
public class PolyhedraBarrelCalorimeterTest extends TestCase
{
    Detector det;
    PolyhedraBarrelCalorimeter pbc;
    
    /** Creates a new instance of ForwardDetectorTest */
    public PolyhedraBarrelCalorimeterTest()
    {}
    
    protected void setUp() throws java.lang.Exception
    {                
        InputStream in = this.getClass().getResourceAsStream("/org/lcsim/geometry/subdetector/PolyhedraBarrelCalorimeterTest.xml");
        GeometryReader reader = new GeometryReader();
        det = reader.read(in);
        
        assertTrue( det.getSubdetectors().get("PolyhedraBarrelCalorimeterSubdet") != null );
        
        try
        {
            pbc = (PolyhedraBarrelCalorimeter) det.getSubdetectors().get("PolyhedraBarrelCalorimeterSubdet");
        }
        catch ( ClassCastException cce )
        {
            throw new RuntimeException("Failed cast to PolyhedraBarrelCalorimeter.");
        }
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(PolyhedraBarrelCalorimeterTest.class);
    }
    
    public void test_PolyhedraBarrelCalorimeter() throws Exception
    {
    	DetectorElementToHepRepConverter.writeHepRep(new TestOutputFile("PolyhedraBarrelCalorimeterTest.heprep").getAbsolutePath());
    }
}