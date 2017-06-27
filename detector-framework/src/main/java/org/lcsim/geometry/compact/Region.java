package org.lcsim.geometry.compact;

import org.jdom.Element;
import org.jdom.DataConversionException;

/**
 * 
 * @author jeremym
 */
public class Region {

    private String name;
    private boolean storeSecondaries = false;
    private boolean killTracks = false;
    private double rangeCut;
    private String lunit;
    private double energyThreshold;
    private String eunit;

    protected Region(Element node) {
        name = node.getAttributeValue("name");

        try {
            if (node.getAttribute("storeSecondaries") != null) {
                storeSecondaries = node.getAttribute("storeSecondaries").getBooleanValue();
            }
        } catch (DataConversionException dce) {
            throw new RuntimeException("Problem converting " + node.getAttributeValue("storeSecondaries") + " to boolean", dce);
        }
        
        try {
            if (node.getAttribute("killTracks") != null) {
                killTracks = node.getAttribute("killTracks").getBooleanValue();
            }
        } catch (DataConversionException dce) {
            throw new RuntimeException("Problem converting to boolean: " + node.getAttributeValue("killTracks"));
        }

        try {
            if (node.getAttribute("cut") != null) {
                rangeCut = node.getAttribute("cut").getDoubleValue();
            } else {
                rangeCut = 1.0;
            }
        } catch (DataConversionException dce) {
            throw new RuntimeException("Problem converting " + node.getAttributeValue("cut") + " to double", dce);
        }

        if (node.getAttribute("lunit") != null) {
            lunit = node.getAttributeValue("lunit");
        } else {
            lunit = "mm";
        }

        try {
            if (node.getAttribute("threshold") != null) {
                energyThreshold = node.getAttribute("threshold").getDoubleValue();
            } else {
                energyThreshold = 0.0;
            }
        } catch (DataConversionException dce) {
            throw new RuntimeException("Problem converting " + node.getAttributeValue("threshold") + " to double", dce);
        }

        if (node.getAttribute("eunit") != null) {
            eunit = node.getAttributeValue("eunit");
        } else {
            eunit = "MeV";
        }
    }

    public String getName() {
        return name;
    }

    public boolean getStoreSecondaries() {
        return storeSecondaries;
    }
    
    public boolean getKillTracks() {
        return killTracks;
    }

    public double getRangeCut() {
        return rangeCut;
    }

    public String getLengthUnit() {
        return lunit;
    }

    public double getEnergyThreshold() {
        return energyThreshold;
    }

    public String getEnergyUnit() {
        return eunit;
    }
}