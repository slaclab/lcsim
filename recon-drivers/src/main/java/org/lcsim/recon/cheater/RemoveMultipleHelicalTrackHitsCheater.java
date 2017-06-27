package org.lcsim.recon.cheater;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.LCRelation;
import org.lcsim.event.MCParticle;
import org.lcsim.event.RelationalTable;
import org.lcsim.event.RelationalTable.Mode;
import org.lcsim.event.RelationalTable.Weighting;
import org.lcsim.event.base.BaseRelationalTable;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.util.Driver;

/**
 * A driver to remove additional tracker hits from the same particle in a tracking layer.
 * This happens in regions where tracker modules are overlapping.</br>
 * The SeedTracker takes only one hit per layer and will create additional tracks from the extra hits.
 * Not removing those additional hits leads to fake tracks as well as a huge loss in performance.</br>
 * This driver uses monte carlo truth to identify hits belonging to the same particle. In addition,
 * the removed hits are required to lie within a cone of a given opening angle as seen from the first
 * hit of that particle in that plane. This driver should be run <b>after</b> digitization.
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 *
 */
public class RemoveMultipleHelicalTrackHitsCheater extends Driver {
	
	protected String trackHitCollection;
	protected String trackHitRelations;
	protected String trackMCRelations;
	protected double coneAngle;
	
	public RemoveMultipleHelicalTrackHitsCheater() {
		trackHitCollection = "HelicalTrackHits";
		trackHitRelations = "HelicalTrackHitRelations";
		trackMCRelations = "HelicalTrackMCRelations";
		coneAngle = Math.PI/18.;
	}
	
	public void setTrackHitCollection(String trackHitCollection) {
		this.trackHitCollection = trackHitCollection;
	}
	
	public void setTrackHitRelations(String trackHitRelations) {
		this.trackHitRelations = trackHitRelations;
	}
	
	public void setTrackMCRelations(String trackMCRelations) {
		this.trackMCRelations = trackMCRelations;
	}
	
	/**
	 * Sets the minimum angle between a first hit and a secondary hit in layer. If inside the cone the secondary hit will be removed.
	 * @param coneAngle
	 */
	public void setConeAngle(double coneAngle) {
		this.coneAngle = coneAngle;
	}
	
	@Override
	protected void process(EventHeader event) {
		// get the collection of HelicalTrackHits and all its LCRelations
		List<HelicalTrackHit> trackHits = event.get(HelicalTrackHit.class, trackHitCollection);
		List<LCRelation> hitRelations = event.get(LCRelation.class, trackHitRelations);
		List<LCRelation> mcRelations = event.get(LCRelation.class, trackMCRelations);
		
		// map to store the LCRelations connecting each hit with its simulated hits
		RelationalTable<HelicalTrackHit, LCRelation> hitToHitRelationMap = new BaseRelationalTable<HelicalTrackHit, LCRelation>(Mode.MANY_TO_MANY, Weighting.UNWEIGHTED);
		for (LCRelation relation : hitRelations) {
			hitToHitRelationMap.add((HelicalTrackHit) relation.getFrom(), relation);
		}
		
		// map to store the LCRelations connecting each hit with its mc particles
		RelationalTable<HelicalTrackHit, LCRelation> hitToMCRelationMap = new BaseRelationalTable<HelicalTrackHit, LCRelation>(Mode.MANY_TO_MANY, Weighting.UNWEIGHTED);
		for (LCRelation relation : mcRelations) {
			hitToMCRelationMap.add((HelicalTrackHit) relation.getFrom(), relation);
		}
		
		// map to store a map of particle to hit relations for each layer.
		Map<String, RelationalTable<HelicalTrackHit, MCParticle>> layerToHitsToParticleMap = new HashMap<String, RelationalTable<HelicalTrackHit,MCParticle>>();
		
		// map to store the list of hits for each layer
		Map<String, List<HelicalTrackHit>> layerToHitsMap = new HashMap<String, List<HelicalTrackHit>>();
		
		// map to store all particles contributing to hits of a layer
		Map<String, List<MCParticle>> layerToParticleMap = new HashMap<String, List<MCParticle>>();
		
		// fill the maps
		for (HelicalTrackHit hit : trackHits) {
			List<MCParticle> particles = hit.getMCParticles();
			String identifier = hit.getLayerIdentifier();
			if (!layerToHitsMap.containsKey(identifier)) {
				layerToHitsToParticleMap.put(identifier, new BaseRelationalTable<HelicalTrackHit, MCParticle>(Mode.MANY_TO_MANY, Weighting.WEIGHTED));
				layerToHitsMap.put(identifier, new ArrayList<HelicalTrackHit>());
				layerToParticleMap.put(identifier, new ArrayList<MCParticle>());
			}
			layerToHitsMap.get(identifier).add(hit);
			for (MCParticle particle : particles) {
				layerToHitsToParticleMap.get(identifier).add(hit, particle);
				if (!layerToParticleMap.get(identifier).contains(particle)) {
					layerToParticleMap.get(identifier).add(particle);
				}
			}
			
		}
		
		// remove hits in each layer
		for (String identifier : layerToHitsMap.keySet()) {
			RelationalTable<HelicalTrackHit, MCParticle> layerTable = layerToHitsToParticleMap.get(identifier);
			
			// lists to store which hits to keep and which to remove
			List<HelicalTrackHit> processedHits = new ArrayList<HelicalTrackHit>();
			for (MCParticle particle : layerToParticleMap.get(identifier)) {
				
				// we just have a single hit from a particle: nothing to do
				if (layerTable.allTo(particle).size() < 2) continue;
				List<HelicalTrackHit> particleHits = new ArrayList<HelicalTrackHit>(layerTable.allTo(particle));
				
				// sort the hits by distance from the IP
				Collections.sort(particleHits, new CompareHelicalTrackHitsByDistanceFromIP());
				for (HelicalTrackHit hit : particleHits) {
					Hep3Vector v1 = hit.getCorrectedPosition();
					processedHits.add(hit);
					for (HelicalTrackHit otherHit : particleHits) {
						if (!trackHits.contains(otherHit) || processedHits.contains(otherHit)) continue;
						Hep3Vector v12 = VecOp.sub(otherHit.getCorrectedPosition(), v1);
						
						// calculate angle of the direction of the second hit with respect to the line-of-sight from IP to the first hit
						double deltaPhi = Math.acos(VecOp.dot(v1, v12)/(v1.magnitude()*v12.magnitude()));
						if (Math.abs(deltaPhi) < coneAngle) {
							// remove the hit and all relations pointing to it
							trackHits.remove(otherHit);
							hitRelations.removeAll(hitToHitRelationMap.allFrom(otherHit));
							mcRelations.removeAll(hitToMCRelationMap.allFrom(otherHit));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Sort HelicalTrackHits by distance from the IP
	 */
	protected class CompareHelicalTrackHitsByDistanceFromIP implements Comparator<HelicalTrackHit> {
		@Override
		public int compare(HelicalTrackHit o1, HelicalTrackHit o2) {
			double r1 = o1.getCorrectedPosition().magnitude();
			double r2 = o2.getCorrectedPosition().magnitude();
			if (r1 < r2) {
				return -1;
			} else if (r2 < r1) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
