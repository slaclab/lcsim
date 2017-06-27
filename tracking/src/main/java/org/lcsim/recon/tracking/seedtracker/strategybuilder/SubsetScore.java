/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

/**
 *This is a not-very smart class to keep track of scores...
 * @author cozzy
 */
public class SubsetScore {
    
    private double score; 
    private int numTracks; 
    private double adjacency; 
    
    public SubsetScore(double score, int numTracks, double adjacency){
        this.score =score; 
        this.numTracks = numTracks; 
        this.adjacency = adjacency; 
    }
    
    public double score() {
        return score; 
    }
    
    public int numTracks() {
        return numTracks; 
    }
    
    public double adjacency() {
       return adjacency;  
    }
}
