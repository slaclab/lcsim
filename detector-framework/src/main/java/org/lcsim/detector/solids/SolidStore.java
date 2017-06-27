package org.lcsim.detector.solids;

import org.lcsim.detector.ObjectStore;

public class SolidStore 
extends ObjectStore<ISolid>
implements ISolidStore
{
	private static ISolidStore store;
	public static ISolidStore getInstance()
	{
		if ( store == null )
		{
			store = new SolidStore();
		}
		return store;
	}
}