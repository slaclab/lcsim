package org.lcsim.recon.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.event.LCRelation;
import org.lcsim.event.MCParticle;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.RelationalTable;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.Track;
import org.lcsim.event.TrackerHit;
import org.lcsim.event.MCParticle.SimulatorStatus;
import org.lcsim.event.base.BaseLCRelation;
import org.lcsim.event.base.BaseRelationalTable;
import org.lcsim.util.Driver;
import org.lcsim.lcio.LCIOConstants;
import org.lcsim.lcio.LCIOUtil;

/**
 * A Driver to create several LCRelations between high level reconstructed
 * objects and their true mc particles.
 * <p>
 * By default three LCRelations are created
 * <ul>
 * <li><b>Tracks to MCParticles:</b><br>
 * This requires the name of the track collection and an LCRelation between
 * TrackerHits and MCParticles.</li>
 * <li><b>Clusters to MCParticles:</b><br>
 * This requires the name of the cluster collection and an LCRelation between
 * CalorimeterHits and SimCalorimeterHits.</li>
 * <li><b>PFOs to MCParticles:</b><br>
 * This requires the name of the PFO collection and the creation of both
 * LCRelations described above.</li>
 * </ul>
 * Putting any of these into the event can be prevented by setting the
 * respective name to an empty String.
 * <p>
 * The weights of these relations are based on fractions contributed by the mc
 * particle. For the track relation it is based on the fraction of hits and for
 * clusters it is based on the fraction of energy. Non-charged PFOs use the
 * weight of the clusters, while charged PFOs use a combined weight of the
 * tracks and the clusters, based on a global track to cluster weight. As a
 * default only the track relations are used for charged PFOs.
 * <p>
 * Instead of the simple fraction-based weights described above a weight based
 * on a Tanimoto metric can be used. Then also the hits produced by a mc
 * particle which are not part of the reconstructed object are taken into
 * account. This leads to a lower weight for missed hits.
 * <p>
 * For all objects, only the relation to the MCParticle which has the highest
 * weight is kept. Setting the fullRecoRelation switch to true will keep all
 * relations instead.
 * <p>
 * By default a reduced set of MCParticles is created. This skimmed list
 * contains only those MCParticles created by the generator (intermediate and
 * final state particles) and emitted bremsstrahlung photons. In addition all
 * particles which are of a pre-defined set of particle types and which decay in
 * flight in the tracking system and their intermediate daughters are kept. By
 * default these particle types are gamma, pi0 and K0s. There is also an energy
 * cut applied to which of these daughter particles are kept. All relations
 * which would point to an MCParticle not contained in this reduced list point
 * to their closest ancestor which is in this list instead. Again, this behavior
 * can be switched off, by setting the name of the skimmed mc particle
 * collection to an empty String.
 * 
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 */
public class McTruthLinker extends Driver {

	protected String trackHitMcRelationName = "HelicalTrackMCRelations";
	protected String trackCollectionName = "Tracks";
	protected String trackMcRelationName = "TrackMCTruthLink";
	protected String caloHitSimHitRelationName = "CalorimeterHitRelations";
	protected String clusterCollectionName = "ReconClusters";
	protected String clusterMcRelationName = "ClusterMCTruthLink";
	protected String pfoCollectionName = "PandoraPFOCollection";
	protected String pfoMcRelationName = "RecoMCTruthLink";
	protected String mcParticleCollectionName = EventHeader.MC_PARTICLES;
	protected String mcParticlesSkimmedName = "MCParticlesSkimmed";
	protected double pfoTrackWeight = 1.0;
	protected double pfoClusterWeight = 0.0;
	protected boolean fullRecoRelation = false;
	protected boolean useTanimotoDistance = false;
	protected boolean useSkimmedMcParticles = true;
	protected List<MCParticle> mcParticlesSkimmed;
	protected List<Integer> keepDaughtersPDGID = new ArrayList<Integer>();
	protected Map<MCParticle, MCParticle> mcParticleToSkimmed;
	protected double daughterEnergyCut = 0.01;

	// -------------------- Constructors --------------------

	public McTruthLinker() {
		keepDaughtersPDGID.add(22); // gamma
		keepDaughtersPDGID.add(111); // pi0
		keepDaughtersPDGID.add(310); // K0s
	}

	// -------------------- Driver Interface Methods --------------------

