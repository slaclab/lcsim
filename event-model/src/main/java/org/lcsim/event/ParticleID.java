package org.lcsim.event;

/**
 *  Used by ReconstructedParticle and Cluster
 *  for different hypotheses on the particle type.
 *
 * @author gaede
 * @version $Id: ParticleID.java,v 1.2 2005/11/08 17:03:48 tonyj Exp $
 * @see ReconstructedParticle#getParticleIDs()
 */
public interface ParticleID
{
   /** Type - userdefined.
    */
   public int getType();
   
   /** The PDG code of this id - UnknownPDG ( 999999 ) if unknown.
    */
   public int getPDG();
   
   /**The likelihood  of this hypothesis - in a user defined normalization.
    */
   public double getLikelihood();
   
   /** Type of the algorithm/module that created this hypothesis.
    * Check/set collection parameters PIDAlgorithmTypeName and PIDAlgorithmTypeID.
    */
   public int getAlgorithmType();
   
   /** Parameters associated with this hypothesis.
    * Check/set collection paramter PIDParameterNames for decoding the indices.
    */
   public double[] getParameters();
   
   /** Constant to be used if the PDG code is not known or undefined.
    */
   public final static int UnknownPDG = 999999;
}

