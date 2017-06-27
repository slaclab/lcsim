package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.geometry.Detector;
import org.lcsim.util.test.OneTimeDetectorSetup;

/**
 * Test that checks BoxDipole returns the correct B values for a test detector.  It 
 * also checks that several BoxDipoles overlay their values correctly.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: BoxDipoleTest.java,v 1.1 2011/06/24 22:17:13 jeremy Exp $
 */
public class BoxDipoleTest extends TestCase
{
    Detector det;
    
    // Test resource containing two BoxDipole fields.
    static final String detLoc = "/org/lcsim/geometry/field/BoxDipoleTest.xml";
    
    public static Test suite()
    {
        TestSuite ts = new TestSuite();
        ts.addTestSuite(BoxDipoleTest.class);
        return new OneTimeDetectorSetup(ts, detLoc);
    }

    protected void setUp() throws Exception
    {
        if (det == null)
            det = OneTimeDetectorSetup.getDetector();
    }
    
    public void testDipole()
    {
        // Points to test.
        List<Hep3Vector> testPoints = new ArrayList<Hep3Vector>();
        testPoints.add(new BasicHep3Vector(0,0,0));
        testPoints.add(new BasicHep3Vector(251,0,0));
        
        // Answer key.
        List<Hep3Vector> answerKey = new ArrayList<Hep3Vector>();
        answerKey.add(new BasicHep3Vector(2., 1., 0.5));
        answerKey.add(new BasicHep3Vector(2.1, 1.2, 0.8));
        
        // Loop over test points.
        for (int i=0, n=testPoints.size(); i<n; i++)
        {
            // Get the point to check.
            Hep3Vector point = testPoints.get(i);
            
            // Get the B-field values at this point.
            Hep3Vector bfield = det.getFieldMap().getField(point);
           
            // Check that B-field value matches key.
            assertEquals(bfield, answerKey.get(i));
        }
    }
}