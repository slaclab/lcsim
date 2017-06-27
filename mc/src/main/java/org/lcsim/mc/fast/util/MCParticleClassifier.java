package org.lcsim.mc.fast.util;

import java.util.List;
import org.lcsim.event.MCParticle;

/**
 * MCParticleClassifier has a single method, getClassification(MCParticle), that returns an enum classifying the particle.
 *
 * Possible return values are:
 *
 * GEN_INITIAL -> Generator particle the simulator never saw
 *
 * GEN_PREDECAY -> Generator particle passed to the simulator along with a predecay, and the simulator generated the decay products.
 *
 * GEN_FINAL_STATE -> Generator final state particle: either FinalState in the input file, or intermediate passed to the Simulator with a predecay that never happened.
 *
 * SIM_BACKSCATTER -> Simulator particle produced as a result of backscatter from a non-tracking region.
 *
 * SIM_VERTEX_NOT_PARENT_ENDPOINT -> Simulator particle produced without destroying parent.
 *
 * SIM_INTERACTED_OR_DECAYED -> Simulator particle produced as a result of an interaction or decay in a tracking region.
 * @version $Id: MCParticleClassifier.java,v 1.5 2006/06/28 04:48:33 jstrube Exp $
 */
public class MCParticleClassifier {
    public enum MCPClass {
        GEN_INITIAL, GEN_PREDECAY, GEN_FINAL_STATE, SIM_BACKSCATTER, SIM_VERTEX_NOT_PARENT_ENDPOINT, SIM_INTERACTED_OR_DECAYED
    };

    public static MCPClass getClassification(MCParticle p) {
        //
        // Check if particle existed in Generator
        //
        if (p.getGeneratorStatus() > 0) {
            //
            // Remove the documentation particles in case the generator left them floating
            //
            if (p.getGeneratorStatus() == MCParticle.DOCUMENTATION)
                return MCPClass.GEN_INITIAL;
            //
            // Check if the generator particle has daughters in the generator
            //
            boolean hasGeneratorDaughters = false;
            List<MCParticle> daughters = p.getDaughters();
            for (MCParticle d : daughters) {
                if (d.getGeneratorStatus() > 0)
                    hasGeneratorDaughters = true;
            }
            //
            // If no generator daughters return final state
            //
            if (!hasGeneratorDaughters) {
                //
                // Trap for strange generator problem: Hanging quark with INTERMEDIATE status
                // Therefore must check that Simulator saw this particle, otherwise return
                // GEN_INITIAL
                //
                boolean inSim = p.getSimulatorStatus().isDecayedInTracker() || p.getSimulatorStatus().isDecayedInCalorimeter() || p.getSimulatorStatus().hasLeftDetector() || p.getSimulatorStatus().isStopped();
                if (inSim) {
                    return MCPClass.GEN_FINAL_STATE;
                } else {
                    return MCPClass.GEN_INITIAL;
                }
            }
            //
            // A generator particle with gerenrator daughters:
            // if the simulator saw it must be a predecay
            //
            if (p.getSimulatorStatus().isDecayedInTracker() | p.getSimulatorStatus().isDecayedInCalorimeter()) {
                return MCPClass.GEN_PREDECAY;
            }
            //
            // simulator never saw it: initial
            //
            return MCPClass.GEN_INITIAL;
        } else {
            //
            // Particle didn't exist in generator: choices are backscatter, production without killing parent,
            // or the result of an interaction or decay( killing parent)
            //
            if (p.getSimulatorStatus().isBackscatter()) {
                return MCPClass.SIM_BACKSCATTER;
            }

            if (p.getSimulatorStatus().vertexIsNotEndpointOfParent()) {
                return MCPClass.SIM_VERTEX_NOT_PARENT_ENDPOINT;
            }

            return MCPClass.SIM_INTERACTED_OR_DECAYED;
        }
    }
}
