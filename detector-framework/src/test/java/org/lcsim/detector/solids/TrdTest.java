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
import org.lcsim.detector.RotationPassiveXYZ;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;
import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * Test that writes out the HepRep for a {@link org.lcsim.detector.solids.Trd}.
 *
 * @author Jeremy McCormick
 * @version $Id: TrdTest.java,v 1.6 2007/08/09 22:44:50 jeremy Exp $
 */

public class TrdTest 
extends TestCase
{	
	private static IMaterial dummymat = new MaterialElement("dummymat",1,1,1.0);
	IPhysicalVolumeNavigator nav;
	IPhysicalVolume world;
	
	public void testTrd() throws Exception
	{
		createGeometry();
		DetectorElementToHepRepConverter.writeHepRep(new TestOutputFile("TrdTest.heprep").getAbsolutePath());
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
		double x1=760.8757065043884/2;
		double x2=1272.6157921770439/2;
		double y1=5544.0/2;
		double y2=5544.0/2;
		double z=954.7200000000001/2;
		
		Trd trd = new Trd("trd",x1,x2,y1,y2,z);
		LogicalVolume lvTest = new LogicalVolume("lvtrd",trd,dummymat);
		new PhysicalVolume(
				new Transform3D(),
				"pvtrd",
				lvTest,
				mom.getLogicalVolume(),
				0);	
			
		double r=1500;
		int n = 8;
		
		for (int i=0; i<n; i++)
		{
			double phi=2*Math.PI*((double)i)/n;
			double zc=-phi-Math.PI/2;
			 
			double x=r*Math.cos(phi);
			double y=r*Math.sin(phi);
			ITranslation3D trans = new Translation3D(x,y,0);
			IRotation3D rotate = new RotationPassiveXYZ(Math.PI/2, 0, zc);
                        
			ITransform3D transform = new Transform3D(trans,rotate); 			
						
			String name="pvtrd_rot_"+i;
			new PhysicalVolume(
					transform,
					name,
					lvTest,
					mom.getLogicalVolume(),
					i
					);
			new DetectorElement("dummy_"+i,null,nav.getPath("/"+name));
		}
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
