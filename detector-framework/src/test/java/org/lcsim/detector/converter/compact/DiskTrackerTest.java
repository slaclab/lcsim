package org.lcsim.detector.converter.compact;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IDetectorElementContainer;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.IPhysicalVolumePath;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.Subdetector;

/**
 * 
 * Perform tests on the detailed geometry of an
 * 
 * @see org.lcsim.geometry.subdetector.DiskTracker
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * 
 */
public class DiskTrackerTest extends TestCase
{
    private Detector detector;

    public DiskTrackerTest(String name)
    {
        super(name);
    }

    public static junit.framework.Test suite()
    {
        return new TestSuite(DiskTrackerTest.class);
    }

    private static final String resource = "/org/lcsim/detector/converter/compact/DiskTrackerTest.xml";

    public void setUp()
    {
        InputStream in = this.getClass().getResourceAsStream(resource);

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

    public void testLayers()
    {
        Subdetector compact = detector.getSubdetector("DiskTracker");
        IDetectorElement subdet = compact.getDetectorElement();
        for (IDetectorElement endcap : subdet.getChildren())
        {
            // System.out.println("endcap: " + endcap.getName());
            IDetectorElementContainer layers = endcap.getChildren();
            // System.out.println("layers: " + layers.size());
            assertEquals("Expected number of layers is wrong.", layers.size(), 1);
            for (IDetectorElement layer : endcap.getChildren())
            {
                int sensorNumber = 0;
                for (IDetectorElement sensor : layer.getChildren())
                {
                    // System.out.println("sensor: " + sensor.getName());
                    IIdentifier id = sensor.getIdentifier();
                    IExpandedIdentifier expId = sensor.getIdentifierHelper().unpack(id);
                    // System.out.println(expId.toString());
                    assertEquals(sensor.getIdentifierHelper().getValue(sensor.getIdentifier(), "system"), compact
                            .getIDDecoder().getSystemNumber());
                    int barrel = sensor.getIdentifierHelper().getValue(sensor.getIdentifier(), "barrel");
                    assertTrue(barrel == 1 || barrel == 2);
                    assertTrue(sensor.getIdentifierHelper().getValue(sensor.getIdentifier(), "layer") == 0);
                    assertTrue(sensor.getIdentifierHelper().getValue(sensor.getIdentifier(), "slice") == sensorNumber);
                    ++sensorNumber;
                }
            }
        }
    }

    /**
     * Scan the geometry with some points along the z axis. Check that the points are within the correct geometric
     * volumes and DetectorElements.
     */
    public void testZPointScan()
    {
        IPhysicalVolumeNavigator navigator = detector.getNavigator();

        double[] zpoints = new double[]{12.1,11.1,10.1,0.,-10.1,-11.1,-12.1};

        String[] pathAnswerKey =
                new String[]{
                    "/tracking_region",
                    "/tracking_region/DiskTracker_layer0_pos/DiskTracker_layer0_slice1",
                    "/tracking_region/DiskTracker_layer0_pos/DiskTracker_layer0_slice0",
                    "/tracking_region",
                    "/tracking_region/DiskTracker_layer0_neg/DiskTracker_layer0_slice0",
                    "/tracking_region/DiskTracker_layer0_neg/DiskTracker_layer0_slice1",
                    "/tracking_region"
                };  
                
        String[] deAnswerKey =
                new String[]{
                    "DiskTrackerTest",
                    "DiskTracker_layer0_slice1_pos",
                    "DiskTracker_layer0_slice0_pos",
                    "DiskTrackerTest",
                    "DiskTracker_layer0_slice0_neg",
                    "DiskTracker_layer0_slice1_neg",
                    "DiskTrackerTest"
                };

        for (int i = 0; i < zpoints.length; i++ )
        {
            Hep3Vector zpoint = new BasicHep3Vector(0, 0, zpoints[i]);

            IPhysicalVolumePath path = navigator.getPath(zpoint);

            //System.out.println(path);
            //System.out.println("path: " + path);
            //System.out.println("pathAnswer: " + pathAnswerKey[i]);

            assertEquals("Wrong path <" + path.toString() + "> at z = " + zpoints[i] + ".  Expected <"
                    + pathAnswerKey[i] + ">.", path.toString(), pathAnswerKey[i]);

            IDetectorElement srch = detector.getDetectorElement().findDetectorElement(zpoint);

            //System.out.println(srch.getName());
            //System.out.println("deAnswer: " + deAnswerKey[i]);            

            assertTrue("Wrong DE <" + srch.getName() + "> at z = " + zpoints[i] + ".  Expected <" + deAnswerKey[i]+ ">.", srch.getName().equals(deAnswerKey[i]));
            
            //System.out.println("--");            
        }
    }
}
