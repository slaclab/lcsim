/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.io.File;
import org.lcsim.recon.tracking.seedtracker.StrategyXMLUtils;
import org.lcsim.util.Driver;

/**
 * This class contains implementations for some convenience methods in IStrategyBuilder
 * @author cozzy
 */
public abstract class AbstractStrategyBuilder extends Driver implements IStrategyBuilder{
    
    public void setStartingStrategyList(String startingStrategies) {
        setStartingStrategyList(StrategyXMLUtils.getStrategyListFromFile(new File(startingStrategies)));
    }
    
    public void setStrategyPrototype(String strategiesFile, int strategyNumber) {   
        setStrategyPrototype(StrategyXMLUtils.getStrategyListFromFile(new File(strategiesFile)).get(strategyNumber)); 
    }
    
    public void setStrategyPrototype(String strategiesFile) {
        setStrategyPrototype(strategiesFile,0); 
    }

    public void setLayerWeight(String layerWeightsFile){
        setLayerWeight(LayerWeight.getLayerWeightFromFile(new File(layerWeightsFile)));
    }
    
    public void setParticleFilter(String filterClassName) {
          try {
                setParticleFilter((IParticleFilter) Class.forName(filterClassName).newInstance());
            } catch (ClassNotFoundException cfne) {
                System.out.println("WARNING: Class "+filterClassName+ " not found :'("); 
            } catch (InstantiationException ie){
                System.out.println("WARNING: Class "+filterClassName+ " could not be instantiated. Does the constructor take arguments?");
            } catch (IllegalAccessException iae){
                System.out.println("WARNING: IllegalAccessException? WTF does that mean?"); 
            } catch (ClassCastException cce){
                System.out.println("WARNING: Unable to cast "+filterClassName+ " as a IParticleFilter."); 
            }
    }
}
