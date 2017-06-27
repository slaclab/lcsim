package org.lcsim.detector.converter;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * 
 * A generic interface for converting from JDOM/XML 
 * to runtime objects.  
 * 
 * The activate method does not return a specific type.  It
 * can create one or more Java objects, but these must be
 * registered with a data store to be accessed later.  Most
 * of the important types in org.lcsim.detector, such as ISolid, 
 * are automatically registered with a user-accessible data store.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public interface XMLConverter 
{
	public void convert(Element element) throws JDOMException;
}