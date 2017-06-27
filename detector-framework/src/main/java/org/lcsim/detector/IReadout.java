package org.lcsim.detector;

import java.util.List;

/**
 * The {@link IReadout} provides access to hit objects
 * from {@link IDetectorElement} objects.  Since there
 * is no class for hits within GeomConverter, this class 
 * provides access based on the hits' concrete class 
 * with the {{@link #getHits(Class)} method.
 * 
 * @author jeremym
 * @version $Id: IReadout.java,v 1.6 2010/04/14 17:52:32 jeremy Exp $
 */
public interface IReadout
{
	/**
	 * Get a list of hits matching type T. 
	 * @param  klass The class of the list to return.
	 * @return A {@link List} containing the hits or containing
	 *         nothing if no hits have been added.
	 */
	public <T> List<T> getHits(Class<T> klass);
	
	/**
	 * Add a hit.
	 * 
	 * @param hit The hit to add.
	 */
	public void addHit(Object hit);
	
	/**
	 * Clear the hits.
	 */
	public void clear();
}