	@Override
	protected void startOfData() {
		if (mcParticlesSkimmedName.equals("")) {
			this.useSkimmedMcParticles = false;
		}
	}

	@Override
	protected void process(EventHeader event) {

		List<LCRelation> trackMcRelation = null;
		List<LCRelation> caloHitMcRelation = null;
		List<LCRelation> clusterMcRelation = null;
		List<LCRelation> pfoMcRelation = null;
		mcParticlesSkimmed = null;
		mcParticleToSkimmed = null;

		// skimmed mc particles
		if (useSkimmedMcParticles) {
			try {
				List<MCParticle> mcParticles = event.get(MCParticle.class, mcParticleCollectionName);
				mcParticlesSkimmed = createSkimmedMcParticleList(mcParticles);
				mcParticleToSkimmed = fillMcParticleToSkimmedMap(mcParticles, mcParticlesSkimmed);
				int flags = event.getMetaData(mcParticles).getFlags();
				flags = LCIOUtil.bitSet(flags, LCIOConstants.BITSubset, true);
				event.put(mcParticlesSkimmedName, mcParticlesSkimmed, MCParticle.class, flags);
				print(HLEVEL_NORMAL, "Added skimmed mc particles \"" + mcParticlesSkimmedName + "\" to the event.");
			} catch (IllegalArgumentException e) {
				print(HLEVEL_DEFAULT, "WARNING: no skimmed mc particle collection created.\n" + "e.getMessage()", true);
			}
		}

		// track to mc particle relation
		if (!trackHitMcRelationName.equals("") && !trackCollectionName.equals("")) {
			try {
				List<Track> tracks = event.get(Track.class, trackCollectionName);
				List<LCRelation> trackHitMcRelation = event.get(LCRelation.class, trackHitMcRelationName);
				trackMcRelation = createTrackMcRelation(tracks, trackHitMcRelation);
				if (!trackMcRelationName.equals("")) {
					int flags = 0;
					flags = LCIOUtil.bitSet(flags, LCIOConstants.LCREL_WEIGHTED, true);
					event.put(trackMcRelationName, trackMcRelation, LCRelation.class, flags);
					print(HLEVEL_NORMAL, "Added track to mc particle relations \"" + trackMcRelationName
							+ "\" to the event.");
				}
			} catch (IllegalArgumentException e) {
				print(HLEVEL_DEFAULT, "WARNING: no track to mc particle relation created.\n" + "e.getMessage()", true);
			}
		}

		// calorimeter hit to mc particle relation
		if (!caloHitSimHitRelationName.equals("")) {
			try {
				caloHitMcRelation = createCaloHitMcRelation(event.get(LCRelation.class, caloHitSimHitRelationName));
			} catch (IllegalArgumentException e) {
				print(HLEVEL_DEFAULT, "WARNING: no calorimeter hit to mc particle relation created.\n"
						+ "e.getMessage()", true);
			}
		}

		// cluster to mc particle relation
		if (!clusterCollectionName.equals("")) {
			try {
				List<Cluster> clusters = event.get(Cluster.class, clusterCollectionName);
				clusterMcRelation = createClusterMcRelation(clusters, caloHitMcRelation);
				if (!clusterMcRelationName.equals("")) {
					int flags = 0;
					flags = LCIOUtil.bitSet(flags, LCIOConstants.LCREL_WEIGHTED, true);
					event.put(clusterMcRelationName, clusterMcRelation, LCRelation.class, flags);
					print(HLEVEL_NORMAL, "Added cluster to mc particle relations \"" + clusterMcRelationName
							+ "\" to the event.");
				}
			} catch (IllegalArgumentException e) {
				print(HLEVEL_DEFAULT, "WARNING: no cluster to mc particle relation created.\n" + "e.getMessage()", true);
			}
		}

		// PFO to mc particle relation
		if (!pfoCollectionName.equals("")) {
			try {
				List<ReconstructedParticle> PFOs = event.get(ReconstructedParticle.class, pfoCollectionName);
				pfoMcRelation = createPfoMcRelation(PFOs, trackMcRelation, clusterMcRelation);
				if (!pfoMcRelationName.equals("")) {
					int flags = 0;
					flags = LCIOUtil.bitSet(flags, LCIOConstants.LCREL_WEIGHTED, true);
					event.put(pfoMcRelationName, pfoMcRelation, LCRelation.class, flags);
					print(HLEVEL_NORMAL, "Added PFO to mc particle relations \"" + pfoMcRelationName
							+ "\" to the event.");
				}
			} catch (IllegalArgumentException e) {
				print(HLEVEL_DEFAULT, "WARNING: no PFO to mc particle relation created.\n" + "e.getMessage()", true);
			}
		}
	}

