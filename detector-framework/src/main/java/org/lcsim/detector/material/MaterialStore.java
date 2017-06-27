package org.lcsim.detector.material;

import java.util.ArrayList;

public class MaterialStore
extends ArrayList<IMaterial>
implements IMaterialStore 
{
	private static MaterialStore materialStore = null;
	public static final IMaterialStore getInstance()
	{
		if ( materialStore == null )
		{
			materialStore = new MaterialStore();
		}
		return materialStore;
	}
	
	public IMaterial get(String name)
	{
		for (IMaterial material : this)
		{
			if (material.getName().equals(name))
			{
				return material;
			}
		}
		return null;
	}
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        for (IMaterial material : this)
        {
            buff.append(material.toString());
            buff.append('\n');
        }
        return buff.toString();
    }    
}