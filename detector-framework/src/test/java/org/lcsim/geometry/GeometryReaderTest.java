package org.lcsim.geometry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.geometry.compact.Header;
import org.lcsim.geometry.compact.Segmentation;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.util.test.OneTimeDetectorSetup;

/**
 *  
 * Checks that basic dimensions, quantities and analysis objects
 * were created correctly from a compact XML file by the GeometryReader.
 * 
 * @author jeremym
 */
public class GeometryReaderTest extends TestCase
{
    static final String detName = "GeometryReaderTest";
    static final String detLoc = detName + ".xml";

    // Detector object for all test methods.
    Detector detector;
    
    static final int nDetectors = 10;
    static final int nConstants = 10;

    static final int nTrackerLayers = 5;
    static final int nTrackerEndcapLayers = 10;
    static final int nFields = 1;

    static final int nEMLayers = 30;
    static final double EMThick = 150.0;
    static final int nEMSlices = 5;

    static final double EMBarrelir = 1270.0;
    static final double EMBarreloz = 1840.0;
    static final double EMBarrelor = EMBarrelir + EMThick;

    static final double EMEndcapir = 200.0;
    static final double EMEndcapiz = 1680.0;
    static final double EMEndcapor = 1250.0;
    static final double EMEndcapoz = EMEndcapiz + EMThick;

    int nHADLayers = 34;
    int nMuonLayers = 32;

    double tol = 0.0001;
    
    public static Test suite()
    {
        TestSuite ts = new TestSuite();
        ts.addTestSuite(GeometryReaderTest.class);
        return new OneTimeDetectorSetup(ts, detLoc);
    }

    public GeometryReaderTest(String testName)
    {                
        super(testName);                       
    }
    
    protected void setUp()
    {
        if (detector == null)
            detector = OneTimeDetectorSetup.getDetector();
    }

    public void testHeader()
    {
        Header hdr = detector.getHeader();
        assertTrue (hdr.getDetectorName().equals("GeometryReaderTest"));
        //assert (hdr.getTitle().equals("GeometryReaderTest from sdjan03"));
        assertTrue (hdr.getComment().equals("Test of org.lcsim.geometry.GeometryReader"));
        //assert (hdr.getAuthor().equals("Jeremy McCormick"));
        //assert (hdr.getURL().equals("http://www.example.com"));
    }

    public void testCollSizes()
    {
        assertEquals(nDetectors, detector.getSubdetectors().size());
        assertEquals(nDetectors, detector.getReadouts().size());
        assertEquals(nConstants, detector.getConstants().size());
        assertEquals(nFields, detector.getFields().size());
    }

