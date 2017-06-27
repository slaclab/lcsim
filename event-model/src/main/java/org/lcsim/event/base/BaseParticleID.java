/*
 * BaseParticleID.java
 *
 * Created on March 24, 2006, 1:21 PM
 *
 * $Id: BaseParticleID.java,v 1.1 2006/03/25 00:37:03 ngraf Exp $
 */

package org.lcsim.event.base;

import hep.physics.particle.properties.ParticleType;
import org.lcsim.event.*;

/**
 *
 * @author Norman Graf
 */
public class BaseParticleID implements ParticleID
{
    protected ParticleType _type;
    protected double _likelihood;
    protected int _algorithmType;
    protected double[] _algorithmParameters;
    
    /** Creates a new instance of BaseParticleID */
    public BaseParticleID()
    {
    }
    
    /**
     * Fully qualified constructor
     * @param type the ParticleType for this ID
     */
    public BaseParticleID(ParticleType type)
    {
        _type = type;
    }
    
    // include these setters since I don't know what the final inheriting classes will
    // look like.
    
    /**
     * Set the particle type. Note that this overrides the previous type.
     * @param type The ParticleType of this ID.
     */
    public void setType(ParticleType type)
    {
        _type = type;
    }
    
    /**
     * Set the likelihood for this type ID
     * @param p The likelihood for this identification.
     */
    public void setLikelihood(double p)
    {
        _likelihood = p;
    }
    
    /**
     * Set the algorithm type used to make this identification.
     * @param alg The algorithm type. Not yet defined.
     */
    //TODO define what this is!
    public void setAlgorithmType(int alg)
    {
        _algorithmType = alg;
    }
    
    /**
     * Set the parameters used in the algorithm.
     * @param pars an array of parameters appropriate for the algorithm typ.e Not yet defined.
     */
    // TODO define what these are!
    public void setParameters(double[] pars)
    {
        _algorithmParameters = pars;
    }
    
    
    // ParticleID interface
    
    /**
     * return the algorithm type.
     * @return currently returns 0. not yet defined.
     */
    // TODO define what this is!
    public int getType()
    {
        return 0;
    }
    
    /**
     * The PDG code of this id - UnknownPDG ( 999999 ) if unknown.
     * @return the Particle Data Group id for this particle type.
     */
    public int getPDG()
    {
        if(_type==null)
        {
            return UnknownPDG;
        }
        else
        {
            return _type.getPDGID();
        }
    }
    
    /**
     * The likelihood  of this hypothesis - in a user defined normalization.
     * @return The likelihood for this particle identification
     */
    public double getLikelihood()
    {
        return _likelihood;
    }
    
    /**
     * Type of the algorithm/module that created this hypothesis.
     * Check/set collection parameters PIDAlgorithmTypeName and PIDAlgorithmTypeID.
     * @return the algorithm type.
     */
    public int getAlgorithmType()
    {
        return _algorithmType;
    }
    
    /**
     * Parameters associated with this hypothesis.
     * Check/set collection parameter PIDParameterNames for decoding the indices.
     * @return the list of parameters for the algorithm used in identifying this particle.
     */
    public double[] getParameters()
    {
        return _algorithmParameters;
    }
}
