package org.lcsim.recon.calorimetry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.base.BaseSimCalorimeterHit;
import org.lcsim.util.Driver;
import org.lcsim.lcio.LCIOConstants;
import org.lcsim.lcio.LCIOUtil;



/**
 * Driver to re-calculate the raw energy of all SimCalorimeterHits from the sum of their energy contributions. 
 * 
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 */
public class ResetCalorimeterHitEnergy extends Driver {
	
	protected List<String> ignoreCollections;
	
	public ResetCalorimeterHitEnergy() {
		ignoreCollections = new ArrayList<String>();
	}
	
	public void setIgnoreCollection(String collectionName) {
		ignoreCollections.add(collectionName);
	}
	
	public void setIgnoreCollections(String[] collectionNames) {
		ignoreCollections.addAll(Arrays.asList(collectionNames));
	}
	
	@Override
	protected void process(EventHeader event) {
		
		List<List<SimCalorimeterHit>> caloHitCollections = event.get(SimCalorimeterHit.class);
		
		for (List<SimCalorimeterHit> caloHitCollection : caloHitCollections) {
			LCMetaData metaData = event.getMetaData(caloHitCollection);
			String collectionName = metaData.getName();
			if (ignoreCollections.contains(collectionName)) {
				continue;
			}
			boolean hasPDG = LCIOUtil.bitTest(metaData.getFlags(),LCIOConstants.CHBIT_PDG);
			List<SimCalorimeterHit> resetHits = new ArrayList<SimCalorimeterHit>();
			for (SimCalorimeterHit caloHit : caloHitCollection) {
				long id = caloHit.getCellID();
				double totalEnergy = 0.;
				double time = caloHit.getTime();
				int nMCP = caloHit.getMCParticleCount();
				Object[] mcparts = new Object[nMCP];
				float[] energies = new float[nMCP];
				float[] times = new float[nMCP];
				int[] pdgs = null;
				List<float[]> steps = new ArrayList<float[]>();
				if (hasPDG) pdgs = new int[nMCP];
				// fill arrays with values from hit
				for (int i = 0; i != nMCP; i++) {
					mcparts[i] = caloHit.getMCParticle(i);
					energies[i] = (float) caloHit.getContributedEnergy(i);
					totalEnergy += energies[i];
					times[i] = (float) caloHit.getContributedTime(i);
					if (hasPDG){
						pdgs[i] = caloHit.getPDG(i);
						steps.add(caloHit.getStepPosition(i));
					}
				}
				
				BaseSimCalorimeterHit resetHit = new BaseSimCalorimeterHit(id, totalEnergy, time, mcparts, energies, times, pdgs, steps, metaData);
				resetHits.add(resetHit);
			}
			caloHitCollection.clear();
			caloHitCollection.addAll(resetHits);
		}
		
	}

}
