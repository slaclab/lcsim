package org.lcsim.detector;

import java.util.List;

public interface IPhysicalVolumeContainer 
extends List<IPhysicalVolume>
{
	public PhysicalVolumeContainer findByName(String name);
	public PhysicalVolumeContainer findByCopyNum(int copyNum);
}
