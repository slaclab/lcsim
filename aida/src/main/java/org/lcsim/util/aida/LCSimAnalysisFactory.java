package org.lcsim.util.aida;

import hep.aida.IPlotterFactory;
import hep.aida.ref.AnalysisFactory;

/**
 * This is an extension of <tt>AnalysisFactory</tt> which currently
 * just provides some additional organization of plots into tabs
 * via an <tt>IPlotterFactory</tt> implementation.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class LCSimAnalysisFactory extends AnalysisFactory {
    
    /**
     * Register this class as the default AnalysisFactory for AIDA by setting
     * the magic property string.
     */
    final static void register() {
        System.setProperty("hep.aida.IAnalysisFactory", LCSimAnalysisFactory.class.getName());
    }
    
    /**
     * Create an unnamed <tt>TabbedPlotterFactory</tt>.
     */
    public IPlotterFactory createPlotterFactory() {
        return new TabbedPlotterFactory();
    }    
    
    /**
     * Create a named <tt>TabbedPlotterFactory</tt>.
     */
    public IPlotterFactory createPlotterFactory(String name) {
        return new TabbedPlotterFactory(name);
    }
}
