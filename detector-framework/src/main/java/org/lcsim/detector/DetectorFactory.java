package org.lcsim.detector;

import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;
import org.lcsim.detector.material.MaterialMixture;
import org.lcsim.detector.material.IMaterial.State;
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.ISolid;
import org.lcsim.detector.solids.Tube;

public class DetectorFactory 
implements IDetectorFactory 
{
	private static DetectorFactory factory = null;
	
	public static DetectorFactory getInstance()
	{
		if (factory == null)
			factory = new DetectorFactory();
		return factory;
	}
	
	public Box createBox(String name, double xHalfLength, double yHalfLength,
			double zHalfLength) 
	{
		return new Box(
				name, 
				xHalfLength, 
				yHalfLength, 
				zHalfLength);
	}

	public ITransform3D createTransform3D() 
	{
		return new Transform3D();
	}

	public IDetectorElement createDetectorElement(
			String name,
			IDetectorElement parent, 
			IPhysicalVolumePath support, 
			IIdentifier id) {
		return new DetectorElement(
				name, 
				parent, 
				support, 
				id);
	}

	public ILogicalVolume createLogicalVolume(
			String name, 
			ISolid solid,
			IMaterial material) 
	{
		return new LogicalVolume(name, solid, material);
	}

    public IMaterial createMaterialElement(
            String name,
            double Z,
            double A,
            double density, 
            State state,
            double temperature,
            double pressure)
	{
		return new MaterialElement(
				name, 
				Z, 
				A, 
				density,  
				state,
                temperature,
                pressure);
	}

	public IMaterial createMaterialMixture(
			String name, 
			int nComponents,
			double density, 
			State state) 
	{
		return new MaterialMixture(
				name,
				nComponents,
				density,
				state);
	}

	public IPhysicalVolume createPhysicalVolume(
			ITransform3D transform, 
			String name,
			ILogicalVolume logicalVolume, 
			ILogicalVolume motherLogicalVolume,
			int copyNum) 
	{
		return new PhysicalVolume(
				transform,
				name,
				logicalVolume,
				motherLogicalVolume,
				copyNum);
	}

	public IPhysicalVolumeNavigator createPhysicalVolumeNavigator(
            String name,
			IPhysicalVolume worldVolume) 
	{
		return new PhysicalVolumeNavigator(name, worldVolume);
	}
    
    public IPhysicalVolumeNavigator createPhysicalVolumeNavigator(IPhysicalVolume world)
    {
        return PhysicalVolumeNavigatorStore.getInstance().get(world);
    }

	public IRotation3D createRotation3D() 
	{
		return new Rotation3D();
	}

	public Tube createTube(
			String name, 
			double innerRadius, 
			double outerRadius,
			double zHalfLength) 
	{
		return new Tube(
				name,
				innerRadius,
				outerRadius,
				zHalfLength);
	}
    
    public IReadout createReadout()
    {
        return new Readout();
    }    
}