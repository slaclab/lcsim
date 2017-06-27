/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.HashMap;
import java.util.Map;

/**
 * A not very smart data object... 
 * @author cozzy
 */
public class StrategyXMLMetadata {

    public String comment = null; 
    public String targetDetector = null; 
    public Map<SeedStrategy, String> strategyComments = new HashMap<SeedStrategy, String>(); 
}
