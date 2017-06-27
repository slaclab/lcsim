package org.lcsim.mc.fast.util;

import hep.physics.vec.Hep3Vector;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.event.MCParticle;
import org.lcsim.util.Driver;
import org.lcsim.event.EventHeader;
import static org.lcsim.mc.fast.util.MCParticleClassifier.MCPClass;

/**
 * CreateFinalStateMCParticleList writes a list of Final state MCParticles to event. The choices are GeneratorFS(Gen) and SimulatorFS(Sim). The final state particles are those at the end of the
 * decay/interaction chain for the choice(Gen or Sim). By default no particles created without destroying the parent are included. This can be overridden by the setKeepContinuousXXX methods. Decays
 * and interactions beyond a radius or z position can be ignored by using the setRadiusCut and setZCut methods. The collection name of the final state particles can also be set.
 * @author Ron Cassell
 */
public class CreateFinalStateMCParticleList extends Driver {
    private MCPClass fs;
    private String FStype;
    private String CollectionName;
    private double Rcut;
    private double Zcut;
    private boolean keepce;
    private boolean keepcp;
    private boolean keepch;
    private final static String[] types = { "Gen", "Sim" };
    private int itype;
    private MCParticleClassifier cl;

    /**
     * Set up the defaults for creating the lists. Type parameter must be Gen or Sim.
     *
     * @param type
     */
    public CreateFinalStateMCParticleList(String type) {
        fs = MCPClass.GEN_FINAL_STATE;
        cl = new MCParticleClassifier();
        FStype = type;
        if (FStype.compareTo(types[0]) == 0) {
            itype = 0;
        } else if (FStype.compareTo(types[1]) == 0) {
            itype = 1;
        } else {
            itype = 0;
            System.out.println("CreateFinalStateMCParticleList created with invalid type " + FStype + ": Defaulting to " + types[0]);
            FStype = types[0];
        }
        Rcut = 99999.;
        Zcut = 99999.;
        keepce = false;
        keepcp = false;
        keepch = false;
        CollectionName = FStype + "FinalStateParticles";
    }

    /**
     * Set the output collection name
     *
     * @param name
     */
    public void setCollectionName(String name) {
        CollectionName = name;
    }

    /**
     * Set a radius cut beyond which interactions and decays are ignored.
     *
     * @param rc
     */
    public void setRadiusCut(double rc) {
        Rcut = rc;
    }

    /**
     * Set a z cut beyond which interactions and decays are ignored.
     *
     * @param zc
     */
    public void setZCut(double zc) {
        Zcut = zc;
    }

    /**
     * Keep electrons created without destroying parent. (deltas, comptons)
     */
    public void setKeepContinuousElectrons() {
        keepce = true;
    }

    /**
     * Keep photons created without destroying parent. (brem, nuclear elastic and pseudo-elastic)
     */
    public void setKeepContinuousPhotons() {
        keepcp = true;
    }

    /**
     * Keep hadrons created without destroying parent. (nuclear elastic and pseudo-elastic)
     */
    public void setKeepContinuousHadrons() {
        keepch = true;
    }

