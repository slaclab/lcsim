package org.lcsim.util.aida;

import hep.aida.ref.plotter.Plotter;
import hep.aida.ref.plotter.PlotterUtilities;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * This is an extension of <tt>Plotter</tt> that draws itself into a tab
 * within a <tt>JTabbedPane</tt>.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class TabbedPlotter extends Plotter {
    
    JTabbedPane tabbedPane;
    JFrame frame;
    
    /**
     * Create an unnamed plotter.
     */
    public TabbedPlotter() {
        super(null);
    }
    
    /**
     * Create a named plotter.
     * @param name The plotter name.
     */
    public TabbedPlotter(String name) {
        super(name);
    }
    
    /**
     * Set the target <tt>JTabbedPane</tt> into which this plotter's
     * plots will be drawn.     
     * @param tabbedPane The target JTabbedPane for the plots.
     */
    public void setTabbedPane(JTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
    }
    
    /**
     * Set the parent <tt>JFrame</tt> for this plotter.
     * @param frame The parent <tt>JFrame</tt>.
     */
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
            
    /**
     * Show this plotter's graphics, setting the parent <tt>JFrame</tt>
     * to visible, if necessary.
     */
    public void show() {
        if (!isShowing()) {
            // Add the tab if not already showing.
            addPlotterTab();
            if (!frame.isVisible()) {
                // Activate the JFrame if it is invisible.
                frame.setVisible(true);
            }
        }
    }
       
    /**
     * Add a tab for this plotter.
     */
    private void addPlotterTab() {
        String title = title();
        if (title == null)
            title = "     ";
        JPanel plotterPanel = new JPanel(new BorderLayout());
        plotterPanel.add(PlotterUtilities.componentForPlotter(this), BorderLayout.CENTER);
        tabbedPane.addTab(title, plotterPanel);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, new JLabel(title));
    }
}
