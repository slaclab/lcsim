package org.lcsim.detector;

public class PhysicalVolumeStore 
extends PhysicalVolumeContainer
implements IPhysicalVolumeStore
{
	private static IPhysicalVolumeStore store;
	public static IPhysicalVolumeStore getInstance()
	{
		if ( store == null )
		{
			store = new PhysicalVolumeStore();
		}
		return store;
	}
	public PhysicalVolumeStore()
	{
		// Disallow duplicate physical volumes to be 
		// added but globally non-unique name and
		// copyNum is okay.
		super(true,false,false);
	}
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        for (IPhysicalVolume pv : this)
        {
            buff.append(pv.getName() + '\n');
        }
        return buff.toString();
    }
}