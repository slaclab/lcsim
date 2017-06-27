package org.lcsim.detector.converter.compact;

import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IDetectorElementContainer;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.Subdetector;

/**
 * 
 * Perform tests on the detailed geometry of an 
 * 
 * @see org.lcsim.geometry.subdetector.MultiLayerTracker 
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class MultiLayerTrackerTest
extends TestCase
{
    private Detector detector;
    
    public MultiLayerTrackerTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(MultiLayerTrackerTest.class);
    }
    
    private static final String resource = 
        "/org/lcsim/detector/converter/compact/MultiLayerTrackerTest.xml";
    
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

    public void testLayers()
    {
        Subdetector compact = detector.getSubdetector("MultiLayerTracker");
        IDetectorElement subdet = compact.getDetectorElement();
        IDetectorElementContainer layers = subdet.getChildren();
        assertEquals("Expected number of sensors is wrong!",layers.size(),2);
        IDetectorElement layer = layers.get(0);
        try {
        	assertEquals(layer.getIdentifierHelper().getValue(layer.getIdentifier(), "system"),compact.getIDDecoder().getSystemNumber());
        	assertEquals(layer.getIdentifierHelper().getValue(layer.getIdentifier(), "barrel"),0);
        	assertTrue(layers.get(0).getIdentifierHelper().getValue(layers.get(0).getIdentifier(), "layer") == 0);
        	assertTrue(layers.get(1).getIdentifierHelper().getValue(layers.get(1).getIdentifier(), "layer") == 1);
        }
        catch (Exception x)
        {
        	throw new RuntimeException(x);
        }
    }
}
