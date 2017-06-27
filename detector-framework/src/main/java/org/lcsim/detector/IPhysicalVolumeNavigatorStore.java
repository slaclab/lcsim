package org.lcsim.detector;

import java.util.List;

public interface IPhysicalVolumeNavigatorStore 
extends IObjectStore<IPhysicalVolumeNavigator>
{
    public IPhysicalVolumeNavigator create(String name, IPhysicalVolume world);
    public IPhysicalVolumeNavigator createDefault(IPhysicalVolume topVolume);
    public IPhysicalVolumeNavigator get(IPhysicalVolume world);
    public IPhysicalVolumeNavigator get(String name);
    public List<IPhysicalVolumeNavigator> find(IPhysicalVolume world);
    public IPhysicalVolumeNavigator getDefaultNavigator();
    public void reset();
}
