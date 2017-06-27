package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;

/**
 * Implementation of @see IPhysicalVolume.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class PhysicalVolume implements IPhysicalVolume
{
	ITransform3D transform;
	ILogicalVolume logicalVolume;
	ILogicalVolume motherLogicalVolume;
	int copyNum;
	boolean sensitive=false;
	String name;
    
	public PhysicalVolume(
			ITransform3D transform,
			String name,
			ILogicalVolume logicalVolume,
			ILogicalVolume motherLogicalVolume,
			int copyNum)
	{
		this.name = name;
		
		if (transform != null)
		{
			this.transform = transform;
		}
		else {
			this.transform = new Transform3D();
		}
		
		this.logicalVolume = logicalVolume;
		this.motherLogicalVolume = motherLogicalVolume;		
		this.copyNum = copyNum;
		
		// Add to mother.
		if ( motherLogicalVolume != null )
		{
			motherLogicalVolume.addDaughter(this);
		}
		
		// Add to store.
		PhysicalVolumeStore.getInstance().add(this);
	}			
	
	public String getName()
	{
		return name;
	}
	
	public IRotation3D getRotation()
	{
		return transform.getRotation();
	}
	
	public int getCopyNumber()
	{
		return copyNum;
	}
	
	public ILogicalVolume getMotherLogicalVolume()
	{
		return motherLogicalVolume;
	}	
	
	public ILogicalVolume getLogicalVolume()
	{
		return logicalVolume;
	}
	
	public ITransform3D getTransform()
	{
		return transform;
	}
	
	public Hep3Vector getTranslation()
	{
		return transform.getTranslation();
	}
	
	public Hep3Vector transformParentToLocal(Hep3Vector point)
	{
		return getTransform().transformed(point);
	}
    
    public void setSensitive(boolean sensitive)
    {
        this.sensitive = sensitive;
    }
    
    public boolean isSensitive()
    {
        return this.sensitive;
    }
}