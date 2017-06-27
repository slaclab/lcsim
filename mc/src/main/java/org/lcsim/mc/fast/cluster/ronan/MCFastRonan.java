package org.lcsim.mc.fast.cluster.ronan;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lcsim.mc.fast.MCFast;
import org.lcsim.conditions.ConditionsEvent;
import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.Cluster;
import org.lcsim.event.LCRelation;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.util.Driver;
import org.lcsim.event.base.MyLCRelation;

/**
 * Fast Monte Carlo cluster simulator
 * @author M.Ronan Oct 2000 - Added "refined" cluster simulation
 * @version
 */
public class MCFastRonan extends Driver implements ConditionsListener {
    private final static int ElecID = 11;
    private final static int NuEID = 12;
    private final static int MuID = 13;
    private final static int NuMuID = 14;
    private final static int NuTauID = 16;
    private final static int PhotonID = 22;
    private final static int Neutralino1 = 1000022;
    private final static int Neutralino2 = 1000023;
    private final static int Neutralino3 = 1000025;
    private final static int Neutralino4 = 1000035;
    private boolean defaultMC = true;
    private String fsname;

    private ClusterResolutionTables clusterParm;

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
            }
        }
        if (clusterParm == null) {
            ConditionsSet conditions = getConditionsManager().getConditions("ClusterParameters");
            conditions.addConditionsListener(this);
            clusterParm = new ClusterResolutionTables(conditions);
        }
        List<Cluster> cl = new ArrayList<Cluster>();
        List<LCRelation> lcrelationList = new ArrayList<LCRelation>();

        boolean hist = getHistogramLevel() > 0;

        List<MCParticle> particles = event.get(MCParticle.class, fsname);
        for (MCParticle p : particles) {

            // filter for FINALSTATE
            if (defaultMC) {
                if (p.getGeneratorStatus() != p.FINAL_STATE) {
                    continue;
                }
            }

            int PDGID = p.getPDGID();
            int absPDGID = Math.abs(PDGID);
            double charge = p.getCharge();

            // filter neutrinos
            boolean neutrino = absPDGID == NuEID || absPDGID == NuMuID || absPDGID == NuTauID || absPDGID == Neutralino1 || absPDGID == Neutralino2 || absPDGID == Neutralino3 || absPDGID == Neutralino4;
            if (neutrino) {
                continue;
            }

            double E = p.getEnergy();
            if (Double.isNaN(E)) {
                continue;
            }

            double pt2 = p.getMomentum().magnitudeSquared() - p.getPZ() * p.getPZ();
            double pt = Math.sqrt(pt2);
            double ptot = p.getMomentum().magnitude();
            double cosTheta = p.getPZ() / p.getMomentum().magnitude();

            Random rand = getRandom();

            // Photons
            if (absPDGID == PhotonID || absPDGID == ElecID) {
                // within acceptance
                // double thing = (1 - 1 / ( 1 + Math.exp( (E-clusterParm.getEMOnset())*clusterParm.getEMSharpness() ) ));
                // if (rand.nextDouble() > thing)
                // {
                // continue;
                // }
                if (E < clusterParm.getEMOnset()) {
                    continue;
                }
                if (Math.abs(cosTheta) > clusterParm.getPolarEMOuter()) {
                    continue;
                }

                cl.add(new ReconEMCluster(clusterParm, rand, p, hist));
                lcrelationList.add(new MyLCRelation(cl.get(cl.size() - 1), (MCParticle) p));

            }

            // Neutral hadrons
            else if (absPDGID != MuID) {
                // within acceptance

                // double thing = (1 - 1 / ( 1 + Math.exp( (E-clusterParm.getHADOnset())*clusterParm.getHADSharpness() ) ));
                // if (rand.nextDouble() > thing)
                // {
                // continue;
                // }
                if (E < clusterParm.getHADOnset()) {
                    continue;
                }
                if (Math.abs(cosTheta) > clusterParm.getPolarHADOuter()) {
                    continue;
                }

                cl.add(new ReconHADCluster(clusterParm, rand, p, hist));
                lcrelationList.add(new MyLCRelation(cl.get(cl.size() - 1), (MCParticle) p));
            }
        }
        double neg_energy_total = 0.;
        double pos_energy_weight_total = 0.;
        for (Cluster rcl : cl) {
            if (Math.abs(((ReconCluster) rcl).getMCParticle().getCharge()) > Double.MIN_VALUE)
                continue;
            neg_energy_total += ((ReconCluster) rcl).getNegEnergy();
            pos_energy_weight_total += ((ReconCluster) rcl).getNegEnergy() < 0. ? 0. : Math.min(((ReconCluster) rcl).getSigma(), ((ReconCluster) rcl).getEnergy());
        }
        MCFast.log.info(" MCFast neg_energy_total= " + neg_energy_total + " pos_energy_weight_total= " + pos_energy_weight_total);
        if (neg_energy_total < -Double.MIN_VALUE)
            for (Cluster rcl : cl)
                if (Math.abs(((ReconCluster) rcl).getMCParticle().getCharge()) < Double.MIN_VALUE && ((ReconCluster) rcl).getNegEnergy() >= 0.)
                    ((ReconCluster) rcl).adjustEnergy(neg_energy_total, pos_energy_weight_total);
        event.put("Clusters", cl, Cluster.class, 0);
        event.put("ClustersToMCP", lcrelationList, LCRelation.class, 0);
    }

    public void conditionsChanged(ConditionsEvent event) {
        ConditionsSet conditions = getConditionsManager().getConditions("ClusterParameters");
        clusterParm = new ClusterResolutionTables(conditions);
    }
}
