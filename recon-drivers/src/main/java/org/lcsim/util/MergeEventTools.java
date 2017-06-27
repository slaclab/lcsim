package org.lcsim.util;

import hep.physics.particle.properties.ParticleType;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.base.BaseMCParticle;
import org.lcsim.event.base.BaseSimCalorimeterHit;
import org.lcsim.event.base.BaseSimTrackerHit;
import org.lcsim.lcio.LCIOConstants;
import org.lcsim.lcio.LCIOUtil;


/**
 * Class with helper methods for event merging.
 * 
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 */
public class MergeEventTools {

	/**
	 * Merge two events. The first event will contain both events afterwards.
	 * @param event the event which have the other event added
	 * @param mergeEvent the event to be merged with the first event
	 */
	static public void mergeEvents(EventHeader event, EventHeader mergeEvent) {
		mergeEvents(event, mergeEvent, new ArrayList<String>(), new HashMap<String, Map<Long,SimCalorimeterHit>>());
	}
	
	/**
	 * Merge two events. The first event will contain both events afterwards. Collections can be ignored.
	 * @param event the event which have the other event added
	 * @param mergeEvent the event to be merged with the first event
	 * @param ignoreCollections list of collection names that will not be merged
	 */
	static public void mergeEvents(EventHeader event, EventHeader mergeEvent, Collection<String> ignoreCollections) {
		mergeEvents(event, mergeEvent, ignoreCollections, new HashMap<String, Map<Long,SimCalorimeterHit>>());
	}
	
	/**
	 * Merge two events. The first event will contain both events afterwards. Collections can be ignored. A map holding maps of cell IDs to sim calorimeter hits is used to avoid
	 * that this information has to be obtained again if this method is called multiple times on the same event.
	 * @param event the event which have the other event added
	 * @param mergeEvent the event to be merged with the first event
	 * @param ignoreCollections list of collection names that will not be merged
	 * @param caloHitMaps map of collection names to maps of cell IDs to SimCalorimeterHits used to identify which cells require merging
	 */
	static public void mergeEvents(EventHeader event, EventHeader mergeEvent, Collection<String> ignoreCollections, Map<String, Map<Long, SimCalorimeterHit>> caloHitMaps) {
		Collection<LCMetaData> mergeMetaDataList = mergeEvent.getMetaData();
		// we need to have a single map of mc particles to their copies to ensure that all MCParticle references point to the same instance
		Map<MCParticle, MCParticle> mcParticleMap = new HashMap<MCParticle, MCParticle>();
		for (LCMetaData mergeMetaData : mergeMetaDataList) {
			String collectionName = mergeMetaData.getName();
			if (ignoreCollections.contains(collectionName)) {
				continue;
			}
			Class collectionType = mergeMetaData.getType();
			if (mergeEvent.get(collectionType, collectionName).isEmpty()) {
				continue;
			}
			LCMetaData metaData = event.getMetaData((List) event.get(collectionName));
			if (collectionType.isAssignableFrom(MCParticle.class)) {
				mergeMCParticleCollections(metaData, mergeMetaData, mcParticleMap);
			} else if (collectionType.isAssignableFrom(SimTrackerHit.class)) {
				mergeSimTrackerHitCollections(metaData, mergeMetaData, mcParticleMap);
			} else if (collectionType.isAssignableFrom(SimCalorimeterHit.class)) {
				if (!caloHitMaps.containsKey(collectionName)) {
					Map<Long, SimCalorimeterHit> hitMap = new HashMap<Long, SimCalorimeterHit>();
					for (SimCalorimeterHit hit : event.get(SimCalorimeterHit.class, collectionName)) {
						hitMap.put(hit.getCellID(), hit);
					}
					caloHitMaps.put(metaData.getName(), hitMap);
				}
				mergeSimCalorimeterHitCollections(metaData, mergeMetaData, mcParticleMap, caloHitMaps.get(collectionName));
			}
		}
	}
	
	/**
	 * Merge two MCParticle collections. The first one will contain both collections afterwards. All MCParticles are deep copied
	 * to allow for garbage collection.
	 * @param metaData meta data of the first collection
	 * @param mergeMetaData meta data of the second collection
	 * @param mcParticleMap map to store relation between the original MCParticles and their copies required to ensure consistency in relations to that MCParticle
	 */
	static public void mergeMCParticleCollections(LCMetaData metaData, LCMetaData mergeMetaData, Map<MCParticle, MCParticle> mcParticleMap) {
		String collectionName = mergeMetaData.getName();
		List<MCParticle> mcParticles = metaData.getEvent().get(MCParticle.class, collectionName);
		List<MCParticle> mergeMcParticles = mergeMetaData.getEvent().get(MCParticle.class, collectionName);
		for (MCParticle mcParticle : mergeMcParticles) {
			mcParticles.add(getMcParticleCopy(mcParticle, mcParticleMap));
		}
	}
	
