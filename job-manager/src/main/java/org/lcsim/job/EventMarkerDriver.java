package org.lcsim.job;

import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 * Driver to print markers during event processing.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @deprecated Use built-in command line option of {@link JobManager} instead.
 */
@Deprecated
public class EventMarkerDriver extends Driver {

    private int interval = 1;
    private String marker = "";
    private int nEvents;

    public EventMarkerDriver() {
    }

    public EventMarkerDriver(int interval) {
        this.interval = interval;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setEventInterval(int interval) {
        this.interval = interval;
    }

    protected void process(EventHeader event) {
        if (nEvents % interval == 0) {
            getLogger().info(marker + "Event " + event.getEventNumber() + " with sequence " + nEvents);
        }
        nEvents++;
    }

    protected void endOfData() {
        getLogger().info(nEvents + " events processed in job.");
    }
}
