package org.lcsim.util.loop;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.freehep.record.loop.DefaultRecordLoop;
import org.freehep.record.loop.RecordLoop.Command;
import org.freehep.record.source.NoSuchRecordException;
import org.freehep.record.source.RecordSource;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManagerImplementation;
import org.lcsim.conditions.ConditionsReader;
import org.lcsim.event.util.LCSimEventGenerator;
import org.lcsim.util.Driver;
import org.lcsim.util.DriverAdapter;

/**
 * A main loop which can be used for standalone jobs (run outside of JAS).
 * @author Tony Johnson
 */
public class LCSimLoop extends DefaultRecordLoop {

    private Driver top = new Driver();

    /**
     * Create a new LCSimLoop
     */
    public LCSimLoop() {
        // Is there no global conditions manager installed yet?
        if (!ConditionsManager.isSetup()) 
            // Register a default conditions manager, which can still be overridden later if needed.
            LCSimConditionsManagerImplementation.register();
        DriverAdapter da = new DriverAdapter(top);
        addRecordListener(da);
        addLoopListener(da);
    }

    /**
     * Add a driver to the loop. The Driver's will be called for each event, in the order in which
     * they are added.
     * @param driver The driver to add to the loop.
     */
    public void add(Driver driver) {
        top.add(driver);
    }

    /**
     * Remove a driver previously added to the loop.
     * @param driver The driver to be removed.
     */
    public void remove(Driver driver) {
        top.remove(driver);
    }

    /**
     * Set the event source to be a give LCIO file.
     * @param file The LCIO file from which to read.
     * @throws java.io.IOException If the file cannot be opened.
     */
    public void setLCIORecordSource(File file) throws IOException {
        super.setRecordSource(new LCIOEventSource(file));
    }

    /**
     * Read events from the given LCIO event source
     * @param src The source from which to read events.
     * @throws java.io.IOException If an exception occurs while opening the file.
     */
    public void setLCIORecordSource(LCIOEventSource src) throws IOException {
        super.setRecordSource(src);
    }

    /**
     * Read events from the given stdhep file. Events will be converted to lcsim events as they are
     * read.
     * @param file The file to read
     * @param detectorName The detector name to be added to the lcsim event.
     * @throws java.io.IOException If an problem occurs when opening the file.
     * @see org.lcsim.util.loop.StdhepEventSource
     */
    public void setStdhepRecordSource(File file, String detectorName) throws IOException {
        super.setRecordSource(new StdhepEventSource(file, detectorName));
    }

    /**
     * Set the event source to be an event generator.
     * @param gen The event generator used to generate events.
     */
    public void setRecordSource(LCSimEventGenerator gen) {
        super.setRecordSource(new EventGeneratorRecordSource(gen, "generator"));
    }

    /**
     * Skip a given number of events.
     * @param recordsToSkip The number of events to skip.
     * @throws org.freehep.record.source.NoSuchRecordException If there are insufficient events
     *         available.
     * @throws java.io.IOException If an IO exception occurs when reading an event
     * @throws IllegalArgumentException if the argument is negative.
     */
    public void skip(long recordsToSkip) throws NoSuchRecordException, IOException {
        if (recordsToSkip < 0L)
            throw new IllegalArgumentException();
        if (recordsToSkip == 0L)
            return;
        RecordSource rs = getRecordSource();
        if (rs.supportsIndex()) {
            try {
                rs.jump(rs.getCurrentIndex() + recordsToSkip);
                return;
            } catch (IllegalStateException x) {
            }
        }
        for (long i = 0; i < recordsToSkip; i++)
            rs.next();
    }

    /**
     * Loop over a given number of events, or until no more events are available. Statistics will
     * be printed to standard output the end of the loop.
     * 
     * @param number The number of events to loop over, or <CODE>-1</CODE> to loop until no more
     *        data is available.
     * 
     * @throws IllegalStateException If there is a problem iterating over events (for example no
     *         event source specified).
     * @throws IOException If there is an IO exception reading or writing events.
     * 
     * @return The number of events actually iterated over.
     */
    public long loop(long number) throws IOException {
        return loop(number, System.out);
    }

    /**
     * Loop for the given number of events, optionally printing statistics to the given output
     * stream
     * 
     * @param number The number of events to loop over, or <CODE>-1</CODE> to loop until no more
     *        data is available.
     * 
     * @throws IllegalStateException If there is a problem iterating over events (for example no
     *         event source specified).
     * @throws IOException If there is an IO exception reading or writing events.
     * 
     * @return The number of events actually iterated over.
     */
    public long loop(long number, PrintStream out) throws IOException {
        top.clearStatistics();
        if (number < 0L) {
            execute(Command.GO, true);
        } else {
            execute(Command.GO_N, number, true);
            execute(Command.STOP); // make sure endOfData() is called on drivers
        }
        Throwable t = getProgress().getException();
        if (t != null && t instanceof IOException)
            throw (IOException) t;
        if (out != null)
            printStatistics(out);
        return getSupplied();
    }

    /**
     * Print the statistics from the last loop call.
     * @param out The PrintStream on which to print statistics.
     */
    public void printStatistics(PrintStream out) {
        top.printStatistics(out);
    }

    /**
     * Set a dummy detector.
     * @param detectorName The name of the dummy detector.
     */
    public void setDummyDetector(String detectorName) {
        ConditionsManager cond = ConditionsManager.defaultInstance();
        ConditionsReader dummyReader = ConditionsReader.createDummy();
        ((ConditionsManagerImplementation) cond).setConditionsReader(dummyReader, detectorName);
        DummyDetector detector = new DummyDetector(detectorName);
        cond.registerConditionsConverter(new DummyConditionsConverter(detector));
    }
    
    public void dispose() {
        super.dispose();
    }
    
    protected void handleClientError(final Throwable x) {
        //System.out.println("LCSimLoop caught client error ...");
        x.printStackTrace();
        this._exception = x;
        this.execute(Command.STOP);
        throw new RuntimeException(x);
    }
    
    protected void handleSourceError(final Throwable x) {
        //System.out.println("LCSimLoop caught source error ...");
        x.printStackTrace();
        this._exception = x;
        this.execute(Command.STOP);
        throw new RuntimeException(x);
    }
       
    public Throwable getLastException() {
        return this._exception;
    }
}
