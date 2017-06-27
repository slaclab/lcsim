package org.lcsim.detector;

import static org.lcsim.units.clhep.SystemOfUnits.m;
import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepPoint;
import hep.graphics.heprep.HepRepTreeID;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.graphics.heprep.HepRepWriter;
import hep.physics.vec.Hep3Vector;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;
import org.lcsim.detector.solids.Box;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * Test case that writes out a heprep file called
 * ShapeRotateTest.heprep which shows some
 * DetectorElements that have translation and
 * rotations.  Visually inspect to determine
 * correctness.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class ShapeRotateTest 
extends TestCase
{
    private static IMaterial dummymat = new MaterialElement("dummymat",1,1,1.0);
    private IPhysicalVolumeNavigator nav;

    List<Hep3Vector> points =
        new ArrayList<Hep3Vector>();

    public ShapeRotateTest(String name)
    {
        super(name);
    }

    public static junit.framework.Test suite()
    {
        return new TestSuite(ShapeRotateTest.class);
    }

    public void testShapeRotate()
    {        
        try {
            writeHepRep(new TestOutputFile("ShapeRotateTest.heprep").getAbsolutePath());
        }
        catch ( Throwable x )
        {
            throw new RuntimeException(x);
        }
    }

    public class TestDE
    extends DetectorElement
    {
        TestDE(String name, IDetectorElement parent, String path)
        {
            super(name, parent, path);
        }        
    }
    
    public void setUp()
    {
        IPhysicalVolume world = createWorld();
        nav = new PhysicalVolumeNavigator("default",world);
        
        Box box1 = new Box("box1",50,25,50);
        ILogicalVolume lv1 = new LogicalVolume("test1",box1,dummymat);
        
        Box box2 = new Box("box2", 10,10,10);
        ILogicalVolume lv2 = new LogicalVolume("test2",box2,dummymat);
        ITranslation3D t1 = new Translation3D(25,0,0);
        IRotation3D r1 = new RotationPassiveXYZ(0, 0, Math.PI/8);
        
        IPhysicalVolume pv1 = 
            new PhysicalVolume(
                    new Transform3D(t1,r1),
                    "box2",
                    lv2,
                    lv1,
                    0
                    );
        
        double transX = 0;
        double transIncr = 100;
        
        double rotZ = 0;
        double rotIncr = Math.PI/2;
        
        for (int i=0; i<5; i++)
        {
            IRotation3D thisRot = new RotationPassiveXYZ(0, 0, rotZ);
            
            ITranslation3D thisTrans = new Translation3D(transX,0,0);
            
            IPhysicalVolume pv =
                new PhysicalVolume(
                        new Transform3D(thisTrans, thisRot),
                        "pv" + i,
                        lv1,
                        world.getLogicalVolume(),
                        i);
            
            rotZ += rotIncr;
            transX += transIncr;
            
            new TestDE("dau"+i,null,"/pv"+i+"/box2");
            new TestDE("par"+i,null,"/pv"+i);
        }
        
        /*
        for (IDetectorElement de : DetectorElementStore.getInstance())
        {
            System.out.println(de.getName());
            if ( de.hasGeometryInfo())
            {
                System.out.println("     " + de.getGeometry().getPhysicalVolumePath());
            }
        }*/
    }
  
    public final IPhysicalVolume createWorld()
    {       
        Box boxWorld = new Box(
                "world_box",
                10.0*m,
                10.0*m,
                10.0*m);

        LogicalVolume lvWorld = 
            new LogicalVolume(
                    "world",
                    boxWorld,
                    dummymat);

        IPhysicalVolume pvTop = 
            new PhysicalVolume(
                    null,
                    "world",
                    lvWorld,
                    null,
                    0);

        return pvTop;
    }      

    public final static String HITS_LAYER = "Hits";
    public final static String PARTICLES_LAYER = "Particles";
   
    private void writeHepRep(String filepath) throws Exception
    {
        HepRepFactory factory = HepRepFactory.create();
        HepRep root = factory.createHepRep();        

        // detector
        HepRepTreeID treeID = factory.createHepRepTreeID("DetectorType", "1.0");
        HepRepTypeTree typeTree = factory.createHepRepTypeTree(treeID);
        root.addTypeTree(typeTree);

        HepRepInstanceTree instanceTree = factory.createHepRepInstanceTree("Detector", "1.0", typeTree);
        root.addInstanceTree(instanceTree);

        String detectorLayer = "Detector";
        root.addLayer(detectorLayer);

        HepRepType barrel = factory.createHepRepType(typeTree, "Barrel");
        barrel.addAttValue("layer", detectorLayer);
        HepRepType endcap = factory.createHepRepType(typeTree, "Endcap");
        endcap.addAttValue("layer", detectorLayer);

        //DetectorElementToHepRepConverter cnv = 
        //    new DetectorElementToHepRepConverter();

        for (IDetectorElement de : DetectorElementStore.getInstance())
        {
            DetectorElementToHepRepConverter.convert(de, factory, root, -1, false, null);
        }
        // end detector

        root.addLayer(PARTICLES_LAYER);
        root.addLayer(HITS_LAYER);
        root.addLayer("axis");

        treeID = factory.createHepRepTreeID("EventType", "1.0");
        typeTree = factory.createHepRepTypeTree(treeID);
        root.addTypeTree(typeTree);
        instanceTree = factory.createHepRepInstanceTree("Event", "1.0", typeTree);
        root.addInstanceTree(instanceTree);  

        // axis
        HepRepType axis = factory.createHepRepType(typeTree, "axis");
        axis.addAttValue("drawAs","Line");
        axis.addAttValue("layer", "axis");

        HepRepType xaxis = factory.createHepRepType(axis, "xaxis");
        xaxis.addAttValue("color",Color.RED);
        xaxis.addAttValue("fill",true);
        xaxis.addAttValue("fillColor",Color.RED);
        HepRepInstance x = factory.createHepRepInstance(instanceTree, xaxis);
        factory.createHepRepPoint(x,0,0,0);
        factory.createHepRepPoint(x,1000,0,0);

        HepRepType yaxis = factory.createHepRepType(axis, "yaxis");
        yaxis.addAttValue("color",Color.GREEN);
        yaxis.addAttValue("fill",true);
        yaxis.addAttValue("fillColor",Color.GREEN);
        HepRepInstance y = factory.createHepRepInstance(instanceTree, yaxis);
        factory.createHepRepPoint(y,0,0,0);
        factory.createHepRepPoint(y,0,1000,0);

        HepRepType zaxis = factory.createHepRepType(axis, "zaxis");
        zaxis.addAttValue("color",Color.BLUE);
        zaxis.addAttValue("fill",true);
        zaxis.addAttValue("fillColor",Color.BLUE);
        HepRepInstance z = factory.createHepRepInstance(instanceTree, zaxis);
        factory.createHepRepPoint(z,0,0,0);
        factory.createHepRepPoint(z,0,0,1000);
        // done axis                              

        // points
        HepRepType typeX = factory.createHepRepType(typeTree, "points");
        typeX.addAttValue("layer",HITS_LAYER);
        typeX.addAttValue("drawAs","Point");
        typeX.addAttValue("color",Color.GREEN);
        typeX.addAttValue("fill",true);
        typeX.addAttValue("fillColor",Color.GREEN);
        typeX.addAttValue("MarkName","Box");
 
        for (Hep3Vector p : points )
        {
            HepRepInstance instanceX = factory.createHepRepInstance(instanceTree, typeX);
            HepRepPoint pp = factory.createHepRepPoint(instanceX,p.x(),p.y(),p.z());
        }        
        // done points

        HepRepWriter writer = 
            HepRepFactory.create().createHepRepWriter(new FileOutputStream(filepath),false,false);
        writer.write(root,"test");
        writer.close();
    }      
}
