package org.lcsim.detector;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of @see IReadout.
 * @see IReadout
 */
public class Readout
implements IReadout
{
	List hits = new ArrayList();

	/**
	 * Get all hits of Class <code>klass</code>.
	 * @return A new typed List containing matching hits.
	 */
	public <T> List<T> getHits(Class<T> klass)
    {   
    	List<T> matches = new ArrayList<T>();
    	for (Object hit : hits)
    	{
    		if (klass.isAssignableFrom(hit.getClass()))
    		{
    			matches.add((T)hit);
    		}
    	    
    	}
    	return matches;
    }
    
    public void addHit(Object hit)
    {
    	hits.add(hit);
    }

    public void clear()
    {
    	hits.clear(); 
    }
}