package org.lcsim.recon.cheater;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import org.lcsim.event.RelationalTable;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.MCParticle;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.base.BaseRelationalTable;

/**
 * A driver to remove additional tracker hits from the same particle in a tracking layer.
 * This happens in regions where tracker modules are overlapping.</br>
 * The SeedTracker takes only one hit per layer and will create additional tracks from the extra hits.
 * Not removing those additional hits leads to fake tracks as well as a huge loss in performance.</br>
 * This driver uses monte carlo truth to identify hits belonging to the same particle. It should be run <b>before</b> digitization.
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 *
 */
public class RemoveMultipleTrackHitsCheater extends Driver {
	
	public RemoveMultipleTrackHitsCheater() {

	}
	
	@Override
	protected void process(EventHeader event) {
		List<List<SimTrackerHit>> trackerHitCollections = event.get(SimTrackerHit.class);
		List<MCParticle> mcParticles = event.getMCParticles();
		
		// need to relate each mc particle to its hits per collection
		Map<LCMetaData, RelationalTable<MCParticle, SimTrackerHit>> collectionToMcToHit = new HashMap<LCMetaData, RelationalTable<MCParticle,SimTrackerHit>>();
		
		int nHits = 0;
		
		// build map of hit to mc particle relation
		for (List<SimTrackerHit> trackerHitCollection : trackerHitCollections) {
			nHits += trackerHitCollection.size();
			LCMetaData meta = event.getMetaData(trackerHitCollection);
			if (!collectionToMcToHit.containsKey(meta)) {
				collectionToMcToHit.put(meta, new BaseRelationalTable<MCParticle, SimTrackerHit>(RelationalTable.Mode.ONE_TO_MANY, RelationalTable.Weighting.UNWEIGHTED));
			}
			RelationalTable<MCParticle, SimTrackerHit> mcpToHits = collectionToMcToHit.get(meta);
			
			for (SimTrackerHit hit : trackerHitCollection) {
				MCParticle mcParticle = hit.getMCParticle();
				if (mcParticles.contains(mcParticle)) {
					mcpToHits.add(mcParticle, hit);
				}
			}
		}
		
		int nHitsRemoved = 0;
		
		for (LCMetaData meta : collectionToMcToHit.keySet()) {
			RelationalTable<MCParticle, SimTrackerHit> mcpToHits = collectionToMcToHit.get(meta);
			
			for (MCParticle mcp : mcParticles) {
				Set<SimTrackerHit> hits = mcpToHits.allFrom(mcp);
				// store which layer has been hit by this mc particle
				Map<Integer, SimTrackerHit> layerToHits = new HashMap<Integer, SimTrackerHit>();
				
				for (SimTrackerHit hit : hits) {
					int layer = hit.getLayerNumber();
					if (layerToHits.containsKey(layer)) {
						SimTrackerHit hit2 = layerToHits.get(layer);
						// remove hit if in the same layer but not on same module
						// detector element is a Sensor and the module is one up in the hierarchy
						if (!hit.getDetectorElement().getParent().equals(hit2.getDetectorElement().getParent())) {
							event.get(SimTrackerHit.class, meta.getName()).remove(hit);
							nHitsRemoved++;
						}
					} else {
						// store that this layer had a hit by this mc particle
						layerToHits.put(layer, hit);
					}
				}
			}
		}
		
		if (this.getHistogramLevel() > HLEVEL_NORMAL) System.out.println("Removed "+nHitsRemoved+"/"+nHits+" tracker hits");
	}
}
