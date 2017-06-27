package org.lcsim.recon.tracking.digitization.sisim.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.event.EventHeader;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.TrackerHit;
import org.lcsim.geometry.Detector;
import org.lcsim.recon.tracking.digitization.sisim.Clusterer;
import org.lcsim.recon.tracking.digitization.sisim.SiDigitizer;
import org.lcsim.recon.tracking.digitization.sisim.SiTrackerHit;
import org.lcsim.util.Driver;
import org.lcsim.lcio.LCIOConstants;

/**
 * Driver for running the tracker digitization.  This
 * driver should be setup as a child driver of a parent
 * which accepts configuration parameters.
 * 
 * @author jeremym
 */
public class DigiDriver extends Driver 
{
	SiDigitizer digitizer;
	Clusterer clusterer;
	
	String rawHitsCollectionName;
	String trackerHitsCollectionName;
	
	List<String> subdetectorNames;
		
	public DigiDriver(
			SiDigitizer digitizer, 
			Clusterer clusterer, 
			String rawHitsCollectionName, 
			String trackerHitsCollectionName,
			List<String> subdetectorNames)
	{
		this.digitizer = digitizer;
		this.clusterer = clusterer;
		this.rawHitsCollectionName = rawHitsCollectionName;
		this.trackerHitsCollectionName = trackerHitsCollectionName;
		this.subdetectorNames = subdetectorNames;
	}
			
	// List of sensors to process.
	Set<SiSensor> sensorsToProcess = new HashSet<SiSensor>();
	
	public void detectorChanged(Detector detector)
	{
		super.detectorChanged(detector);
		setupSensorList(detector);
	}
	
	private void setupSensorList(Detector detector)
	{
		sensorsToProcess.clear();
		
		IDetectorElement detectorDe = detector.getDetectorElement();
		
		for (String subdetectorName : subdetectorNames)
		{
			IDetectorElement subdetectorDe = detectorDe.findDetectorElement(subdetectorName);
			sensorsToProcess.addAll(subdetectorDe.findDescendants(SiSensor.class));
		}
	}
	
	public void process(EventHeader event)
	{
		super.process(event);
					
		List<RawTrackerHit> rawHits = new ArrayList<RawTrackerHit>();
		List<SiTrackerHit> trackerHits = new ArrayList<SiTrackerHit>();
				
		for (SiSensor sensor : sensorsToProcess)
		{
			rawHits.addAll(digitizer.makeHits(sensor));
			trackerHits.addAll(clusterer.makeHits(sensor));
		}			
		
		//int flag = (1 << LCIOConstants.RTHBIT_HITS | 1 << LCIOConstants.TRAWBIT_ID1);
		int flag = (1 << LCIOConstants.TRAWBIT_ID1);
		event.put(rawHitsCollectionName, rawHits, RawTrackerHit.class, flag, toString());
        event.put(trackerHitsCollectionName, trackerHits, TrackerHit.class, 0, toString());
	}
}
