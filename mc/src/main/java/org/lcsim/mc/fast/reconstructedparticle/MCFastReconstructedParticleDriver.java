package org.lcsim.mc.fast.reconstructedparticle;

import hep.physics.particle.Particle;
import hep.physics.particle.properties.ParticlePropertyManager;
import hep.physics.particle.properties.ParticlePropertyProvider;
import hep.physics.particle.properties.ParticleType;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.Track;
import org.lcsim.mc.fast.cluster.ronan.ReconHADCluster;
import org.lcsim.mc.fast.tracking.ReconTrack;
import org.lcsim.util.Driver;
import org.lcsim.conditions.ConditionsEvent;
import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsSet;

import static java.lang.Math.abs;
import org.lcsim.mc.fast.cluster.ronan.ReconEMCluster;
import org.lcsim.util.aida.AIDA;

/**
 *
 * @author ngraf
 */
public class MCFastReconstructedParticleDriver extends Driver implements ConditionsListener {
    private boolean refPoint000;
    private ParticlePropertyProvider ppp;
    private IDResolutionTables IDEff;
    private AIDA aida = AIDA.defaultInstance();
    private ParticleType eminus;
    private ParticleType eplus;
    private ParticleType klong;
    private ParticleType muminus;
    private ParticleType muplus;
    private ParticleType neutron;
    private ParticleType photon;
    private ParticleType pizero;
    private ParticleType piplus;
    private ParticleType piminus;
    private ParticleType pplus;
    private ParticleType pminus;
    private ParticleType kplus;
    private ParticleType kminus;

    /** Creates a new instance of MCFastReconstructedParticleDriver */
    public MCFastReconstructedParticleDriver() {
        this(true);
    }

    public MCFastReconstructedParticleDriver(boolean refPoint000) {
        this(ParticlePropertyManager.getParticlePropertyProvider(), refPoint000);
    }

    public MCFastReconstructedParticleDriver(ParticlePropertyProvider ppp) {
        this(ppp, true);
    }

    public MCFastReconstructedParticleDriver(ParticlePropertyProvider ppp, boolean refPoint000) {
        this.refPoint000 = refPoint000;
        this.ppp = ppp;
        //
        eminus = ppp.get(11);
        eplus = ppp.get(-11);
        klong = ppp.get(130);
        muminus = ppp.get(13);
        muplus = ppp.get(-13);
        neutron = ppp.get(2112);
        photon = ppp.get(22);
        pizero = ppp.get(111);
        piplus = ppp.get(211);
        piminus = ppp.get(-211);
        pplus = ppp.get(2212);
        pminus = ppp.get(-2212);
        kplus = ppp.get(321);
        kminus = ppp.get(-321);
    }