	// -------------------- Setter Methods --------------------

	public void setFullRecoRelation(boolean fullRecoRelation) {
		this.fullRecoRelation = fullRecoRelation;
	}

	public void setUseTanimotoDistance(boolean useTanimotoDistance) {
		this.useTanimotoDistance = useTanimotoDistance;
	}

	public void setPfoTrackWeight(double pfoTrackWeight) throws IllegalArgumentException {
		if (pfoTrackWeight < 0)
			throw new IllegalArgumentException("PFO track weight can not be negative.");
		this.pfoTrackWeight = pfoTrackWeight;
	}

	public void setPfoClusterWeight(double pfoClusterWeight) throws IllegalArgumentException {
		if (pfoTrackWeight < 0)
			throw new IllegalArgumentException("PFO cluster weight can not be negative.");
		this.pfoClusterWeight = pfoClusterWeight;
	}

	public void setTrackHitMcRelationName(String trackHitMcRelationName) {
		this.trackHitMcRelationName = trackHitMcRelationName;
	}

	public void setTrackCollectionName(String trackCollectionName) {
		this.trackCollectionName = trackCollectionName;
	}

	public void setTrackMcRelationName(String trackMcRelationName) {
		this.trackMcRelationName = trackMcRelationName;
	}

	public void setCaloHitSimHitRelationName(String caloHitSimHitRelationName) {
		this.caloHitSimHitRelationName = caloHitSimHitRelationName;
	}

	public void setClusterCollectionName(String clusterCollectionName) {
		this.clusterCollectionName = clusterCollectionName;
	}

	public void setClusterMcRelationName(String clusterMcRelationName) {
		this.clusterMcRelationName = clusterMcRelationName;
	}

	public void setPfoCollectionName(String pfoCollectionName) {
		this.pfoCollectionName = pfoCollectionName;
	}

	public void setPfoMcRelationName(String pfoMcRelationName) {
		this.pfoMcRelationName = pfoMcRelationName;
	}

	public void setMcParticleCollectionName(String mcParticleCollectionName) {
		this.mcParticleCollectionName = mcParticleCollectionName;
	}

	public void setMcParticlesSkimmedName(String mcParticlesSkimmedName) {
		this.mcParticlesSkimmedName = mcParticlesSkimmedName;
	}

	public void setKeepDaughtersPDGID(int[] keepDaughtersPDGID) {
		this.keepDaughtersPDGID.clear();
		for (int pdgid : keepDaughtersPDGID) {
			this.keepDaughtersPDGID.add(pdgid);
		}
	}

	public void setDaughterEnergyCut(double daughterEnergyCut) {
		this.daughterEnergyCut = daughterEnergyCut;
	}

	// -------------------- Protected Methods --------------------

	/**
	 * Creates a list of skimmed mc particles which are kept together with all
	 * their ancestors. First of all, all the particles that are created by the
	 * generator (IntermediateState, Documentation or FinalState) are kept. In
	 * addition bremsstrahlung photons created by these particles are kept.
	 * Finally all the particles from a given list (default: gamma, pi0, K0s)
	 * are kept together with their direct daughters.
	 */
	protected List<MCParticle> createSkimmedMcParticleList(List<MCParticle> mcParticles) {

		List<MCParticle> skimmedMcParticles = new ArrayList<MCParticle>();

		for (MCParticle mcParticle : mcParticles) {
			SimulatorStatus simStatus = mcParticle.getSimulatorStatus();
			if (mcParticle.getGeneratorStatus() == MCParticle.INTERMEDIATE) {
				// first add all intermediate particles
				addMcParticleWithParents(mcParticle, skimmedMcParticles);
			}
			if (mcParticle.getGeneratorStatus() == MCParticle.DOCUMENTATION) {
				// add all documentation particles.
				addMcParticleWithParents(mcParticle, skimmedMcParticles);
			}
			if (mcParticle.getGeneratorStatus() > 3) {
				// add all particles with unknown generator status.
				// Mokka adds 100 to the generator status of particles that
				// should not be passed through simulation.
				addMcParticleWithParents(mcParticle, skimmedMcParticles);
			}
			if (mcParticle.getGeneratorStatus() == MCParticle.FINAL_STATE) {
				// add all mc particles created by the generator
				addMcParticleWithParents(mcParticle, skimmedMcParticles);
				// check if there is some interaction in the tracking region
				if (simStatus.isDecayedInCalorimeter()) {
					// keep bremsstrahlung
					for (MCParticle daughter : mcParticle.getDaughters()) {
						if (daughter.getPDGID() == 22 && daughter.getEnergy() > daughterEnergyCut
								&& !daughter.getSimulatorStatus().isBackscatter()) {
							addMcParticleWithParents(daughter, skimmedMcParticles);
						}
					}
				}
				//
			} else if (mcParticle.getSimulatorStatus().isDecayedInTracker()) {
				// now add all daughters of the particles that decayed in flight
				// and should be kept
				if (keepDaughtersPDGID.contains(mcParticle.getPDGID())) {
					for (MCParticle daughter : mcParticle.getDaughters()) {
						if (daughter.getEnergy() > daughterEnergyCut && !daughter.getSimulatorStatus().isBackscatter()) {
							addMcParticleWithParents(daughter, skimmedMcParticles);
						}
					}
				}

			}
		}

		print(HLEVEL_NORMAL, "Keeping " + skimmedMcParticles.size() + " of " + mcParticles.size()
				+ " mc particles in skimmed list.");

		return skimmedMcParticles;
	}

