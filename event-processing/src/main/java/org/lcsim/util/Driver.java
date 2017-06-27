package org.lcsim.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lcsim.conditions.ConditionsManager;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.Detector;

/**
 * A driver is a steering routine which can deal with event processing, and/or call any number of child drivers. 
 * When used as a child driver all parameters such as histogramLevel and logger are inherited from the parent driver.
 * <p>
 * It also allows controlling the histogram level of the processors being called and handles coordination of random numbers 
 * between Monte Carlo processors.
 *
 * @author Tony Johnson
 * @version $Id: Driver.java,v 1.14 2007/09/11 00:21:00 tonyj Exp $
 */

public class Driver {
    
    // We dont use an enum, because we want to be able to test for level>some value.
    // and because drivers can use special values for special purposes.
    public final static int HLEVEL_DEFAULT = -1;
    public final static int HLEVEL_OFF = 0;
    public final static int HLEVEL_NORMAL = 1;
    public final static int HLEVEL_HIGH = 3;
    public final static int HLEVEL_FULL = 5;

    private static Driver mother = new MotherOfAllDrivers();
    private final List<Driver> subDrivers = new ArrayList<Driver>();
    private Driver parent = mother;
    private int histogramLevel = HLEVEL_DEFAULT;
    private Random random;
    private final String driverName;
    private int nEvents;
    private long nNanos;

    /**
     * Creates a driver
     */
    public Driver() {
        this(null);
    }

    Driver(String name) {
        if (name == null || name.length() == 0) {
            String id = getClass().getName();
            int pos = id.lastIndexOf('.');
            driverName = id.substring(pos < 0 ? 0 : pos + 1);
        } else {
            driverName = name;
        }
    }

    /**
     * Add a sub-Driver to this Driver. Sub-drivers are automatically called from the process method.
     * @param driver The Driver to be added
     */
    public void add(Driver driver) {
        subDrivers.add(driver);
        driver.parent = this;
    }

    /**
     * Removes a sub-Driver from this Driver
     * @param driver The Driver to be removed
     */
    public void remove(Driver driver) {
        subDrivers.remove(driver);
        driver.parent = mother;
    }

    /**
     * Returns a List of all the drivers added to this Driver
     */
    public List<Driver> drivers() {
        return subDrivers;
    }

    /**
     * Tests to see if a given Driver is already a child of this Driver     
     * @param driver Driver to be checked
     */
    public boolean contains(Driver driver) {
        return subDrivers.contains(driver);
    }

    /**
     * Returns a logger for logging diagnostic messages from this driver
     */
    public Logger getLogger() {
        //return Logger.getLogger(pathToMother());
        return Logger.getLogger(getClass().getName());
    }

    /**
     * Get the name of this driver. Normally this will be the class name of the driver (without the packaging information).
     */
    public String getName() {
        return driverName;
    }

    String pathToMother() {
        return parent.pathToMother() + "." + driverName;
    }

    /**
     * Get the default histogram level for this driver
     */
    public int getHistogramLevel() {
        return histogramLevel <= HLEVEL_DEFAULT ? parent.getHistogramLevel() : histogramLevel;
    }

    /**
     * Set the histogram level for this driver (and its child drivers)
     */
    public void setHistogramLevel(int level) {
        histogramLevel = level;
    }

    public ConditionsManager getConditionsManager() {
        return parent.getConditionsManager();
    }

    /**
     * Called by the framework when event processing is suspended.
     */
    protected void suspend() {
        for (Driver driver : subDrivers)
            driver.suspend();
    }

    /**
     * Called by the framework when event processing is resumed.
     */
    protected void resume() {
        for (Driver driver : subDrivers)
            driver.resume();
    }

    /**
     * Called when all data processing is finished.
     */
    protected void endOfData() {
        for (Driver driver : subDrivers)
            driver.endOfData();
    }

    /**
     * Called before the first event is processed, or after a rewind.
     */
    protected void startOfData() {
        for (Driver driver : subDrivers)
            driver.startOfData();
    }

    /**
     * Called by the framework before process method when the detector geometry changes. This method is gauranteed to be called once before the first call to process.
     * 
     * @param Detector The new detector
     */
    protected void detectorChanged(Detector detector) {
        for (Driver driver : subDrivers)
            driver.detectorChanged(detector);
    }

