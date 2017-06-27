package org.lcsim.job;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.conditions.ConditionsManager;
import org.lcsim.geometry.Detector;
import org.lcsim.util.Driver;

/**
 * This Driver checks that a list of required conditions are present.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: ConditionsCheckDriver.java,v 1.1 2012/05/17 00:14:05 jeremy Exp $
 */
public class ConditionsCheckDriver extends Driver {
    
    List<String> requiredConditions = new ArrayList<String>();
    List<String> detectors = new ArrayList<String>();
    boolean checkConditions = true;
    boolean checkDetector = true;
    
    ConditionsCheckDriver() {}
    
    public void detectorChanged(Detector detector) {
        if (checkDetector) {
            if (detectors.size() > 0) {
                if (!detectors.contains(detector.getDetectorName())) {
                    throw new RuntimeException("The detector " + detector.getDetectorName() + " is not in the list of valid detectors.");
                }
            }
        }
        if (checkConditions) {
            ConditionsManager mgr = ConditionsManager.defaultInstance();
            for (String condition : requiredConditions) {
                try {
                    mgr.getRawConditions(condition);
                } catch (ConditionsManager.ConditionsSetNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    public void setCondition(String c) {
        requiredConditions.add(c);
    }
    
    public void setDetector(String d) {
        detectors.add(d);
    }
    
    public void setCheckConditions(boolean b) {
        this.checkConditions = b;
    }
    
    public void setCheckDetector(boolean b) {
        this.checkDetector = b;
    }
}
