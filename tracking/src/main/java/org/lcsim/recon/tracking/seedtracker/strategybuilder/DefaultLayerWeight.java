/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

/**
 *The layer weighting scheme used by default by StrategyBuilder if no other is
 * provided. It's loaded from a resource based on the name of the detector. 
 * 
 * After construction, the LayerWeight object may be accessed using getWeight(); 
 * 
 * @author cozzy
 */
public class DefaultLayerWeight {

    private static final String prefix = "default_weights_";
    private static final String suffix = ".xml"; 

    private LayerWeight weight;
    public DefaultLayerWeight(String detectorName){
        try { 

            // This is kind of screwy: if the detector name has a period in it,
            // then loading the resource would normally throw an exception. 
            // We can fix this by replacing "." with "%2E" (URL encoding).  
            // It's probably best to avoid having periods in detector names
            // though. 
            
            detectorName = detectorName.replace(".","%2E"); 
            weight = LayerWeight.getLayerWeightFromResource(LayerWeight.getDefaultResourcePrefix()+prefix+detectorName+suffix);
        } catch(Exception e) {
            System.out.println("WARNING: could not find default layer weights for detector "+detectorName+". Falling back to empty layer weights with possibly insane default parameters.");
            weight = new LayerWeight(); 
        }
    }   
    /**
     * Return the constructed weight for the detector. 
     * @return The default weight for the detector name specified in the 
     * constructor, or, if nothing is found, then an unmodified LayerWeight 
     * object. 
     */
    public LayerWeight getWeight(){
        return weight; 
    }

}
