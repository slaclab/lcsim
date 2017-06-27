package org.lcsim.util;

import hep.physics.particle.properties.ParticleType;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;
import hep.physics.vec.SpacePoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.MathException;
//import org.apache.commons.math.distribution.DistributionFactory;
import org.apache.commons.math.distribution.PoissonDistribution;
import org.apache.commons.math.distribution.PoissonDistributionImpl;
import org.freehep.record.source.NoSuchRecordException;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.event.EventHeader;
import org.lcsim.event.GenericObject;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.Hit;
import org.lcsim.event.base.BaseMCParticle;
import org.lcsim.event.base.BaseSimCalorimeterHit;
import org.lcsim.event.base.BaseSimTrackerHit;
import org.lcsim.geometry.Detector;
import org.lcsim.lcio.LCIOConstants;
import org.lcsim.lcio.LCIOUtil;
import org.lcsim.lcio.SIOMCParticle;
import org.lcsim.util.loop.LCIOEventSource;

/**
 * Driver to overlay one or more events from another <i>SLCIO</i> source over the current event.
 * A bunch train can be modeled by setting the number of bunch crossings and the time between
 * those bunch crossings. The number of events overlaid per bunch crossing is drawn from a
 * Poisson distribution and its mpv is set by the weight. Time windows can be set for each
 * collection to model a realistic readout. They control which hits to keep and are applied
 * relative to the time of the original event plus a time of flight correction. A separate
 * collection of <b>McParticles</b> is created only for the particles from the overlaid events.
 * 
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 */
public class OverlayDriver extends Driver {

	static protected double c = 299.792458; // speed of light in mm/ns
	static protected SpacePoint interactionPoint = new SpacePoint(); // assuming 0 0 0 as IP
	//protected DistributionFactory df;
	protected double tofCaloOffset = -0.25; // tolerance for keeping calo hits: tof is calculated to center of cell, but interaction can happen earlier
	protected int bunchCrossings;
	protected double bunchSpacing;
	protected boolean randomSignal;
	protected boolean fullCaloProcessing;
	protected boolean randomizeTrainOverlay;
	protected boolean randomizeOverlay;
	protected boolean signalAtZero;
	protected double signalTime;
	protected int signalBunchCrossing;
	protected String mcOverlayName;
	protected String mcSignalName;
	protected LCIOEventSource overlayEvents;
	protected double overlayWeight;
	protected List<Integer> overlayList;
	protected PoissonDistribution backgroundDistribution;
	protected Map<String, Double> readoutTimes;
	protected Map<String, Map<Long,SimCalorimeterHit>> caloHitMap;
	protected List<MCParticle> overlayMcParticles;
	protected List<MCParticle> allMcParticles;
	protected Map<MCParticle, MCParticle> mcParticleReferences;
	
	// -------------------- Constructors --------------------
	/**
	 * Default constructor
	 */
	public OverlayDriver() {
		//df = DistributionFactory.newInstance();
		bunchCrossings = 1;
		bunchSpacing = 1.;
		randomSignal = true;
		fullCaloProcessing = false;
		signalAtZero = true;
		signalBunchCrossing = -1;
		mcOverlayName = "MCParticles_overlay";
		mcSignalName = "MCParticles_signal";
		overlayWeight = 0;
		overlayList = new ArrayList<Integer>();
		readoutTimes = new HashMap<String, Double>();
		caloHitMap = new HashMap<String, Map<Long,SimCalorimeterHit>>();
		overlayMcParticles = new ArrayList<MCParticle>();
		allMcParticles = new ArrayList<MCParticle>();
		randomizeTrainOverlay = true;
		randomizeOverlay = false;
		mcParticleReferences = new HashMap<MCParticle, MCParticle>();
	}
	
	// -------------------- Steering Parameters --------------------
	/**
	 * Sets the number of bunch crossings in a train. This is the maximum number
	 * of bunch crossings overlaid, independent of readout times.
	 * @param n the number of bunch crossings in a bunch train, default is 1
	 */
	public void setBunchCrossings(int n) {
		if (n > 1) {
			this.bunchCrossings = n;
		} else {
			this.bunchCrossings = 1;
		}
	}
	
	/**
	 * Sets the time between two bunch crossings.
	 * @param t the time between two bunch crossings in ns, default is 1 ns
	 */
	public void setBunchSpacing(double t) {
		if (t > 0.) {
			this.bunchSpacing = t;
		} else {
			this.bunchSpacing = 0.;
		}
	}
	
