package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;

import org.lcsim.event.LCRelation;

/**
 * Block handler for relation objects.
 *
 * @author Guilherme Lima
 * @version $Id: SIOLCRelationBlockHandler.java,v 1.4 2007/10/17 02:06:23 tonyj Exp $
 */
class SIOLCRelationBlockHandler extends AbstractBlockHandler
{
    public String getType() { return "LCRelation"; }
    public Class getClassForType() { return LCRelation.class; }
    LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection,
				      SIOInputStream in, int n, int version)
	throws IOException
    {
      for (int i = 0; i < n; i++)
	collection.add(new SIOLCRelation(in, collection.getFlags(), version));
      return null;
    }
      
    void writeCollectionElement(Object element, SIOOutputStream out, int flags)
	throws IOException
    {
	SIOLCRelation.write((LCRelation) element, out, flags);
    }
}
