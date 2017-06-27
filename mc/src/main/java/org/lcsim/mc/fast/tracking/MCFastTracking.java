package org.lcsim.mc.fast.tracking;

/**
 *  $Id: MCFastTracking.java,v 1.19 2009/05/29 22:49:55 timb Exp $
 */
import hep.physics.particle.Particle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lcsim.conditions.ConditionsEvent;
import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.EventHeader;
import org.lcsim.event.Track;
import org.lcsim.event.LCRelation;
import org.lcsim.event.MCParticle;
import org.lcsim.util.Driver;
import org.lcsim.event.base.MyLCRelation;

/**
 * Fast Monte Carlo tracking simulator
 */
public class MCFastTracking extends Driver implements ConditionsListener {
    private TrackResolutionTables parm;
    private SimpleTables SmTbl;
    private boolean beamSpotConstraint;
    private boolean simple;
    private final static double[] IP = { 0, 0, 0 };
    private boolean defaultMC = true;
    private String fsname;
    private String outputListName = null;

    public MCFastTracking() {
        this(false);
    }

    public MCFastTracking(boolean beamSpotConstraint) {
        this.beamSpotConstraint = beamSpotConstraint;
    }

    public MCFastTracking(boolean beamSpotConstraint, boolean simple) {
        this.beamSpotConstraint = beamSpotConstraint;
        this.simple = simple;
    }

    public void setBeamSpotConstraint(boolean beamSpotConstraint) {
        this.beamSpotConstraint = beamSpotConstraint;
        if (parm != null) {
            ConditionsSet conditions = getConditionsManager().getConditions("TrackParameters");
            parm = setTrackResolutionTables(conditions, beamSpotConstraint);
        }
    }

    public boolean isBeamSpotConstraint() {
        return this.beamSpotConstraint;
    }

    private TrackResolutionTables setTrackResolutionTables(ConditionsSet conditions, boolean beamSpotConstraint) {
        try {
            return new TrackResolutionTables(conditions, beamSpotConstraint);
        } catch (IOException x) {
            throw new RuntimeException("Error reading track resolution tables", x);
        }
    }

    public void setFSList(String fslist) {
        fsname = fslist;
        defaultMC = false;
    }

    protected void process(EventHeader event) {
        if (defaultMC) {
            fsname = "MCParticle";
        } else {
            if (!event.hasCollection(MCParticle.class, fsname)) {
                System.err.println("Collection " + fsname + " not found. Default Final State particles being used");
                fsname = "MCParticle";
                defaultMC = true;
            }
        }
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

        double bField = event.getDetector().getFieldMap().getField(IP)[2];
        boolean hist = getHistogramLevel() > 0;

        List<Track> trackList = new ArrayList<Track>();
        List<LCRelation> lcrelationList = new ArrayList<LCRelation>();
        for (Iterator i = event.get(MCParticle.class, fsname).iterator(); i.hasNext();) {
            Particle p = (Particle) i.next();

            // filter for FINAL_STATE
            if (defaultMC) {
                if (p.getGeneratorStatus() != Particle.FINAL_STATE) {
                    continue;
                }
            }
            double pCharge = p.getCharge();
            if (pCharge == 0 || Double.isNaN(pCharge) || pCharge == Double.NEGATIVE_INFINITY || pCharge == Double.POSITIVE_INFINITY) {
                continue;
            }

            double[] momentum = p.getMomentum().v();
            if (Double.isNaN(momentum[0]) || Double.isNaN(momentum[1]) || Double.isNaN(momentum[2])) {
                continue;
            }

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

            try {
                ReconTrack newTrack = new ReconTrack(bField, parm, SmTbl, getRandom(), p, hist, simple);
                trackList.add(newTrack);
                lcrelationList.add(new MyLCRelation((Track) newTrack, (MCParticle) p));
            } catch (hep.physics.particle.properties.UnknownParticleIDException x) {
                System.out.println("WARNING: MCFastTracking ignored a particle of type " + p.getPDGID());
            }
        }
        if (outputListName == null) {
            outputListName = "Tracks";
        }
        event.put(outputListName, trackList, Track.class, 0);
        event.put("TracksToMCP", lcrelationList, LCRelation.class, 0);
    }

    /** Specify the name under which to write out the list of tracks to the event. Default is EventHeader.TRACKS ("Tracks") */
    public void setOutputList(String name) {
        outputListName = name;
    }

    public void conditionsChanged(ConditionsEvent event) {
        ConditionsSet conditions = getConditionsManager().getConditions("TrackParameters");
        ConditionsSet simpleconditions = getConditionsManager().getConditions("SimpleTrack");
        parm = setTrackResolutionTables(conditions, beamSpotConstraint);
        SmTbl = new SimpleTables(simpleconditions);
    }
}
