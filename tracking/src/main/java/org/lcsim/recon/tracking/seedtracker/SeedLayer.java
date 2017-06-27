/*
 * SeedLayer.java
 *
 * Created on March 29, 2006, 1:59 PM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import org.lcsim.fit.helicaltrack.HitIdentifier;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 * Encapsulates information about a tracker layer needed for the SeedTracker algorithm
 * @author Richard Partridge
 * @version 1.0
 */
public class SeedLayer {
    /**
     * Enumeration of possible layer types
     */
    public enum SeedType {
        /**
         * Seed layer
         */
        Seed, 
        /**
         * Confirmation layer
         */
        Confirm, 
        /**
         * Track extension layer
         */
        Extend}

    private String _detname;
    private int _layer;
    private BarrelEndcapFlag _beflag;
    private SeedType _type;
    
    /**
     * Creates a new instance of SeedLayer
     * @param detname Decector name
     * @param layer Layer number
     * @param beflag Barrel-endcap flag
     * @param type Layer type
     */
    public SeedLayer(String detname, int layer, BarrelEndcapFlag beflag, SeedType type) {
        _detname = detname;
        _layer = layer;
        _beflag = beflag;
        _type = type;
    }
    
    /**
     * Return the detector name
     * @return Detector name
     */
    public String getDetName() {
        return _detname;
    }
    
    /**
     * Return the layer number
     * @return Layer number
     */
    public int getLayer() {
        return _layer;
    }
    
    /**
     * Return the BarrelEndcapFlag
     * @return Barrel-endcap flag
     */
    public BarrelEndcapFlag getBarrelEndcapFlag() {
        return _beflag;
    }
    
    /**
     * Retrun the layer type
     * @return Layer type
     */
    public SeedType getType() {
        return _type;
    }

    public String LayerID() {
        return HitIdentifier.Identifier(_detname, _layer, _beflag);
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true; 
        if (!(other instanceof SeedLayer)) return false; 
        SeedLayer lyr = (SeedLayer) other; 
        
        return (this._beflag.equals(lyr._beflag) && this._detname.equals(lyr.getDetName())
                && this._layer == lyr.getLayer() && this._type.equals(lyr.getType())); 
    }
    
    @Override
    public int hashCode(){
        return (_detname + _beflag.toString() + _layer + _type.toString()).hashCode(); 
    }
}
