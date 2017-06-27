package org.lcsim.detector.material;

import org.lcsim.detector.IObjectStore;

/**
 * 
 * This is an interface to the global materials store.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: IMaterialStore.java,v 1.3 2010/04/14 18:24:53 jeremy Exp $
 */
public interface IMaterialStore 
extends IObjectStore<IMaterial>
{
	public IMaterial get(String name);
}
