package org.lcsim.detector.solids;

public abstract class AbstractSolid
implements ISolid
{
	String name;
	
	public AbstractSolid(String name)
	{
		this.name = name;
		SolidStore.getInstance().add(this);
	}
	
	public String getName()
	{
		return name;
	}
}