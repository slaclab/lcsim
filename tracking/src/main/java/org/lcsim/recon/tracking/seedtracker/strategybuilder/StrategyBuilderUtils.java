/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.lcsim.recon.tracking.seedtracker.SeedLayer;
import org.lcsim.recon.tracking.seedtracker.SeedLayer.SeedType;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.recon.tracking.seedtracker.StrategyXMLMetadata;

/**
 * StrategyBuilder was getting too bloated, so some of the code has been moved here
 * @author cozzy
 */
public class StrategyBuilderUtils {
     
    private static final BarrelEndcapFlag[] beArray = 
            new BarrelEndcapFlag[]{BarrelEndcapFlag.ENDCAP_NORTH, BarrelEndcapFlag.ENDCAP_SOUTH}; 
    
    /**
     * Symmetrizes the given StrategyList... Modifies 
     * the original list rather than returning a new one. 
     * 
     * It is assumed that the  input list uses the beflag ENDCAP rather 
     * than ENDCAP_NORTH or ENDCAP_SOUTH. Anything with ENDCAP_NORTH or 
     * ENDCAP_SOUTH won't be touched... 
     * 
     * @param strat_list
     */
    static void symmetrizeStrategies (List<SeedStrategy> strat_list, StrategyXMLMetadata meta) {
        
        /**
         * There are 3 interesting cases here:
         * 
         *   1) None of the layers are in the endcap. Symmetrization unnecessary.
         *   2) Only extension layers are in the endcap. In this case we should 
         *      modify the extension layers so that both endcaps appear. We remove
         *      the old extension layer and replace it with two new ones. 
         *   3) Extension layers appear in the seeds or confirm layers. 
         *      In this case, we remove the old strategy and replace it with 
         *      two new ones. 
         * 
         * 
         *   We can't tell the difference between (2) and (3) until after we have
         *   gone through the entire set, so the code keeps track of the changes
         *   that would be necessary for (2) and apply them only if (3) doesn't
         *   happen. 
         * 
         */

        List<SeedStrategy> symmetrized = new ArrayList<SeedStrategy>(); 
        Iterator<SeedStrategy> it = strat_list.iterator(); 
        //this will store additional extension layers in the case that there are no endcap Seed/Confirm layers
        List<SeedLayer> additionalExtendLayers = new ArrayList<SeedLayer>(); 
        List<SeedLayer> removeLayers = new ArrayList<SeedLayer>(); 
        while (it.hasNext()) {
            SeedStrategy next = it.next(); 
            boolean extendOnlyFlag = true; //If all the Seed/Confirm layers are BarrelOnly, then we can 
                                           //just have both endcaps in the extend layers without creating a new strategy. 
            additionalExtendLayers.clear(); 
            removeLayers.clear(); 
            for (SeedLayer l : next.getLayerList()) {
                //Anything with BarrelEndcapFlag ENDCAP in seed or confirm should be mirrored for both sides
                if (l.getBarrelEndcapFlag()==BarrelEndcapFlag.ENDCAP && l.getType()!=SeedType.Extend) {
                    it.remove(); //remove this strategy... we will replace it with the other two symmetrized ones
                    //loop through both north and south configurations
                    for (BarrelEndcapFlag be : beArray) {
                        SeedStrategy newstrat = makeMirroredLayer(next, be);
                        symmetrized.add(newstrat); 
                        meta.strategyComments.put(newstrat, meta.strategyComments.get(next)+
                                "\n\t\t\tNOTE: These layers are combined for both endcaps of this symmetrized strategy\n\t\t"); 
                    }
                    meta.strategyComments.remove(next); 
                    extendOnlyFlag = false; 
                    break; 

                } else if (l.getBarrelEndcapFlag()==BarrelEndcapFlag.ENDCAP) {
                    removeLayers.add(l); 
                    additionalExtendLayers.add(new SeedLayer(l.getDetName(), l.getLayer(), beArray[0], l.getType()));
                    additionalExtendLayers.add(new SeedLayer(l.getDetName(), l.getLayer(), beArray[1], l.getType()));
                }
            }

            if (extendOnlyFlag) {
                next.getLayerList().addAll(additionalExtendLayers);
                next.getLayerList().removeAll(removeLayers);
            }

        }
        strat_list.addAll(symmetrized); 
    }
    
    
    private static SeedStrategy makeMirroredLayer(SeedStrategy next, BarrelEndcapFlag be) {
        List<SeedLayer> symmlyrs = new ArrayList<SeedLayer>(); //store the new layers here
        for (SeedLayer ll : next.getLayerList()) {
            if (ll.getBarrelEndcapFlag().isBarrel()) {
                // if it's a barrel layer, we can just add it
                symmlyrs.add(ll);
            } else {
                //otherwise change from ENDCAP to either ENDCAP_NORTH or ENDCAP_SOUTH
                SeedLayer newlyr = new SeedLayer(ll.getDetName(), ll.getLayer(), be, ll.getType());
                symmlyrs.add(newlyr);
            }
        }
        SeedStrategy newstrat = new SeedStrategy(next.getName() + be.toString(), symmlyrs); //create new strategy
        newstrat.copyCutoffsFromStrategy(next); //copy parameters
        return newstrat;
    }
    
    
   /**
    * Returns a list of possible subsets of DumbLayers.... wrapper around generateAllPossibleSubsets
    * @param allLayers
    * @param subset_size
    * @return
    */
    public static List<Set<DumbLayer>> generateAllPossibleDumbLayerSubsetsList(Set<DumbLayer> allLayers, int subset_size) {
        Set<Object> set = new HashSet<Object>();
        set.addAll(allLayers); 
        Set<Set> subsets = generateAllPossibleSubsets(set, subset_size);         
        List<Set<DumbLayer>> ret = new ArrayList<Set<DumbLayer>>(); 
        for (Set<DumbLayer> subset : subsets) {
            ret.add(subset); 
        }   
        return ret; 
    }
  
