package org.lcsim.detector;

import org.lcsim.detector.material.IMaterialStore;
import org.lcsim.detector.solids.ISolidStore;

public interface IDetectorStore 
{
    public IPhysicalVolumeNavigatorStore getPhysicalVolumeNavigatorStore();
	public IPhysicalVolumeStore getPhysicalVolumeStore();
	public ILogicalVolumeStore getLogicalVolumeStore();
	public ISolidStore getSolidStore();
	public IMaterialStore getMaterialStore();
	public IDetectorElementStore getDetectorElementStore();
    public IParametersStore getParametersStore();
    public void clear();
}
