package org.lcsim.recon.tracking.digitization.sisim.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.Detector;
import org.lcsim.recon.tracking.digitization.sisim.CDFSiSensorSim;
import org.lcsim.recon.tracking.digitization.sisim.GenericReadoutChip;
import org.lcsim.recon.tracking.digitization.sisim.Kpix;
import org.lcsim.recon.tracking.digitization.sisim.NearestNeighbor;
import org.lcsim.recon.tracking.digitization.sisim.PixelHitMaker;
import org.lcsim.recon.tracking.digitization.sisim.RawTrackerHitMaker;
import org.lcsim.recon.tracking.digitization.sisim.ReadoutChip;
import org.lcsim.recon.tracking.digitization.sisim.SiDigitizer;
import org.lcsim.recon.tracking.digitization.sisim.SiSensorSim;
import org.lcsim.util.Driver;

/**
 * Driver for configuring and running the tracking digitization for strip detectors.
 * Accepts a number of parameters related to clustering and readout chip, as well
 * as a list of subdetectors and the name of the output digit collections. 
 * 
 * @author jeremym
 */
public class PixelDigiSetupDriver extends Driver
{	
	// Output collection names.
	private String rawHitsCollectionName;
	private String trackerHitsCollectionName;
	
	// Subdetector names.
	private List<String> subdetectorNames = new ArrayList<String>();

	// Readout chip parameters.
	String readoutChipType = "generic";
	private double noiseIntercept;
	private double noiseSlope;
	private double noiseThreshold;
	private double readoutNeighborThreshold;
	
	// NN clustering algorithm parameters.
	private double seedThreshold;
	private double neighborThreshold;
	
	// Clustering parameters.	
	private int maxClusterSize = 10;
	
	double oneClusterErr = 1 / Math.sqrt(12);
    double twoClusterErr = 1 / 5;
    double threeClusterErr = 1 / 3;
    double fourClusterErr = 1 / 2;
    double fiveClusterErr = 1;
	
    private SimTrackerHitReadoutDriver readoutDriver;
    
	// List of sensors to process.
	Set<SiSensor> sensorsToProcess = new HashSet<SiSensor>();	
	
	// Objects for running the digitization. These are setup in the detectorChanged() method.
	SiSensorSim sisim = new CDFSiSensorSim();
	ReadoutChip readout;
	SiDigitizer digitizer;
	PixelHitMaker clusterer;
			
	// Setup flag.
	private boolean wasSetup = false;
	
	public PixelDigiSetupDriver()
	{
		readoutDriver = new SimTrackerHitReadoutDriver();
		add(readoutDriver);
	}
	
	public void setReadoutChipType(String readoutChipType)
	{
		this.readoutChipType = readoutChipType;
	}
	
	public void setSubdetectorName(String name)
	{
		if (!subdetectorNames.contains(name))
			subdetectorNames.add(name);
	}	
	
	public void setSubdetectorNames(String names[])
	{
		subdetectorNames.addAll(Arrays.asList(names));
	}
		
	public void setNoiseIntercept(double noiseIntercept)
	{
		this.noiseIntercept = noiseIntercept;
	}
		
	public void setNoiseSlope(double noiseSlope)
	{
		this.noiseSlope = noiseSlope;
	}
	
	public void setNoiseThreshold(double noiseThreshold)
	{
		this.noiseThreshold = noiseThreshold;
	}
	
	public void setReadoutNeighborThreshold(double readoutNeighborThreshold)
	{
		this.readoutNeighborThreshold = readoutNeighborThreshold;
	}
	
	public void setSeedThreshold(double seedThreshold)
	{
		this.seedThreshold = seedThreshold;
	}
	
	public void setNeighborThreshold(double neighborThreshold)
	{
		this.neighborThreshold = neighborThreshold;
	}
	
	public void setMaxClusterSize(int maxClusterSize)
	{
		this.maxClusterSize = maxClusterSize;
	}
		
	public void setOneClusterErr(double oneClusterErr)
	{
		this.oneClusterErr = oneClusterErr;
	}
	
	public void setTwoClusterErr(double twoClusterErr)
	{
		this.twoClusterErr = twoClusterErr;
	}
	
	public void setThreeClusterErr(double threeClusterErr)
	{
		this.threeClusterErr = threeClusterErr;
	}
	
	public void setFourClusterErr(double fourClusterErr)
	{
		this.fourClusterErr = fourClusterErr;
	}
	
	public void setFiveClusterErr(double fiveClusterErr)
	{
		this.fiveClusterErr = fiveClusterErr;
	}
	
	public void setRawHitsCollectionName(String rawHitsCollectionName)
	{
		this.rawHitsCollectionName = rawHitsCollectionName;
	}
	
	public void setTrackerHitsCollectionName(String trackerHitsCollectionName)
	{
		this.trackerHitsCollectionName = trackerHitsCollectionName;
	}
	
	public String getRawHitsCollectionName()
	{
		return rawHitsCollectionName;
	}
	
	public String getTrackerHitsCollectionName()
	{
		return trackerHitsCollectionName;
	}
		
	public void detectorChanged(Detector detector)
	{
		setupReadoutDriver(detector);
		setupDigi();
		super.detectorChanged(detector);
	}
	
	private void setupReadoutDriver(Detector detector)
	{
		List<String> readouts = new ArrayList<String>();
		for (String subdetectorName : subdetectorNames)
		{
			readouts.add(detector.getSubdetector(subdetectorName).getReadout().getName());
		}
		readoutDriver.setCollections(readouts.toArray(new String[] {}));
	}
	
	public void setupDigi()
	{
		if (wasSetup)
			return;
		
		// Setup Kpix or Generic readout chip.
		if (readoutChipType.toLowerCase().equals("kpix"))
		{
			Kpix chip = new Kpix();
			chip.setNoiseThreshold(noiseThreshold);
			chip.setNeighborThreshold(readoutNeighborThreshold);
		}
		else if (readoutChipType.toLowerCase().equals("generic"))
		{
			GenericReadoutChip chip = new GenericReadoutChip();
			chip.setNoiseIntercept(noiseIntercept);
			chip.setNoiseSlope(noiseSlope);
			chip.setNoiseThreshold(noiseThreshold);
			chip.setNeighborThreshold(readoutNeighborThreshold);
			this.readout = chip;
		}
		else
		{
			throw new RuntimeException(readoutChipType + " is not a valid reaodut chip type.");
		}
				
		digitizer = new RawTrackerHitMaker(sisim, readout);
		
		NearestNeighbor clustering = new NearestNeighbor();
		clustering.setSeedThreshold(seedThreshold);
		clustering.setNeighborThreshold(neighborThreshold);
		  
		clusterer = new PixelHitMaker(sisim, readout, clustering);
		clusterer.setMaxClusterSize(maxClusterSize);
		clusterer.SetOneClusterErr(oneClusterErr);
		clusterer.SetTwoClusterErr(twoClusterErr);
		clusterer.SetThreeClusterErr(threeClusterErr);
		clusterer.SetFourClusterErr(fourClusterErr);
		clusterer.SetFiveClusterErr(fiveClusterErr);
		
		// Add driver to run the digitization.
		add(new DigiDriver(
				digitizer,
				clusterer,
				getRawHitsCollectionName(),
				getTrackerHitsCollectionName(),
				subdetectorNames));
		
		wasSetup = true;
	}
	
	public void startOfData()
	{
		super.startOfData();
	}
	
	public void process(EventHeader event)
	{
		super.process(event);		
	}	
}