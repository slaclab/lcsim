package org.lcsim.recon.tracking.digitization.sisim.config;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.SimTrackerHit;

/**
 * This Driver assigns {@link org.lcsim.event.SimTrackerHit} objects to the readouts 
 * of their corresponding {@link org.lcsim.detector.IDetectorElement}.
 * 
 * @author jeremym
 */
public class SimTrackerHitReadoutDriver extends CollectionHandler {
    
    boolean needCleanupDriver = true;
    static boolean debug = false;

    public SimTrackerHitReadoutDriver() {
    }

    public SimTrackerHitReadoutDriver(List<String> collectionNames) {
        super(collectionNames);
    }

    public SimTrackerHitReadoutDriver(String[] collectionNames) {
        super(collectionNames);
    }

    public void setReadoutCollections(String[] collectionNames) {
        super.setCollections(collectionNames);
    }

    public void setDebug(boolean debug) {
        SimTrackerHitReadoutDriver.debug = debug;
    }

    public void startOfData() {
        if (needCleanupDriver) {
            add(new ReadoutCleanupDriver(new ArrayList<String>(collections)));
            needCleanupDriver = false;
        }
    }

    protected void process(EventHeader header) {
        super.process(header);

        if (debug) {
            System.out.println(this.getClass().getSimpleName() + " - handling collections");

            for (String cname : this.collections) {
                System.out.println("  " + cname);
            }
        }

        List<List<SimTrackerHit>> collections = header.get(SimTrackerHit.class);
        for (List<SimTrackerHit> collection : collections) {
            LCMetaData meta = header.getMetaData(collection);
            if (canHandle(meta.getName())) {
                for (SimTrackerHit hit : collection) {
                    //hit.setMetaData(meta);
                    hit.getDetectorElement().getReadout().addHit(hit);
                    if (debug) {
                        System.out.println("Driver assigned hit " + hit.getExpandedIdentifier().toString() + " to "
                                + hit.getDetectorElement().getName() + " with id " + hit.getDetectorElement().getExpandedIdentifier().toString()
                                + ".");
                        System.out.println("    hit raw: " + hit.getIdentifier().toHexString() + ", DE raw: "
                                + hit.getDetectorElement().getIdentifier().toHexString());
                        System.out.println("    hit pos: " + hit.getPositionVec().toString() + ", DE pos: "
                                + hit.getDetectorElement().getGeometry().getPosition().toString());
                        System.out.println();
                        if (!hit.getDetectorElement().getReadout().getHits(SimTrackerHit.class).contains(hit)) {
                            throw new RuntimeException("Readout of " + hit.getDetectorElement().getName()
                                    + " is missing a SimTrackerHit that points to it.");
                        }
                    }
                }
            }
        }
    }
}
