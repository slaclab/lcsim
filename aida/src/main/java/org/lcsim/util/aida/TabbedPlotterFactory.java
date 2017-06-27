package org.lcsim.util.aida;

import hep.aida.IPlotter;
import hep.aida.ref.plotter.PlotterFactory;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * This is an extension of <tt>PlotterFactory</tt> that puts the output
 * from each <tt>IPlotter</tt> in a separate tab.  Each of these factory
 * objects has an associated <tt>JFrame</tt> with a set of tabs.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class TabbedPlotterFactory extends PlotterFactory {
    
    private String name;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JFrame frame; 
    
    /**
     * Create a named factory.
     * @param name The name of the factory.
     */
    public TabbedPlotterFactory(String name) {
        this.name = name;
        createFrame();
    }
    
    /**
     * Create an un-named factory.
     */
    public TabbedPlotterFactory() {
        if (!(new RuntimeException()).getStackTrace()[2].getClassName()
                .equals("hep.aida.ref.plotter.style.registry.StyleStoreXMLReader")) {
            createFrame();
        }
    }
    
    /**
     * This method initializes a <tt>JFrame</tt> for this <tt>TabbedPlotterFactory</tt>.
     * It is initially set to invisible.  When the method {@link IPlotter#show()} on an
     * associated <tt>TabbedPlotter</tt> is called, the frame will be set to visible
     * if it isn't already showing.
     */
    private void createFrame() {
        frame = new JFrame();
        frame.add(tabbedPane, BorderLayout.CENTER);
        if (name != null)
            frame.setTitle(name);
        frame.setContentPane(tabbedPane);
        frame.pack();
        frame.setSize(800, 600);        
    }
        
    /**
     * Create a named plotter which will plot its graphics onto a tabbed pane.
     * @param plotterName The name of the plotter.
     * @return The plotter.
     */
    public IPlotter create(String plotterName) {
        TabbedPlotter plotter = new TabbedPlotter(plotterName);
        plotter.setTabbedPane(tabbedPane);
        plotter.setFrame(frame);
        plotter.setTitle(plotterName);
        return plotter;
    }

    /**
     * Create an unnamed plotter.
     * @return The plotter.
     */
    public IPlotter create() {
        return create((String) null);
    }    
}
