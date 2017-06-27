package org.lcsim.util.test;

import java.io.InputStream;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;

/**
 * Setup a Detector once for a set of JUnit TestCases.
 * @author jeremym
 */
public class OneTimeDetectorSetup extends TestSetup
{
    static Detector detector = null;
    String detLoc = null;
    
    public OneTimeDetectorSetup(Test test, String detLoc)
    {
        super(test);
        this.detLoc = detLoc;
    }
    
    public void setUp()
    {
        //System.out.println("OneTimeDetectorSetup.setUp()");
        try
        {
            InputStream in = GeometryReader.class.getResourceAsStream(detLoc);
            GeometryReader reader = new GeometryReader();
            detector = reader.read(in);
        }
        catch (Exception e)
        {
            throw new RuntimeException("GeometryReaderTest.setUp() - GeometryReader failed.", e);
        }        
    }
    
    public static Detector getDetector()
    {
        return detector;
    }      
}