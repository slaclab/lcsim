package org.lcsim.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.lcio.LCIOWriter;

/**
 * Driver to merge multiple consecutive events into a single event.
 * No Driver should be run after this one, since it will be seeing partially merged events.
 *
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 */
public class MergeEventsDriver extends Driver {
	
	protected int nEvts;
	protected String outputFile;
	protected EventHeader currentEvent;
	protected int currentEventNumber;
	protected LCIOWriter writer;
	protected Collection<String> ignoreCollections;
	protected boolean writeOnlyFullEvents;
	protected Map<String, Map<Long, SimCalorimeterHit>> caloHitMaps;
	
	public MergeEventsDriver() {
		nEvts = 1;
		ignoreCollections = new ArrayList<String>();
		writeOnlyFullEvents = false;
	}
	
	/**
	 * Number of events merged into one event. A negative number will merge all events
	 * into a single event.
	 * @param nEvts number of events
	 */
	public void setNumberOfEvents(int nEvts) {
		this.nEvts = nEvts;
	}
	
	/**
	 * Decides if an event at the end of the sample which has less than the desired number
	 * of events is written to the output file
	 * @param writeOnlyFullEvents
	 */
	public void setWriteOnlyFullEvents(boolean writeOnlyFullEvents) {
		this.writeOnlyFullEvents = writeOnlyFullEvents;
	}
	
	/**
	 * Defines the output file
	 * @param outputFile
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	public void setIgnoreCollection(String collectionName) {
		this.ignoreCollections.add(collectionName);
	}
	
	public void setIgnoreCollections(String[] collectionNames) {
		this.ignoreCollections.addAll(Arrays.asList(collectionNames));
	}
	
	@Override
	protected void startOfData() {
		try {
			writer = new LCIOWriter(outputFile);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		writer.addAllIgnore(ignoreCollections);
		// map to store the cell IDs of all calorimeter hits to decide which hits have to be merged.
		caloHitMaps = new HashMap<String, Map<Long,SimCalorimeterHit>>();
		currentEventNumber = 0;
	};
	
	@Override
	protected void process(EventHeader event) {
		if (currentEventNumber == 0 && nEvts != 1) {
			currentEvent = event;
		} else if (nEvts > 0 && currentEventNumber % nEvts == 0) {
			writeCurrentEvent();
			currentEvent = event;
			caloHitMaps.clear();
		} else {
			MergeEventTools.mergeEvents(currentEvent, event, ignoreCollections, caloHitMaps);
		}
		currentEventNumber++;
	}
	
	@Override
	protected void endOfData() {
		if (!writeOnlyFullEvents || (nEvts > 0 && currentEventNumber % nEvts == 0)) {
			writeCurrentEvent();
		}
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void writeCurrentEvent() {
		if (currentEvent != null) {
			try {
				writer.write(currentEvent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
