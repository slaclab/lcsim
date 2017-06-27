package org.lcsim.mc.fast;

import org.lcsim.mc.fast.cluster.ronan.MCFastRonan;
import org.lcsim.mc.fast.reconstructedparticle.MCFastReconstructedParticleDriver;
import org.lcsim.mc.fast.tracking.MCFastTracking;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lcsim.mc.fast.util.CreateFinalStateMCParticleList;
import org.lcsim.util.Driver;

/**
 *
 * @author Tony Johnson
 */
public class MCFast extends Driver {
    /** Creates a new instance of MCFast */

    public static Logger log;
    String FSname = "GenFinalStateParticles";

    public MCFast(boolean beamSpotConstraint, boolean simple, long seed, boolean printinfo, boolean refPoint000) {
        this(beamSpotConstraint, simple, printinfo, refPoint000);
        getRandom().setSeed(seed);
    }

    public MCFast(boolean beamSpotConstraint, boolean simple, long seed, boolean printinfo) {
        this(beamSpotConstraint, simple, printinfo);
        getRandom().setSeed(seed);
    }

    public MCFast(boolean beamSpotConstraint, boolean simple, boolean printinfo) {
        this(beamSpotConstraint, simple, printinfo, false);
    }

    public MCFast(boolean beamSpotConstraint, boolean simple, boolean printinfo, boolean refPoint000) {
        log = getLogger();
        if (printinfo) {
            log.setLevel(Level.INFO);
        } else {
            log.setLevel(Level.WARNING);
        }
        add(new CreateFinalStateMCParticleList("Gen"));
        MCFastTracking mcft = new MCFastTracking(beamSpotConstraint, simple);
        mcft.setFSList(FSname);
        add(mcft);
        MCFastRonan mcfr = new MCFastRonan();
        mcfr.setFSList(FSname);
        add(mcfr);
        add(new MCFastReconstructedParticleDriver(refPoint000));
    }

    public MCFast(boolean beamSpotConstraint, boolean simple) {
        this(beamSpotConstraint, simple, false);
    }

    public MCFast(boolean beamSpotConstraint, boolean simple, long seed) {
        this(beamSpotConstraint, simple, seed, false);
    }

    public MCFast() {
        this(false, false);
    }
}
