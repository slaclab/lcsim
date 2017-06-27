package org.lcsim.detector;

import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.IMaterial.State;
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.ISolid;
import org.lcsim.detector.solids.Tube;

public interface IDetectorFactory 
{
    public IRotation3D createRotation3D();
	
    public ITransform3D createTransform3D();
	
    public IDetectorElement createDetectorElement(
			String name,
			IDetectorElement parent,
			IPhysicalVolumePath support,
			IIdentifier id);
	
    public ILogicalVolume createLogicalVolume(
			String name, 
			ISolid solid, 
			IMaterial material);	
	
    public IPhysicalVolume createPhysicalVolume(
			ITransform3D transform,
			String name,
			ILogicalVolume logicalVolume,
			ILogicalVolume motherLogicalVolume,
			int copyNum);		
	
	public IPhysicalVolumeNavigator createPhysicalVolumeNavigator(IPhysicalVolume world);
    
    public IPhysicalVolumeNavigator createPhysicalVolumeNavigator(
            String name,
            IPhysicalVolume worldVolume);    
	
    public IMaterial createMaterialElement(
			String name,
			double Z,
			double A,
			double density, 
            State state,
            double temperature,
            double pressure);
		
    public IMaterial createMaterialMixture(
	  		String name,
            int nComponents,
            double density,
            State state);
	
    public Box createBox(
			String name, 
			double xHalfLength, 
			double yHalfLength, 
			double zHalfLength);
	
    public Tube createTube(
			String name, 
			double innerRadius, 
			double outerRadius, 
			double zHalfLength);	
    
    public IReadout createReadout();    
}