package org.lcsim.job;

import java.util.logging.Logger;

import org.freehep.record.loop.RecordEvent;
import org.freehep.record.loop.RecordListener;
import org.lcsim.event.EventHeader;

/**
 * Prints out information from event processing such as the event number at a specified interval.
 * 
 * @author Jeremy McCormick, SLAC
 */
class EventPrintLoopAdapter implements RecordListener {

    /**
     * Setup the logger.
     */
    private static Logger LOGGER = Logger.getLogger(EventPrintLoopAdapter.class.getName());

    /**
     * Sequence number of events processed.
     */
    private long eventSequence = 0;
    
    /**
     * Event print interval which means every Nth event will be printed.
     */
    private long printInterval = 1;

    /**
     * Class constructor.
     * @param printInterval the event print interval
     */
    EventPrintLoopAdapter(long printInterval) {
        this.printInterval = printInterval;
    }

    /**
     * Process an event and print the event information.
     */
    @Override
    public void recordSupplied(RecordEvent recordEvent) {
        Object record = recordEvent.getRecord();
        if (record instanceof EventHeader) {
            EventHeader event = (EventHeader) recordEvent.getRecord();
            if (eventSequence % printInterval == 0) {
                LOGGER.info("event: " + event.getEventNumber() + "; time: " + event.getTimeStamp() + "; seq: " 
                        + eventSequence);
            }
            ++eventSequence;
        }
    }
}