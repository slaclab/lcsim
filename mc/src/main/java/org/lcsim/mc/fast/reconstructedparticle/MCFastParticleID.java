package org.lcsim.mc.fast.reconstructedparticle;

import hep.physics.particle.properties.ParticleType;
import org.lcsim.event.ParticleID;

/**
 * An implementation of ParticleID appropriate for the fast Monte Carlo
 * @author ngraf
 */
public class MCFastParticleID implements ParticleID {
    private ParticleType _type;

    /** Creates a new instance of MCFastParticleID */
    public MCFastParticleID(ParticleType type) {
        _type = type;
    }

    /**
     * Type - defined to be the pdgId.
     */
    public int getType() {
        return _type.getPDGID();
    }

    /**
     * The PDG code of this id - UnknownPDG ( 999999 ) if unknown.
     */
    public int getPDG() {
        return _type.getPDGID();
    }

    /**
     * The likelihood of this hypothesis - in a user defined normalization.
     */
    public double getLikelihood() {
        return 1.;
    }

    /**
     * Type of the algorithm/module that created this hypothesis. Check/set collection parameters PIDAlgorithmTypeName and PIDAlgorithmTypeID.
     */
    public int getAlgorithmType() {
        return 0;
    }

    /**
     * Parameters associated with this hypothesis. Check/set collection paramter PIDParameterNames for decoding the indices.
     */
    public double[] getParameters() {
        return new double[1];
    }
}
