package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import java.io.IOException;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 * Block handler for calorimeter hits.
 * 
 * @author Guilherme Lima
 * @version $Id: SIOCalorimeterHitBlockHandler.java,v 1.2 2005/08/02 17:18:06
 *          tonyj Exp $
 */
class SIOCalorimeterHitBlockHandler extends AbstractBlockHandler
{
    public String getType()
    {
        return "CalorimeterHit";
    }

    public Class getClassForType()
    {
        return CalorimeterHit.class;
    }

    LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version)
            throws IOException
    {
        LCMetaData meta = event.getMetaData(collection);
        for (int i = 0; i < n; i++)
            collection.add(new SIOCalorimeterHit(in, collection.getFlags(), version, meta));
        return null;
    }

    void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
    {
        SIOCalorimeterHit.write((CalorimeterHit) element, out, flags);
    }
}
