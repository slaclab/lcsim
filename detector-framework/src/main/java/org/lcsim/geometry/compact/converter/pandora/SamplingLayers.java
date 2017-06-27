package org.lcsim.geometry.compact.converter.pandora;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of SamplingLayerRange objects to represent the sampling for a
 * subdetector.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * 
 */
//FIXME: Put into separate package from Pandora converter.
public class SamplingLayers extends ArrayList<SamplingLayerRange>
{
    public SamplingLayers()
    {
    }

    public SamplingLayers(SamplingLayerRange range)
    {
        this.add(range);
    }

    public SamplingLayers(List<SamplingLayerRange> ranges)
    {
        this.addAll(ranges);
    }

    public SamplingLayerRange getSamplingLayerRange(int layern)
    {
        for (SamplingLayerRange range : this)
        {
            if (range.inRange(layern))
                return range;
        }
        return null;
    }
}