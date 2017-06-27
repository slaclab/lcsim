package org.lcsim.geometry.compact;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.lcsim.geometry.util.BaseIDDecoder;

/**
 * 
 * @author tonyj
 */
public class Segmentation extends BaseIDDecoder
{
    protected List<Double> cellSizes = new ArrayList<Double>(2);
    boolean useForHitPosition = true;

    protected Segmentation(Element segmentation)
    {
        super();

        // Flag if hit position is to be kept independent of cell position.
        useForHitPosition = Boolean.parseBoolean(segmentation.getAttribute("useForHitPosition").getValue());
    }

    public boolean useForHitPosition()
    {
        return this.useForHitPosition;
    }

    public double getCellSizeU()
    {
        return this.cellSizes.get(0);
    }

    public double getCellSizeV()
    {
        return this.cellSizes.get(1);
    }
    
    public String[] getSegmentationFieldNames() {
        return new String[]{};
    }
}