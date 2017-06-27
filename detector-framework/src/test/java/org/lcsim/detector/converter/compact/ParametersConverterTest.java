package org.lcsim.detector.converter.compact;

import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.IParameters;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;

/**
 * Tests {@link ParametersConverter} by reading the resource 
 * ParametersConverterTest.xml and checking the converted values
 * for correctness.
 * 
 * @author Jeremy McCormick
 * @version $Id: ParametersConverterTest.java,v 1.1 2007/05/22 20:02:07 jeremy Exp $
 */
public class ParametersConverterTest
extends TestCase
{
    private Detector detector;
    
	public ParametersConverterTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(ParametersConverterTest.class);
    }
    
    private static final String resource = "/org/lcsim/detector/converter/compact/ParametersConverterTest.xml";    
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
    
    public void testParameters() throws Exception
    {
        IParameters param = detector.getSubdetector("test").getDetectorElement().getParameters();
        assertTrue( param != null );
        
        assertEquals(param.getIntegerParameter("int"), 1);
        int[] intArray = param.getIntegerArrayParameter("intArray");
        assertEquals(intArray[0], 1);
        assertEquals(intArray[1], 2);
        assertEquals(intArray[2], 3);
        assertEquals(intArray[3], 4);
        
        assertEquals(param.getDoubleParameter("double"), 1.0);
        double[] doubleArray = param.getDoubleArrayParameter("doubleArray");
        assertEquals(doubleArray[0], 1.1);
        assertEquals(doubleArray[1], 2.2);
        assertEquals(doubleArray[2], 3.3);
        assertEquals(doubleArray[3], 4.4);
        
        assertEquals(param.getBooleanParameter("boolean"), true);
        boolean[] booleanArray = param.getBooleanArrayParameter("booleanArray");
        assertEquals(booleanArray[0], true);
        assertEquals(booleanArray[1], false);
        
        assertEquals(param.getStringParameter("string"), "foo");
        String[] stringArray = param.getStringArrayParameter("stringArray");
        assertEquals(stringArray[0], "foo");
        assertEquals(stringArray[1], "bar");
        assertEquals(stringArray[2], "baz");        
    }    
}