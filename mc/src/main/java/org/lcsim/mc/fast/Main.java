package org.lcsim.mc.fast;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.freehep.record.source.NoSuchRecordException;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.util.DetectorLocator;
import org.lcsim.mc.fast.MCFast;
import org.lcsim.util.Driver;
import org.lcsim.util.loop.LCIODriver;
import org.lcsim.util.loop.LCSimLoop;
import org.lcsim.util.loop.LCSimConditionsManagerImplementation;
import org.lcsim.mc.fast.util.MonitorStdhep;

/**
 * This is a reworked class from an old main program provided by Tim and Norman. The jet and vertex reconstruction was removed, so this now only runs the Fast MC and writes the output LCIO file.
 * Analysis and reconstruction would be performed in a subsequent lcsim XML job.
 * 
 * @author Norman A. Graf
 * @author Jeremy McCormick
 */
public class Main {
    private static final String defaultDetector = "sidloi3";
    private static final int defaultNumProcess = -1;
    private static final int defaultNumSkip = -1;
    private static final boolean defaultRefPoint000 = true;
    private static final String defaultOutputFileName = "fastmc.slcio";
    private static final boolean defaultBeamSpotConstraint = true;
    private static final boolean defaultSimple = true;
    private static final int defaultSeed = (new Random()).nextInt();
    private static final boolean defaultDebug = false;

    String inputFileName;
    String outputFileName;
    String detector;
    int numToProcess;
    int numToSkip;
    int seed;
    boolean refPoint000;
    boolean beamSpotConstraint;
    boolean simple;
    boolean debug;

    private static Options options = null;

    private Main() {
        createOptions();
    }

    Options getOptions() {
        return options;
    }

    void parse(String[] args) {

        // Create the parser and parse command line options.
        PosixParser parser = new PosixParser();

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException x) {
            System.out.println("Parsing failed: " + x.getMessage());
            usage();
        }

        // Set the parameters from the parsed command line.
        setParameters(cmd);

        // Print out the parameters to System.out.
        printParameters();
    }

    private void setParameters(CommandLine cmd) {
        inputFileName = cmd.getOptionValue("i");
        if (inputFileName == null)
            usage();
        outputFileName = cmd.getOptionValue("o", defaultOutputFileName);
        detector = cmd.getOptionValue("d", defaultDetector);
        numToProcess = Integer.valueOf(cmd.getOptionValue("r", String.valueOf(defaultNumProcess)));
        numToSkip = Integer.valueOf(cmd.getOptionValue("s", String.valueOf(defaultNumSkip)));
        seed = Integer.valueOf(cmd.getOptionValue("m", String.valueOf(defaultSeed)));
        refPoint000 = Boolean.valueOf(cmd.getOptionValue("p", String.valueOf(defaultRefPoint000)));
        beamSpotConstraint = Boolean.valueOf(cmd.getOptionValue("b", String.valueOf(defaultBeamSpotConstraint)));
        simple = Boolean.valueOf(cmd.getOptionValue("S", String.valueOf(defaultSimple)));
        debug = Boolean.valueOf(cmd.getOptionValue("v", String.valueOf(defaultDebug)));
    }

    private void printParameters() {
        System.out.println("Received the following command line parameters:");
        System.out.println('\t' + "inputFileName = " + inputFileName);
        System.out.println('\t' + "outputFileName = " + outputFileName);
        System.out.println('\t' + "detector = " + detector);
        System.out.println('\t' + "events to process = " + numToProcess);
        System.out.println('\t' + "events to skip = " + numToSkip);
        System.out.println('\t' + "seed = " + seed);
        System.out.println('\t' + "refPoint000 = " + refPoint000);
        System.out.println('\t' + "beamSpotConstraint = " + beamSpotConstraint);
        System.out.println('\t' + "simple mode = " + simple);
        System.out.println('\t' + "debug mode = " + debug);
    }

    private void createOptions() {
        options = new Options();
        options.addOption("h", false, "print usage information");
        options.addOption("i", true, "input file");
        options.addOption("o", true, "output file name");
        options.addOption("d", true, "detector name");
        options.addOption("r", true, "number of events to process");
        options.addOption("s", true, "number of events to skip");
        options.addOption("m", true, "random seed");
        options.addOption("p", false, "use default ref point");
        options.addOption("b", false, "use beam spot constraint");
        options.addOption("S", false, "use simple");
        options.addOption("v", false, "print debug info");
    }

    private void usage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(getClass().getCanonicalName(), options);
        System.exit(1);
    }

    private void error(String message) {
        System.out.println(message);
        System.exit(1);
    }

    private void run() throws IOException, NoSuchRecordException {
        // Check existence of detector.
        Detector det = DetectorLocator.findDetector(detector);
        if (det == null) {
            error("Unknown detector: " + detector);
        }

        // Check existence of input file.
        File input = new File(inputFileName);
        if (!input.exists()) {
            error("The input file " + input + " does not exist!");
        }

        // Setup the LCIO output driver.
        LCIODriver writer = null;
        File output = new File(outputFileName);
        if (output.exists()) {
            throw new RuntimeException("Output file already exists!");
        }
        writer = new LCIODriver(output);

        // Initialize Fast MC driver.
        Driver fast = new MCFast(beamSpotConstraint, simple, seed, debug, refPoint000);

        // create the event loop
        LCSimConditionsManagerImplementation.register();
        LCSimLoop loop = new LCSimLoop();
        if (input.getName().contains(".stdhep")) {
            loop.setStdhepRecordSource(input, detector);
        } else {
            loop.setLCIORecordSource(input);
        }

        // Add drivers.
        if (debug) {
            MonitorStdhep analysis = new MonitorStdhep();
            loop.add(analysis);
        }
        loop.add(fast);
        loop.add(writer);

        // Run the job.
        if (numToSkip > 0) {
            System.out.println("skipping " + numToSkip + " events");
            loop.skip(numToSkip);
        }
        loop.loop(numToProcess);
        loop.dispose();
        System.out.println(getClass().getSimpleName() + ": processed " + loop.getTotalSupplied() + " events");
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.parse(args);
        try {
            main.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