    /**
     * Process and event. Create the final state particle list and store it in event.
     *
     * @param event
     */
    public void process(EventHeader event) {
        //
        // Create a list to hold the final state particles
        //
        List<MCParticle> fslist = new ArrayList<MCParticle>();
        //
        // Get the list of all MCParticles
        //
        List<MCParticle> all = (List<MCParticle>) event.get("MCParticle");
        //
        // If Generator final state particles requested, use the MCParticleClassifier
        // to identify the final state particles, and add them to the list
        //
        if (itype == 0) {
            boolean simulated = false;
            for (MCParticle p : all) {
                if (p.getSimulatorStatus().isDecayedInTracker() || p.getSimulatorStatus().isDecayedInCalorimeter() || p.getSimulatorStatus().hasLeftDetector() || p.getSimulatorStatus().isStopped()) {
                    simulated = true;
                    continue;
                }
            }
            if (!simulated) {
                for (MCParticle p : all) {
                    if (p.getGeneratorStatus() == MCParticle.FINAL_STATE)
                        fslist.add(p);
                }
            } else {
                for (MCParticle p : all) {
                    if (cl.getClassification(p) == fs) {
                        fslist.add(p);
                    }
                }
            }
        }
        //
        // Simulator final state particles requested
        //
        else {
            //
            // Check if we are keeping any continuous process created particles
            //
            boolean keepsome = keepce || keepcp || keepch;
            //
            // Loop over all MCParticles
            //
            for (MCParticle p : all) {
                //
                // Never keep backscatter particles
                //
                if (p.getSimulatorStatus().isBackscatter())
                    continue;
                //
                // Don't keep particles the Simulator never saw
                //
                boolean inSim = p.getSimulatorStatus().isDecayedInTracker() || p.getSimulatorStatus().isDecayedInCalorimeter() || p.getSimulatorStatus().hasLeftDetector() || p.getSimulatorStatus().isStopped();
                if (!inSim)
                    continue;
                //
                // Find out if the particle has endpoint daughters
                //
                boolean hasepd = false;
                for (MCParticle d : p.getDaughters()) {
                    if (d.getSimulatorStatus().isBackscatter())
                        continue;
                    if (d.getSimulatorStatus().vertexIsNotEndpointOfParent())
                        continue;
                    hasepd = true;
                    break;
                }
                //
                // If the particle has endpoint daughters it is not final state
                //
                if (hasepd)
                    continue;
                //
                // This is a simulator final state particle. If it is a result of a
                // continuous process, check to see if we keep it.
                //
                MCParticle pp = getFirstContinuousParticle(p);
                if (pp != null) {
                    if (keepsome) {
                        if (keepThis(p, pp))
                            fslist.add(p);
                    }
                }
                //
                // Add the final state particle to the list
                //
                else {
                    fslist.add(p);
                }
            }
        }
        //
        // We now have the complete list of final state particles. Check for
        // production vertex cuts
        //
        if ((Rcut < 9999.) || (Zcut < 9999.)) {
            //
            // Create a remove list and an add list
            //
            List<MCParticle> removelist = new ArrayList<MCParticle>();
            List<MCParticle> addtolist = new ArrayList<MCParticle>();
            //
            // Loop over the final state list
            //
            for (MCParticle p : fslist) {
                //
                // Check vertex against cuts
                //
                Hep3Vector vtx = p.getOrigin();
                if ((Math.abs(vtx.z()) > Zcut) || (Math.sqrt(vtx.x() * vtx.x() + vtx.y() * vtx.y()) > Rcut)) {
                    //
                    // Vertex failed cuts. Remove it.
                    //
                    removelist.add(p);
                    //
                    // Unless a continuous process particle, add the parent to the list of particles to add back in
                    //
                    if (!p.getSimulatorStatus().vertexIsNotEndpointOfParent()) {
                        if (!addtolist.contains(p.getParents().get(0)))
                            addtolist.add(p.getParents().get(0));
                    }
                }
            }
            //
            // Remove all the particles in the remove list from the final state list
            //
            for (MCParticle p : removelist) {
                fslist.remove(p);
            }
            //
            // Loop over the add list
            //
            for (MCParticle p : addtolist) {
                Hep3Vector vtx = p.getOrigin();
                MCParticle pp = p;
                boolean hasparent = true;
                //
                // Trace parentage until cut is passed (or run out of parents or break chain with continuous particle)
                //
                while ((hasparent) && ((Math.abs(vtx.z()) > Zcut) || (Math.sqrt(vtx.x() * vtx.x() + vtx.y() * vtx.y()) > Rcut))) {
                    if (pp.getSimulatorStatus().vertexIsNotEndpointOfParent()) {
                        pp = null;
                    } else {
                        pp = pp.getParents().get(0);
                    }
                    if (pp == null) {
                        hasparent = false;
                    } else {
                        vtx = pp.getOrigin();
                    }
                }
                //
                // Add the particle to the fs list if it exists
                //
                if (!(pp == null)) {
                    if (!fslist.contains(pp))
                        fslist.add(pp);
                }
            }
        }
        //
        // Write the collection to event
        //
        event.put(CollectionName, fslist);
        event.getMetaData(fslist).setSubset(true);
    }

    /**
     * Decide if a particle resulting from continuous production should be kept as a final state particle.
     *
     * @param p - particle in question
     * @param pp - first continuous production particle in parentage chain of p
     */
    public boolean keepThis(MCParticle p, MCParticle pp) {
        boolean keepit = false;
        //
        // if first vneop particle is the particle in question, only need to check it for validity
        //
        if (p == pp) {
            if (Math.abs(p.getPDGID()) == 11) {
                if (keepce)
                    keepit = true;
            } else if (p.getPDGID() == 22) {
                if (keepcp)
                    keepit = true;
            } else {
                if (keepch)
                    keepit = true;
            }
            return keepit;
        }
        //
        // otherwise, check the whole chain for validity
        //
        else {
            //
            // First check the particle in question
            //
            if (p.getSimulatorStatus().vertexIsNotEndpointOfParent()) {
                if (Math.abs(p.getPDGID()) == 11) {
                    if (keepce)
                        keepit = true;
                } else if (p.getPDGID() == 22) {
                    if (keepcp)
                        keepit = true;
                } else {
                    if (keepch)
                        keepit = true;
                }
                if (!keepit)
                    return keepit;
            }
            //
            // Particle itself is valid, check the chain
            //
            keepit = true;
            MCParticle ppp = p;
            while ((keepit) && (ppp != pp)) {
                ppp = ppp.getParents().get(0);
                if (ppp.getSimulatorStatus().vertexIsNotEndpointOfParent()) {
                    keepit = false;
                    if (Math.abs(ppp.getPDGID()) == 11) {
                        if (keepce)
                            keepit = true;
                    } else if (ppp.getPDGID() == 22) {
                        if (keepcp)
                            keepit = true;
                    } else {
                        if (keepch)
                            keepit = true;
                    }
                }
            }
            return keepit;
        }
    }

    /**
     * Find and return the top VNEOP particle in parentage chain
     *
     * @param p - particle in question
     */
    public MCParticle getFirstContinuousParticle(MCParticle p) {
        MCParticle rp = null;
        if (p.getSimulatorStatus().vertexIsNotEndpointOfParent())
            rp = p;
        MCParticle pp = p;
        while (pp.getParents().size() == 1) {
            pp = pp.getParents().get(0);
            if (pp.getSimulatorStatus().vertexIsNotEndpointOfParent())
                rp = pp;
        }
        return rp;
    }
}
