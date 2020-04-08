package org.lcsim.job;

import org.freehep.record.loop.RecordEvent;
import org.freehep.record.loop.RecordListener;
import org.lcsim.event.EventHeader;

/**
 * Prints out information from event processing such as the event number at a specified interval.
 * 
 * @author Jeremy McCormick, SLAC
 */
public class EventPrintLoopAdapter implements RecordListener {

    /**
     * Sequence number of events processed.
     */
    private long eventSequence = 0;
    
    /**
     * Event print interval which means every Nth event will be printed.
     */
    private long printInterval = 1;

    /**
      * Start time for event timing.
      */
    private long startTime = -1;
    
    /**
     * Class constructor.
     * @param printInterval the event print interval
     */
    public EventPrintLoopAdapter(long printInterval) {
        this.printInterval = printInterval;
    }

    /**
     * Set the event print interval
     * @param printInterval the event print interval
     */
    public void setPrintInterval(long printInterval) {
        this.printInterval = printInterval;
    }

    /**
     * Process an event and print the event information.
     */
    @Override
    public void recordSupplied(RecordEvent recordEvent) {
        Object record = recordEvent.getRecord();
        if (record instanceof EventHeader) { 
            ++eventSequence;
            EventHeader event = (EventHeader) recordEvent.getRecord();
            if (eventSequence % printInterval == 0) {
                long elapsed = 0;
                double hz = 0;
                double millisPerEvent = 0;
                if (startTime > 0) {
                    elapsed = System.nanoTime() - startTime;
                    hz = (double)printInterval / ((double)elapsed / 1e9d);
                    millisPerEvent = ((double)elapsed / 1e6d ) / (double)printInterval;
                }
                System.out.printf("Event: %d, Sequence: %d, Timestamp: %d, %.2f ms/event, %.2f Hz%n",
                        event.getEventNumber(), eventSequence, event.getTimeStamp(),
                        millisPerEvent, hz);
                startTime = System.nanoTime();
            }
        }
    }
}
