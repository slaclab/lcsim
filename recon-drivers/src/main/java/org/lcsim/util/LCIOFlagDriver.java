package org.lcsim.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.event.Track;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.lcio.LCIOConstants;
import org.lcsim.lcio.LCIOUtil;

/**
 * Driver to allow modification of collection LCIO bits from XML.
 * 
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 */
public class LCIOFlagDriver extends Driver {
	
	protected Map<String, Boolean> subsetCollections;
	protected Map<String, Boolean> transientCollections;
	protected Map<String, Boolean> keepClusterHits;
	protected Map<String, Boolean> keepTrackHits;
	
	public LCIOFlagDriver() {
		subsetCollections = new HashMap<String, Boolean>();
		transientCollections = new HashMap<String, Boolean>();
		keepClusterHits = new HashMap<String, Boolean>();
		keepTrackHits = new HashMap<String, Boolean>();
	}
	
	@Override
	protected void process(EventHeader event) {
		
		for (String collection : subsetCollections.keySet()) {
			getMetaData(event, collection).setSubset(subsetCollections.get(collection));
		}
		
		for (String collection : transientCollections.keySet()) {
			getMetaData(event, collection).setTransient(transientCollections.get(collection));
		}
		
		for (String collection : keepClusterHits.keySet()) {
			LCMetaData metaData = getMetaData(event, collection);
			if (metaData.getType().isAssignableFrom(Cluster.class)) {
				int flags = metaData.getFlags();
				boolean isSet = keepClusterHits.get(collection);
				if (LCIOUtil.bitTest(flags, LCIOConstants.CLBIT_HITS) != isSet) {
					flags = LCIOUtil.bitSet(flags, LCIOConstants.CLBIT_HITS, isSet);
					replaceMetaDataFlags(metaData, flags);
				}
			} else {
				System.out.println(getName()+": "+collection+" is not a Cluster collection. Bit not changed.");
			}
		}
		
		for (String collection : keepTrackHits.keySet()) {
			LCMetaData metaData = getMetaData(event, collection);
			if (metaData.getType().isAssignableFrom(Track.class)) {
				int flags = metaData.getFlags();
				boolean isSet = keepTrackHits.get(collection);
				if (LCIOUtil.bitTest(flags, LCIOConstants.TRBIT_HITS) != isSet) {
					flags = LCIOUtil.bitSet(flags, LCIOConstants.TRBIT_HITS, isSet);
					replaceMetaDataFlags(metaData, flags);
				}
			} else {
				System.out.println(getName()+": "+collection+" is not a Cluster collection. Bit not changed.");
			}
		}
		
	}
	
	public void setSubset(String[] collection) {
		parseAndAdd(subsetCollections, collection);
	}
	
	public void setTransient(String[] collection) {
		parseAndAdd(transientCollections, collection);
	}
	
	public void setKeepClusterHits(String[] collection) {
		parseAndAdd(keepClusterHits, collection);
	}
	
	public void setKeepTrackHits(String[] collection) {
		parseAndAdd(keepTrackHits, collection);
	}
	
	protected void replaceMetaDataFlags(LCMetaData metaData, int flags) {
		EventHeader event = metaData.getEvent();
		String collectionName = metaData.getName();
		Class type = metaData.getType();
		List collection = (List) event.get(collectionName);
		event.remove(collectionName);
		event.put(collectionName, collection, type, flags);
	}
	
	protected LCMetaData getMetaData(EventHeader event, String collection) {
		LCMetaData metaData = null;
		try {
			List list = (List) event.get(collection);
			metaData = event.getMetaData(list);
		} catch (IllegalArgumentException e) {
			System.err.println(getName()+": "+e.getMessage());
		}
		return metaData;
	}
	
	protected void parseAndAdd(Map<String, Boolean> map, String[] collection) {
		if (collection.length != 2) {
			throw new RuntimeException(getName()+": Has to be a String of length 2.");
		}
		String collectionName = collection[0];
		String setString = collection[1];
		Boolean isSet = null;
		if (setString.toLowerCase().equals("true")) {
			isSet = true;
		} else if (setString.toLowerCase().equals("false")) {
			isSet = false;
		} else {
			throw new RuntimeException(getName()+": Second String has to be either \"true\" or \"false\"");
		}
		map.put(collectionName, isSet);
	}
}
