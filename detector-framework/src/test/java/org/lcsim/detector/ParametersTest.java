package org.lcsim.detector;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ParametersTest extends TestCase
{
    private IParameters parameters = new Parameters("test");
    
    public ParametersTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(ParametersTest.class);
    }
    
    public void setUp() throws Exception
    {
        parameters.addStringParameter("string", "foo");
        parameters.addStringArrayParameter("stringArray", new String[] {"foo","bar"});
        
        parameters.addDoubleParameter("double", 1.1);
        parameters.addDoubleArrayParameter("doubleArray", new double[] {1.1,2.1});
        
        parameters.addIntegerParameter("integer", 1);
        parameters.addIntegerArrayParameter("integerArray", new int[] {1,2} );
        
        parameters.addBooleanParameter("boolean", true);
        parameters.addBooleanArrayParameter("booleanArray", new boolean[] {true,false});      
    }
    
    public void testParameterValues()
    {
        assertTrue(parameters.getStringParameter("string").equals("foo"));
        assertTrue(parameters.getStringArrayParameter("stringArray")[0].equals("foo"));
        assertTrue(parameters.getStringArrayParameter("stringArray")[1].equals("bar"));
        
        assertTrue(parameters.getDoubleParameter("double") == 1.1);
        assertTrue(parameters.getDoubleArrayParameter("doubleArray")[0] == 1.1);
        assertTrue(parameters.getDoubleArrayParameter("doubleArray")[1] == 2.1);
        
        assertTrue(parameters.getIntegerParameter("integer") == 1);
        assertTrue(parameters.getIntegerArrayParameter("integerArray")[0] == 1);
        assertTrue(parameters.getIntegerArrayParameter("integerArray")[1] == 2);
        
        assertTrue(parameters.getBooleanParameter("boolean") == true);
        assertTrue(parameters.getBooleanArrayParameter("booleanArray")[0] == true);
        assertTrue(parameters.getBooleanArrayParameter("booleanArray")[1] == false);
    }
}