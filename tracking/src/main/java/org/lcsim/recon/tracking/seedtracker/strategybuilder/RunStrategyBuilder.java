/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.io.File;
import org.lcsim.recon.tracking.seedtracker.StrategyXMLUtils;
import org.lcsim.util.Driver;
import org.lcsim.util.loop.LCSimLoop;

/**
 * Class used to run StrategyBuilder from outside of Jas3. 
 * 
 * With Maven 2, it is possible to use the command (from inside the base lcsim directory):
 * 
 * mvn exec:java -Dexec.mainClass="org.lcsim.contrib.seedtracker.strategybuilder.RunStrategyBuilder" -Dexec.args="arg1 arg2 etc..."
 * 
 * A shell script to do this is provided in resources/org/lcsim/contrib/seedtracker/strategybuilder/, 
 * note that the script should be run from the base lcsim directory. 
 * 
 * If anybody wants to write a windows shell script, that would be great. 
 * 
 * @author cozzy
 */
public class RunStrategyBuilder {
    public static void main(String[] args) {
        String filename=""; 
        String outFile = StrategyBuilder.defaultOutputFile; 
        int numEvents=-1; 
        String startingStrategies = ""; 
        String prototypeFile = "";
        int prototypeN = -1; 
        boolean verbose = StrategyBuilder.defaultVerbose; 
        boolean symmetrize = StrategyBuilder.defaultSymmetrize; 
        String lwfn = ""; 
        int mintrks = StrategyBuilder.defaultMinUnweightedScore;
        String filterClassName = ""; 
        String altDriver = ""; 
        
        //parse arguments.. if error print usage
        try{
            int no_flag_counter = 0; 
            for (int i = 0; i < args.length; i++){
                
                String arg = args[i]; 
                
                if (arg.equals("-h")) {
                    printUsage();
                    System.exit(0);
                } else if (arg.equals("-o")) {
                    outFile = args[++i]; 
                } else if (arg.equals("-e")) {
                    numEvents = Integer.valueOf(args[++i]); 
                } else if (arg.equals("-s")) {
                    startingStrategies = args[++i]; 
                } else if (arg.equals("-p")) {
                    prototypeFile = args[++i];
                    prototypeN = Integer.valueOf(args[++i]); 
                } else if (arg.equals("-v")) {
                    verbose = !verbose; 
                } else if (arg.equals("-l")) {
                    lwfn = args[++i]; 
                } else if (arg.equals("-m")) {
                    mintrks = Integer.valueOf(args[++i]);
                } else if (arg.equals("-f")) {
                    filterClassName = args[++i]; 
                } else if (arg.equals("-a")) {
                    altDriver = args[++i]; 
                } else if (arg.equals("-y")) {
                    symmetrize = !symmetrize; 
                } else if (arg.startsWith("-")){
                    throw new Exception(); 
                } else {
                    if (no_flag_counter > 0) throw new Exception(); 
                    no_flag_counter++; 
                    filename = arg; 
                }
            }
            if (no_flag_counter == 0) throw new Exception(); 
            
        } catch (Exception e){
            printUsage();
            System.exit(1); 
        }
        IStrategyBuilder builder = new StrategyBuilder(); 
        
        if (altDriver.length() > 0) {
            try {
                builder = (IStrategyBuilder) Class.forName(altDriver).newInstance();
            }  catch (ClassNotFoundException cfne) {
                System.out.println("Class "+altDriver+ " not found :'( Exiting."); 
                System.exit(3); 
            } catch (InstantiationException ie) {
                System.out.println("Class "+altDriver+ " could not be instantiated. Does the constructor take arguments? Exiting.");
                System.exit(4); 
            } catch (IllegalAccessException iae) {
                System.out.println("IllegalAccessException? WTF does that mean? Exiting."); 
                System.exit(5); 
            } catch (ClassCastException cce) {
                System.out.println("Unable to cast "+altDriver+ " as a IStrategyBuilder. Exiting"); 
                System.exit(6); 
            }
            
            if (!(builder instanceof Driver)) {
                System.out.println("Alternative driver must extend Driver. Exiting"); 
                System.exit(14123); 
            }
        } 
        
        builder.setVerbose(verbose);
        builder.setMinimumUnweightedScore(mintrks);
        builder.setOutput(outFile);
        builder.setSymmetrize(symmetrize);
        
        if (startingStrategies.length() > 0)
            builder.setStartingStrategyList(startingStrategies);
        
        if (prototypeFile.length() > 0)
            builder.setStrategyPrototype(prototypeFile,prototypeN);
        
        if (lwfn.length() > 0)
            builder.setLayerWeight(lwfn);
        
        
        // if a non-default MCParticle Filter is set, then try to load it and assign it. 
        if (filterClassName.length() > 0){
            builder.setParticleFilter(filterClassName);
        }
        
        // check data file existence
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Cannot find data file "+file.toString()+". Exiting. "); 
            System.exit(7);
        }
        
        
        if(verbose){
            System.out.println("Starting... Reading Geometry"); 
        }
        
        
        //load the driver and run it. 
        try {

            LCSimLoop loop = new LCSimLoop(); 
            loop.setLCIORecordSource(file); 
            loop.add( (Driver) builder ); 
            loop.loop(numEvents, null); 
            loop.dispose(); 
        } catch (Exception e){
            e.printStackTrace(); 
            throw new RuntimeException("oopsie"); 
        }    
    }
    
    
    private static void printUsage(){
        
        System.out.println("Usage: RunStrategyBuilder INPUTRECORD [flags]");
        System.out.println(" -h \t\t\t\tPrint this message");
        System.out.println(" -v \t\t\t\tBe verbose");
        System.out.println(" -y \t\t\t\tDon't auto-symmetrize north/south. \n\t\t\t\t You may want to do this is starting strategies\n\t\t\t\t aren't symmetric.");
        System.out.println(" -o OUTPUTFILE \t\t\tset output XML file to OUTPUTFILE\n\t\t\t\t (default is in TEMP dir)");
        System.out.println(" -e NUM_EVENTS \t\t\tRun only NUM_EVENTS events instead of all"); 
        System.out.println(" -s STARTING_STRATEGY_FILE \tUse starting strategies in the specified file"); 
        System.out.println(" -p PROTOTYPE_STRATEGY_FILE N\tUse the Nth strategy in the specified file\n\t\t\t\t as a prototype. N is 0-indexed.");       
        System.out.println(" -l LAYER_WEIGHTS_FILE\t\tUse the weights in the specified file");       
        System.out.println(" -m MIN_TRKS_FOR_STRATEGY \tMinimum number of tracks to \n\t\t\t\t that could theoretically be found" +
                "\n\t\t\t\tnecessary to make a strategy\n\t\t\t\t (default: 3)");    
        System.out.println(" -f FILTER_CLASS\t\tSpecify an MCParticle filter by naming a " +
                "\n\t\t\t\t fully qualified (i.e. org.lcsim.etc) class. \n\t\t\t\t By default, " +
                "a filter based on the prototype \n\t\t\t\t strategy cutoffs is used.");
        System.out.println(" -a ALTERNATIVE_DRIVER\t\tUse an alternative driver instead of \n\t\t\t\t the default StrategyBuilder. " +
                "Must implement\n\t\t\t\t IStrategyBuilder and extend Driver. UNTESTED.\n\t\t\t\t " +
                "Fully qualified class name must be used \n\t\t\t\t (i.e. org.lcsim.etc)"); 
    }
    
}