	/**
	 * Merge two SimTrackerHit collections. The first one will contain both collections afterwards. All SimTrackerHits are deep copied
	 * to allow for garbage collection. Similarly, the MCParticles referenced by the SimTrackerHits are copied.
	 * @param metaData meta data of the first collection
	 * @param mergeMetaData meta data of the second collection
	 * @param mcParticleMap map to store relation between the original MCParticles and their copies required to ensure consistency in relations to that MCParticle
	 */
	static public void mergeSimTrackerHitCollections(LCMetaData metaData, LCMetaData mergeMetaData, Map<MCParticle, MCParticle> mcParticleMap) {
		String collectionName = mergeMetaData.getName();
		List<SimTrackerHit> trackerHits = metaData.getEvent().get(SimTrackerHit.class, collectionName);
		List<SimTrackerHit> mergeTrackerHits = mergeMetaData.getEvent().get(SimTrackerHit.class, collectionName);
		for (SimTrackerHit hit : mergeTrackerHits) {
			trackerHits.add(copySimTrackerHit(hit, metaData, mcParticleMap));
		}
	}
	
	/**
	 * Merge two SimCalorimeterHit collections. The first one will contain both collections afterwards. All SimCalorimeterHits are deep copied
	 * to allow for garbage collection. Similarly, the MCParticles referenced by the SimCalorimeterHits are copied.
	 * @param metaData meta data of the first collection
	 * @param mergeMetaData meta data of the second collection
	 * @param mcParticleMap map to store relation between the original MCParticles and their copies required to ensure consistency in relations to that MCParticle
	 */
	static public void mergeSimCalorimeterHitCollections(LCMetaData metaData, LCMetaData mergeMetaData, Map<MCParticle, MCParticle> mcParticleMap, Map<Long, SimCalorimeterHit> caloHitMap) {
		String collectionName = mergeMetaData.getName();
		List<SimCalorimeterHit> caloHits = metaData.getEvent().get(SimCalorimeterHit.class, collectionName);
		List<SimCalorimeterHit> mergeCaloHits = mergeMetaData.getEvent().get(SimCalorimeterHit.class, collectionName);
		for (SimCalorimeterHit caloHit : mergeCaloHits) {
			long cellID = caloHit.getCellID();
			if (caloHitMap.containsKey(cellID)) {
				SimCalorimeterHit hit = caloHitMap.get(cellID);
				caloHit = copySimCalorimeterHit(caloHit, metaData, mcParticleMap);
				SimCalorimeterHit mergedHit = mergeSimCalorimeterHits(hit, caloHit);
				caloHits.remove(hit);
				caloHitMap.put(cellID, mergedHit);
				caloHits.add(mergedHit);
			} else {
				SimCalorimeterHit caloHitCopy = copySimCalorimeterHit(caloHit, metaData, mcParticleMap); 
				caloHitMap.put(cellID, caloHitCopy);
				caloHits.add(caloHitCopy);
			}
		}
	}

	/**
	 * Creates a deep copy of an MCParticle.
	 * @param mcParticle the particle to be copied
	 * @return the copied MCParticle
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
	 * Helper method to make a proper deep copy of an MCParticle and all its ancestors. A map between the original
	 * particle and can be used to ensure that all references to the original particle use the same copy.
	 * @param mcParticle the particle to be cpoied
	 * @param mcParticleMap map to store relation between the original MCParticles and their copies required to ensure consistency in relations to that MCParticle
	 * @return the copied MCParticle
	 */
	static public MCParticle getMcParticleCopy(MCParticle mcParticle, Map<MCParticle, MCParticle> mcParticleMap) {
		if (!mcParticleMap.containsKey(mcParticle)) {
			MCParticle mcParticleCopy = copyMcParticle(mcParticle);
			mcParticleMap.put(mcParticle, mcParticleCopy);
			for (MCParticle parent : mcParticle.getParents()) {
				MCParticle parentCopy = getMcParticleCopy(parent, mcParticleMap);
				((BaseMCParticle) parentCopy).addDaughter(mcParticleCopy);
			}
		}
		return mcParticleMap.get(mcParticle);
	}
	
	/**
	 * Creates a deep copy of a SimTrackerHit and assigns the given meta data. The corresponding MCParticles are also copied if they are not present in the map,
	 * otherwise the reference is updated to use the copy provided by the map.
	 * @param hit the original hit
	 * @param metaData the new meta data
	 * @param mcParticleMap map to store relation between the original MCParticles and their copies required to ensure consistency in relations to that MCParticle
	 * @return the copied hit
	 */
	static public SimTrackerHit copySimTrackerHit(SimTrackerHit hit, LCMetaData metaData, Map<MCParticle, MCParticle> mcParticleMap) {
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
    	MCParticle mcParticle = getMcParticleCopy(hit.getMCParticle(), mcParticleMap);
    	IDetectorElement de = hit.getDetectorElement();
    	
		return new BaseSimTrackerHit(position, dEdx, momentum, pathLength, time, cellID, mcParticle, metaData, de);
	}
	
