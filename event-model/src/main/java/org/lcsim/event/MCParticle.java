package org.lcsim.event;

import hep.physics.particle.Particle;
import hep.physics.vec.Hep3Vector;
import java.util.List;

/**
 * A simulated track.
 * 
 * @author Tony Johnson
 * @author Jeremy McCormick
 * @version $Id: MCParticle.java,v 1.7 2011/08/24 18:51:17 jeremy Exp $
 */
public interface MCParticle extends Particle
{
    /**
     * Get the parents of this particle.
     * @return The particle's parents.
     */
    List<MCParticle> getParents();

    /**
     * Get the daughters of this particle.
     * @return The particle daughters.
     */
    List<MCParticle> getDaughters();

    /**
     * If this event has been simulated by Geant4 this method will return the
     * simulation status.
     * @return The particle's simulator status.
     */
    SimulatorStatus getSimulatorStatus();

    /**
     * The endpoint of the simulated track.
     * @throws RuntimeException
     *             if endpoint is not available.
     * @return The particle's end point.
     */
    Hep3Vector getEndPoint();

    /**
     * Get the X, Y, and Z spin components of this particle.
     * @return The particle's spin components.
     */
    float[] getSpin();

    /**
     * Get the color flow of particle.
     * @return The particle's color flow.
     */
    int[] getColorFlow();
    
    /**
     * Get the momentum at the particle's endpoint.
     * @return the momentum at the particle's endpoint
     */
    float[] getMomentumAtEndpoint();

    /**
     * Simulation flags.
     */
    public interface SimulatorStatus
    {
        /**
         * Get the raw undecoded simulator status
         */
        int getValue();

        /**
         * True if the particle has been created by the simulation program
         * (rather than the generator).
         */
        boolean isCreatedInSimulation();

        /**
         * True if the particle was created by the simulator as a result of an
         * interaction or decay in non-tracking region, e.g. a calorimeter. By
         * convention, such particles are not saved. However, if this particle
         * creates a tracker hit, the particle is added to the MCParticle list
         * with this flag set, and the parent set to the particle that initially
         * decayed or interacted in a non-tracking region.
         */
        boolean isBackscatter();

        /**
         * True if the particle was created as a result of a continuous process
         * where the parent particle continues, i.e. hard ionization,
         * Bremsstrahlung, elastic interactions, etc.
         */
        boolean vertexIsNotEndpointOfParent();

        /**
         * True if the particle decayed or interacted in a tracking region.
         */
        boolean isDecayedInTracker();

        /**
         * True if the particle decayed or interacted (non-continuous
         * interaction, particle terminated) in non-tracking region.
         */
        boolean isDecayedInCalorimeter();

        /**
         * True if the particle left the world volume undecayed.
         */
        boolean hasLeftDetector();

        /**
         * True if the particle lost all kinetic energy inside the world volume
         * and did not decay.
         */
        boolean isStopped();
    }
}