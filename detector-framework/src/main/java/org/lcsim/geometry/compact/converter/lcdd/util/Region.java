package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 * 
 * @author tonyj
 */
public class Region extends RefElement {

    public Region(String name) {
        super("region", name);
        setAttribute("store_secondaries", "false");
        setAttribute("kill_tracks", "false");
        setAttribute("cut", "10.0");
        setAttribute("lunit", "mm");
        setAttribute("threshold", "0.0");
        setAttribute("eunit", "MeV");

    }

    public void setStoreSecondaries(boolean store) {
        setAttribute("store_secondaries", String.valueOf(store));
    }
    
    public void setKillTracks(boolean killTracks) {
        setAttribute("kill_tracks", String.valueOf(killTracks));
    }

    public void setThreshold(double mev) {
        setAttribute("threshold", String.valueOf(mev));
    }

    public void setEnergyUnit(String eunit) {
        setAttribute("eunit", eunit);
    }

    public void setLengthUnit(String lunit) {
        setAttribute("lunit", lunit);
    }

    public void setCut(double cut) {
        setAttribute("cut", String.valueOf(cut));
    }
}
