package org.lcsim.job;

import hep.aida.ref.BatchAnalysisFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.freehep.record.loop.RecordEvent;
import org.freehep.record.loop.RecordListener;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.lcsim.event.EventHeader;
import org.lcsim.units.Constants;
import org.lcsim.util.Driver;
import org.lcsim.util.DriverAdapter;
import org.lcsim.util.cache.FileCache;
import org.lcsim.util.loop.LCIOEventSource;
import org.lcsim.util.loop.LCSimLoop;
import org.lcsim.util.xml.ClasspathEntityResolver;
import org.lcsim.util.xml.JDOMExpressionFactory;

/**
 * <p>
 * This class provides a front end for running and managing LCSim event processing jobs using XML steering files.
 * <p>
 * More details about this XML format can be found at the<br/>
 * <a href="https://confluence.slac.stanford.edu/display/ilc/lcsim+xml">LCSim XML Confluence Page</a>.
 * <p>
 * The command line syntax is:<br/>
 * <code>java org.lcsim.job.JobManager steeringFile.xml [options]</code>
 * <p>
 * To see the available command line options with descriptions, run with "-h" as the only option.
 * <p>
 * Command-line parameters that can be defined using switches are overridden by the corresponding settings in the job
 * XML file, if they are present. This means that if these parameters are to be taken from the CL, the matching settings
 * should be left out of the XML job file. This is not the case, however, for input files specified by the "-i" option,
 * which are appended to the ones listed in the steering file.
 *
 * @version $Id: JobControlManager.java,v 1.65 2013/02/14 22:30:07 jeremy Exp $
 * @author Jeremy McCormick
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class JobControlManager {

    /**
     * Initialize the logger which uses the package name.
     */
    protected static final Logger LOGGER = Logger.getLogger(JobControlManager.class.getPackage().getName());

    /**
     * The command line options.
     */
    private Options options;

    /**
     * The regular expression for extracting the variables from an XML file.
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[$][{][a-zA-Z_-]*[}]");
    
    /**
     * Default conditions system setup.
     */
    protected ConditionsSetup conditionsSetup = new DefaultConditionsSetup();

    /**
     * Create the command line options.
     *
     * @return The command line options for the manager.
     */
    protected Options createCommandLineOptions() {
        options = new Options();
        options.addOption(new Option("h", "help", false, "Print help and exit"));
        options.addOption(new Option("p", "properties", true, "Load a properties file containing variable definitions"));
        options.addOption(new Option("D", "define", true, "Define a variable with form [name]=[value]"));
        options.addOption(new Option("w", "rewrite", true, "Rewrite the XML file with variables resolved"));
        options.addOption(new Option("s", "skip", true, "Set the number of events to skip"));
        options.addOption(new Option("n", "nevents", true, "Set the max number of events to process"));
        options.addOption(new Option("x", "dry-run", false, "Perform a dry run which does not process events"));
        options.addOption(new Option("i", "input-file", true, "Add an LCIO input file to process"));
        options.addOption(new Option("r", "resource", false, "Use a steering resource rather than a file"));
        options.addOption(new Option("b", "batch", false, "Run in batch mode in which plots will not be shown."));
        options.addOption(new Option("e", "event-print", true, "Event print interval"));
        options.addOption(new Option("d", "detector", true, "user supplied detector name (careful!)"));
        options.addOption(new Option("R", "run", true, "user supplied run number (careful!)"));
        return options;
    }
    
    public Options getOptions() {
        return options;
    }
        
    /**
     * Get the Java primitive type class from a type name.
     *
     * @param name The name of the type.
     * @return The primitive type class.
     */
    private static Class getPrimitiveType(final String name) {
        if (name.equals("byte")) {
            return byte.class;
        }
        if (name.equals("short")) {
            return short.class;
        }
        if (name.equals("int")) {
            return int.class;
        }
        if (name.equals("long")) {
            return long.class;
        }
        if (name.equals("char")) {
            return char.class;
        }
        if (name.equals("float")) {
            return float.class;
        }
        if (name.equals("double")) {
            return double.class;
        }
        if (name.equals("boolean")) {
            return boolean.class;
        }
        if (name.equals("String")) {
            return String.class;
        }
        return null;
    }

    /**
     * Run from the command line.
     * <p>
     * Takes command-line options (use -h option to see them).
     *
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        final JobControlManager mgr = new JobControlManager();
        mgr.parse(args);
        mgr.run();
    }

    /**
     * Print help and exit.
     */
    private void printHelp() {
        LOGGER.info("java " + JobControlManager.class.getCanonicalName() + " [options] steeringFile.xml");
        final HelpFormatter help = new HelpFormatter();
        help.printHelp(" ", options);
        System.exit(1);
    }

    /**
     * Root directory for file caching.
     */
    private File cacheDirectory;

    /**
     * The class loader that will be used for the job.
     */
    private ClassLoader classLoader;

    /**
     * Command line that is setup from <code>main</code> arguments.
     */
    CommandLine commandLine = null;

    /**
     * Map of constants definitions.
     */
    private final Map<String, Double> constantsMap = new HashMap<String, Double>();

    /**
     * User supplied detector name.
     */
    private String detectorName = null;

    /**
     * A driver adapter created on the fly in case it is needed by an external program.
     */
    private DriverAdapter driverAdapter = null;

    /**
     * List of drivers to execute in the job.
     */
    private final List<Driver> driverExec = new ArrayList<Driver>();

    /**
     * Map of driver names to objects.
     */
    private final Map<String, Driver> driverMap = new LinkedHashMap<String, Driver>();

    /**
     * Enable dry run so no events are processed.
     */
    private boolean dryRun = false;

    /**
     * Setup a "dummy" detector in the conditions system.
     */
    //private boolean dummyDetector;

    /**
     * Event printing interval (null means no event printing).
     */
    private Long eventPrintInterval = null;

    /**
     * JDOM expression factory for variables.
     */
    private final JDOMExpressionFactory factory = new JDOMExpressionFactory();

    /**
     * File cache.
     */
    private FileCache fileCache; // Start with default dir.

    /**
     * List of input LCIO files.
     */
    private final List<File> inputFiles = new ArrayList<File>();

    /**
     * Flag set to <code>true</code> after setup is performed.
     */
    private boolean isSetup;

    /**
     * The job end timestamp in ms.
     */
    private long jobEnd = 0;

    /**
     * The job start timestamp in ms.
     */
    private long jobStart = 0;

    /**
     * The LCIO record loop.
     */
    private LCSimLoop loop;

    /**
     * Number of events to run before stopping job.
     */
    private int numberOfEvents = -1;

    /**
     * Helper for converting Driver parameters.
     */
    private final ParameterConverters paramConverter = new ParameterConverters(factory);

    /**
     * Set to <code>true</code> to print out driver statistics at the end of the job.
     */
    private boolean printDriverStatistics;

    /**
     * Path for rewriting steering file with variables resolved.
     */
    private File rewriteFile;

    /**
     * Enable rewriting of the steering file to a new path with variables resolved.
     */
    private boolean rewriteSteering;

    /**
     * The root node of the XML document providing config to the manager.
     */
    private Element root;

    /**
     * User supplied run number.
     */
    private Integer runNumber = null;

    /**
     * Number of events to skip at start of job.
     */
    private int skipEvents = -1;

    /**
     * Interpret steering file argument as a resource rather than file path.
     */
    private boolean useSteeringResource;

    /**
     * Map of variable names to their values.
     */
    private final Map<String, String> variableMap = new HashMap<String, String>();

    /**
     * The default constructor.
     */
    public JobControlManager() {
        try {
            fileCache = new FileCache();
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Add a Driver to the internal Driver map.
     *
     * @param name the unique name of the Driver
     * @param driver the instance of the Driver
     */
    private void addDriver(final String name, final Driver driver) {
        if (driverMap.containsKey(name)) {
            throw new RuntimeException("Duplicate driver name: " + name);
        }
        driverMap.put(name, driver);
    }

    /**
     * Add an input LCIO file to be proceesed.
     *
     * @param inputFile The input LCIO file.
     */
    public void addInputFile(final File inputFile) {
        if (isSetup) {
            throw new RuntimeException("Input files cannot be added when manager has already been setup.");
        }
        inputFiles.add(inputFile);
    }

    /**
     * Add a variable definition to be substituted into the job's XML file. This method is public so that caller's not
     * using the CL can still define necessary variables for the steering file.
     *
     * @param key The variable name.
     * @param value The variable's value.
     */
    public void addVariableDefinition(final String key, final String value) {
        LOGGER.config(key + " = " + value);
        if (!this.variableMap.containsKey(key)) {
            variableMap.put(key, value);
        } else {
            throw new RuntimeException("Duplicate variable definition: " + key);
        }
    }

    /**
     * Configure start of job (usually done automatically).
     */
    public void configure() {
        this.getDriverAdapter().start(null);
    }

    /**
     * Create a driver adapter.
     */
    private void createDriverAdapter() {
        if (this.isSetup == false) {
            throw new IllegalStateException("The job manager was never setup.");
        }
        final Driver topDriver = new Driver();
        for (final Driver driver : this.getDriverExecList()) {
            topDriver.add(driver);
        }
        driverAdapter = new DriverAdapter(topDriver);
    }

    /**
     * Create the <code>Driver</code> execution list.
     */
    private void createDriverExecList() {
        // Make a list of Drivers to be executed.
        final List<Element> exec = root.getChild("execute").getChildren("driver");
        for (final Element execDriver : exec) {
            final String driverName = execDriver.getAttributeValue("name");
            final Driver driverFind = driverMap.get(driverName);
            if (driverFind != null) {
                driverExec.add(driverFind);
            } else {
                throw new RuntimeException("A Driver called " + driverName + " was not found.");
            }
        }

        // Add the drivers to the LCSimLoop.
        for (final Driver driver : driverExec) {
            loop.add(driver);
        }
    }

    /**
     * Turn on the batch analysis factory so plots are not shown on the screen even when <code>Plotter.show()</code> is
     * called.
     */
    public void enableHeadlessMode() {
        System.setProperty("hep.aida.IAnalysisFactory", BatchAnalysisFactory.class.getName());
    }

    /**
     * Activate end of job hooks (usually done automatically).
     */
    public void finish() {
        this.getDriverAdapter().finish(null);
    }

    /**
     * Get a <code>DriverAdapter</code> from the currently configured Driver list.
     *
     * @return the driver adapter
     */
    public DriverAdapter getDriverAdapter() {
        if (driverAdapter == null) {
            // Driver adapter created on demand.
            this.createDriverAdapter();
        }
        return driverAdapter;
    }

    /**
     * Return a list of Drivers to be executed. This can be used from an external framework like JAS3. The list will be
     * empty unless the <code>setup()</code> method has been called.
     *
     * @return A <code>List</code> of <code>Drivers</code>.
     */
    public List<Driver> getDriverExecList() {
        return this.driverExec;
    }

    /**
     * Get the <code>LCSimLoop</code> of this JobManager.
     *
     * @return The LCSimLoop.
     */
    public LCSimLoop getLCSimLoop() {
        return loop;
    }

    /**
     * Get a list of a class's setter methods.
     *
     * @param klass The class.
     * @return A list of setter methods.
     */
    private List<Method> getSetterMethods(final Class klass) {
        final List<Method> methods = new ArrayList<Method>();
        Class currentClass = klass;
        while (currentClass != null) {
            for (final Method method : currentClass.getMethods()) {
                if (method.getName().startsWith("set") && !methods.contains(method)) {
                    methods.add(method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return methods;
    }

    /**
     * Initialize the <code>LCSimLoop</code>.
     */
    private void initializeLoop() {
        LOGGER.config("initializing LCSim loop");
        loop = new LCSimLoop();
        if (this.eventPrintInterval != null) {
            loop.addRecordListener(new EventPrintLoopAdapter(this.eventPrintInterval));
            LOGGER.config("Added EventPrintLoopAdapter with event print interval " + eventPrintInterval);
        } else {
            LOGGER.config("EventPrintLoopAdapter is disabled.");
        }
    }

    /**
     * Parse command-line options and setup job state from them. This method calls {@link #setup(File)} to load the
     * steering paramters from an XML file, after processing other command line options. This method is private so that
     * callers must all use the {@link #main(String[])} routine as the primary entry point.
     *
     * @param args The command line arguments.
     */
    public CommandLine parse(final String args[]) {
        
        LOGGER.config("parsing command line arguments");

        // Create command line options.
        createCommandLineOptions();
        
        // Setup parser.
        final CommandLineParser parser = new PosixParser();

        // Parse the command line arguments.
        try {
            commandLine = parser.parse(options, args);
        } catch (final ParseException x) {
            throw new RuntimeException("Problem parsing command line options.", x);
        }
        
        // Print help and exit.
        if (args.length == 0 || commandLine.hasOption("h")) {
            printHelp();
        }

        // Load a properties file containing variable definitions.
        if (commandLine.hasOption("p")) {
            final String[] propValues = commandLine.getOptionValues("p");
            for (final String propFileName : propValues) {
                InputStream in = null;
                try {
                    in = new FileInputStream(propFileName);
                } catch (final FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                final Properties props = new Properties();
                try {
                    props.load(in);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
                for (final Entry<Object, Object> entry : props.entrySet()) {
                    final String key = (String) entry.getKey();
                    final String value = (String) entry.getValue();
                    this.addVariableDefinition(key, value);
                }
                LOGGER.config("loaded variable definitions from " + propFileName);
            }
        }

        // Process the user variable definitions.
        if (commandLine.hasOption("D")) {
            final String[] defValues = commandLine.getOptionValues("D");
            for (final String def : defValues) {
                final String[] s = def.split("=");
                if (s.length != 2) {
                    throw new RuntimeException("Bad variable format: " + def);
                }
                final String key = s[0];
                final String value = s[1];
                this.addVariableDefinition(key, value);
                LOGGER.config("defined " + key + " = " + value);
            }
        }

        // Rewrite XML file with variables resolved.
        if (commandLine.hasOption("w")) {
            this.rewriteSteering = true;
            final String rewritePath = commandLine.getOptionValue("w");
            this.rewriteFile = new File(rewritePath);
            if (this.rewriteFile.exists()) {
                throw new RuntimeException("Rewrite file already exists: " + rewritePath);
            }
            LOGGER.config("XML will be rewritten to " + this.rewriteFile.getPath());
        }

        // Set max number of events to run.
        if (commandLine.hasOption("n")) {
            this.numberOfEvents = Integer.valueOf(commandLine.getOptionValue("n"));
            LOGGER.config("max number of events set to " + this.numberOfEvents);
        }

        // Set number of events to skip.
        if (commandLine.hasOption("s")) {
            this.skipEvents = Integer.valueOf(commandLine.getOptionValue("s"));
            LOGGER.config("skip events set to " + this.skipEvents);
        }

        // Perform a dry run, not processing any events but doing job setup.
        if (commandLine.hasOption("x")) {
            this.dryRun = true;
            LOGGER.config("dry run is enabled");
        }

        // Interpret steering argument as a resource rather than file path.
        if (commandLine.hasOption("r")) {
            this.useSteeringResource = true;
            LOGGER.config("steering resource enabled");
        }

        // Check that there is exactly one extra argument for the XML steering file.
        if (commandLine.getArgList().size() == 0) {
            throw new RuntimeException("Missing LCSim XML file argument.");
        } else if (commandLine.getArgList().size() > 1) {
            throw new RuntimeException("Too many extra arguments.");
        }

        // Local LCIO files to process.
        if (commandLine.hasOption("i")) {
            final String[] files = commandLine.getOptionValues("i");
            for (final String fileName : files) {
                final File file = new File(fileName);
                if (!file.exists()) {
                    throw new RuntimeException("File given as command line option does not exist: " + fileName);
                }
                inputFiles.add(new File(fileName));
                LOGGER.config("added input file " + fileName);
            }
        }

        // Run in headless mode in which plots will not show.
        if (commandLine.hasOption("b")) {
            this.enableHeadlessMode();
            LOGGER.config("headless mode enabled");
        }

        // Steering argument points to either a file or embedded resource.
        final String steering = (String) commandLine.getArgList().get(0);

        if (commandLine.hasOption("e")) {
            this.eventPrintInterval = Long.parseLong(commandLine.getOptionValue("e"));
            LOGGER.config("eventPrintInterval: " + this.eventPrintInterval);
            if (this.eventPrintInterval <= 0) {
                throw new IllegalArgumentException("The event print interval must be > 0.");
            }
        }

        if (commandLine.hasOption("d")) {
            this.detectorName = commandLine.getOptionValue("d");
            LOGGER.config("detector: " + this.detectorName);
        }

        if (commandLine.hasOption("R")) {
            this.runNumber = Integer.parseInt(commandLine.getOptionValue("R"));
            LOGGER.config("runNumber: " + this.runNumber);
        }

        if (this.detectorName != null && this.runNumber == null || 
                this.runNumber != null && this.detectorName == null) {
            throw new IllegalArgumentException("The detector name and run number must be given together.");
        }
        
        // This will actually initialize everything from the steering file so it must come last.
        if (this.useSteeringResource) {
            // Using an embedded resource in the jar for steering.
            LOGGER.config("initializing from steering resource " + steering);
            this.setup(steering);
        } else {
            // Steering from a local file.
            final File xmlRunControlFile = new File(steering);
            if (!xmlRunControlFile.exists()) {
                throw new RuntimeException("The steering file " + args[0] + " does not exist!");
            }
            LOGGER.config("initializing from steering file " + xmlRunControlFile.getPath());
            this.setup(xmlRunControlFile);
        }
        
        return commandLine;
    }

    /**
     * Print the list of input files.
     */
    private void printInputFileList() {
        final StringBuffer sb = new StringBuffer();
        sb.append('\n');
        sb.append("--- Input Files ---");
        for (final File file : inputFiles) {
            sb.append(file.getAbsolutePath());
            sb.append('\n');
        }
        LOGGER.config(sb.toString());
    }

    /**
     * Print out extra URLs added to the classpath from the XML.
     */
    private void printUserClasspath() {
        final StringBuffer sb = new StringBuffer();
        final URL[] urls = ((URLClassLoader) classLoader).getURLs();
        if (urls.length > 0) {
            for (final URL url : ((URLClassLoader) classLoader).getURLs()) {
                sb.append(url + " ");
            }
            sb.append('\n');
            LOGGER.config("Extra classpath URLs:" + sb.toString());
        }
    }

    /**
     * Create the constants from the XML file.
     */
    private void processConstants() {
        final Element define = root.getChild("define");
        if (define != null) {
            for (final Object o : define.getChildren()) {
                final Element e = (Element) o;
                final Text txt = (Text) e.getContent().get(0);
                final double dval = factory.computeDouble(txt.getValue());
                this.constantsMap.put(e.getName(), dval);
                factory.addConstant(e.getName(), dval);
            }
        }
    }

    /**
     * A fairly ugly method to process the provided XML parameters on a <code>Driver</code>.
     *
     * @param driverClass the Java class of the driver
     * @param newDriver the instantiated Driver
     * @param parameters the list of XML parameters
     */
    private void processDriverParameters(final Class driverClass, final Driver newDriver, final List<Element> parameters) {
        // Process the parameter elements.
        for (final Element parameterElement : parameters) {

            // The parameter's setter method that we will try to find.
            Method setter = null;

            // The parameter's type that will be inferred from the method or provided by an XML attribute.
            Class propertyType = null;

            // Get the parameter's name.
            final String pname = parameterElement.getName();

            // Find setter methods that look like good matches for this parameter.
            final List<Method> methods = this.getSetterMethods(driverClass);
            final List<Method> methodCandidates = new ArrayList<Method>();
            for (final Method method : methods) {
                String propHack = method.getName().replaceFirst("set", "");
                propHack = propHack.substring(0, 1).toLowerCase() + propHack.substring(1);
                if (propHack.equals(pname)) {
                    methodCandidates.add(method);
                }
            }
            if (methodCandidates.size() == 1) {
                // Found the single setter method so try to use it.
                setter = methodCandidates.get(0);
                if (setter.getParameterTypes().length > 1) {
                    throw new RuntimeException("The set method has too many arguments for parameter: " + pname);
                }
                propertyType = setter.getParameterTypes()[0];
            } else if (methodCandidates.size() > 1) {
                // Found several, overloaded methods. Try to disambiguate them if possible.
                if (parameterElement.getAttribute("type") == null) {
                    throw new RuntimeException("Parameter " + pname + " in Driver " + driverClass.getCanonicalName()
                            + " is overloaded, but a type field is missing from the parameter's XML element.");
                }
                try {
                    // Try a primitive type first.
                    propertyType = getPrimitiveType(parameterElement.getAttribute("type").getValue());

                    // If type is null, then parameter is an Object and not a primitive, or it
                    // is not a valid type.
                    if (propertyType == null) {
                        propertyType = Class.forName(parameterElement.getAttribute("type").getValue());
                    }
                } catch (final ClassNotFoundException x) {
                    throw new RuntimeException("Bad type " + parameterElement.getAttribute("type").getValue()
                            + " given for parameter " + pname + ".");
                }
                // Find a method that matches the user type.
                for (final Method candidateMethod : methodCandidates) {
                    if (candidateMethod.getParameterTypes().length == 1
                            && candidateMethod.getParameterTypes()[0].equals(propertyType)) {
                        setter = candidateMethod;
                        break;
                    }
                }
            } else if (methodCandidates.size() == 0) {
                // No method found. The parameter name is probably invalid.
                throw new RuntimeException("Set method for Driver parameter " + pname + " was not found.");
            }

            // No setter method found.
            if (setter == null) {
                throw new RuntimeException("Unable to find set method for parameter " + pname + ".");
            }

            // Convert the parameter to the appropriate type.
            final IParameterConverter converter = paramConverter.getConverterForType(propertyType);
            if (converter == null) {
                throw new RuntimeException("No converter found for parameter " + parameterElement.getName()
                        + " with type " + propertyType.getName() + ".");
            }
            final Object nextParameter = converter.convert(factory, parameterElement);

            // Call the setter with the parameter as argument.
            final Object pargs[] = new Object[1];
            pargs[0] = nextParameter;
            try {
                // This invokes the setter method of the driver.
                setter.invoke(newDriver, pargs);

                // Print parameters and values as they are set.
                LOGGER.fine("    " + pname + " = " + parameterElement.getText().trim());
                
            } catch (final Exception x) {
                throw new RuntimeException("Problem processing parameter " + parameterElement.getName() + ".", x);
            }
        } // parameter loop
    }

    /**
     * Process a single event by calling the record listeners on the loop.
     *
     * @param event the input event
     */
    public void processEvent(final EventHeader event) {        
        for (RecordListener listener : loop.getRecordListeners()) {
            listener.recordSupplied(new RecordEvent(loop, event));
        }
    }

    /**
     * Create a <code>File</code> object from the text in an XML element.
     *
     * @param fileElement The element containing a file path or URL.
     * @param fileList List to append new <code>File</code>.
     * @return The <code>File</code> object.
     */
    private File processFileElement(final Element fileElement, final List<File> fileList) {

        final String fileLoc = this.processPath(fileElement.getText().trim());
        File file = null;

        // Try to process the file text as a URL.
        try {
            final URL fileURL = new URL(fileLoc);

            // Local file URL.
            if (fileLoc.startsWith("file:")) {
                file = new File(fileURL.getPath());
            } else {
                // Remote file URL.
                try {
                    file = this.fileCache.getCachedFile(fileURL);
                } catch (final IOException x) {
                    throw new RuntimeException("Unable to fetch file " + fileLoc + " to the cache directory.", x);
                }
            }
        } catch (final MalformedURLException x) {
            // Interpret as local file.
            file = new File(fileLoc);
        }

        // Add to the list.
        if (fileList != null) {
            fileList.add(file);
        }

        return file;
    }

    /**
     * Cleanup file text and add to the file list.
     *
     * @param fileText the text containing the file's path from the XML
     * @param fileList the list of files to which the new file will be appended
     * @return the file that was created from the text
     */
    private File processFileText(final String fileText, final List<File> fileList) {
        final Element fileElement = new Element("file");
        fileElement.setText(fileText.trim());
        return this.processFileElement(fileElement, fileList);
    }

    /**
     * Process the file path string to substitute in the user's home directory for the "~" character. This is needed if
     * running on Windows.
     *
     * @param path The original path.
     * @return The path with home dir substitution.
     */
    private String processPath(final String path) {
        if (path.startsWith("~")) {
            return path.replaceFirst("~", System.getProperty("user.home"));
        } else {
            return path;
        }
    }

    /**
     * Rewrite the XML steering file after resolving variables (done externally).
     * <p> 
     * The output path is set by command line argument to "-w" option.
     *
     * @param doc The XML steering doc with variables substituted.
     */
    private void rewriteXMLSteering(final Document doc) {
        LOGGER.info("Rewriting XML to " + this.rewriteFile);
        final XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        try {
            final FileOutputStream out = new FileOutputStream(this.rewriteFile);
            outputter.output(doc, out);
            out.close();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute a job using the current parameters.
     */
    public void run() {

        LOGGER.info("running job");

        // If setup was not called first, then abort the job.
        if (!isSetup) {
            throw new IllegalStateException("The job manager was never setup!");
        }

        if (!dryRun) {

            // Setup dummy detector if selected.
            // if (dummyDetector) {
            // LOGGER.info("Using dummy detector for conditions system!");
            // loop.setDummyDetector("dummy");
            // }

            this.jobStart = System.currentTimeMillis();

            LOGGER.info("Job started: " + new Date(jobStart));

            try {
                
                // Add the LCIO files to the loop.
                loop.setLCIORecordSource(new LCIOEventSource(this.getClass().getSimpleName(), inputFiles));

                // Setup the conditions system by calling setDetector if run and detector name are set.
                conditionsSetup.setup();

                // Post-init of conditions system (by default does nothing).
                conditionsSetup.postInitialize();
                
                // Skip events.
                if (this.skipEvents > 0) {
                    LOGGER.info("Skipping " + skipEvents + " events ...");
                    loop.skip(skipEvents);
                    LOGGER.info("Done skipping events.");
                }
                
                // Execute the loop.
                final long processedEvents = loop.loop(numberOfEvents, this.printDriverStatistics ? System.out : null);
                
                // Check if there was an error that was caught which stopped event processing.
                if (loop.getLastException() != null) {
                    LOGGER.log(Level.SEVERE, "Job was stopped due to an event processing error.", loop.getLastException());
                }

                // Print some job stats.
                LOGGER.info("Job processed " + processedEvents + " events.");
                this.jobEnd = System.currentTimeMillis();
                LOGGER.info("Job ended: " + new Date(this.jobEnd));
                final long elapsed = this.jobEnd - this.jobStart;                
                LOGGER.info("Job took " + (elapsed / 1000.) + " seconds.");
                if (processedEvents > 0) {
                    LOGGER.info("Event processing took " + ((double) elapsed / (double) processedEvents) + " ms/event or " 
                            + ((double) processedEvents/((double) elapsed / 1000.)) + " events/second.");
                } else {
                    LOGGER.warning("No events were processed!");
                }
                
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "A fatal error occurred when running the job.", e);
            } finally {
                try {
                    loop.dispose();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } else {
            // Dry run selected. No events will be processed.
            LOGGER.info("Executed dry run.  No events processed!");
        }
    }

    /**
     * Set whether a dry run should be performed which will only perform setup and not process any events.
     *
     * @param dryRun <code>true</code> to enable a dry run
     */
    public void setDryRun(final boolean dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * Set the number of events to run on the loop before ending the job. This should be called after the
     * {@link #setup(File)} method is called or it will be overridden.
     */
    public void setNumberOfEvents(final int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    /**
     * Setup the job parameters from an XML Document.
     * <p>
     * This method contains the primary logic for setting up job parameters from an XML file. The other setup methods
     * such as {@link #setup(InputStream)}, {@link #setup(String)} and {@link #setup(File)} all call this method.
     *
     * @param xmlDocument The lcsim recon XML document describing the job.
     */
    private void setup(final Document xmlDocument) {

        // This method should not be called more than once.
        if (isSetup) {
            throw new IllegalStateException("The job manager was already setup.");
        }

        // Set the root element from the XML document.
        root = xmlDocument.getRootElement();

        // Do variable substitutions into the document first.
        this.substituteVariables(xmlDocument);

        // Rewrite XML after variable substitution.
        if (this.rewriteSteering) {
            this.rewriteXMLSteering(xmlDocument);
        }

        // Setup the job control parameters.
        this.setupJobControlParameters();

        // Setup the class loader.
        this.setupClassLoader();

        // Setup system of units.
        this.setupUnits();

        // Process the constant definitions.
        this.processConstants();

        // Initialize the LCSimLoop.
        this.initializeLoop();
        
        // Configure the conditions system before it is initialized.
        conditionsSetup.configure();
        
        // Setup drivers with parameters and execution order.
        this.setupDrivers();
                        
        // Setup the file cache.
        this.setupFileCache();

        // Setup the input files.
        this.setupInputFiles();

        // Throw an error if there were no files provided and dry run is not enabled.
        if (inputFiles.size() == 0 && !this.dryRun) {
            LOGGER.severe("No input files provided and dry run is not enabled.");
            throw new IllegalStateException("No input files to process.");
        }

        // Flag JobManager as setup.
        isSetup = true;
    }

    /**
     * Setup job parameters from a <code>File</code>.
     *
     * @param file the path to the XML file
     */
    public void setup(final File file) {
        try {
            this.setup(new FileInputStream(file));
        } catch (final FileNotFoundException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Setup job parameters from an <code>InputStream</code> that should be valid XML text.
     *
     * @param in the XML input stream
     */
    public void setup(final InputStream in) {

        // Make the Document builder.
        final SAXBuilder builder = new SAXBuilder();

        // Setup XML schema validation.
        builder.setEntityResolver(new ClasspathEntityResolver());
        builder.setValidation(true);
        builder.setFeature("http://apache.org/xml/features/validation/schema", true);

        // Setup expression resolution.
        builder.setFactory(factory);

        // Build the document.
        Document doc = null;
        try {
            doc = builder.build(in);
        } catch (final Exception x) {
            throw new RuntimeException(x);
        }

        // Setup the JobControlManager from the XML file.
        this.setup(doc);
    }

    /**
     * Setup job parameters from an embedded resource. This method calls {@link #setup(InputStream)}.
     */
    public void setup(final String resourceURL) {
        this.setup(this.getClass().getResourceAsStream(resourceURL));
    }

    /**
     * Setup the manager's class loader.
     */
    private void setupClassLoader() {

        if (classLoader != null) {
            LOGGER.info("The ClassLoader was already set externally, so custom classpaths will be ignored!");
            return;
        }

        final Element classpath = root.getChild("classpath");
        final List<URL> urlList = new ArrayList<URL>();
        if (classpath != null) {
            for (final Object jarObject : classpath.getChildren("jar")) {
                final Element jarElement = (Element) jarObject;
                try {
                    urlList.add(new File(this.processPath(jarElement.getText())).toURI().toURL());
                } catch (final Exception x) {
                    throw new RuntimeException("Bad jar location: " + jarElement.getText(), x);
                }
            }
            for (final Object jarUrlObject : classpath.getChildren("jarUrl")) {
                final Element jarUrlElement = (Element) jarUrlObject;
                try {
                    urlList.add(new URL(jarUrlElement.getText()));
                } catch (final Exception x) {
                    throw new RuntimeException("Bad jar URL: " + jarUrlElement.getText(), x);
                }
            }
            for (final Object cpDirObject : classpath.getChildren("directory")) {
                final Element cpDirElement = (Element) cpDirObject;
                try {
                    final File cpFile = new File(this.processPath(cpDirElement.getText()));
                    if (!cpFile.isDirectory()) {
                        throw new RuntimeException("The classpath component " + cpFile.getPath()
                                + " is not a valid directory!");
                    }
                    urlList.add(cpFile.toURI().toURL());
                } catch (final Exception x) {
                    throw new RuntimeException("Bad classpath directory: " + cpDirElement.getText(), x);
                }
            }
        }
        final URL[] urls = urlList.toArray(new URL[] {});

        classLoader = new LCSimClassLoader(urls);

        // Print extra user classpath entries.
        this.printUserClasspath();
    }

    /**
     * Create the drivers from the XML.
     */
    protected void setupDrivers() {

        // Loop over the list of driver elements.
        final List<Element> drivers = root.getChild("drivers").getChildren("driver");
        for (final Element driver : drivers) {

            // Get the name of the Driver.
            final String name = driver.getAttributeValue("name");

            // Get the fully qualified type of the Driver. ([packageName].[className])
            final String type = driver.getAttributeValue("type");

            // Get the Java class of the Driver.
            Class driverClass;
            try {
                driverClass = classLoader.loadClass(type);
            } catch (final ClassNotFoundException x) {
                throw new RuntimeException("The Driver class " + type + " was not found.", x);
            }

            LOGGER.fine("adding driver " + driverClass.getCanonicalName());

            // Create an instance of the driver.
            Driver newDriver;
            try {
                newDriver = (Driver) driverClass.newInstance();
            } catch (final InstantiationException x) {
                throw new RuntimeException("Failed to create a Driver of class " + type + ".", x);
            } catch (final IllegalAccessException x) {
                throw new RuntimeException("Cannot access Driver type " + type + ".", x);
            }

            // Get the list of Driver parameters from the XML.
            final List<Element> parameters = driver.getChildren();

            // Process the parameters provided for the driver.
            this.processDriverParameters(driverClass, newDriver, parameters);

            // Add the driver to the manager.
            this.addDriver(name, newDriver);

        } // driver loop

        // Make the list of drivers to execute.
        this.createDriverExecList();
    }

    /**
     * Setup the file cache.
     */
    private void setupFileCache() {
        if (fileCache == null) {
            try {
                fileCache = new FileCache();                
                // Set cache dir from setting in steering, if present.
                if (cacheDirectory != null) {
                    fileCache.setCacheDirectory(cacheDirectory);
                }
                fileCache.setPrintStream(null);
                LOGGER.config("File cache created at " + fileCache.getCacheDirectory().getPath());
            } catch (final IOException x) {
                throw new RuntimeException(x);
            }
        }
    }

    /**
     * Setup the list of input files to be processed from the XML job file.
     */
    private void setupInputFiles() {

        if (root.getChild("inputFiles") == null) {
            // This is not a warning because input files can be provided via the command line.
            LOGGER.config("No input files in XML file.");
            return;
        }

        // Process the <file> elements.
        final List<Element> files = root.getChild("inputFiles").getChildren("file");
        for (final Element fileElem : files) {
            this.processFileElement(fileElem, this.inputFiles);
        }

        // Read lists of file locations given by <fileList> elements.
        final List<Element> fileLists = root.getChild("inputFiles").getChildren("fileList");
        for (final Element fileList : fileLists) {
            final String filePath = fileList.getText();
            BufferedReader input;
            try {
                input = new BufferedReader(new FileReader(new File(filePath)));
            } catch (final FileNotFoundException x) {
                throw new RuntimeException("File not found: " + filePath, x);
            }
            String line = null;
            try {
                // Read the next file, turn the text into an XML element, and process it using
                // common method.
                while ((line = input.readLine()) != null) {
                    this.processFileText(line.trim(), inputFiles);
                }
            } catch (final IOException x) {
                throw new RuntimeException(x);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Process <fileSet> elements.
        final List<Element> fileSets = root.getChild("inputFiles").getChildren("fileSet");
        for (final Element fileSet : fileSets) {
            final Attribute basedirAttrib = fileSet.getAttribute("baseDir");
            String basedir = "";
            if (basedirAttrib != null) {
                basedir = basedirAttrib.getValue();
            }
            final List<Element> fsFiles = fileSet.getChildren("file");
            for (final Element file : fsFiles) {
                final String filePath = basedir + File.separator + file.getText().trim();
                this.processFileText(filePath, inputFiles);
            }
        }

        // Read <fileRegExp> elements, which may only reference local files, not URLs.
        final List<Element> fileRegExps = root.getChild("inputFiles").getChildren("fileRegExp");
        for (final Element fileRegExp : fileRegExps) {
            final Pattern pattern = Pattern.compile(fileRegExp.getText());
            final String basedir = fileRegExp.getAttributeValue("baseDir");
            final File dir = new File(basedir);
            if (!dir.isDirectory()) {
                throw new RuntimeException(basedir + " is not a valid directory!");
            }
            final String dirlist[] = dir.list();
            for (final String file : dirlist) {
                if (file.endsWith(".slcio")) {
                    final Matcher matcher = pattern.matcher(file);
                    if (matcher.matches()) {
                        this.processFileText(basedir + File.separator + file, inputFiles);
                        LOGGER.fine("Matched file <" + file.toString() + "> to pattern <" + pattern.toString() + ">");
                    } else {
                        LOGGER.fine("Did NOT match file <" + file.toString() + "> to pattern <" + pattern.toString()
                                + ">");
                    }
                }
            }
        }

        // Check that all the files exist if job is not a dry run.
        if (!dryRun) {
            for (final File file : inputFiles) {
                if (!file.exists()) {
                    LOGGER.info("The input file " + file.getAbsolutePath() + " does not exist.");
                    throw new RuntimeException("The input file " + file.getAbsolutePath() + " does not exist!");
                }
            }
        }

        // Print out the input file list.
        this.printInputFileList();

        if (inputFiles.size() == 0 && !this.dryRun) {
            throw new RuntimeException("No input files were provided by the steering file or command line options.");
        }
    }

    /**
     * Setup the job control parameters using the XML steering document.
     */
    private void setupJobControlParameters() {

        final Element control = root.getChild("control");
        
        if (control == null) {
            // The control element is optional.
            return;
        }

        // Print hello world message to appear at top of log.
        LOGGER.config(this.getClass().getCanonicalName() + " is initialized.");

        // Number of events to run.
        final Element controlElement = control.getChild("numberOfEvents");
        if (controlElement != null) {
            numberOfEvents = Integer.valueOf(controlElement.getText());
            LOGGER.config("numberOfEvents: " + numberOfEvents);
        }

        final Element skipElement = control.getChild("skipEvents");
        if (skipElement != null) {
            skipEvents = Integer.valueOf(skipElement.getText());
            LOGGER.config("skipEvents: " + skipEvents);
        }

        final Element dryRunElement = control.getChild("dryRun");
        if (dryRunElement != null) {
            dryRun = Boolean.valueOf(dryRunElement.getText());
            LOGGER.config("dryRun: " + dryRun);
        }

        // Cache directory for downloading files (can be null which is okay).
        final Element cacheDirElement = control.getChild("cacheDirectory");
        if (cacheDirElement != null) {
            cacheDirectory = new File(cacheDirElement.getText());
            if (!cacheDirectory.exists()) {
                throw new RuntimeException("cacheDirectory does not exist at location: " + cacheDirElement.getText());
            }
        } 

        LOGGER.config("cacheDirectory: " + cacheDirectory);

        final Element printStatisticsElement = control.getChild("printDriverStatistics");
        if (printStatisticsElement != null) {
            printDriverStatistics = Boolean.valueOf(printStatisticsElement.getText());
            LOGGER.config("printDriverStatistics: " + printDriverStatistics);
        }
 
        //final Element dummyDetectorElement = control.getChild("dummyDetector");
        //if (dummyDetectorElement != null) {
        //    dummyDetector = Boolean.valueOf(dummyDetectorElement.getText());
        //    LOGGER.config("dummyDetector: " + dummyDetector);
        //}
    }

    /**
     * Setup the system of units.
     */
    private void setupUnits() {
        final Constants constants = Constants.getInstance();
        for (final Entry<String, Double> unit : constants.entrySet()) {
            factory.addConstant(unit.getKey(), unit.getValue());
            LOGGER.finest(unit.getKey() + " = " + unit.getValue());
        }
    }

    /**
     * Perform variable substitution within all text data in a entire document.
     *
     * @param doc The XML document.
     */
    private void substituteVariables(final Document doc) {
        this.substituteVariables(doc.getRootElement());
    }

    /**
     * Substitute values from the <code>variableMap</code> into an XML element and all its children, recursively.
     *
     * @param element The XML element.
     * @throw RuntimeException If a variable does not exist in the <code>variableMap</code>.
     */
    private void substituteVariables(final Element element) {

        final String text = element.getTextNormalize();

        if (text.length() != 0) {

            // Create a new matcher.
            final Matcher match = VARIABLE_PATTERN.matcher(text);

            // No variables were used.
            if (!match.find()) {
                return;
            }

            // Text data on which to perform substitutions.
            String newText = new String(text);

            // Reset the matcher.
            match.reset();

            // Loop over the matches.
            while (match.find()) {

                // Variable string with ${...} enclosure included.
                final String var = match.group();

                // The name of the variable for lookup.
                final String varName = var.substring(2, var.length() - 1);

                // The value of the variable.
                final String varValue = variableMap.get(varName);

                // If a variable was not defined, then the application will immediately exit here.
                if (varValue == null) {
                    throw new RuntimeException("Required variable was not defined: " + varName);
                }

                // Substitute this variable's value into the text.
                newText = newText.replace(var, varValue);
            }

            // Set this element's new text value.
            element.setText(newText);
        }

        // Recursively process all child elements of this one.
        for (final Iterator it = element.getChildren().iterator(); it.hasNext();) {
            this.substituteVariables((Element) it.next());
        }
    }
    
    public void setConditionsSetup(ConditionsSetup conditionsSetup) {
        this.conditionsSetup = conditionsSetup;
    }
    
    public ConditionsSetup getConditionsSetup() {
        return this.conditionsSetup;
    }
    
    public void setEventPrintInterval(long eventPrintInterval) {
        this.eventPrintInterval = eventPrintInterval;
    }
}
