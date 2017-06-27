package org.lcsim.detector.tracker.silicon;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;

/**
 * DetectorElement for an SiTrackerBarrel layer.
 */ 
public class SiTrackerLayer 
extends DetectorElement
{
    private int layerNumber;       
    public SiTrackerLayer(
            String name,
            IDetectorElement parent,
            String path,
            int layerNumber)
    {
        super(name,parent,path);
        this.layerNumber = layerNumber;
    }   
    
    public int getLayerNumber()
    {
        return layerNumber;
    }
}