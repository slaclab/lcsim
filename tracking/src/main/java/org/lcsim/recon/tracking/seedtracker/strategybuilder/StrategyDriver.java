package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.io.File;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;
import org.lcsim.recon.tracking.seedtracker.StrategyXMLUtils;
import org.lcsim.util.Driver;

/**
 * This Driver makes the StrategyBuilder accessible from an LCSim XML steering file.
 * 
 * @author jeremym
 */
public class StrategyDriver extends Driver
{
    String outputFile = null;
    String prototypeFile = null;
    boolean verbose = false;
    
    public StrategyDriver()
    {}
    
    /**
     * Set the output file path for the generated strategies.
     * @param outputFile The output file path.
     */
    public void setOutputFile(String outputFile)
    {        
        this.outputFile = outputFile;
    }
    
    /**
     * Set verbose flag for debugging.
     * @param verbose True to turn on debug output; false to turn off.
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }
    
    /**
     * Set a strategy file to use as a prototype for the output strategies.
     * @param prototypeFile The prototype strategy file.
     */
    public void setPrototypeStrategyFile(String prototypeFile)
    {
        this.prototypeFile = prototypeFile; 
    }
    
    public void startOfData()
    {        
        // Setup the StrategyBuilder Driver and add to child Driver list.
        StrategyBuilder builder = new StrategyBuilder();
        builder.setOutput(this.outputFile);
        builder.setSymmetrize(true);        
        List<SeedStrategy> prototype = StrategyXMLUtils.getStrategyListFromFile(new File(prototypeFile));   
        builder.setStrategyPrototype(prototype.get(0));
        builder.setMinimumUnweightedScore(1);
        this.add(builder);        
        super.startOfData();
    }
    
    public void process(EventHeader event)
    {
        super.process(event);
    }
}