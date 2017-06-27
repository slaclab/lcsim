/*
 * HitIdentifier.java
 *
 * Created on July 1, 2008, 11:33 AM
 *
 */

package org.lcsim.fit.helicaltrack;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 *
 * @author Richard Partridge
 * @version 1.0
 */
public class HitIdentifier {
    private List<String> _special;
    
    /** Creates a new instance of HitIdentifier */
    public HitIdentifier() {
        _special = new ArrayList<String>();
    }
    
    public HitIdentifier(List<String> special) {
        this();
        _special.addAll(special);
    }
    
    public String Identifier(IDetectorElement de) {
        String detname = getName(de);
        BarrelEndcapFlag beflag = getBarrelEndcapFlag(de);
        int layer = getLayer(de);
        return Identifier(detname, layer, beflag);
    }

    public String Identifier(SimTrackerHit hit) {
        String detname = getName(hit);
        BarrelEndcapFlag beflag = getBarrelEndcapFlag(hit);
        int layer = getLayer(hit);
        return Identifier(detname, layer, beflag);
    }

    public static String Identifier(String detname, int layer, BarrelEndcapFlag beflag) {
        String identifier = detname + layer + beflag;
        return identifier;
    }
    
    public String getName(IDetectorElement de) {
        //  Find the first level down from the top of the de tree
        while (de.getParent().getParent() != null) de = de.getParent();
        //  Find the name of this detector
        String detname = de.getName();
        return detname;
    }

    public String getName(SimTrackerHit hit) {
        return hit.getSubdetector().getName();
    }

    public int getLayer(IDetectorElement de) {
        int layer = -1;
        IIdentifierHelper hlp = de.getIdentifierHelper();
        if (hlp instanceof DetectorIdentifierHelper) {
            DetectorIdentifierHelper dehlp = (DetectorIdentifierHelper) hlp;
            //  Get the identifier
            IIdentifier id = de.getIdentifier();
            //  Get the layer number
            layer = dehlp.getLayerValue(id);
            //  See if it needs "special" treatment
            for (String name : _special) {
                if (getName(de).equals(name)) {
                    //  Special layers have stereo layers with distinct layer numbers
                    //  on the two stereo sensor planes, whereas the seedtracker
                    //  convention is that both planes have the same layer number
                    layer /= 2;
                    break;
                }
            }
        }
        return layer;
    }

    public int getLayer(SimTrackerHit hit) {
        return hit.getLayer();
    }

    public BarrelEndcapFlag getBarrelEndcapFlag(IDetectorElement de) {
        BarrelEndcapFlag beflag = BarrelEndcapFlag.UNKNOWN;
        //  Find the second level down from the top of the de tree
        while (de.getParent().getParent().getParent() != null) de = de.getParent();
        //  Get the DetectorIdentifierHelper
        IIdentifierHelper hlp = de.getIdentifierHelper();
        if (hlp instanceof DetectorIdentifierHelper) {
            DetectorIdentifierHelper dehlp = (DetectorIdentifierHelper) hlp;
            //  Get the identifier
            IIdentifier id = de.getIdentifier();
            //  Get the BarrelEndcapFlag
            if (dehlp.isBarrel(id)) beflag = BarrelEndcapFlag.BARREL;
            else if (dehlp.isEndcapPositive(id)) beflag = BarrelEndcapFlag.ENDCAP_NORTH;
            else if (dehlp.isEndcapNegative(id)) beflag = BarrelEndcapFlag.ENDCAP_SOUTH;
        }
        return beflag;
    }

    public BarrelEndcapFlag getBarrelEndcapFlag(SimTrackerHit hit) {
        return hit.getBarrelEndcapFlag();
    }

    public void setNonStandardLayering(String name) {
        _special.add(name);
        return;
    }
    
    public static List<String> getSpecialLayers(String detectorName) {
        
        List<String> returnMe = new ArrayList<String>(); 
        if (detectorName.equals("sid02")) {
            returnMe.add("TrackerEndcap"); 
        } else if (detectorName.equals("sid01")) {
            returnMe.add("TrackerForward");
            returnMe.add("TrackerEndcap"); 
        }
        
        return returnMe; 
    }
    
}