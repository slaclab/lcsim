/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**Simple layer to uniquely define layers. Like SeedLayer but without
 * information about layer type. 
 * 
 * @author cozzy
 */
public class DumbLayer{

        String detectorName; 
        int layer; 
        BarrelEndcapFlag be; 
        
        public DumbLayer(String det, int lyr, BarrelEndcapFlag be){
            detectorName = det; 
            layer = lyr;  
            this.be = be;
        }
        
        
        @Override
        public boolean equals(Object other) {
            if (this == other) return true; 
            if (! (other instanceof DumbLayer)) return false; 
            DumbLayer dl = (DumbLayer) other; 
            return ( this.be.equals(dl.be) &&
                     this.layer == dl.layer &&
                     this.detectorName.equals(dl.detectorName)); 
        }
        
        //dumb hash function... 
        @Override
        public int hashCode() {
            return 20*( be.ordinal() + 1) + layer + 500*(detectorName.hashCode() % 1000);
        }

        @Override
        public String toString() {
            return ("DumbLayer: det="+detectorName+" lyr="+layer+" be="+ be.toString()); 
        }
        
    }
