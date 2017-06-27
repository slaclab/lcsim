/*
 * CylindricalBarrelCalorimeterTest.java
 *
 * Created on June 15, 2005, 12:00 PM
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
public class ScalingTest extends TestCase
{        
    public ScalingTest()
    {}
    
    protected void setUp() throws java.lang.Exception
    {
    	/*
        InputStream in = 
        	this.getClass().getResourceAsStream("/org/lcsim/geometry/subdetector/ScalingTest.xml");        
        GeometryReader reader = new GeometryReader();
        Detector det = reader.read(in);                  
        return;
        */
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(ScalingTest.class);
    }
    
    public void testScaling()
    {
    	//System.out.println("testScaling");
    }    
}