	/**
	 * Fills a map connecting an mc particle with its closest ancestor that is
	 * present in the skimmed mc particle list. If no suitable ancestor is found
	 * the map is filled with null for that mc particle.
	 * 
	 * @param mcParticles
	 *            The list of all mc particles
	 * @param skimmedMcParticles
	 *            A subset of the mc particles
	 * @return A mapping between all mc particles and their closest ancestor
	 *         present in the skimmed mc particles
	 */
	protected Map<MCParticle, MCParticle> fillMcParticleToSkimmedMap(List<MCParticle> mcParticles,
			List<MCParticle> skimmedMcParticles) {

		Map<MCParticle, MCParticle> mcParticleToSkimmedMap = new HashMap<MCParticle, MCParticle>();

		for (MCParticle mcParticle : mcParticles) {
			MCParticle ancestor = findMcParticleAncestor(mcParticle, skimmedMcParticles);
			mcParticleToSkimmedMap.put(mcParticle, ancestor);
			String motherPDGID = "none";
			if (mcParticle.getParents().size() > 0) {
				motherPDGID = Integer.toString(mcParticle.getParents().get(0).getPDGID());
			}
			if (mcParticle != ancestor) {
				print(HLEVEL_FULL, "Warning: Rejecting mc particle." + "\tEnergy: " + mcParticle.getEnergy() + "\n"
						+ "\tCharge: " + mcParticle.getCharge() + "\n" + "\tPDGID: " + mcParticle.getPDGID() + "\n"
						+ "\tGenStatus: " + mcParticle.getGeneratorStatus() + "\n" + "\tCreated in simulation: "
						+ mcParticle.getSimulatorStatus().isCreatedInSimulation() + "\n" + "\tBackscatter: "
						+ mcParticle.getSimulatorStatus().isBackscatter() + "\n" + "\tDecay in calorimeter: "
						+ mcParticle.getSimulatorStatus().isDecayedInCalorimeter() + "\n" + "\tDecay in tracker: "
						+ mcParticle.getSimulatorStatus().isDecayedInTracker() + "\n" + "\tStopped: "
						+ mcParticle.getSimulatorStatus().isStopped() + "\n" + "\tMother: " + motherPDGID, true);
			}
		}
		return mcParticleToSkimmedMap;
	}

