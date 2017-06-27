package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;

import org.lcsim.event.RawCalorimeterHit;

/**
 * Block handler for raw calorimeter hits.
 *
 * @author Guilherme Lima
 * @version $Id: SIORawCalorimeterHitBlockHandler.java,v 1.4 2007/10/17 02:06:23 tonyj Exp $
 */
class SIORawCalorimeterHitBlockHandler extends AbstractBlockHandler
{
    public String getType() { return "RawCalorimeterHit"; }
    public Class getClassForType() { return RawCalorimeterHit.class; }
    LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection,
				      SIOInputStream in, int n, int version)
	throws IOException
    {
      for (int i = 0; i < n; i++)
	collection.add(new SIORawCalorimeterHit(in, collection.getFlags(), version));
      return null;
    }
      
    void writeCollectionElement(Object element, SIOOutputStream out, int flags)
	throws IOException
    {
	SIORawCalorimeterHit.write((RawCalorimeterHit) element, out, flags);
    }
}