    protected void process(EventHeader event) {

        boolean hist = getHistogramLevel() > 0;

        if (IDEff == null) {
            ConditionsSet idconditions = getConditionsManager().getConditions("IDEfficiency");
            idconditions.addConditionsListener(this);
            IDEff = new IDResolutionTables(idconditions);
        }

        Random rand = getRandom();

        List<Track> tracks = event.get(Track.class, "Tracks");
        List<Cluster> clusters = event.get(Cluster.class, "Clusters");

        // Set up Track-Cluster association; for now cheat using MCParticle
        Map<Particle, Track> m_pt = new HashMap<Particle, Track>();
        Map<Particle, Cluster> m_pc = new HashMap<Particle, Cluster>();
        Map<Cluster, Track> m_ct = new HashMap<Cluster, Track>();
        Map<Track, Cluster> m_tc = new HashMap<Track, Cluster>();

        for (Track t : tracks)
            m_pt.put(((ReconTrack) t).getMCParticle(), t);
        for (Cluster c : clusters)
            m_pc.put((c instanceof ReconEMCluster ? ((ReconEMCluster) c).getMCParticle() : ((ReconHADCluster) c).getMCParticle()), c);
        for (Track t : tracks)
            m_tc.put(t, m_pc.get(((ReconTrack) t).getMCParticle()));
        for (Cluster c : clusters)
            m_ct.put(c, m_pt.get(c instanceof ReconEMCluster ? ((ReconEMCluster) c).getMCParticle() : ((ReconHADCluster) c).getMCParticle()));

        List<ReconstructedParticle> rpList = new ArrayList<ReconstructedParticle>();
        // start with the smeared tracks...
        for (Track t : tracks) {
            ParticleType type = null;
            if (t instanceof ReconTrack) {
                ReconTrack rt = (ReconTrack) t;
                Particle p = rt.getMCParticle();
                int pdgid = p.getPDGID();

                // charged track id
                if ((abs(pdgid) == 11) && (rand.nextDouble() < IDEff.getElectronEff())) {
                    type = rt.getCharge() > 0 ? eplus : eminus;
                } else if ((abs(pdgid) == 13) && (rand.nextDouble() < IDEff.getMuonEff())) {
                    type = rt.getCharge() > 0 ? muplus : muminus;
                } else if ((abs(pdgid) == 2212) && (rand.nextDouble() < IDEff.getProtonEff())) {
                    type = rt.getCharge() > 0 ? pplus : pminus;
                } else if ((abs(pdgid) == 321) && (rand.nextDouble() < IDEff.getKaonEff())) {
                    type = rt.getCharge() > 0 ? kplus : kminus;
                } else {
                    type = rt.getCharge() > 0 ? piplus : piminus;
                }

                if ((p.getEnergy() > 10) && (hist)) {
                    if (Math.abs(t.getTrackParameter(4)) < 1) {
                        aida.histogram1D("track-particle", 150, -0.0003, 0.0003).fill((Math.sqrt(t.getPX() * t.getPX() + t.getPY() * t.getPY() + t.getPZ() * t.getPZ()) - Math.sqrt(p.getPX() * p.getPX() + p.getPY() * p.getPY() + p.getPZ() * p.getPZ())) / (p.getPX() * p.getPX() + p.getPY() * p.getPY() + p.getPZ() * p.getPZ()));
                    }
                    if ((Math.abs(t.getTrackParameter(4)) < 2.16) && (Math.abs(t.getTrackParameter(4)) > 1.96) && (p.getMomentum().magnitude() > 80) && (p.getMomentum().magnitude() < 120)) {
                        aida.histogram1D("track-particle-cut", 150, -0.01, 0.01).fill((Math.sqrt(t.getPX() * t.getPX() + t.getPY() * t.getPY() + t.getPZ() * t.getPZ()) - Math.sqrt(p.getPX() * p.getPX() + p.getPY() * p.getPY() + p.getPZ() * p.getPZ())) / (Math.sqrt(p.getPX() * p.getPX() + p.getPY() * p.getPY() + p.getPZ() * p.getPZ())));
                    }
                }

                // assume pion for remaining charged tracks
                MCFastReconstructedParticle rp = new MCFastReconstructedParticle(t, type, p, m_tc.get(t), IDEff.getWtChgTrkCal(), refPoint000);
                rpList.add(rp);

                if (hist) {
                    aida.histogram1D("recon-particle", 150, -5, 5).fill((rp.getEnergy() - p.getEnergy()) / (Math.sqrt(p.getEnergy())));
                }
            }
        }

        // loop over clusters...
        for (Cluster c : clusters) {
            // if(m_ct.get(c) != null) continue;
            Particle p = null;
            ParticleType type = null;
            // photons for EM
            if (c instanceof ReconEMCluster) {
                ReconEMCluster emc = (ReconEMCluster) c;
                p = emc.getMCParticle();
                if (m_ct.get(c) != null)
                    continue;
                type = photon;
                if (hist) {
                    aida.histogram1D("photonCLS-particle", 150, -3, 3).fill((emc.getEnergy() - emc.getMCParticle().getEnergy()) / (Math.sqrt(emc.getMCParticle().getEnergy())));
                }
            }
            // assume a KZeroLong here for had cluster
            else if (c instanceof ReconHADCluster) {
                ReconHADCluster emc = (ReconHADCluster) c;
                p = emc.getMCParticle();
                int pdgid = p.getPDGID();
                if ((abs(pdgid) == 2112) && (rand.nextDouble() < IDEff.getNeutronEff())) {
                    type = neutron;
                } else {
                    type = klong;
                }
                if (m_ct.get(c) != null || c.getEnergy() < type.getMass())
                    continue;
                if (hist) {
                    aida.histogram1D("hadronCLS-particle", 150, -3, 3).fill((emc.getEnergy() - emc.getMCParticle().getEnergy()) / (Math.sqrt(emc.getMCParticle().getEnergy())));
                }

            }
            MCFastReconstructedParticle rp = new MCFastReconstructedParticle(c, type, p);
            rpList.add(rp);
            if (hist) {
                aida.histogram1D("recon-particle", 150, -10, 10).fill(rp.getEnergy() - p.getEnergy());
            }

        }
        // add the reconstructedparticles to the event
        event.put("MCFastReconstructedParticles", rpList, ReconstructedParticle.class, 0);

    }

    public void conditionsChanged(ConditionsEvent event) {
        ConditionsSet idconditions = getConditionsManager().getConditions("IDEfficiency");
        IDEff = new IDResolutionTables(idconditions);
    }

}
