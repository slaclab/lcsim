package org.lcsim.detector;

import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.solids.ISolid;

/**
 * Implementation of @see ILogicalVolume.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class LogicalVolume 
implements ILogicalVolume
{
	IMaterial material;
	ISolid solid;
	String name;
	
	// The same PhysicalVolume cannot be added twice.
	// The volumes must have unique names within their mother.
	// PhysicalVolume copy numbers are allowed to be duplicated.
	IPhysicalVolumeContainer physicalVolumes = new PhysicalVolumeContainer(true,true,false);
		
	public LogicalVolume(String name, ISolid solid, IMaterial material)
	{        
        if (name == null)
            throw new IllegalArgumentException("LogicalVolume name points to null!"); 
		this.name = name;
        if (solid == null)
            throw new IllegalArgumentException("Solid for LogicalVolume " + name + " points to null!");
		this.solid = solid;
        if (material == null)
            throw new IllegalArgumentException("Material for LogicalVolume " + name + " points to null!");
		this.material = material;		
		
		// Register with the store.
		LogicalVolumeStore.getInstance().add(this);
	}
	
	public String getName()
	{
		return name;
	}
	
	public IMaterial getMaterial()
	{
		return material;
	}

	public ISolid getSolid()
	{
		return solid;
	}
	
	public IPhysicalVolumeContainer getDaughters()
	{
		return physicalVolumes;
	}
	
	public void addDaughter(IPhysicalVolume physvol)
	{	
		physicalVolumes.add(physvol);				
	}
	
	public IPhysicalVolume getDaughter(String name)
	{
		IPhysicalVolume pvfnd=null;
		for (IPhysicalVolume pv : physicalVolumes)
		{
			if (pv.getName().equals(name))
			{
				pvfnd = pv;
				break;
			}
		}
		return pvfnd;			
	}
	
	public IPhysicalVolume getDaughter(int i)
	{
		return physicalVolumes.get(i);
	}
	
	public boolean isDaughter(IPhysicalVolume physvol)
	{
		return physicalVolumes.contains(physvol);
	}
	
	public int getNumberOfDaughters()
	{
		return physicalVolumes.size();
	}
	
	public boolean isAncestor(IPhysicalVolume physvol)
	{
		boolean isDaughter = isDaughter(physvol);
		if (!isDaughter)
		{
			for (IPhysicalVolume iphysvol : physicalVolumes)
			{
				isDaughter = iphysvol.getLogicalVolume().isAncestor(physvol);
				if (isDaughter) break;
			}
		}
		return isDaughter;
	}
}