    /**
     * Called by the framework to process an event. 
     * Don't forget to call <code>super.process(event)</code> to cause the child processes to be executed. 
     * In addition the process event call can throw some special exceptions:
     * <ul>
     * <li>NextEventException - aborts further processing of this event</li>
     * <li>StopRunException - causes event processing to be stopped</li>
     * </ul>
     *
     * @param event The event to be processed
     * @see Driver.NextEventException
     * @see Driver.AbortRunException
     */
    protected void process(EventHeader event) {
        processChildren(event);
    }

    /**
     * Clear statistics
     */
    public void clearStatistics() {
        nEvents = 0;
        nNanos = 0;
    }

    /**
     * Print statistics for this driver and its children
     */
    public void printStatistics(PrintStream out) {
        printStatistics(out, 0, 0);
    }

    private void printStatistics(PrintStream out, int indent, long parentNanos) {
        printStatisticsLine(out, indent, getName(), nEvents, nNanos, parentNanos);
        if (!subDrivers.isEmpty()) {
            int nIndent = indent + 1;
            long self = nNanos;
            for (Driver driver : subDrivers) {
                driver.printStatistics(out, nIndent, nNanos);
                self -= driver.nNanos;
            }
            printStatisticsLine(out, nIndent, "*self", nEvents, self, nNanos);
        }
    }

    private static void printStatisticsLine(PrintStream out, int indent, String name, int nEvents, long time, long parentTime) {
        out.print(formatName(indent, name, 40));
        out.print(' ');
        out.print(nEvents);
        out.print(' ');
        out.printf(formatTime(time));
        if (parentTime > 0) {
            out.print(' ');
            out.printf("%3.1f", 100. * time / parentTime);
            out.print('%');
        }
        out.println();
    }

    private static String formatName(int indent, String name, int width) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++)
            builder.append("   ");
        builder.append(name);
        if (builder.length() > width)
            builder.setLength(width);
        else
            for (int i = builder.length(); i < width; i++)
                builder.append(' ');
        return builder.toString();
    }

    private static String formatTime(long nanos) {
        String unit = "ms";
        double time = nanos / 1000000.;
        if (time > 1000) {
            time /= 1000;
            unit = "s";
        }
        java.util.Formatter formatter = new java.util.Formatter();
        formatter.format("%3.3g", time);
        formatter.format("%s", unit);
        return formatter.toString();
    }

    void doProcess(EventHeader event) {
        nEvents++;
        long start = System.nanoTime();
        process(event);
        long stop = System.nanoTime();
        nNanos += (stop - start);
    }

    /**
     * Calls the sub-Drivers process() method. <b>Note:</b> This method is only public so that it can be called from Jython, see LCSIM-30
     */
    public void processChildren(EventHeader event) {
        for (Driver driver : subDrivers)
            driver.doProcess(event);
    }

    public Random getRandom() {
        return random == null ? parent.getRandom() : random;
    }

    /**
     * Set default random number generator for this driver and all child drivers.
     * 
     * @param random The random number generator, or <code>null</code> to reset to default
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    public void setLogLevel(String logLevel) {
        this.getLogger().setLevel(Level.parse(logLevel));
    }

    // The only driver that does not have a parent
    // This is used to set defaults for "inherited" items
    private static class MotherOfAllDrivers extends Driver {
        private Random random = new Random();

        MotherOfAllDrivers() {
            super("TOP");
        }

        public Random getRandom() {
            return random;
        }

        public int getHistogramLevel() {
            return 0;
        }

        public ConditionsManager getConditionsManager() {
            return ConditionsManager.defaultInstance();
        }

        String pathToMother() {
            return getName();
        }
    }
   
    /**
     * If thrown during the process method of a driver, causes processing of the current event to be aborted. Event procssing skips immediately to the next event.
     */
    public static class NextEventException extends RuntimeException {
        public NextEventException() {
            super("Next Event");
        }
    }

    /**
     * If thrown during the process method of a driver, causes processing of events to be aborted.
     */
    public static class AbortRunException extends RuntimeException {
        public AbortRunException() {
            super("Abort Run");
        }
    }
}