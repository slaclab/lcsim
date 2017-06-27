package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;

import java.io.IOException;

import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 * @author tonyj
 */
class SIOSimTrackerHitBlockHandler extends AbstractBlockHandler
{
    public String getType()
    {
        return "SimTrackerHit";
    }

    public Class getClassForType()
    {
        return SimTrackerHit.class;
    }

    LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version)
            throws IOException
    {
        LCMetaData meta = event.getMetaData(collection);
        for (int i = 0; i < n; i++)
            collection.add(new SIOSimTrackerHit(in, collection.getFlags(), version, meta));
        return null;
    }

    void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
    {
        SIOSimTrackerHit.write((SimTrackerHit) element, out, flags);
    }
}