     /**
     * Returns a set of DumbLayers consisting of the seed and confirm layers of the given SeedStrategy
     * @param strategy SeedStrategy to extract layers from
     * @param ignoreNorthSouth Whether or not the DumbLayers should know about North or South
     * @return A set of DumbLayers
     */
    public static Set<DumbLayer> getRelevantSet(SeedStrategy strategy, boolean ignoreNorthSouth) {
        Set<DumbLayer> subset = new HashSet<DumbLayer>();
        for (SeedLayer lyr : strategy.getLayers(SeedLayer.SeedType.Seed)) {
            
            BarrelEndcapFlag be = lyr.getBarrelEndcapFlag(); 
            if (ignoreNorthSouth && be.isEndcap()) be = BarrelEndcapFlag.ENDCAP; 
            subset.add(new DumbLayer(lyr.getDetName(), lyr.getLayer(), be));
           
        }
        for (SeedLayer lyr : strategy.getLayers(SeedLayer.SeedType.Confirm)) {
            BarrelEndcapFlag be = lyr.getBarrelEndcapFlag(); 
            if (ignoreNorthSouth && be.isEndcap()) be = BarrelEndcapFlag.ENDCAP; 
            subset.add(new DumbLayer(lyr.getDetName(), lyr.getLayer(), be));
        }
        return subset;
    }
    
    
    
    /**
     * Returns all possible subsets of a given size of the set allObjects
     * @param allObjects The set to find subsets of
     * @param subset_size The size desired of subsets. 
     * @return A set of subsets, all of size sub_set size
     */
    public static Set<Set> generateAllPossibleSubsets(Set allObjects, int subset_size) {

        assert(subset_size > 0 && subset_size <= allObjects.size()); 
        
        if (subset_size == 1){
            Set<Set> ret = new HashSet<Set>(); 
            for (Object o : allObjects){
                Set set = new HashSet(); 
                set.add(o); 
                ret.add(set); 
            }
            return ret; 
        }

        else {
            Set<Set> ret = new HashSet<Set>();  
            for(Object o : allObjects) {
                Set newSet = new HashSet(); 
                newSet.addAll(allObjects); 
                newSet.remove(o); 
                Set<Set> partial = generateAllPossibleSubsets(newSet, subset_size-1); 
                for (Set s : partial) s.add(o); 
                ret.addAll(partial); 
            }
            return ret; 
        }
    }
    
}
