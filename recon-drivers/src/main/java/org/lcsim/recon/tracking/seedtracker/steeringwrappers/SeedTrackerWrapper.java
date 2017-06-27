/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.steeringwrappers;

import java.io.File;
import org.lcsim.recon.tracking.seedtracker.*;
import org.lcsim.util.Driver;

/**
 *This is a wrapper around SeedTracker so that one can instantiate SeedTracker 
 * and a list of strategies without a constructor. This is necessary when 
 * using Jeremy's XML steering driver 
 * @author cozzy
 */
public class SeedTrackerWrapper extends Driver{
    
    private boolean added = false;
    private SeedTracker seedTracker = null;
    
    public void setStrategyFile(String file) {
        if(added) throw new AlreadyAddedException();
        seedTracker = new SeedTracker(StrategyXMLUtils.getStrategyListFromFile(new File(file)));
        add(seedTracker); 
        added = true; 
    }
    
    public void setStrategyResource(String resource) {
        if(added) throw new AlreadyAddedException(); 
        seedTracker = new SeedTracker(StrategyXMLUtils.getStrategyListFromResource(resource)); 
        added = true; 
    }
    
    public void setStrategyResourceWithDefaultPrefix(String resource) {
        setStrategyResource(StrategyXMLUtils.getDefaultStrategiesPrefix() + resource);
    }
    
    public void setTimingPlots(boolean timing) {
    	if (!added) throw new NotAddedException();
    	seedTracker.setTimingPlots(timing);
    }
    
    public void setTrkCollectionName(String name) {
    	if (!added) throw new NotAddedException();
    	seedTracker.setTrkCollectionName(name);
	}
    
    public void setInputCollectionName(String name) {
    	seedTracker.setInputCollectionName(name);
    }
    
    public void setMaxFit(int maxfit) {
    	seedTracker.setMaxFit(maxfit);
    }
    
    public void setBField(double bfield) {
    	seedTracker.setBField(bfield);
    }
    
    class AlreadyAddedException extends RuntimeException {
        public AlreadyAddedException(){
            super("SeedTracker already has been added"); 
        }
    }
    
    class NotAddedException extends RuntimeException {
    	public NotAddedException(){
    		super("SeedTracker has not been added, yet");
    	}
    }
}
