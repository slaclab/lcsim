package org.lcsim.detector.converter.compact;

import static org.lcsim.units.clhep.SystemOfUnits.cm;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IGeometryInfo;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.PhysicalVolumeNavigatorStore;
import org.lcsim.detector.solids.Inside;
import org.lcsim.detector.solids.Tube;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;

public class DetectorConverterTest
extends TestCase
{
    private Detector detector;
    
	public DetectorConverterTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(DetectorConverterTest.class);
    }
    
    private static final String resource = "/org/lcsim/detector/converter/compact/DetectorConverterTest.xml";    
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
    
    public void testGeometry() throws Exception
    {
        IPhysicalVolumeNavigator nav = 
            PhysicalVolumeNavigatorStore.getInstance().getDefaultNavigator();
        IPhysicalVolume world = nav.getTopPhysicalVolume();
       
        // Check that the world volume is there with the correct name.
        assertTrue("Top volume is not the world!", world.getName().equals("world"));
        
        // Check that the world volume does not have a mother volume.
        assertTrue("World volume has a mother!", world.getMotherLogicalVolume() == null);
        
        IDetectorElement deDetector = detector.getDetectorElement();
        
        String des[] = new String[]{
                "ecal_barrel",
                "ecal_endcap",
                "tracker"};

        // Check for top-level DEs that should have been built.
        for ( String de : des ) 
        {
            IDetectorElement search = deDetector.getChildren().find(de).get(0);
            
            // Check that the DE was created.
            assertTrue("The expected DetectorElement <" + de + "> is missing!", search != null);
            
            // Check that the DeDetector is findable from this node.
            List<DeDetector> detectorSearch = search.findAncestors(DeDetector.class);
            assertTrue( detectorSearch.size() == 1);
            assertTrue( detectorSearch.get(0).getName().equals("test_detector"));
        }                
    }
    
    /**
     * Read in a Detector and test the detailed geometry that is created.
     * 
     * @throws Exception
     */
    public void testIsInside() throws Exception
    {   
        /*
        System.out.println("dumping pv store ...");
        for (IPhysicalVolume pv : PhysicalVolumeStore.getInstance())
        {
            System.out.println(pv.getName());
        }
        System.out.println();
        
        System.out.println("dumping de store ...");
        for (IDetectorElement de : DetectorElementStore.getInstance())
        {
            System.out.println(de.getName());
            for (IDetectorElement child : de.getChildren())
            {
                System.out.println("    "+child.getName());
            }
        }
        System.out.println(); 
        */       
        
        List<Hep3Vector> points = new ArrayList<Hep3Vector>();
        //points.add(new BasicHep3Vector(0,105*cm,0));        
        //points.add(new BasicHep3Vector(0,115*cm,0));
        points.add(new BasicHep3Vector(0,0,255*cm));
        //points.add(new BasicHep3Vector(0,0,265*cm));
        points.add(new BasicHep3Vector(0,0,-255.0*cm));
        //points.add(new BasicHep3Vector(0,0,-265*cm));
                
        IPhysicalVolumeNavigator nav = 
            PhysicalVolumeNavigatorStore.getInstance().getDefaultNavigator();
        
        //IPhysicalVolumePath ecn = nav.getPath("/ecal_endcap_negative");
        //System.out.println("ecn="+ecn.getTopVolume().getName());
        //System.out.println("ecn trans="+ecn.getTopVolume().getTransform());
        
        for ( IDetectorElement child : detector.getDetectorElement().getChildren() ) 
        {
            for ( IDetectorElement sensor : child.getChildren() )
            {
                IGeometryInfo sensorGeo = sensor.getGeometry();
                                
                Tube sensorTube = (Tube)sensorGeo.getLogicalVolume().getSolid();
                
                double zsensor = sensorGeo.getPosition().z();
                double zwidth = sensorTube.getZHalfLength();
                                
                for ( Hep3Vector point : points )
                {                                       
                    double zpoint = point.z();
                    double ypoint = point.y();
                    
                    // Check isInside for barrel.
                    if ( sensor.getName().contains("barrel") )
                    {
                        if ( ypoint > sensorTube.getInnerRadius() &&
                             ypoint < sensorTube.getOuterRadius() )
                        {                            
                            assertEquals(sensorGeo.inside(point),Inside.INSIDE);
                        }
                    }                    
                    // Check isInside for endcap positive.
                    else if ( child.getName().contains("endcap_positive") )
                    {   
                        /*
                        System.out.println("endcap_positive");
                        System.out.println("point="+point);
                        System.out.println("zpoint="+zpoint);
                        System.out.println("zsensor="+zsensor);
                        System.out.println("zcheck1="+(zsensor - zwidth));
                        System.out.println("zcheck2="+(zsensor + zwidth));
                        System.out.println("zpoint > (zsensor - zwidth ) = " + (zpoint > (zsensor - zwidth )));
                        System.out.println("zpoint < (zsensor + zwidth ) = " + (zpoint < (zsensor + zwidth )));
                        */
                        
                        //assertTrue(sensor.getGeometry().isInside(point));
                        
                        if ( zpoint > (zsensor - zwidth ) &&
                             zpoint < (zsensor + zwidth ) )
                        {
                            assertEquals(sensorGeo.inside(point),Inside.INSIDE);
                        }                        
                    }    
                    // Check isInside for endcap negative.
                    else if ( child.getName().contains("ecal_endcap_negative") )
                    {
                        //System.out.println("point = " + point);
                        //System.out.println("point path = " + nav.getPath(point));
                        //System.out.println("endcap_negative");
                        if ( zpoint < (zsensor + zwidth ) &&
                             zpoint > (zsensor - zwidth ) )
                        {
                            sensorGeo.getDetectorElement();
                            sensorGeo.getDetectorElement().getParent();
                            sensorGeo.getDetectorElement().getParent().getName();
                            
                            //System.out.println("check " + sensor.getName() + " @ point = " + point);
                            
                            assertEquals(sensorGeo.inside(point),Inside.INSIDE);
                        }
                    }
                }
            }
        }
    }
}