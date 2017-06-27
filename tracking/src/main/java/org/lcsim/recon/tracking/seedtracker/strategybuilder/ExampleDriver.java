/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import org.lcsim.util.Driver;

/**
 * Driver used to illustrate usage of StrategyBuilder w/out using the 
 * command line. (Look at RunStrategyBuilder to learn how to use Strategy
 * Builder with the command line). 
 * 
 * @author cozzy
 */
public class ExampleDriver extends Driver {
    
   //this will be the output file... this example outputs it in the 
   //system's temporary directory. 
   private String outputfile = System.getProperties().getProperty("java.io.tmpdir")+
            System.getProperties().getProperty("file.separator")+
            "ExampleStrategyList.xml"; 
   
   public ExampleDriver(){
       
       StrategyBuilder builder = new StrategyBuilder(); 
       
       //set the output file 
       builder.setOutput(outputfile);
       builder.setVerbose(true); //verbose output
      
       //see the IStrategyBuilder interface for other settable options
       
       add(builder); 
       
   }
   

}