	/**
	 * Sets the bunch crossing of the signal event. In case of a negative value
	 * the bunch crossing will be selected randomly. In case of a value higher
	 * than the total amount of bunch crossings in a train it will be placed in
	 * the last bunch crossing. Default is a random bunch crossing.
	 * crossing.
	 * @param bunchCrossing the bunch crossing of the signal event
	 */
	public void setSignalBunchCrossing(int bunchCrossing) {
		if (bunchCrossing < 0) {
			this.randomSignal = true;
		} else {
			this.randomSignal = false;
			this.signalBunchCrossing = bunchCrossing;
		}
	}
	
	/**
	 * Sets a name as an identifier for the overlaid events.
	 * It names the sub collection used to identify the overlaid McParticles.
	 * @param name identifier for the overlay events
	 */
	public void setOverlayName(String name) {
		mcOverlayName = "MCParticles_"+name;
	}
	
	/**
	 * Sets a name as an identifier for the signal events.
	 * It names the sub collection used to identify the signal McParticles.
	 * @param name identifier for the overlay events
	 */
	public void setSignalName(String name) {
		mcSignalName = "MCParticles_"+name;
	}
	
	/**
	 * Sets the number of overlay event used per bunch crossing.
	 * The actual number per event is drawn from a Poisson distribution with the
	 * weight being the most probable value of the distribution.
	 * A weight of 0 will instead add one overlay event per bunch crossing.
	 * @param weight the most probable number of overlay events added per bunch crossing
	 */
	public void setOverlayWeight(double weight) {
		overlayWeight = weight;
	}
	