    public void testLayers()
    {
        assertEquals(detName, detector.getName());

        org.lcsim.geometry.Subdetector subdetector = null;
                
        for (String sn : detector.getSubdetectorNames())
        {
            subdetector = detector.getSubdetector(sn);
            assertTrue (sn != null);

            Layering layers = ((Layered) subdetector).getLayering();
            assertTrue (layers != null);

            // System.out.println("layer count: " + layers.getLayerCount() );

            int nLayers = -1;
            int nSlices = -1;

            if (sn.equals("BarrelVertex") || sn.equals("BarrelTracker") || sn.equals("EndcapVertex"))
            {
                nLayers = nTrackerLayers;
            }

            if (sn.equals("EndcapTracker"))
            {
                nLayers = nTrackerEndcapLayers;
            }

            if (sn.equals("EMBarrel") || sn.equals("EMEndcap"))
            {
                nLayers = nEMLayers;
                nSlices = nEMSlices;
            }

            if (sn.equals("HADBarrel") || sn.equals("HADEndcap"))
            {
                nLayers = nHADLayers;
            }

            if (sn.equals("MuonBarrel") || sn.equals("MuonEndcap"))
            {
                nLayers = nMuonLayers;
            }

            /* look at basic attributes if got known subdet */
            if (nLayers != -1)
            {
                boolean sensitive=false;
                
                /* has # of layers from above */
                assertEquals(layers.getLayerCount(), nLayers);

                for (int i = 0; i < nLayers; i++)
                {
                    /* layer exists at this idx */
                    assert (layers.getLayerStack().getLayer(i) != null);

                    /* has # of slices from above (if set) */
                    if (nSlices != -1)
                    {
                        assert (layers.getLayerStack().getLayer(i).getSlices().size() == nSlices);
                    }

                    /* has a valid set of slices at each layer */
                    boolean gotLayers = false;

                    for (LayerSlice slice : layers.getLayerStack().getLayer(i).getSlices())
                    {
                        assertTrue (slice != null);
                        assertTrue (slice.getMaterial() != null);
                        assertTrue (slice.getThickness() >= 0);
                        gotLayers = true;
                        if ( slice.isSensitive() )
                        {
                            sensitive=true;
                        }
                    }

                    /* got at least one layer */
                    assertTrue( gotLayers );
                }
                
                /* has Readout */
                //Readout readout = detector.getReadout( sn );                
                //if ( readout == null )
                //{
                //    System.out.println("!!!! readout is null - " + sn );
                //}
                //if ( sensitive )
                //{
                //    assertTrue ( readout != null );
                //}

                /* has IDDecoder */
                //IDDecoder dec = subdetector.getIDDecoder();
                //if ( sensitive )
                //{
                //    assertTrue(dec != null);
                //}

                /* if tracker, test for TrackerIDDecoder */
                // if ( subdetector.isTracker() )
                // {
                // TrackerIDDecoder tdec =
                // ((Tracker)subdetector).getTrackerIDDecoder();
                // assertTrue(tdec != null);
                // }
                /* test for Segmentation */
                if (subdetector.isCalorimeter())
                {
                    // CalorimeterIDDecoder cdec =
                    // ((Calorimeter)subdetector).getCalorimeterIDDecoder();

                    /* test for segmentation */
                    Segmentation seg = (Segmentation) subdetector.getIDDecoder();
                    // ((Segmentation)cdec);
                    assertTrue (seg != null);
                }
            }
        }
    }

    public void testAttributes()
    {
        for (String sn : detector.getSubdetectorNames())
        {
            Subdetector subdetector = detector.getSubdetector(sn);

            if (sn.equals("EMBarrel") || sn.equals("EMEndcap"))
            {
                double totThick = ((Layered) subdetector).getLayering().getLayerStack().getTotalThickness();

                /* test thickness */
                assertEquals(totThick, EMThick, tol);

                CylindricalSubdetector cyl = (CylindricalSubdetector) subdetector;
                assertTrue (cyl != null);

                /* test barrel attributes */
                if (sn.equals("EMBarrel"))
                {
                    assertEquals(cyl.getInnerRadius(), EMBarrelir, tol);
                    assertEquals(cyl.getZMax(), EMBarreloz, tol);
                    assertEquals(cyl.getZMin(), -EMBarreloz, tol);
                    assertEquals(cyl.getOuterRadius(), EMBarrelor, tol);
                }
                /* test endcap attributes */
                else if (sn.equals("EMEndcap"))
                {
                    assertEquals(cyl.getInnerRadius(), EMEndcapir, tol);
                    assertEquals(cyl.getZMin(), EMEndcapiz, tol);
                    assertEquals(cyl.getOuterRadius(), EMEndcapor, tol);
                    assertEquals(cyl.getZMax(), EMEndcapoz, tol);
                }

                /*
                 * test whether manually adding up slice thicknesses = total
                 * thickness
                 */
                double compThick = 0;
                int lnum = 0;
                int nlay = ((Layered) subdetector).getLayering().getLayerStack().getNumberOfLayers();
                for (int i = 0; i < nlay; i++)
                {
                    Layer l = ((Layered) subdetector).getLayering().getLayerStack().getLayer(i);

                    assertTrue (l != null);

                    for (LayerSlice sl : l.getSlices())
                    {
                        compThick += sl.getThickness();
                    }
                }

                assertTrue (compThick == totThick);
            }
        }
    }
}