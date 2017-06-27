package org.lcsim.detector.converter.compact;

import java.io.InputStream;

import junit.framework.TestCase;

import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.util.test.TestUtil.TestOutputFile;

public class PolyhedraEndcapCalorimeter2Test extends TestCase 
{   
    Detector detector;
    
    private static final String resource = 
        "/org/lcsim/detector/converter/compact/PolyhedraEndcapCalorimeter2Test.xml";
    
    public void setUp()
    {
        InputStream in = 
            this.getClass().
            getResourceAsStream(resource);
        
        GeometryReader reader = new GeometryReader();
        
        try 
        {
            detector = reader.read(in);
        }
        catch (Throwable x)
        {
            throw new RuntimeException(x);
        }               
    }
    
    public void testWriteHepRep()
    {
        try 
        {
            DetectorElementToHepRepConverter.writeHepRep((new TestOutputFile("PolyhedraEndcapCalorimeter2Test.heprep")).getPath());
        }
        catch (Exception x)
        {
            throw new RuntimeException(x);
        }
    }
}
