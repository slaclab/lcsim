package org.lcsim.util.aida;

import hep.aida.IAnalysisFactory;
import hep.aida.IHistogram1D;
import hep.aida.IPlotter;
import hep.aida.IPlotterFactory;

import java.util.Random;

import junit.framework.TestCase;

/**
 * This is a basic test of the {@link TabbedPlotterFactory} class.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class TabbedPlotterFactoryTest extends TestCase {
    
    static {
        // Register the default analysis factory.
        LCSimAnalysisFactory.register();
    }
    
    /**
     * Test the <tt>TabbedPlotterFactory</tt>.
     */
    public void testTabbedPlotterFactory() {
        AIDA aida = AIDA.defaultInstance();
        IAnalysisFactory analysisFactory = aida.analysisFactory();
        IPlotterFactory plotterFactory = analysisFactory.createPlotterFactory(this.getClass().getSimpleName());
        
        IHistogram1D histogram = aida.histogram1D("Fancy Histogram", 100, 0., 10.);
        Random random = new Random();
        for (int i=0; i<1000; i++) {
            histogram.fill(random.nextDouble() * 10);
        }        
                                      
        IPlotter plotter = plotterFactory.create("Test Plot");
        plotter.createRegions();
        plotter.region(0).plot(histogram);
        plotter.show();
        
        IPlotter plotter2 = plotterFactory.create("Test Plot 2");
        plotter.createRegion();
        plotter2.region(0).plot(histogram);
        plotter2.show();
        
        IPlotter plotter3 = plotterFactory.create();
        plotter.createRegion();
        plotter3.region(0).plot(histogram);
        plotter3.show();
                        
        synchronized(this) {
            try {
                wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
