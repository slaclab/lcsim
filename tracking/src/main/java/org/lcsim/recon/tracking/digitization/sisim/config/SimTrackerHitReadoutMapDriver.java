package org.lcsim.recon.tracking.digitization.sisim.config;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IDetectorElementContainer;
import org.lcsim.detector.IReadout;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.recon.tracking.digitization.SimTrackerHitReadoutMap;

/**
 * Stores the links between DetectorElement objects and their hits using
 * a map from IReadout to a list of hits in the event header, to avoid adding
 * transient data to the detector model objects.
 */
public class SimTrackerHitReadoutMapDriver extends CollectionHandler {
    
    public void setReadoutCollections(String[] collectionNames) {
        super.setCollections(collectionNames);
    }

    protected void process(EventHeader header) {
        super.process(header);
        
        List<List<SimTrackerHit>> collections = header.get(SimTrackerHit.class);
        for (List<SimTrackerHit> collection : collections) {
            LCMetaData meta = header.getMetaData(collection);
            IDetectorElement subdet = meta.getIDDecoder().getSubdetector().getDetectorElement();
            SimTrackerHitReadoutMap readoutMap = new SimTrackerHitReadoutMap();
            if (canHandle(meta.getName())) {
                for (SimTrackerHit hit : collection) {
                    IDetectorElement de = findDetectorElement(subdet, hit);
                    if (de == null) {
                        throw new RuntimeException("Detector element not found for SimTrackerHit!");
                    }
                    IReadout ro = de.getReadout();
                    if (!readoutMap.containsKey(ro)) {
                        readoutMap.put(ro, new ArrayList<SimTrackerHit>());
                    }
                    readoutMap.get(ro).add(hit);
                }
            }
            header.put(meta.getName() + "_ReadoutMap", readoutMap);
        }
    }
    
    private IDetectorElement findDetectorElement(IDetectorElement subdet, SimTrackerHit hit) {
        IDetectorElement de = null;
        IDetectorElementContainer matches = subdet.findDetectorElement(hit.getIdentifier());
        if (matches.size() == 1) {
            de = matches.get(0);
        } else {
            de = subdet.findDetectorElement(hit.getPositionVec());
        }
        return de;
    }
}