	/**
	 * Creates the relations from tracks to mc particles by using a list of
	 * LCRelations from hits to mc particles. In case of a skimmed mc particle
	 * list the relations are pointing to the closest ancestor present in the
	 * skimmed list.
	 * <p>
	 * The relations are weighted by the fraction of hits belonging to a certain
	 * mc particle (N_{match}/N_{track}).
	 * <p>
	 * In case of Tanimoto distance also the total number of hits produced by
	 * the mc particle are taken into account. It gives less weight to tracks
	 * that miss true hits. The weight is then calculated as 1 -
	 * (N_{track}+N_{mc}-2*N_{match})/(N_{track}+N_{mc}-N_{match).
	 * 
	 * @param tracks
	 *            The list of tracks to be truth linked
	 * @param trackHitMcRelation
	 *            The LCRelations between track hits and mc particles
	 * @return The weighted LCRelations between tracks and mc particles
	 */
	protected List<LCRelation> createTrackMcRelation(List<Track> tracks, List<LCRelation> trackHitMcRelation) {

		if (trackHitMcRelation == null) {
			throw new IllegalArgumentException("No tracker hit to mc relations given.");
		}

		RelationalTable<TrackerHit, MCParticle> trackHitMcRelationTable = createRelationalTable(trackHitMcRelation);
		List<LCRelation> trackMcRelation = new ArrayList<LCRelation>();

		for (Track track : tracks) {
			// Store number of hits contributed by each mc particle
			Map<MCParticle, Integer> mcParticleContribution = new HashMap<MCParticle, Integer>();
			List<TrackerHit> trackHitsList = track.getTrackerHits();
			double trackHits = trackHitsList.size();
			double sumOfWeights = 0;
			for (TrackerHit trackHit : trackHitsList) {
				for (MCParticle mcParticle : trackHitMcRelationTable.allFrom(trackHit)) {
					if (useSkimmedMcParticles)
						mcParticle = mcParticleToSkimmed.get(mcParticle);
					if (mcParticleContribution.containsKey(mcParticle)) {
						mcParticleContribution.put(mcParticle, mcParticleContribution.get(mcParticle) + 1);
					} else {
						mcParticleContribution.put(mcParticle, 1);
					}
				}
			}
			mcParticleContribution = sortMapByHighestValue(mcParticleContribution);
			for (MCParticle mcParticle : mcParticleContribution.keySet()) {
				double weight = 0.0;
				double recoHits = mcParticleContribution.get(mcParticle);
				if (useTanimotoDistance) {
					double trueHits = trackHitMcRelationTable.allTo(mcParticle).size();
					weight = 1 - (trackHits + trueHits - 2 * recoHits) / (trackHits + trueHits - recoHits);
				} else {
					weight = recoHits / trackHits;
				}
				sumOfWeights += weight;
				trackMcRelation.add(new BaseLCRelation(track, mcParticle, weight));
				print(HLEVEL_FULL, "Added a track to mc particle relation with weight " + weight + ".");
				if (!fullRecoRelation)
					break;
			}
			print(HLEVEL_HIGH, "Total weight of track contributions is " + sumOfWeights + ".");
		}

		print(HLEVEL_NORMAL, "Created " + trackMcRelation.size() + " track to mc particle relations.");

		return trackMcRelation;
	}

	/**
	 * Creates the relations from calorimeter hits to mc particles by using a
	 * list of LCRelations from CalorimeterHits to SimCalorimeterHits and the
	 * intrinsic link to mc particles of the sim hits.
	 * <p>
	 * The produced relations are weighted by the energy fraction contributed by
	 * the mc particle to the SimCalorimeterHit (E_{MC,Hit}/E_{Hit})
	 * 
	 * @param caloHitSimHitRelation
	 *            The relations between CalorimeterHits and SimCalorimeterHits
	 * @return The weighted LCRelations between CalorimeterHits and MCParticles
	 */
	protected List<LCRelation> createCaloHitMcRelation(List<LCRelation> caloHitSimHitRelation) {

		List<LCRelation> caloHitMcRelation = new ArrayList<LCRelation>();

		for (LCRelation relation : caloHitSimHitRelation) {
			CalorimeterHit digiHit = (CalorimeterHit) relation.getFrom();
			SimCalorimeterHit simHit = (SimCalorimeterHit) relation.getTo();
			double hitEnergy = simHit.getRawEnergy();
			double sumOfWeights = 0;
			for (int i = 0; i < simHit.getMCParticleCount(); i++) {
				double weight = simHit.getContributedEnergy(i) / hitEnergy;
				sumOfWeights += weight;
				caloHitMcRelation.add(new BaseLCRelation(digiHit, simHit.getMCParticle(i), weight));
				print(HLEVEL_FULL, "Added a calorimeter hit to mc particle relation with weight " + weight + ".");
			}
			print(HLEVEL_FULL, "Total weight of calorimeter hit contributions is " + sumOfWeights + ".");
		}

		print(HLEVEL_NORMAL, "Created " + caloHitMcRelation.size() + " calorimeter hit to mc particle relations.");

		return caloHitMcRelation;
	}

