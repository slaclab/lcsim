package org.lcsim.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PhysicalVolumeContainer 
extends ArrayList<IPhysicalVolume> 
implements IPhysicalVolumeContainer
{	
	public boolean uniqPhysVol=false;
	public boolean uniqName=false;
	public boolean uniqCopyNum=false;
    
    public class PhysicalVolumeContainerException extends Exception
    {
        public PhysicalVolumeContainerException(String mesg)
        {
            super(mesg);
        }
    }
    
    public class DuplicatePhysicalVolumeException extends PhysicalVolumeContainerException
    {
        public DuplicatePhysicalVolumeException(IPhysicalVolume pv)
        {
            super("The PhysicalVolume " + pv + " is already in this container!");
        }
    }
    
    public class DuplicateNameException extends PhysicalVolumeContainerException
    {
        public DuplicateNameException(IPhysicalVolume pv)
        {
            super("This container already has a PhysicalVolume called <" + pv.getName() + ">!");
        }
    }
    
    public class DuplicateCopyNumberException extends PhysicalVolumeContainerException
    {
        public DuplicateCopyNumberException(IPhysicalVolume pv)
        {
            super("This container already has a PhysicalVolume with copy number <" + pv.getCopyNumber() + ">!");
        }
    }
    	
	public PhysicalVolumeContainer()
	{}
	
        private HashMap<String,List<IPhysicalVolume>> map = new HashMap<String,List<IPhysicalVolume>>(); 
        
	public PhysicalVolumeContainer(
            boolean uniqPhysVol, 
            boolean uniqName, 
            boolean uniqCopyNum)
	{
		this.uniqPhysVol = uniqPhysVol;
		this.uniqName = uniqName;
		this.uniqCopyNum= uniqCopyNum;
	}
	
        @Override
	public boolean add(IPhysicalVolume physvol) 
	{
        try {
            checkAdd(physvol);
            
            String name = physvol.getName();
            
            List<IPhysicalVolume> l; 
            if (map.containsKey(name)) {
                l = map.get(name);
            }
            
            else {
                l = new ArrayList<IPhysicalVolume>(); 
            }
            l.add(physvol);
            map.put(name, l);
            super.add(physvol);
        }
        catch ( PhysicalVolumeContainerException x )
        {
            throw new RuntimeException(x);
        }
		return true;
	}
	
	private void checkAdd(IPhysicalVolume physvol) throws PhysicalVolumeContainerException
	{	
		if ( uniqPhysVol && contains(physvol))
		{
            throw new DuplicatePhysicalVolumeException(physvol);
		}		
		else if (uniqName && findByName(physvol.getName()).size() != 0)
		{
		    throw new DuplicateNameException(physvol);
		}
		else if (uniqCopyNum && findByCopyNum(physvol.getCopyNumber()).size() != 0)
		{
		    throw new DuplicateCopyNumberException(physvol);
		}
	}
	
	public PhysicalVolumeContainer findByName(String name)
	{
		PhysicalVolumeContainer physvols = new PhysicalVolumeContainer();
//		for (IPhysicalVolume physvol : this)
//		{
//			if (physvol.getName().equals(name))
//			{
//				physvols.add(physvol);
//			}
//		}
                if (map.containsKey(name)) 
                    physvols.addAll(map.get(name)); 
		return physvols;
	}
	
	public PhysicalVolumeContainer findByCopyNum(int copyNum)
	{
		PhysicalVolumeContainer physvols = new PhysicalVolumeContainer();
		for (IPhysicalVolume physvol : this)
		{
			if (physvol.getCopyNumber() == copyNum)
			{
				physvols.add(physvol);
			}
		}
		return physvols;
	}
}