/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.util.List;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;

/**
 *This interface is mainly written for documentation purposes (since StrategyBuilder
 * is somewhat full of code). 
 * 
 * @author cozzy
 */
public interface IStrategyBuilder {
    
    /**
     *  Sets the location of the output XML file of strategy lists
     * @param filename
     */
    public void setOutput(String filename);
    
    /**
     * If symmetrize is true, then the Strategy will be force symmetrized between the two endcaps. 
     * @param symmetrize
     */
    public void setSymmetrize(boolean symmetrize); 
    
    /**
     * Sets the minimum number of layers that an MCParticle must go through to be considered
     * for strategy finidng
     * @param min
     */
    public void setMinLayers(int min);
    
    /**
     * Sets the number of confirmation layers desired
     * @param confirmed
     */
    public void setNumConfirmLayers(int confirmed); 
    
    /**
     * Sets the number of seed layers desired
     * @param seed
     */
    public void setNumSeedLayers(int seed);
    
    /**
     * Sets the LayerWeight object of the strategy builder. A layer weight is 
     * used to treat certain layers preferentially. If no layer weight specified, 
     * the default layer weight will be used. 
     * @param lw The LayerWeight object to use
     */
    public void setLayerWeight(LayerWeight lw); 
    
     /**
     * Sets the LayerWeight object of the strategy builder. A layer weight is 
     * used to treat certain layers preferentially. If no layer weight specified, 
     * the default layer weight will be used. 
     * @param layerWeightsFile A string representing the filename of an XML file representing a LayerWeight object
     */
    public void setLayerWeight(String layerWeightsFile); 
    
    /**
     * Set the prototype for the generated strategies. The values for cutoffs
     * and such will be copied from the prototype. If not prototype is specified,
     * the default values for SeedStrategy will be used. 
     * @param strategy The SeedStrategyObject to use
     */
    public void setStrategyPrototype(SeedStrategy strategy); 
    
    /**
     * Set the prototype for the generated strategies. The values for cutoffs
     * and such will be copied from the prototype. If not prototype is specified,
     * the default values for SeedStrategy will be used. 
     * @param strategiesFile Filename of XML file containing a list of strategies
     * @param strategyNumber The (0-indexed) number of the strategy to use as a prototype
     */
    public void setStrategyPrototype(String strategiesFile, int strategyNumber); 
    
    /**
     * Set the prototype for the generated strategies. The values for cutoffs
     * and such will be copied from the prototype. If not prototype is specified,
     * the default values for SeedStrategy will be used. 
     * @param strategiesFile Filename of XML file containing a list of strategies. The first strategy will be used. 
     */    
    
    public void setStrategyPrototype(String strategiesFile); 
    /**
     * Set a starting strategy list. New strategies will only be generated for 
     * particles not already found by the starting strategy list. 
     * @param startList a List of SeedStrategy's 
     */
    public void setStartingStrategyList(List<SeedStrategy> startList); 
    
     /**
     * Set a starting strategy list. New strategies will only be generated for 
     * particles not already found by the starting strategy list. 
     * @param startList a file containing a number of SeedStrategies
     */
    public void setStartingStrategyList(String startingStrategiesFile); 
    
    /**
     * Enables extra output if verbose is true. 
     * @param verbose
     */
    public void setVerbose(boolean verbose); 
    
    /**
     * Set the minimum unweighted score to create a strategy. This represents
     * the minimum number of additional tracks in the test event that could
     * theoretically be found if the strategy is included. 
     * @param score
     */
    public void setMinimumUnweightedScore(int score);
    
    /**
     * Sets the particle filter applied to MCParticles during the processing step. 
     * MCParticles which do not pass this filter will not be considered for
     * strategy finding. Note that MCParticles must also pass the MinLayers 
     * requirement regardless of the filter.  
     * 
     * If no filter is specified, a filter will be generated from dca, z0 and
     * pt cutoffs present in the prototype SeedStrategy. 
     * 
     * If no filter is desired, AllPassFilter may be used. 
     * 
     * @param filter An object implementing the IParticleFilter interface
     */
    public void setParticleFilter(IParticleFilter filter);
    
        /**
     * Sets the particle filter applied to MCParticles during the processing step. 
     * MCParticles which do not pass this filter will not be considered for
     * strategy finding. Note that MCParticles must also pass the MinLayers 
     * requirement regardless of the filter.  
     * 
     * If no filter is specified, a filter will be generated from dca, z0 and
     * pt cutoffs present in the prototype SeedStrategy. 
     * 
     * If no filter is desired, AllPassFilter may be used. 
     * 
     * @param filter Fully qualified class name for an object implementing the IParticleFilter interface
     */
    public void setParticleFilter(String filterClassName); 
}