	/**
	 * Creates the relations from Clusters to MCParticles by using a list of
	 * LCRelations from CalorimeterHits to MCParticles.
	 * <p>
	 * The produced relations are weighted by the energy fraction contributed by
	 * the MCParticle to the Cluster (E_{MC,Cluster}/E_{Cluster})
	 * 
	 * @param clusters
	 *            The list of clusters to be truth linked
	 * @param caloHitMcRelation
	 *            The relations between CalorimeterHits and MCParticles
	 * @return The weighted LCRelations between Clusters and MCParticles
	 * @throws IllegalArgumentException
	 */
	protected List<LCRelation> createClusterMcRelation(List<Cluster> clusters, List<LCRelation> caloHitMcRelation)
			throws IllegalArgumentException {

		if (caloHitMcRelation == null) {
			throw new IllegalArgumentException("No calorimeter hit to mc relations given.");
		}

		RelationalTable<CalorimeterHit, MCParticle> caloHitMcRelationTable = createRelationalTable(caloHitMcRelation);
		List<LCRelation> clusterMcRelation = new ArrayList<LCRelation>();

		for (Cluster cluster : clusters) {
			double sumOfWeights = 0;
			double clusterEnergy = cluster.getEnergy();
			Map<MCParticle, Double> mcParticlesWeight = new HashMap<MCParticle, Double>();
			for (CalorimeterHit hit : cluster.getCalorimeterHits()) {
				double hitEnergy = hit.getCorrectedEnergy();
				double hitWeight = hitEnergy / clusterEnergy;
				Map<MCParticle, Double> hitMcParticlesWeight = caloHitMcRelationTable.allFromWithWeights(hit);
				for (MCParticle mcParticle : hitMcParticlesWeight.keySet()) {
					// TODO implement optional use of Tanimoto distance
					double weight = hitWeight * hitMcParticlesWeight.get(mcParticle);
					if (useSkimmedMcParticles)
						mcParticle = mcParticleToSkimmed.get(mcParticle);
					if (mcParticlesWeight.containsKey(mcParticle)) {
						mcParticlesWeight.put(mcParticle, mcParticlesWeight.get(mcParticle) + weight);
					} else {
						mcParticlesWeight.put(mcParticle, weight);
					}
				}
			}
			mcParticlesWeight = sortMapByHighestValue(mcParticlesWeight);
			for (MCParticle mcParticle : mcParticlesWeight.keySet()) {
				double weight = mcParticlesWeight.get(mcParticle);
				sumOfWeights += weight;
				clusterMcRelation.add(new BaseLCRelation(cluster, mcParticle, weight));
				print(HLEVEL_FULL, "Added a cluster to mc particle relation with weight " + weight + ".");
				if (!fullRecoRelation)
					break;
			}
			print(HLEVEL_HIGH, "Total weight of cluster contributions is " + sumOfWeights + ".");
		}

		print(HLEVEL_NORMAL, "Created " + clusterMcRelation.size() + " cluster to mc particle relations.");

		return clusterMcRelation;
	}

