/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author cozzy
 */
public class SubsetScorer {
        
        Map<Set<DumbLayer>,Integer> setmap = new HashMap<Set<DumbLayer>,Integer>(); 
        LayerWeight weighter = new LayerWeight(); 
        Map<Set<DumbLayer>,Double> adjacencemap = new HashMap<Set<DumbLayer>,Double>(); 
        
        public SubsetScorer(List<Set<DumbLayer>> setlist, List<List<DumbLayer>> adjacentSets) {
            
            for (Set<DumbLayer> set : setlist) {
                if(setmap.containsKey(set)){
                    setmap.put(set,setmap.get(set).intValue()+1);  
                } else {
                    setmap.put(set,1); 
                }
            }
            
           for (List<DumbLayer> list : adjacentSets) {
               //convert to a set because of faster lookup? (actually it might not be since the collection sizes are so small?)
               Set<DumbLayer> set = new HashSet<DumbLayer>(list.size());
               set.addAll(list); 
                if(adjacencemap.containsKey(set)){
                    adjacencemap.put(set,adjacencemap.get(set).doubleValue()+1.0);  
                } else {
                    adjacencemap.put(set,1.0); 
                }
            }

           //Normalize adjacencies
           for(Set<DumbLayer> s : adjacencemap.keySet()) {
               double pct = adjacencemap.get(s) / getUnweightedScore(s);
               if (pct>1.0) pct = 1.0; //Fix anomalous cases where the numerator is slightly (by no more than a few) bigger than the denominator
//             System.out.println(adjacencemap.get(s) + " , "+ getUnweightedScore(s) + ", " + s.toString());
               adjacencemap.put(s, pct);
           }
        }
        
        public void setLayerWeight(LayerWeight lw) {
            weighter = lw; 
        }
        
       
        public SubsetScore getScoreObject(Set<DumbLayer> testset) {
            
            return new SubsetScore(getScore(testset), getUnweightedScore(testset), getAdjacence(testset)); 
            
        }
        
        
        //s * Pi(w) * (1 + a*(adjacence))
        //
        public double getScore(Set<DumbLayer> testSet) {
          return getUnweightedScore(testSet) * weighter.getWeight(testSet)
                  * (  1 + 
                       (getAdjacence(testSet))
                         * weighter.getAdjacenceMultiplier() 
                    ); 
        }
        
        public int getUnweightedScore(Set<DumbLayer> testSet){
            
            int ret = 0; 
            for (Set<DumbLayer> s : setmap.keySet()){
                if (s.containsAll(testSet)) {
                    ret+= setmap.get(s).intValue();
                }
            }
            return ret;
        }
        
        public double getAdjacence(Set<DumbLayer> testSet){
            if (adjacencemap.containsKey(testSet)){
                return adjacencemap.get(testSet); 
            } 
            
            return 0; 
        }
        
        public void markUsed(Set<DumbLayer> testSet) {
            for (Set<DumbLayer> s : setmap.keySet()){
                if (s.containsAll(testSet)) {
                    setmap.put(s,0); 
                }
            }
        }
        
        
    }