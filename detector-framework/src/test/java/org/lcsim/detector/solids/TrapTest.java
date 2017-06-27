package org.lcsim.detector.solids;

import static org.lcsim.units.clhep.SystemOfUnits.m;
import junit.framework.TestCase;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.ITranslation3D;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.PhysicalVolumeNavigatorStore;
import org.lcsim.detector.RotationGeant;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * Test that writes out the HepRep for a {@link org.lcsim.detector.solids.Trap} solid.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: TrapTest.java,v 1.3 2008/03/26 22:02:27 tknelson Exp $
 */

public class TrapTest
        extends TestCase
{
    private static IMaterial dummymat = new MaterialElement("dummymat",1,1,1.0);
    IPhysicalVolumeNavigator nav;
    IPhysicalVolume world;
    
    public void testTrd() throws Exception
    {
        createGeometry();
        DetectorElementToHepRepConverter.writeHepRep(new TestOutputFile("TrapTest.heprep").getAbsolutePath());
    }
    
    public IPhysicalVolume createGeometry()
    {
        world = createWorld();
        nav = PhysicalVolumeNavigatorStore.getInstance().createDefault(world);
        createSolids(world);
        return world;
    }
    
    public final void createSolids(IPhysicalVolume mom)
    {
//		double dx1 = 30;
//		double dx2 = 40;
//		double dy1 = 40;
//		double dx3 = 10;
//		double dx4 = 14;
//		double dy2 = 16;
//		double dz = 60;
//		double theta = Math.toRadians(20);
//		double phi = Math.toRadians(5);
//		double alph1 = Math.toRadians(10);
//		double alph2 = alph1;
        
        double dx1 = 100;
        double dx2 = 100;
        double dy1 = 10;
        double dx3 = 50;
        double dx4 = 50;
        double dy2 = 10;
        double dz = 200;
        double theta = Math.toRadians(-15);
        double phi = Math.toRadians(0);
        double alph1 = Math.toRadians(0);
        double alph2 = alph1;
        
        
        // Unrotated
        IRotation3D rotation = new RotationGeant(0,0,0);
        ITranslation3D translation = new Translation3D(0,0,0);
        ITransform3D transform = new Transform3D(translation,rotation);
        
        Trap trap = new Trap("trap",dz,theta,phi,dy1,dx1,dx2,alph1,dy2,dx3,dx4,alph2);
        LogicalVolume lvTest = new LogicalVolume("lvtrap",trap,dummymat);
        new PhysicalVolume(
                transform,
                "pvtrap",
                lvTest,
                mom.getLogicalVolume(),
                0);
        new DetectorElement("detrap",null,"/pvtrap");
        
        
        // X rotation
        rotation = new RotationGeant(Math.PI/2,0,0);
        translation = new Translation3D(200,0,0);
        transform = new Transform3D(translation,rotation);
        
        new PhysicalVolume(
                transform,
                "pvtrap_x",
                lvTest,
                mom.getLogicalVolume(),
                0);
        new DetectorElement("detrap_x",null,"/pvtrap_x");
        
        // X and Y rotations
        rotation = new RotationGeant(Math.PI/2,Math.PI/2,0);
        translation = new Translation3D(400,0,0);
        transform = new Transform3D(translation,rotation);
        
        new PhysicalVolume(
                transform,
                "pvtrap_xy",
                lvTest,
                mom.getLogicalVolume(),
                0);
        new DetectorElement("detrap_xy",null,"/pvtrap_xy");
        
        // X, Y, Z rotations
        rotation = new RotationGeant(Math.PI/2,Math.PI/2,Math.PI/2);
        translation = new Translation3D(600,0,0);
        transform = new Transform3D(translation,rotation);
        
        new PhysicalVolume(
                transform,
                "pvtrap_xyz",
                lvTest,
                mom.getLogicalVolume(),
                0);
        new DetectorElement("detrap_xyz",null,"/pvtrap_xyz");

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
}