	/**
	 * Creates the relations from PFOs to MCParticles by using a list of
	 * LCRelations from Tracks to MCParticles and a second list of LCRelations
	 * from Clusters to MCParticles.
	 * <p>
	 * In case of a non-charged PFO the relation is weighted using the weights
	 * from the contributing Cluster to MCParticle relations.
	 * <p>
	 * For charged PFOs the weight of the relations are calculated separately
	 * for tracks and clusters and then combined depending on a global track to
	 * cluster weight. By default the track weight is 1 and the cluster weight
	 * is 0. Thus, only the relation via track is taken into account.
	 * 
	 * @param recoParticles
	 *            The list of PFOs to be truth linked
	 * @param trackMcRelation
	 *            The relations between Tracks and MCParticles
	 * @param clusterMcRelation
	 *            The relations between Clusters and MCParticles
	 * @return The weighted LCRelations between PFOs and MCParticles
	 * @throws IllegalArgumentException
	 */
	protected List<LCRelation> createPfoMcRelation(List<ReconstructedParticle> recoParticles,
			List<LCRelation> trackMcRelation, List<LCRelation> clusterMcRelation) throws IllegalArgumentException {

		if (trackMcRelation == null) {
			throw new IllegalArgumentException("No track to mc relations given.");
		}
		if (clusterMcRelation == null) {
			throw new IllegalArgumentException("No cluster to mc relations given.");
		}

		RelationalTable<Track, MCParticle> trackMcRelationTable = createRelationalTable(trackMcRelation);
		RelationalTable<Cluster, MCParticle> clusterMcRelationTable = createRelationalTable(clusterMcRelation);
		List<LCRelation> pfoMcRelation = new ArrayList<LCRelation>();

		for (ReconstructedParticle recoParticle : recoParticles) {
			double sumOfWeights = 0;
			int pfoTrackHits = 0;
			double pfoEnergy = recoParticle.getEnergy();
			double thisPfoClusterWeight = pfoClusterWeight;
			double trackClusterNormalization = pfoTrackWeight + pfoClusterWeight;
			Map<MCParticle, Double> mcParticlesWeight = new HashMap<MCParticle, Double>();
			// if PFO has tracks use them for truth link and ignore cluster
			if (pfoTrackWeight != 0) {
				for (Track track : recoParticle.getTracks()) {
					pfoTrackHits += track.getTrackerHits().size();
				}
				for (Track track : recoParticle.getTracks()) {
					Map<MCParticle, Double> trackMcParticlesWeight = trackMcRelationTable.allFromWithWeights(track);
					double trackWeight = track.getTrackerHits().size() / (double) pfoTrackHits;
					// weigh the contribution by track to cluster weight
					trackWeight *= pfoTrackWeight / trackClusterNormalization;
					for (MCParticle mcParticle : trackMcParticlesWeight.keySet()) {
						double weight = trackWeight * trackMcParticlesWeight.get(mcParticle);
						if (useSkimmedMcParticles)
							mcParticle = mcParticleToSkimmed.get(mcParticle);
						if (mcParticlesWeight.containsKey(mcParticle)) {
							mcParticlesWeight.put(mcParticle, mcParticlesWeight.get(mcParticle) + weight);
						} else {
							mcParticlesWeight.put(mcParticle, weight);
						}
					}
				}
			}
			// If no tracks attached, only use clusters
			if (pfoTrackHits == 0) {
				thisPfoClusterWeight = 1.0;
				trackClusterNormalization = 1.0;
			}
			// if PFO has no tracks use clusters for truth link
			if (thisPfoClusterWeight != 0) {
				for (Cluster cluster : recoParticle.getClusters()) {
					Map<MCParticle, Double> clusterMcParticlesWeight = clusterMcRelationTable
							.allFromWithWeights(cluster);
					double clusterWeight = cluster.getEnergy() / pfoEnergy;
					// weigh the contribution by cluster to cluster weight
					clusterWeight *= thisPfoClusterWeight / trackClusterNormalization;
					for (MCParticle mcParticle : clusterMcParticlesWeight.keySet()) {
						double weight = clusterWeight * clusterMcParticlesWeight.get(mcParticle);
						if (useSkimmedMcParticles)
							mcParticle = mcParticleToSkimmed.get(mcParticle);
						if (mcParticlesWeight.containsKey(mcParticle)) {
							mcParticlesWeight.put(mcParticle, mcParticlesWeight.get(mcParticle) + weight);
						} else {
							mcParticlesWeight.put(mcParticle, weight);
						}
					}
				}
			}
			mcParticlesWeight = sortMapByHighestValue(mcParticlesWeight);
			for (MCParticle mcParticle : mcParticlesWeight.keySet()) {
				double weight = mcParticlesWeight.get(mcParticle);
				// need to normalize to total number of track hits
				sumOfWeights += weight;
				pfoMcRelation.add(new BaseLCRelation(recoParticle, mcParticle, weight));
				print(HLEVEL_FULL, "Added a PFO to mc particle relation with weight " + weight + ".\n" + "\tEnergy: "
						+ mcParticle.getEnergy() + "\n" + "\tCharge: " + mcParticle.getCharge() + "\n" + "\tPDGID: "
						+ mcParticle.getPDGID());
				if (!fullRecoRelation)
					break;
			}
			print(HLEVEL_HIGH, "Total weight of PFO contributions is " + sumOfWeights + ".");
		}

		print(HLEVEL_NORMAL, "Created " + pfoMcRelation.size() + " PFO to mc particle relations.");

		return pfoMcRelation;
	}

	// -------------------- Static Methods --------------------
	// TODO These can most likely move to a more general class

