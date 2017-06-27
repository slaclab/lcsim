package org.lcsim.util.aida;

import hep.aida.IAnalysisFactory;
import hep.aida.IAxisStyle;
import hep.aida.IBaseHistogram;
import hep.aida.IDataPointSet;
import hep.aida.IDataStyle;
import hep.aida.IFunction;
import hep.aida.ILegendBoxStyle;
import hep.aida.IManagedObject;
import hep.aida.IPlottable;
import hep.aida.IPlotter;
import hep.aida.IPlotterRegion;
import hep.aida.IPlotterStyle;
import hep.aida.ITitleStyle;
import hep.aida.ITree;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * This class will write all the plots from an AIDA file to a directory tree
 * on the file system, in a user-specified graphics format.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class GraphicsWriter {

    private ITree tree;
    private String graphicsFormat = "png";
    private File outputDir = new File(".");
    private IPlotter plotter;
    private File aidaFile;
    
    /**
     * Define command line options.
     */
    private static Options OPTIONS = new Options();
    static {
        OPTIONS.addOption("h", "help", false, "Print help and exit");
        OPTIONS.addOption("f", "format", true, "Output graphics format (default is PNG)");
        OPTIONS.addOption("i", "input-file", true, "AIDA input file (required)");
        OPTIONS.addOption("d", "output-dir", true, "Output directory for plots (default is current dir");
    }
    
    /**
     * Implementation of <code>main</code> method. 
     * @param args argument array containing command switches and arguments
     */
    public static void main(String[] args) {
        new GraphicsWriter().run(args);
    }
    
    /**
     * Print usage and exit.
     */
    private static void usage() {
        final HelpFormatter help = new HelpFormatter();
        help.printHelp(" ", OPTIONS);
        System.exit(1);
    }
    
    /**
     * Run the graphics writer with the given command line arguments. 
     * @param args the command line arguments
     */
    private void run(String[] args) {
        final CommandLineParser parser = new PosixParser();
        final CommandLine commandLine;
        try {
            commandLine = parser.parse(OPTIONS, args);
        } catch (final ParseException x) {
            throw new RuntimeException(x);
        }
        
        if (commandLine.hasOption("h") || !commandLine.hasOption("i")) {
            usage();
        }
                
        aidaFile = new File(commandLine.getOptionValue("i"));
        if (!aidaFile.exists()) {
            throw new IllegalArgumentException("The input file " + aidaFile.getPath() + " does not exist.");
        }    
        if (commandLine.hasOption("d")) {
            outputDir = new File(commandLine.getOptionValue("d"));
        }
        if (commandLine.hasOption("f")) {
            graphicsFormat = commandLine.getOptionValue("f");
        }
        
        IAnalysisFactory analysisFactory = IAnalysisFactory.create();
        try {
            tree = analysisFactory.createTreeFactory().create(aidaFile.getAbsolutePath());
        } catch (IllegalArgumentException | IOException e) {
            throw new RuntimeException(e);
        }    
        plotter = analysisFactory.createPlotterFactory().create();
        
        this.writeToDir();
    }
    
    /**
     * No argument constructor.
     */
    protected GraphicsWriter() {
    }

    /**
     * Class constructor.
     * @param aidaFile The input AIDA file.
     * @param graphicsFormat The graphics format e.g. "png", etc.
     * @param outputDir The base output directory, which is created if it does not exist.
     */
    public GraphicsWriter(File aidaFile, String graphicsFormat, File outputDir) {
        IAnalysisFactory analysisFactory = IAnalysisFactory.create();
        if (!aidaFile.exists())
            throw new IllegalArgumentException("The input AIDA file does not exist.");
        try {
            tree = analysisFactory.createTreeFactory().create(aidaFile.getAbsolutePath());
        } catch (IllegalArgumentException | IOException e) {
            throw new RuntimeException(e);
        }
        plotter = analysisFactory.createPlotterFactory().create();
        if (graphicsFormat != null)
            this.graphicsFormat = graphicsFormat;
        if (outputDir != null) {
            this.outputDir = outputDir;
        }
    }
    
    /**
     * Class constructor.
     * @param tree An existing AIDA ITree that is already in memory.
     * @param graphicsFormat The graphics format e.g. "png" etc.
     * @param outputDir The base output directory, which is created if it does not exist.
     */
    public GraphicsWriter(ITree tree, String graphicsFormat, File outputDir) {
        if (tree != null)
            this.tree = tree;
        else
            throw new IllegalArgumentException("The tree points to null.");
        plotter = IAnalysisFactory.create().createPlotterFactory().create();
        if (graphicsFormat != null)
            this.graphicsFormat = graphicsFormat;
        if (outputDir != null)
            this.outputDir = outputDir;
    }

    /**
     * Write the AIDA objects from the current ITree to a hierarchy of directories.
     */
    public void writeToDir() {
        configurePlotterStyle(plotter);
        File absDir = outputDir.getAbsoluteFile();
        for (String path : tree.listObjectNames()) {
            String[] objectNames = tree.listObjectNames(path);
            for (String objectName : objectNames) {
                IManagedObject object = tree.find(objectName);                
                plotter.createRegion();
                IPlotterRegion region = plotter.region(0);
                if (object instanceof IBaseHistogram)
                    region.plot((IBaseHistogram) object);
                else if (object instanceof IDataPointSet)
                    region.plot((IDataPointSet) object);
                else if (object instanceof IFunction)
                    region.plot((IFunction) object);
                else if (object instanceof IPlottable)
                    region.plot((IPlottable) object);

                File plotFile = new File(absDir + File.separator + objectName + "." + graphicsFormat);
                File plotDir = plotFile.getParentFile();
                if (!plotDir.exists())
                    plotDir.mkdirs();
                
                try {
                    System.out.println("Writing [" + objectName + "] to [" + plotFile.getAbsolutePath() + "]");
                    plotter.writeToFile(plotFile.getAbsolutePath(), graphicsFormat);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                plotter.destroyRegions();
            }
        }
    }
    
    /**
     * Configure style settings on the plotter.
     * @param plotter the AIDA plotter object
     */
    private static void configurePlotterStyle(IPlotter plotter) {
        
        IPlotterStyle style = plotter.style();        
        IDataStyle dataStyle = style.dataStyle();
        IAxisStyle xAxisStyle = style.xAxisStyle();
        IAxisStyle yAxisStyle = style.yAxisStyle();
        ITitleStyle titleStyle = style.titleStyle();
        ILegendBoxStyle legendBoxStyle = style.legendBoxStyle();
        
        dataStyle.lineStyle().setColor("black");
        dataStyle.lineStyle().setVisible(true);
        dataStyle.errorBarStyle().setVisible(false);
        dataStyle.fillStyle().setVisible(false);
        dataStyle.markerStyle().setColor("black");
        dataStyle.markerStyle().setSize(1);
        dataStyle.markerStyle().setShape("0");

        xAxisStyle.lineStyle().setThickness(1);
        xAxisStyle.labelStyle().setBold(true);
        xAxisStyle.labelStyle().setFont("Arial");
        xAxisStyle.labelStyle().setFontSize(12);
        
        yAxisStyle.lineStyle().setThickness(1);
        yAxisStyle.labelStyle().setBold(true);
        yAxisStyle.labelStyle().setFont("Arial");
        yAxisStyle.labelStyle().setFontSize(12);

        titleStyle.boxStyle().borderStyle().setVisible(true);
        titleStyle.boxStyle().borderStyle().setThickness(1);
        titleStyle.textStyle().setFont("Arial");
        titleStyle.textStyle().setBold(true);
        titleStyle.textStyle().setFontSize(16);
                               
        legendBoxStyle.setVisible(false);
    }
}