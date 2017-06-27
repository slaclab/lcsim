package org.lcsim.detector;

import org.lcsim.detector.identifier.IIdentifierDictionaryManager;
import org.lcsim.detector.identifier.IdentifierDictionaryManager;
import org.lcsim.detector.material.IMaterialStore;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.ISolidStore;
import org.lcsim.detector.solids.SolidStore;

/**
 * 
 * This class provides access to the global stores of detector objects,
 * including materials, logical volumes, and physical volumes.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class DetectorStore implements IDetectorStore
{
	private static IDetectorStore store;
	
	public static IDetectorStore getInstance()
	{
		if ( store == null )
		{
			store = new DetectorStore();
		}
		return store;
	}

	public IDetectorElementStore getDetectorElementStore() 
	{
		return DetectorElementStore.getInstance();
	}

	public ILogicalVolumeStore getLogicalVolumeStore() 
	{
		return LogicalVolumeStore.getInstance();
	}

	public IMaterialStore getMaterialStore() 
	{
		return MaterialStore.getInstance();
	}

	public IPhysicalVolumeStore getPhysicalVolumeStore() 
	{
		return PhysicalVolumeStore.getInstance();
	}

	public ISolidStore getSolidStore() 
	{	
		return SolidStore.getInstance();
	}
    
    public IPhysicalVolumeNavigatorStore getPhysicalVolumeNavigatorStore()
    {
        return PhysicalVolumeNavigatorStore.getInstance();
    }

    public IIdentifierDictionaryManager getIdentifierDictionaryManager()
    {
        return IdentifierDictionaryManager.getInstance();
    }
    
    public IParametersStore getParametersStore()
    {
        return ParametersStore.getInstance();
    }
    
    public void clear()
    {
        getDetectorElementStore().clear();
        getLogicalVolumeStore().clear();
        getMaterialStore().clear();
        getPhysicalVolumeNavigatorStore().clear();
        getPhysicalVolumeStore().clear();
        getSolidStore().clear(); 
        getIdentifierDictionaryManager().clear();
    }

}
