package org.lcsim.detector;

import java.util.List;

public interface IPhysicalVolumePath 
extends List<IPhysicalVolume> 
{
	public IPhysicalVolume getTopVolume();
	public IPhysicalVolume getLeafVolume();	
	public boolean isEmpty();
	public boolean equalsPrefix(IPhysicalVolumePath path);
	public boolean equals(IPhysicalVolumePath path);
}