package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 * @author tonyj
 */
public class Structure extends Element {

    private Volume worldVolume;
    private Volume trackingVolume;

    public Structure() {
        super("structure");
    }

    public void addVolume(Volume volume) {
        addContent(volume);
    }

    public void setWorldVolume(Volume volume) {
        addVolume(volume);
        worldVolume = volume;
    }

    public Volume getWorldVolume() {
        return worldVolume;
    }

    public void setTrackingVolume(Volume volume) {
        addVolume(volume);
        trackingVolume = volume;
    }

    public Volume getTrackingVolume() {
        return trackingVolume;
    }
}