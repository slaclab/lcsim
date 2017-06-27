package org.lcsim.geometry.subdetector;

import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * This class tests whether the {@link org.lcsim.subdetector.PolyhedraEndcapCalorimeter}
 * subdetector type loads without errors from a test compact description.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class PolyhedraEndcapCalorimeterTest extends TestCase
{
    Detector det;
    PolyhedraEndcapCalorimeter pbc;
    
    /** Creates a new instance of ForwardDetectorTest */
    public PolyhedraEndcapCalorimeterTest()
    {}
    
    protected void setUp() throws java.lang.Exception
    {                
        InputStream in = this.getClass().getResourceAsStream("/org/lcsim/geometry/subdetector/PolyhedraEndcapCalorimeterTest.xml");
        GeometryReader reader = new GeometryReader();
        det = reader.read(in);
        
        assertTrue( det.getSubdetectors().get("PolyhedraEndcapCalorimeter") != null );
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(PolyhedraEndcapCalorimeterTest.class);
    }
    
    public void testHepRep() throws Exception
    {
    	DetectorElementToHepRepConverter.writeHepRep(new TestOutputFile("PolyhedraEndcapCalorimeterTest.heprep").getAbsolutePath());
    }
}