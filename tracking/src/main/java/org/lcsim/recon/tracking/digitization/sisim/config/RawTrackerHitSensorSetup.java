package org.lcsim.recon.tracking.digitization.sisim.config;

import java.util.List;

import org.lcsim.detector.DetectorElementStore;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.RawTrackerHit;

/**
 * Assigns {@link org.lcsim.detector.IDetectorElement}s to {@link org.lcsim.event.RawTrackerHit}s.
 * 
 * @author jeremym
 */
public class RawTrackerHitSensorSetup extends CollectionHandler {
    
    public RawTrackerHitSensorSetup() {
    }

    public void setReadoutCollections(String[] collectionNames) {
        super.setCollections(collectionNames);
    }


    protected void process(EventHeader header) {
        super.process(header);
        List<List<RawTrackerHit>> collections = header.get(RawTrackerHit.class);
        for (List<RawTrackerHit> collection : collections) {
            LCMetaData meta = header.getMetaData(collection);
            if (canHandle(meta.getName())) {                
                // Set the sensor links on the hits.
                setSensors(meta, collection);
            }
        }
    }
    
    static public final void setSensors(LCMetaData meta, List<RawTrackerHit> hits) {

        // Get the ID dictionary and field information.
        IIdentifierDictionary dict = meta.getIDDecoder().getSubdetector().getDetectorElement().getIdentifierHelper().getIdentifierDictionary();
        int fieldIdx = dict.getFieldIndex("side");
        int sideIdx = dict.getFieldIndex("strip");
        
        for (RawTrackerHit hit : hits) {
               
            // The "side" and "strip" fields needs to be stripped from the ID for sensor lookup.
            IExpandedIdentifier expId = dict.unpack(hit.getIdentifier());
            expId.setValue(fieldIdx, 0);
            expId.setValue(sideIdx, 0);
            IIdentifier strippedId = dict.pack(expId);
        
            // Find the sensor DetectorElement.
            List<IDetectorElement> des = DetectorElementStore.getInstance().find(strippedId);
            if (des == null || des.size() == 0) {
                throw new RuntimeException("Failed to find any DetectorElements with stripped ID <0x" + Long.toHexString(strippedId.getValue()) + ">.");
            }
            else if (des.size() == 1) {
                hit.setDetectorElement((SiSensor)des.get(0));
            }
            else {
                // Use first sensor found, which should work unless there are sensors with duplicate IDs.
                for (IDetectorElement de : des) {
                    if (de instanceof SiSensor) {
                        hit.setDetectorElement((SiSensor)de);
                        break;
                    }
                }
            }   
            
            // No sensor was found.
            if (hit.getDetectorElement() == null) {
                throw new RuntimeException("No sensor was found for hit with stripped ID <0x" + Long.toHexString(strippedId.getValue()) + ">.");
            }
        }
    }
    
}