	/**
	 * Creates a deep copy of a SimCalorimeterHit and assigns the given meta data. The corresponding MCParticles are also copied if they are not present in the map,
	 * otherwise the reference is updated to use the copy provided by the map.
	 * @param hit the original hit
	 * @param metaData the new meta data
	 * @param mcParticleMap map to store relation between the original MCParticles and their copies required to ensure consistency in relations to that MCParticle
	 * @return the copied hit
	 */
	static public SimCalorimeterHit copySimCalorimeterHit(SimCalorimeterHit hit, LCMetaData metaData, Map<MCParticle, MCParticle> mcParticleMap) {
		long id = hit.getCellID();
		double rawEnergy = hit.getRawEnergy();
		double time = hit.getTime();
		int nMCP = hit.getMCParticleCount();
		Object[] mcparts = new Object[nMCP];
		float[] energies = new float[nMCP];
		float[] times = new float[nMCP];
		int[] pdgs = null;
		List<float[]> steps = new ArrayList<float[]>();
		boolean hasPDG = LCIOUtil.bitTest(metaData.getFlags(),LCIOConstants.CHBIT_PDG);
		if (hasPDG) {
			pdgs = new int[nMCP];
		}
		// fill arrays with values from hit
		for (int i = 0; i != nMCP; i++) {
			mcparts[i] = getMcParticleCopy(hit.getMCParticle(i), mcParticleMap);
			energies[i] = (float)hit.getContributedEnergy(i);
			times[i] = (float)hit.getContributedTime(i);
			if (hasPDG){
				pdgs[i] = hit.getPDG(i);
				steps.add(hit.getStepPosition(i));
			}
		}
		
		BaseSimCalorimeterHit copyHit = new BaseSimCalorimeterHit(id, rawEnergy, time, mcparts, energies, times, pdgs, steps, metaData);
		//copyHit.setDetectorElement(hit.getDetectorElement());
		copyHit.setMetaData(metaData);
		
		return copyHit;
	}
	
	/**
	 * Merges two SimCalorimeterHits that occupy the same cell
	 * @param hit the first hit to be merged
	 * @param mergeHit the second hit to be merged
	 * @return the combined calorimeter hit
	 */
	static public SimCalorimeterHit mergeSimCalorimeterHits(SimCalorimeterHit hit, SimCalorimeterHit mergeHit) {
		int nMcpHit = hit.getMCParticleCount();
		int nMcpMergeHit = mergeHit.getMCParticleCount();
		int nMcp = nMcpHit + nMcpMergeHit;
		// arrays of mc particle contributions to the hit
		Object[] mcpList = new Object[nMcp];
		float[] eneList = new float[nMcp];
		float[] timeList = new float[nMcp];
		int[] pdgList = null;
		LCMetaData metaData = hit.getMetaData();
		boolean hasPDG = LCIOUtil.bitTest(metaData.getFlags(),LCIOConstants.CHBIT_PDG);
		if (hasPDG) {
			pdgList = new int[nMcp];
		}
		List<float[]> steps = new ArrayList<float[]>();
		double rawEnergy = 0.;
		// fill arrays with values from hit
		for (int i = 0; i != nMcpHit; i++) {
			mcpList[i] = hit.getMCParticle(i);
			eneList[i] = (float) hit.getContributedEnergy(i);
			timeList[i] = (float) hit.getContributedTime(i);
			if (hasPDG) {
				pdgList[i] = hit.getPDG(i);
				steps.add(hit.getStepPosition(i));
			}
			rawEnergy += eneList[i];
		}
		// add values of overlay hit
		for (int i = 0; i != nMcpMergeHit; i++) {
			int j = nMcpHit + i;
	    	mcpList[j] = mergeHit.getMCParticle(i);
			eneList[j] = (float) mergeHit.getContributedEnergy(i);
			timeList[j] = (float) mergeHit.getContributedTime(i);
			if (hasPDG) {
				pdgList[j] = mergeHit.getPDG(i);
				steps.add(mergeHit.getStepPosition(i));
			}
			rawEnergy += eneList[j];
		}
		// need to set time to 0 so it is recalculated from the timeList
		SimCalorimeterHit mergedHit = new BaseSimCalorimeterHit(hit.getCellID(),
				rawEnergy, 0., mcpList, eneList, timeList, pdgList, steps, metaData);
		return mergedHit;
	}
	
}
