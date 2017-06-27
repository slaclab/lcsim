package org.lcsim.detector.converter.compact;

import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * COMMENT
 *
 * @author Jeremy McCormick
 * @version $Id: SiTrackerEndcapConverterTest.java,v 1.4 2008/05/22 07:54:09 jeremy Exp $
 */

public class SiTrackerEndcapConverterTest 
extends TestCase
{
    private Detector detector;

    public SiTrackerEndcapConverterTest(String name)
    {
        super(name);
    }

    public static junit.framework.Test suite()
    {
        return new TestSuite(SiTrackerEndcapConverterTest.class);
    }

    // Location in testResources dir.
    private static final String resource = 
        "/org/lcsim/detector/converter/compact/SiTrackerEndcapTest.xml";

    public void setUp()
    {
        InputStream in = 
            this.getClass().
            getResourceAsStream(resource);

        GeometryReader reader = new GeometryReader();

        try {
            detector = reader.read(in);
        }
        catch ( Throwable x )
        {
            throw new RuntimeException(x);
        }
    }    
    
    public void testHepRep()
    {
        try {
        	DetectorElementToHepRepConverter.writeHepRep(new TestOutputFile("SiTrackerEndcapTest.heprep").getAbsolutePath());
        }
        catch ( Exception x )
        {
            throw new RuntimeException( x );
        }
    }        
}
