package org.lcsim.detector;

import org.lcsim.detector.identifier.IIdentifier;

/**
 * 
 * This class is the global store of {@link org.lcsim.detector.IDetectorElement}
 * objects and can be accessed as a singleton.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class DetectorElementStore 
extends DetectorElementContainer
implements IDetectorElementStore
{
	private static DetectorElementStore store = null;

	//DetectorElementMap cache = new DetectorElementMap();
	DetectorElementIdentifierHash cache = new DetectorElementIdentifierHash();	
    
	public static IDetectorElementStore getInstance()
	{
		if ( store == null )
		{
			store = new DetectorElementStore();
		}
		return store;
	}
    
    public boolean add(IDetectorElement de)
    {   
    	super.add(de);    	
    	cache.put(de);    	    
    	return true;
    }

    public IDetectorElementContainer find(IIdentifier id)
    {  
    	return cache.get(id);
    }      
    
    public void clear()
    {
        cache.clear();
        super.clear();
    }
}
