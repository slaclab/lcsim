package org.lcsim.recon.calorimetry;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.base.BaseLCSimEvent;
import org.lcsim.util.Driver;

/**
 * Helper class to select instrumented layers in a calorimeter collection and remove all the other hits from the collection.
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 *
 */
public class CalorimeterLayerSelector extends Driver {

	protected String caloCollection = "";
	protected List<Integer> activeLayers;
	
	public CalorimeterLayerSelector() {
		
	}
	
	public void setCollectionName(String collectionName) {
		this.caloCollection = collectionName;
		this.activeLayers = new ArrayList<Integer>();
	}
	
	public void setActiveLayers(int[] layers) {
		activeLayers.clear();
		for (int i = 0; i != layers.length; i++) {
			activeLayers.add(layers[i]);
		}
	}
	
	@Override
	protected void process(EventHeader event) {
		List<SimCalorimeterHit> caloHits = new ArrayList<SimCalorimeterHit>();
		try {
			caloHits = event.get(SimCalorimeterHit.class, caloCollection);
		} catch (Exception e) {
			System.err.println("Error: Collection "+caloCollection+" does not exist.");
			return;
		}
		if (this.getHistogramLevel() > HLEVEL_NORMAL) {
			System.out.println("Removing calorimeter hits from "+caloCollection+" for all layers except:");
			for (int layer : activeLayers) {
				System.out.println("\tLayer "+layer);
			}
		}
		
		List<SimCalorimeterHit> hitsToKeep = new ArrayList<SimCalorimeterHit>();
		
		for (SimCalorimeterHit hit : caloHits) {
			if (activeLayers.contains(hit.getLayerNumber())) {
				hitsToKeep.add(hit);
			}
		}
		
		caloHits.clear();
		caloHits.addAll(hitsToKeep);
	}
}
