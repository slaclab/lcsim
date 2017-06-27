package org.lcsim.detector.tracker.silicon;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;

/**
 * 
 * DetectorElement for an SiTrackerBarrel module.
 *
 */ 
// TODO: Add getThickness() method.
// TODO: Subclasses for box and trd modules.
public class SiTrackerModule extends DetectorElement
{
    private int moduleId;
    
    public SiTrackerModule(String name,
            IDetectorElement parent,
            String path,
            int moduleId) 
    {
        super(name, parent, path);
        this.moduleId = moduleId;
    }
    
    public int getModuleId()
    {
        return moduleId;
    }
    
    public boolean isDoubleSided()
    {
        return this.findDescendants(SiSensor.class).size() == 2;
    }             
}