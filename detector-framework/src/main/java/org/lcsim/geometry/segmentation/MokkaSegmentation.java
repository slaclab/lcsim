package org.lcsim.geometry.segmentation;

import org.jdom.Element;
import org.jdom.DataConversionException;

import org.lcsim.geometry.util.IDDescriptor;
    
/**
 * @author jeremym
 *
 */
public class MokkaSegmentation extends SegmentationBase
{
    public MokkaSegmentation(Element node) throws DataConversionException
    {
        super(node);
    }

    public void setIDDescription(IDDescriptor id)
    {
        super.setIDDescription(id);
    }

    public boolean supportsNeighbours()
    {
        return false;
    }

    public long[] getNeighbourIDs(int layerRange, int zRange, int phiRange)
    {
        if (true)
            throw new RuntimeException("MokkaSegmentation does not support neighbors.");
        return null;
    }

    public long findCellContainingXYZ(double x, double y, double z)
    {
        throw new RuntimeException(".MokkaSegmentation does not support findCellContainingXYZ.");
    }
    
    public String[] getSegmentationFieldNames() {
        throw new RuntimeException("I dunno.");
    }
}