	public void setOverlayFiles(String[] fileList) {
		List<File> files = new ArrayList<File>();
		for (String fileName : fileList) {
			files.add(new File(fileName));
		}
		try {
			LCIOEventSource lcio = new LCIOEventSource("overlay", files);
			overlayEvents = lcio;
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Sets the readout time window for an LCCollection. The list of strings has to have
	 * an exact length of two with the following pattern:
	 * "CollectionName time", where the time is given in ns.
	 * @param collection a string of the form "CollectionName time"
	 */
	public void setReadoutTime(String[] collection) {
		if (collection.length != 2) {
			throw new RuntimeException("setReadoutTime takes a list of exactly two strings");
		}
		readoutTimes.put(collection[0], Double.valueOf(collection[1]));
	}
	
	/**
	 * Removes all readout times that were set.
	 */
	public void clearReadoutTimes() {
		readoutTimes.clear();
	}
	
	/**
	 * Selects if calorimeter hits are treated in full detail or simplified.
	 * The simple mode just takes the time of the first contribution in a SimCalorimeterHit
	 * in order to check if it falls into a relevant time window (keeping all contributions).
	 * The full mode checks all contributions and only keeps those inside the time window.
	 * @param fullCaloProcessing use full detailed mode (default false)
	 */
	public void setFullCaloProcessing(boolean fullCaloProcessing) {
		this.fullCaloProcessing = fullCaloProcessing;
	}
	
	/**
	 * @deprecated replaced by <i>setShuffleTrainOverlay</i>
	 * @param shuffleOverlay
	 */
	public void setShuffleOverlay(boolean shuffleOverlay) {
		this.setRandomizeTrainOverlay(shuffleOverlay);
	}
	
	/**
	 * Selects if the overlay events are randomly placed within the bunch train instead of
	 * in a serial way
	 * @param shuffleOverlay shuffle the overlay events (default true)
	 */
	public void setRandomizeTrainOverlay(boolean randomizeTrainOverlay) {
		this.randomizeTrainOverlay = randomizeTrainOverlay;
	}
	
	/**
	 * Selects if overlay events are read in random order from the overlay event source. 
	 * @param randomizeOverlay
	 */
	public void setRandomizeOverlay(boolean randomizeOverlay) {
		this.randomizeOverlay = randomizeOverlay;
	}
	
	/**
	 * Selects if the time of the signal event is placed at time 0, so that all other
	 * bunch crossings are shifted accordingly. Otherwise the first bunch crossing is
	 * at time 0 and the signal event at the time of its bunch crossing.
	 * @param signalAtZero set time of signal to 0 (default true)
	 */
	public void setSignalAtZero(boolean signalAtZero) {
		this.signalAtZero = signalAtZero;
	}
	
	// -------------------- Driver Interface --------------------
	@Override
	protected void startOfData() {
		if (overlayWeight != 0.) {
		        // backgroundDistribution = df.createPoissonDistribution(overlayWeight);
		        // Changed for compatibility with commons-math 2.2 instead of 1.2.  Needs verification!
                        backgroundDistribution = new PoissonDistributionImpl(overlayWeight);
		}
	}
	
	@Override
	protected void detectorChanged(Detector detector) {
		// nothing to do
	}
	
	@Override
	protected void process(EventHeader event) {
		
		// reset the mcParticle lists
		overlayMcParticles.clear();
		allMcParticles.clear();
		
		// always keep all mc particles from signal
		allMcParticles = event.getMCParticles();
		
		// check if we already have a signal mcParticle collection
		try {
			event.get(MCParticle.class, mcSignalName);
			// we already have one, so the some other instance of this driver has taken care of defining the signal mc particles
		} catch (IllegalArgumentException e) {
			List<MCParticle> signalMcParticles = new ArrayList<MCParticle>();
			signalMcParticles.addAll(allMcParticles);
			
			// keep a sub collection of only signal mc particles
			int signalFlags = event.getMetaData(event.getMCParticles()).getFlags();
			signalFlags = LCIOUtil.bitSet(signalFlags, LCIOConstants.BITSubset, true);
			event.put(mcSignalName, signalMcParticles, MCParticle.class, signalFlags);
		}
		
		// shift the signal event in time according to its BX
		if (randomSignal) {
			signalBunchCrossing = this.getRandom().nextInt(bunchCrossings);
		} else if (signalBunchCrossing >= bunchCrossings) {
			signalBunchCrossing = bunchCrossings -1;
		}
		double signalTime = 0;
		if (!signalAtZero) signalTime = signalBunchCrossing * bunchSpacing;
		if (this.getHistogramLevel() > HLEVEL_OFF) System.out.println("Moving signal event to BX: "+signalBunchCrossing);
		this.moveEventToTime(event, signalTime);
		
		// building a list of all bunch crossings in this train
		for (int bX = 0; bX != bunchCrossings; bX++) {
			int nBackgroundEvts = 1;
			if (overlayWeight != 0.) {
				try {
					// need to add one to the number because it is an integer distribution with a lower limit
					nBackgroundEvts = backgroundDistribution.inverseCumulativeProbability(this.getRandom().nextDouble()) + 1;
				} catch (MathException e) {
					System.err.println("Error getting poisson distribution: "+e.getMessage());
				}
			}
			// add this bX one time for every background event to happen during this bX
			for (int i = 0; i < nBackgroundEvts; i++) {
				overlayList.add(bX);
			}
		}
		// shuffle the list
		if (randomizeTrainOverlay) Collections.shuffle(overlayList, this.getRandom());
		int bxCounter = 0;
		
		for (int bX : overlayList) {
			bxCounter++;
			
			double overlayTime = (bX - signalBunchCrossing) * bunchSpacing;
			if (!signalAtZero) overlayTime = bX * bunchSpacing;
			
			if (this.getHistogramLevel() > HLEVEL_OFF) {
				System.out.println("Overlaying background event "+bxCounter+" / "+overlayList.size()+" at BX "+bX+" ("+overlayTime+"ns)");
			}
			
			if (this.getHistogramLevel() > HLEVEL_OFF) {
				int toMB = 1024*1024;
				long freeMemory = Runtime.getRuntime().freeMemory();
				long totalMemory = Runtime.getRuntime().totalMemory();
				System.out.println("Memory free: "+freeMemory/toMB+"MB / "+totalMemory/toMB+"MB ("+100*freeMemory/totalMemory+"%)");
			}
			
			EventHeader overlayEvent = null;
			if (randomizeOverlay) {
				int maxNumber = (int) Math.abs(overlayWeight + 1);
				int skipEvents = this.getRandom().nextInt(maxNumber) + 1;
				overlayEvent = skipEvents(overlayEvents, skipEvents);
			} else {
				overlayEvent = getNextEvent(overlayEvents);
			}
			if (overlayEvent != null) {
				if (event.getDetector().equals(overlayEvent.getDetector())) {
					// clear the mc particle references, which are only needed within one background event
					mcParticleReferences.clear();
					this.mergeEvents(event, overlayEvent, overlayTime);
				} else {
					System.err.println("Unable to merge events simulated in different detectors");
				}
			} else {
				System.err.println("Error reading from overlay event list");
			}
		}
		
		// put the overlay mc particles into the event
		int overlayFlags = event.getMetaData(event.getMCParticles()).getFlags();
		overlayFlags = LCIOUtil.bitSet(overlayFlags, LCIOConstants.BITSubset, true);
		event.put(mcOverlayName, overlayMcParticles, MCParticle.class, overlayFlags);
		
		// reset all lists
		caloHitMap.clear();
		overlayList.clear();
	}
	
	@Override
	protected void suspend() {
		// nothing to do
	}
	
	@Override
	protected void endOfData() {
		// nothing to do
	}
	
	// -------------------- Protected Methods --------------------
	/**
	 * Goes to the next event in the <b>LCIOEventSource</b> and returns it.
	 * If the end of the source is reached, the source is rewound and
	 * the first event will be returned. If any other error occurs,
	 * i.e. the source does not exist, null is returned instead.
	 * @param lcio The LCIO source
	 * @return The next event in the LCIO file
	 */
	static protected EventHeader getNextEvent(LCIOEventSource lcio) {
		EventHeader event = null;
		try {
			lcio.next();
			event = (EventHeader) lcio.getCurrentRecord();
		} catch (NoSuchRecordException e) {
			try {
				lcio.rewind();
				lcio.next();
				event = (EventHeader) lcio.getCurrentRecord();
			} catch (Exception e2) {
				System.err.println(e2.getMessage());
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return event;
	}
	
	/**
	 * Returns the event from the <b>LCIOEventSource</b> after skipping a given
	 * number of events. If the end of the source is reached, the source is rewound
	 * and the remaining events are skipped counting from the first event. If any
	 * other error occurs, i.e. the source does not exist, null is returned instead.
	 * @param lcio The LCIO source
	 * @param nEvents The number of events to skip
	 * @return
	 */
	static protected EventHeader skipEvents(LCIOEventSource lcio, int nEvents) {
		EventHeader event = null;
		for (int nEvent = 0; nEvent < nEvents; nEvent++) {
			event = getNextEvent(lcio);
			if (event == null) break; // no need to loop further if error occurred.
		}
		return event;
	}
	
	/**
	 * Calculates the time of flight from the interaction point to
	 * the position of the given hit along a straight line.
	 * @param hit
	 */
	static protected double getLosTof(Hit hit) {
		return SpacePoint.distance(new SpacePoint(hit.getPositionVec()), interactionPoint)/c;
	}
	
	/**
	 * Adds a collection to an event using the meta data information from the
	 * given collection and the entries from the given list.
	 * @param collection the collection to take the meta data from
	 * @param entries the list of entries to put into the event
	 * @param event the event to put the collection
	 */
	protected void putCollection(LCMetaData collection, List entries,EventHeader event) {
		if (this.getHistogramLevel() > HLEVEL_NORMAL) System.out.println("Putting collection " + collection.getName() + " with " + entries.size() + " entries into event.");
		try {
			List oldEntries = (List) event.get(collection.getName());
			if (entries != oldEntries) {
				oldEntries.clear();
				oldEntries.addAll(entries);
			}
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Shifts an event in time. Moves all entries in all collections
	 * in the event by the given offset in time.
	 * @param event the event to move in time
	 * @param time the time shift applied to all entries in all collections
	 */
	protected void moveEventToTime(EventHeader event, double time) {
		// need to copy list of collections to avoid concurrent modification
		List<LCMetaData> collections = new ArrayList<LCMetaData>(event.getMetaData());
		for (LCMetaData collection : collections) {
			List movedCollection = this.moveCollectionToTime(collection, time);
			if (movedCollection != null) {
				// replace the original collection 
				//event.remove(collection.getName());
				this.putCollection(collection, movedCollection, event);
			}
		}
	}
	
	/**
	 * Shifts a collection in time. Moves all entries in the collection
	 * by the given offset in time. If a readout time is set for the
	 * given collection, all entries outside of that window will be removed.
	 * @param collection the collection to move in time
	 * @param time the time shift applied to all entries in the collection
	 * @return returns the list of moved entries
	 */
	protected List moveCollectionToTime(LCMetaData collection, double time ) {
		EventHeader event = collection.getEvent();
		String collectionName = collection.getName();
		Class collectionType = collection.getType();
		int flags = collection.getFlags();
		if (this.getHistogramLevel() > HLEVEL_NORMAL) System.out.println("Moving collection: "+collectionName+" of type "+collectionType+" to "+time+"ns");
		
		double timeWindow = 0;
		if (readoutTimes.get(collectionName) != null) {
			timeWindow = readoutTimes.get(collectionName);
		}
		
		// negative time window means ignore hits and return empty list
		if (timeWindow < 0) return new ArrayList<Object>();
		
		List movedCollection;
		if (collectionType.isAssignableFrom(MCParticle.class)) {
			// MCParticles
			// don't create new list, just move existing particles
			movedCollection = event.get(MCParticle.class, collectionName);
			for (MCParticle mcP : (List<MCParticle>)movedCollection) {
				if (mcP instanceof SIOMCParticle) {
					((SIOMCParticle) mcP).setTime(mcP.getProductionTime()+time);
				} else if (mcP instanceof BaseMCParticle){
					((BaseMCParticle) mcP).setProductionTime(mcP.getProductionTime()+time);
				} else {
					throw new RuntimeException("Unknown type of MCParticle. Can not modify production time.");
				}
			}
		} else if (collectionType.isAssignableFrom(SimTrackerHit.class)) {
			// SimTrackerHits
			movedCollection = new ArrayList<SimTrackerHit>();
			for (SimTrackerHit hit : (List<SimTrackerHit>)event.get(SimTrackerHit.class, collectionName)) {
				// check if hit falls into relevant readout time window
				double hitTime = hit.getTime() + time;
				double tofCorr = getLosTof(hit);
				if (timeWindow > 0) {
					if (hitTime < signalTime + tofCorr + tofCaloOffset || hitTime > signalTime + tofCorr + timeWindow) continue;
				}
				((BaseSimTrackerHit)hit).setTime(hit.getTime()+time);
				movedCollection.add(hit);
			}
		} else if (collectionType.isAssignableFrom(SimCalorimeterHit.class)) {
			// SimCalorimeterHits
			movedCollection = new ArrayList<SimCalorimeterHit>();
			// check if hit contains PDGIDs
			boolean hasPDG = LCIOUtil.bitTest(flags,LCIOConstants.CHBIT_PDG);
			List<SimCalorimeterHit> hits = event.get(SimCalorimeterHit.class, collectionName);
			int nSimCaloHits = hits.size();
			int nHitsMoved = 0;
			for (SimCalorimeterHit hit : event.get(SimCalorimeterHit.class, collectionName)) {
				// check if earliest energy deposit is later than relevant time window
				double tofCorr = getLosTof(hit);
				BaseSimCalorimeterHit movedHit = null;
				nHitsMoved++;
				if (this.getHistogramLevel() > HLEVEL_HIGH && nHitsMoved%100 == 0) System.out.print("Moved "+nHitsMoved+" / "+nSimCaloHits+" hits\n");
				if (fullCaloProcessing) {
					if (timeWindow > 0) {
						if (hit.getTime() > signalTime + tofCorr + timeWindow) continue;
					}
					nHitsMoved++;
					// create arrays to hold contributions from different mc particles
					List<Object> mcList = new ArrayList<Object>();
					List<Float> eneList = new ArrayList<Float>();
					List<Float> timeList = new ArrayList<Float>();
					List<Integer> pdgList = new ArrayList<Integer>();
					List<float[]> steps = new ArrayList<float[]>();
					double rawEnergy = 0.;
					for (int i = 0; i != hit.getMCParticleCount(); i++) {
						float hitTime = (float) (hit.getContributedTime(i) + time);
						if (timeWindow > 0) {
							if (hitTime < signalTime + tofCorr + tofCaloOffset || hitTime > signalTime + tofCorr + timeWindow) continue;
						}
						float hitEnergy = (float) hit.getContributedEnergy(i);
						mcList.add(hit.getMCParticle(i));
						eneList.add(hitEnergy);
						timeList.add(hitTime);
						if (hasPDG) {
							pdgList.add(hit.getPDG(i));
							steps.add(hit.getStepPosition(i));
						}
						rawEnergy += hitEnergy;
					}
					int hitEntries = mcList.size();
					if (hitEntries == 0) continue;
					Object[] mcArr = mcList.toArray();
					float[] eneArr = new float[hitEntries];
					float[] timeArr = new float[hitEntries];
					int[] pdgArr = null;
					if (hasPDG) pdgArr = new int[hitEntries];
					for (int i = 0; i != hitEntries; i++) {
						mcArr[i] = mcList.get(i);
						eneArr[i] = eneList.get(i);
						timeArr[i] = timeList.get(i);
						if (hasPDG) {
							pdgArr[i] = pdgList.get(i);
						}
					}
					// need to set time to 0 so it is recalculated from the timeList
					movedHit = new BaseSimCalorimeterHit(hit.getCellID(),
							rawEnergy, 0., mcArr, eneArr, timeArr, pdgArr, steps, collection);
				} else {
					double hitTime = hit.getTime() + time;
					if (timeWindow > 0) {
						if (hitTime < signalTime + tofCorr + tofCaloOffset || hitTime > signalTime + tofCorr + timeWindow) continue;
					}
					movedHit = (BaseSimCalorimeterHit) hit;
					movedHit.shiftTime(time);
				}
				movedCollection.add(movedHit);
			}
		} else if (collectionType.isAssignableFrom(GenericObject.class)) {
			// nothing to do for GenericObjects
			return event.get(GenericObject.class, collectionName);
		} else {
			System.err.println("Unable to move collection: "+collectionName+" of type "+collectionType);
			return null;
		}
		return movedCollection;
	}
	
	/**
	 * Merges all collections from the given events and applies a time offset
	 * to all entries in all collections of the overlay event.
	 * @param event the event where everything is merged into
	 * @param overlayEvent the event overlaid
	 * @param overlayTime the time offset for the overlay event
	 */
	protected void mergeEvents(EventHeader event, EventHeader overlayEvent, double overlayTime) {
		
		// need to copy list of collections to avoid concurrent modification
		List<LCMetaData> overlayCollections = new ArrayList<LCMetaData>(overlayEvent.getMetaData());
		for (LCMetaData overlayCollection : overlayCollections) {
			String overlayCollectionName = overlayCollection.getName();
			if (event.hasItem(overlayCollectionName)) {
				this.mergeCollections(event.getMetaData((List)event.get(overlayCollectionName)), overlayCollection, overlayTime);
			} else {
				// event does not contain corresponding collection from overlayEvent, just put it there
				// First move hits and apply timing cuts
				List collection = this.moveCollectionToTime(overlayCollection, overlayTime);
				this.putCollection(overlayCollection, (List)overlayEvent.get(overlayCollectionName), event);
			}
		}	
	}
	
	/**
	 * Copies an mc particle and stores it together with  the copy in a map.
	 * Adds it to the list of mc particles as well as the overlay mc particles.
	 * Also copies and keeps all ancestors.
	 * @param event
	 * @param particle
	 */
	protected void addOverlayMcParticle(MCParticle particle) {
		if (!mcParticleReferences.containsKey(particle)) {
			// keep a copy of the mc particle instead of the original in order to close the background event
			MCParticle mcp = copyMcParticle(particle);
			mcParticleReferences.put(particle, mcp);
			overlayMcParticles.add(mcp);
			allMcParticles.add(mcp);
			List<MCParticle> parents = particle.getParents();
			// keep the parents as well and set the parent daughter relations
			for (MCParticle parent : parents) {
				this.addOverlayMcParticle(parent);
				((BaseMCParticle)mcParticleReferences.get(parent)).addDaughter(mcp);
			}
		}
	}
	
	/**
	 * Deep copy of an mc particle. Necessary in order to be able to close an
	 * overlay event. The parent and daught relations are <b>not</b> set for the
	 * copied mc particle. Because those should most likely also point to copies
	 * this should be handled somewhere else.
	 * @param mcParticle The mc particle to be copied
	 * @return the copied mc particle
	 */
	static public MCParticle copyMcParticle(MCParticle mcParticle) {
		Hep3Vector origin = new BasicHep3Vector(mcParticle.getOriginX(), mcParticle.getOriginY(), mcParticle.getOriginZ());
		HepLorentzVector p = new BasicHepLorentzVector(mcParticle.getEnergy(), new double[] {mcParticle.getPX(), mcParticle.getPY(), mcParticle.getPZ()});
		ParticleType ptype = mcParticle.getType().getParticlePropertyProvider().get(mcParticle.getPDGID());
		int status = mcParticle.getGeneratorStatus();
		double time = mcParticle.getProductionTime();
		BaseMCParticle copyMcP = new BaseMCParticle(origin, p, ptype, status, time);
		// override the mass and charge from the particle type to prevent unknown particle exceptions
		copyMcP.setMass(mcParticle.getMass());
		copyMcP.setCharge(mcParticle.getCharge());
		copyMcP.setSimulatorStatus(mcParticle.getSimulatorStatus().getValue());
		return copyMcP;
	}
	
	/**
	 * Deep copy of an SimTrackerHit. Necessary in order to be able to close an
	 * overlay event.
	 * @param hit The hit to be copied
	 * @param meta The meta data that will be attached to the hit
	 * @return The copied SimTrackerHit
	 */
	protected SimTrackerHit copySimTrackerHit(SimTrackerHit hit, LCMetaData meta) {
		
    	double[] position = new double[3];
    	double[] momentum = new double[3];
    	double[] hitp = hit.getPosition();
    	double[] hitm = hit.getMomentum();
    	for (int i = 0; i != 3; i++ ) {
    		position[i] = hitp[i];
    		momentum[i] = hitm[i];
    	}
    	double dEdx = hit.getdEdx();
    	double pathLength = hit.getPathLength();
    	double time = hit.getTime();
    	int cellID = hit.getCellID();
    	MCParticle hitMC = hit.getMCParticle();
    	this.addOverlayMcParticle(hitMC);
    	MCParticle mcParticle = mcParticleReferences.get(hitMC);
    	IDetectorElement de = hit.getDetectorElement();
    		
		return new BaseSimTrackerHit(position, dEdx, momentum, pathLength, time, cellID, mcParticle, meta, de);
	}
	
	/**
	 * Deep copy of an SimCalorimeterHit. Necessary in order to be able to close an
	 * overlay event.
	 * @param hit The hit to be copied
	 * @param meta The meta data that will be attached to the hit
	 * @param hasPDG Flag if the pdg code of the mc contriutions should be saved
	 * @return The copied SimCalorimeterHit
	 */
	protected SimCalorimeterHit copySimCalorimeterHit(SimCalorimeterHit hit, LCMetaData meta, boolean hasPDG) {
		long id = hit.getCellID();
		double rawEnergy = hit.getRawEnergy();
		double time = hit.getTime();
		int nMCP = hit.getMCParticleCount();
		Object[] mcparts = new Object[nMCP];
		float[] energies = new float[nMCP];
		float[] times = new float[nMCP];
		int[] pdgs = null;
		List<float[]> steps = new ArrayList<float[]>();
		if (hasPDG) pdgs = new int[nMCP];
		// fill arrays with values from hit
		for (int i = 0; i != nMCP; i++) {
			MCParticle hitMC = hit.getMCParticle(i);
	    	this.addOverlayMcParticle(hitMC);
			mcparts[i] = mcParticleReferences.get(hitMC);
			energies[i] = (float)hit.getContributedEnergy(i);
			times[i] = (float)hit.getContributedTime(i);
			if (hasPDG){
				pdgs[i] = hit.getPDG(i);
				steps.add(hit.getStepPosition(i));
			}
		}
		
		BaseSimCalorimeterHit copyHit = new BaseSimCalorimeterHit(id, rawEnergy, time, mcparts, energies, times, pdgs, steps, meta);
		return copyHit;
	}
	
	/**
	 * Merges two collections and applies a time offset to all entries in
	 * the overlay collection.
	 * @param collection the collection where the overlay collection is merged into
	 * @param overlayCollection the collection overlaid
	 * @param overlayTime the time offset for the overlay collection
	 * @return returns <c>false</c> if unable to merge collections, otherwise <c>true</c>
	 */
	protected boolean mergeCollections(LCMetaData collection, LCMetaData overlayCollection, double overlayTime) {
		String collectionName = collection.getName();
		Class collectionType = collection.getType();
		Class overlayCollectionType = overlayCollection.getType();
		if (this.getHistogramLevel() > HLEVEL_NORMAL) System.out.println("Merging collection: "+collectionName+" of type "+collectionType+".");
		if (!collectionType.equals(overlayCollectionType)) {
			System.err.println("Can not merge collections: "+collectionName
					+" of type "+collectionType+" and "+overlayCollectionType);
			return false;
		}
		
		// move the overlay hits in time, signal should have been moved already
		List overlayEntries = this.moveCollectionToTime(overlayCollection, overlayTime);
		//List overlayEntries = overlayCollection.getEvent().get(overlayCollectionType, overlayCollection.getName());
		// Check if there are actually entries to overlay
		if (overlayEntries.isEmpty()) return true;
		EventHeader event = collection.getEvent();
		
		if (collectionType.isAssignableFrom(MCParticle.class)) {
			// Nothing to do. Only add mc particles that are connected to something kept in the event.
			// This is done in the other steps below.
			if (!collectionName.equals(event.MC_PARTICLES) )  {
				event.get(MCParticle.class, collectionName).addAll(overlayEntries);
			}
			
		} else if (collectionType.isAssignableFrom(SimTrackerHit.class)) {
			// SimTrackerHits: just append all hits from overlayEvents
			List<SimTrackerHit> signalTrackerHits = event.get(SimTrackerHit.class, collectionName);
			
			// add contributing mc particles to lists
			for (SimTrackerHit hit : (List<SimTrackerHit>)overlayEntries) {
				SimTrackerHit overlayHit = copySimTrackerHit(hit, collection);
				signalTrackerHits.add(overlayHit);
			}
			
		} else if (collectionType.isAssignableFrom(SimCalorimeterHit.class)) {
			// SimCalorimeterHits: need to merge hits in cells which are hit in both events
			// check if map has already been filled
			Map<Long,SimCalorimeterHit> hitMap;
			List<SimCalorimeterHit> signalCaloHits = event.get(SimCalorimeterHit.class, collectionName);
			if (!caloHitMap.containsKey(collectionName)) {
				// build map of cells which are hit in signalEvent
				hitMap = new HashMap<Long, SimCalorimeterHit>();
				for (SimCalorimeterHit hit : signalCaloHits) {
					hitMap.put(hit.getCellID(), hit);
				}
				caloHitMap.put(collectionName, hitMap);
			} else {
				hitMap = caloHitMap.get(collectionName);
			}
			
			boolean hasPDG = LCIOUtil.bitTest(collection.getFlags(),LCIOConstants.CHBIT_PDG);
			// loop over the hits from the overlay event
			int nHitsMerged = 0;
			int nSimCaloHits = overlayEntries.size();
			for (SimCalorimeterHit hit : (List<SimCalorimeterHit>)overlayEntries) {
				long cellID = hit.getCellID();
				
				nHitsMerged++;
				if (this.getHistogramLevel() > HLEVEL_HIGH && nHitsMerged%100 == 0) System.out.print("Merged "+nHitsMerged+" / "+nSimCaloHits+" hits\n");
				if (hitMap.containsKey(cellID)) {
					SimCalorimeterHit oldHit = hitMap.get(hit.getCellID());
					int nHitMcP = oldHit.getMCParticleCount();
					int nOverlayMcP = hit.getMCParticleCount();
					int nMcP = nHitMcP + nOverlayMcP;
					// arrays of mc particle contributions to the hit
					Object[] mcpList = new Object[nMcP];
					float[] eneList = new float[nMcP];
					float[] timeList = new float[nMcP];
					int[] pdgList = null;
					if (hasPDG) pdgList = new int[nMcP];
					List<float[]> steps = new ArrayList<float[]>();
					double rawEnergy = 0.;
					// fill arrays with values from hit
					for (int i = 0; i != nHitMcP; i++) {
						mcpList[i] = oldHit.getMCParticle(i);
						eneList[i] = (float)oldHit.getContributedEnergy(i);
						timeList[i] = (float)oldHit.getContributedTime(i);
						if (hasPDG) {
							pdgList[i] = oldHit.getPDG(i);
							steps.add(hit.getStepPosition(i));
						}
						rawEnergy += eneList[i];
					}
					// add values of overlay hit
					for (int i = 0; i != nOverlayMcP; i++) {
						int j = nHitMcP + i;
						MCParticle hitMC = hit.getMCParticle(i);
				    	if (!mcParticleReferences.containsKey(hitMC)) {
				    		this.addOverlayMcParticle(hitMC);
				    	}
				    	mcpList[j] = mcParticleReferences.get(hitMC);
						eneList[j] = (float)hit.getContributedEnergy(i);
						timeList[j] = (float)hit.getContributedTime(i);
						if (hasPDG) {
							pdgList[j] = hit.getPDG(i);
							steps.add(hit.getStepPosition(i));
						}
						rawEnergy += eneList[j];
					}
					// need to set time to 0 so it is recalculated from the timeList
					SimCalorimeterHit mergedHit = new BaseSimCalorimeterHit(oldHit.getCellID(),
							rawEnergy, 0., mcpList, eneList, timeList, pdgList, steps, collection);
					//mergedHit.setDetectorElement(oldHit.getDetectorElement());
					//mergedHit.setMetaData(collection);
					// replace old hit with merged hit
					signalCaloHits.remove(oldHit);
					signalCaloHits.add(mergedHit);
					hitMap.put(cellID, mergedHit);
				} else {
					SimCalorimeterHit overlayHit = copySimCalorimeterHit(hit, collection, hasPDG);
					signalCaloHits.add(overlayHit);
					hitMap.put(cellID, overlayHit);
				}
				
			}
		} else if (collectionType.isAssignableFrom(GenericObject.class)) {
			// need to implement all kinds of possible GenericObjects separately
			if (collectionName.equals("MCParticleEndPointEnergy")) {
				// TODO decide what to do with this collection in the overlay events
				// TODO would need to resolve the position of kept mc particles and keep the same position here
				//event.get(GenericObject.class, collectionName).addAll(overlayEntries);
				//event.remove("MCParticleEndPointEnergy");
			} else {
				System.err.println("Can not merge collection "+collectionName
						+" of type "+collectionType+". Unhandled type.");
				return false;
			}
		}
		return true;
	}
	
}