	/**
	 * Helper method to write a message to the output stream if the histogram
	 * level set for the driver is equal or higher than the given value.
	 * 
	 * @param histogramLevel
	 *            The level at which the message is printed
	 * @param message
	 *            The message, which will be printed to the stream
	 */
	protected void print(int histogramLevel, String message) {
		print(histogramLevel, message, false);
	}

	/**
	 * Helper method to write a message to the output stream if the histogram
	 * level set for the driver is equal or higher than the given value.
	 * 
	 * @param histogramLevel
	 *            The level at which the message is printed
	 * @param message
	 *            The message, which will be printed to the stream
	 * @param error
	 *            If true, writes to error stream instead of standard
	 */
	protected void print(int histogramLevel, String message, boolean error) {
		if (getHistogramLevel() >= histogramLevel) {
			message = getName() + ": " + message;
			if (error) {
				System.err.println(message);
			} else
				System.out.println(message);
		}
	}

	/**
	 * Adds an mc particle to a list of mc particles if it is not yet in the
	 * list. Also adds all its ancestors recursively to the same list.
	 * 
	 * @param mcParticle
	 *            The mc particle to be added to the list
	 * @param mcParticles
	 *            The list to add the mc particle to
	 */
	protected void addMcParticleWithParents(MCParticle mcParticle, List<MCParticle> mcParticles) {
		if (!mcParticles.contains(mcParticle)) {
			mcParticles.add(mcParticle);
			print(HLEVEL_FULL, "Adding mc particle to skimmed list.\n" + "\tEnergy: " + mcParticle.getEnergy() + "\n"
					+ "\tCharge: " + mcParticle.getCharge() + "\n" + "\tPDGID: " + mcParticle.getPDGID() + "\n"
					+ "\tGenStatus: " + mcParticle.getGeneratorStatus() + "\n" + "\tSimStatus: "
					+ mcParticle.getSimulatorStatus().getValue());
			for (MCParticle parent : mcParticle.getParents()) {
				addMcParticleWithParents(parent, mcParticles);
			}
		}
	}

	/**
	 * Finds the first ancestor of a given mc particle within a list of mc
	 * particles. Used to find the relevant particle in the skimmed list, when
	 * trying to find out which true particle caused a hit.
	 * 
	 * @param mcParticle
	 *            The mc particle
	 * @param mcParticles
	 *            The list of mc particles containing possible ancestors
	 * @return The mc particle ancestor. Null if none is found.
	 */
	protected MCParticle findMcParticleAncestor(MCParticle mcParticle, List<MCParticle> mcParticles) {
		MCParticle ancestor = null;

		if (mcParticles.contains(mcParticle)) {
			ancestor = mcParticle;
		} else {
			List<MCParticle> parents = mcParticle.getParents();
			if (parents.size() > 0) {
				// just look for the first ancestor here if multiple are present
				ancestor = findMcParticleAncestor(parents.get(0), mcParticles);
			}
		}
		if (ancestor == null) {
			print(HLEVEL_DEFAULT,
					"Warning: no ancestor found in mc particle list." + "\tEnergy: " + mcParticle.getEnergy() + "\n"
							+ "\tCharge: " + mcParticle.getCharge() + "\n" + "\tPDGID: " + mcParticle.getPDGID() + "\n"
							+ "\tGenStatus: " + mcParticle.getGeneratorStatus() + "\n" + "\tSimStatus: "
							+ mcParticle.getSimulatorStatus().getValue(), true);
		}
		return ancestor;
	}

	/**
	 * Converts a List of LCRelations (one to one relations with weights) into a
	 * RelationalTable (many to many relations with weights). This improves
	 * access if the relations in the LCRelation are actually many to many or
	 * one to many relations described with multiple one to one relations.
	 * 
	 * @param relations
	 *            A list of LCRelations
	 * @return A RelationalTable with the same content as the given list
	 */
	public static <F, T> RelationalTable<F, T> createRelationalTable(List<LCRelation> relations) {
		RelationalTable<F, T> relationalTable = new BaseRelationalTable<F, T>();
		for (LCRelation relation : relations) {
			relationalTable.add((F) relation.getFrom(), (T) relation.getTo(), relation.getWeight());
		}
		return relationalTable;
	}

	/**
	 * Creates a map with its keys sorted by its values in descending order from
	 * an existing map. The values have to be comparable.
	 * 
	 * @param map
	 *            The original map which should be sorted
	 * @return A new map with keys sorted by values
	 */
	public static Map sortMapByHighestValue(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return -((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
