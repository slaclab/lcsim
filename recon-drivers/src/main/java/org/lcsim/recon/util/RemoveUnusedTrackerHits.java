package org.lcsim.recon.util;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.Track;
import org.lcsim.event.TrackerHit;
import org.lcsim.util.Driver;

/**
 * Driver to filter and remove all tracker hits from a given track collection that
 * did not contribute to any track. In addition, allows to clear the RawTrackerHit
 * lists to allow writing out events without those collections.
 * 
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 * 
 */
public class RemoveUnusedTrackerHits extends Driver {

	protected String trackHitCollection;
	protected String trackCollection;
	protected boolean clearRawTrackerHits;
	
	public RemoveUnusedTrackerHits() {
		trackHitCollection = "HelicalTrackHits";
		trackCollection = "Tracks";
		clearRawTrackerHits = true;
	}
	
	public void setTrackCollection(String trackCollection) {
		this.trackCollection = trackCollection;
	}
	
	public void setTrackHitCollection(String trackHitCollection) {
		this.trackHitCollection = trackHitCollection;
	}
	
	public void setClearRawTrackerHits(boolean clearRawTrackerHits) {
		this.clearRawTrackerHits = clearRawTrackerHits;
	}
	
	@Override
	protected void process(EventHeader event) {
		List<TrackerHit> trackerHits = event.get(TrackerHit.class, trackHitCollection);
		List<Track> tracks = event.get(Track.class, trackCollection);
		List<TrackerHit> unusedTrackerHits = new ArrayList<TrackerHit>();
		unusedTrackerHits.addAll(trackerHits);
		
		for (Track track : tracks) {
			unusedTrackerHits.removeAll(track.getTrackerHits());
		}
		
		trackerHits.removeAll(unusedTrackerHits);
		
		if (clearRawTrackerHits) {
			for (TrackerHit hit : trackerHits) {
				hit.getRawHits().clear();
			}
		}
	}
	
}
