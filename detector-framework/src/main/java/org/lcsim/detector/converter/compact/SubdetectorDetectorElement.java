package org.lcsim.detector.converter.compact;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;

/**
 * A DetectorElement that is a subdetector.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */

public class SubdetectorDetectorElement extends DetectorElement {
            
    protected SubdetectorDetectorElement(String name, IDetectorElement parent) {
        super(name, parent);
    }
    
    public int getSystemID() {
        return getIdentifierHelper().getValue(getIdentifier(), "system");
    }
}