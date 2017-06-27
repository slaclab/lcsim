package org.lcsim.detector.solids;

import static org.lcsim.units.clhep.SystemOfUnits.m;
import junit.framework.TestCase;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.PhysicalVolumeNavigatorStore;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * Writes out the HepRep for a {@link org.lcsim.detector.solids.RightRegularPolyhedron} to
 * be viewed in Wired.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: RightRegularPolyhedronHepRepTest.java,v 1.1 2010/04/12 17:31:31 jeremy Exp $
 */

public class RightRegularPolyhedronHepRepTest extends TestCase
{
    private static IMaterial dummymat = new MaterialElement("dummymat",1,1,1.0);
    IPhysicalVolumeNavigator nav;
    IPhysicalVolume world;
    
    public void testPoly() throws Exception
    {
        createGeometry();
        DetectorElementToHepRepConverter.writeHepRep(new TestOutputFile("RightRegularPolyhedronTest.heprep").getAbsolutePath());
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
        RightRegularPolyhedron poly = 
            new RightRegularPolyhedron("TestPolyhedron", 8, 500, 1000, -1000, 1000);

        LogicalVolume lvTest = new LogicalVolume("lvpoly", poly, dummymat);
        new PhysicalVolume(
                new Transform3D(),
                "pvpoly",
                lvTest,
                mom.getLogicalVolume(),
                0);
        new DetectorElement("depoly",null,"/pvpoly");
    }
    
    private IPhysicalVolume createWorld()
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
