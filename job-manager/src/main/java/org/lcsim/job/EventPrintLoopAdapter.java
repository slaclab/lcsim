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
     * Job start time for averaging.
     */
    private long jobStartTime = -1;
    
    /**
     * Class constructor.
     * @param printInterval the event print interval
     */
    public EventPrintLoopAdapter(long printInterval) {
        this.printInterval = printInterval;
    }

    /**
     * Set the event print interval.
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
        printEventMessage(recordEvent);
    }

    /**
     * Print the event message.
     * @param recordEvent the current event being processed
     */
    private void printEventMessage(RecordEvent recordEvent) {
        if(jobStartTime < 0) jobStartTime = System.nanoTime();
        Object record = recordEvent.getRecord();
        if (record instanceof EventHeader) { 
            ++eventSequence;
            EventHeader event = (EventHeader) recordEvent.getRecord();
            if (eventSequence % printInterval == 0) {
                long elapsed = 0;
                long endTime = System.nanoTime();
                double rate = 0;
                double avgRage = 0;
                double millisPerEvent = 0;
                if (startTime > 0) {
                    elapsed = endTime - startTime;
                    rate = (double)printInterval / ((double)elapsed / 1e9d);
                    avgRage = (double)eventSequence / ((double)(endTime - jobStartTime) / 1e9d);
                    millisPerEvent = ((double)elapsed / 1e6d ) / (double)printInterval;
                }
                System.out.printf("Event: %8d, Run: %5d, Sequence: %7d, %.2f ms/event, %.2f Hz, Avg: %.2f Hz%n",
                        event.getEventNumber(), event.getRunNumber(), eventSequence,
                        millisPerEvent, rate, avgRage);
                startTime = System.nanoTime();
            }
        }
    }
}
