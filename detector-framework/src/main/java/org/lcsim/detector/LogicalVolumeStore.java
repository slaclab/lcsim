package org.lcsim.detector;

public class LogicalVolumeStore  
extends ObjectStore<ILogicalVolume>
implements ILogicalVolumeStore
{
	private static ILogicalVolumeStore store;
	public static ILogicalVolumeStore getInstance()
	{
		if (store == null)
		{
			store = new LogicalVolumeStore();
		}
		return store;
	}
}