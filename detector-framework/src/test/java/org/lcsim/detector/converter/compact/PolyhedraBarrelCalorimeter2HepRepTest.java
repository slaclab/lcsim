package org.lcsim.detector.converter.compact;

import java.io.InputStream;

import junit.framework.TestCase;

import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * 
 * Write a HepRep for the {@link org.lcsim.geometry.subdetector.PolyhedraBarrelCalorimeter2}
 * subdetector type.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: PolyhedraBarrelCalorimeter2HepRepTest.java,v 1.1 2010/05/03 18:01:40 jeremy Exp $
 */
public class PolyhedraBarrelCalorimeter2HepRepTest extends TestCase 
{   
    Detector detector;
    
    private static final String resource = 
        "/org/lcsim/geometry/subdetector/PolyhedraBarrelCalorimeter2Test.xml";
    
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
            DetectorElementToHepRepConverter.writeHepRep((new TestOutputFile("PolyhedraBarrelCalorimeter2Test.heprep")).getPath());
        }
        catch (Exception x)
        {
            throw new RuntimeException(x);
        }
    }
}
