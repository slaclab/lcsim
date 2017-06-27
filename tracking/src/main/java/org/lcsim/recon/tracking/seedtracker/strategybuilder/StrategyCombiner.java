/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;
import org.lcsim.recon.tracking.seedtracker.StrategyXMLUtils;

/**
 *
 * @author cozzy
 */
public class StrategyCombiner {

    public static void main(String[] args){
        
        if (args.length < 3){
            printUsage(); 
            System.exit(1); 
        }
        
        String filename = args[0]; 
        
        List<SeedStrategy> lst = new ArrayList<SeedStrategy>(); 
        
        for (int i = 1; i < args.length; i++) {
            List<SeedStrategy> portion = StrategyXMLUtils.getStrategyListFromFile(new File(args[i])); 
            for (SeedStrategy s : portion){
                if (!lst.contains(s)) lst.add(s); 
            }
        }
        
        StrategyXMLUtils.writeStrategyListToFile(lst, new File(filename));
    }
    
    
    
    
    private static void printUsage(){
        System.out.println("Usage:"); 
        System.out.println("StrategyCombiner OUTPUT_FILE STRATEGY1 STRATEGY2 [STRATEGY3... ]");
        System.out.println("\t\tWhere the different strategies are XML files");   
    }
    
}
