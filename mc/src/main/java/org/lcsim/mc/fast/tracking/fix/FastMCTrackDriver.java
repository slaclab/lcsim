/**
 * @version $Id: FastMCTrackDriver.java,v 1.1 2007/11/29 21:29:35 jstrube Exp $
 */
package org.lcsim.mc.fast.tracking.fix;

import hep.physics.particle.Particle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lcsim.conditions.ConditionsEvent;
import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.event.Track;
import org.lcsim.mc.fast.tracking.SimpleTables;
import org.lcsim.mc.fast.tracking.TrackResolutionTables;
import org.lcsim.util.Driver;

/**
 * A replacement for the current FastMC Tracks. The simple tables are currently not implemented
 * @author jstrube
 * 
 */
public class FastMCTrackDriver extends Driver implements ConditionsListener {
    private TrackResolutionTables parm;
    private SimpleTables SmTbl;
    private boolean beamSpotConstraint;
    private boolean simple;
    private final static double[] IP = { 0, 0, 0 };

    public FastMCTrackDriver(boolean beamSpotConstraint) {
        this.beamSpotConstraint = beamSpotConstraint;
    }

    public FastMCTrackDriver() {
        this(false);
    }

    protected void process(EventHeader event) {
        if (parm == null) {
            ConditionsSet conditions = getConditionsManager().getConditions("TrackParameters");
            conditions.addConditionsListener(this);
            parm = setTrackResolutionTables(conditions, beamSpotConstraint);
        }

        if (SmTbl == null) {
            ConditionsSet conditions = getConditionsManager().getConditions("SimpleTrack");
            conditions.addConditionsListener(this);
            SmTbl = new SimpleTables(conditions);
        }

        FastMCTrackFactory factory = new FastMCTrackFactory(event, beamSpotConstraint);

        double bField = event.getDetector().getFieldMap().getField(IP)[2];
        boolean hist = getHistogramLevel() > 0;

        List<Track> trackList = new ArrayList<Track>();
        for (MCParticle p : event.getMCParticles()) {
            // filter for FINAL_STATE
            if (p.getGeneratorStatus() != Particle.FINAL_STATE) {
                continue;
            }
            double pCharge = p.getCharge();
            if (pCharge == 0 || Double.isNaN(pCharge) || pCharge == Double.NEGATIVE_INFINITY || pCharge == Double.POSITIVE_INFINITY) {
                continue;
            }

            double[] momentum = p.getMomentum().v();
            double pt2 = (momentum[0] * momentum[0]) + (momentum[1] * momentum[1]);
            double pt = Math.sqrt(pt2);
            double ptot = Math.sqrt(pt2 + (momentum[2] * momentum[2]));
            double cosTheta = momentum[2] / ptot;

            // within acceptance
            if (pt < parm.getPtMin()) {
                continue;
            }
            if (Math.abs(cosTheta) > parm.getPolarOuter()) {
                continue;
            }

            Track t = factory.getTrack(p);
            trackList.add(t);
        }
        event.put("Tracks", trackList, Track.class, 0);
    }

    public void conditionsChanged(ConditionsEvent event) {
        ConditionsSet conditions = getConditionsManager().getConditions("TrackParameters");
        ConditionsSet simpleconditions = getConditionsManager().getConditions("SimpleTrack");
        parm = setTrackResolutionTables(conditions, beamSpotConstraint);
        SmTbl = new SimpleTables(simpleconditions);
    }

    private TrackResolutionTables setTrackResolutionTables(ConditionsSet conditions, boolean beamSpotConstraint) {
        try {
            return new TrackResolutionTables(conditions, beamSpotConstraint);
        } catch (IOException x) {
            throw new RuntimeException("Error reading track resolution tables", x);
        }
    }

}
