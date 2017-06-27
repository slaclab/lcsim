package org.lcsim.event.base;

/**
 *
 * @author cassell
 */
import org.lcsim.event.ParticleID;

public class CheatParticleID implements ParticleID
{
    int pdg;
    /** Creates a new instance of CheatParticleID */
    public CheatParticleID(int id)
    {
        pdg = id;
    }
   /** Type - userdefined.
    */
   public int getType()
   {
        return 0;
   }
   
   /** The PDG code of this id - UnknownPDG ( 999999 ) if unknown.
    */
   public int getPDG()
   {
       return pdg;
   }
   
   /**The likelihood  of this hypothesis - in a user defined normalization.
    */
   public double getLikelihood()
   {
       return 0.;
   }
   
   /** Type of the algorithm/module that created this hypothesis.
    * Check/set collection parameters PIDAlgorithmTypeName and PIDAlgorithmTypeID.
    */
   public int getAlgorithmType()
   {
       return 0;
   }
   
   /** Parameters associated with this hypothesis.
    * Check/set collection paramter PIDParameterNames for decoding the indices.
    */
   public double[] getParameters()
   {
       double[] result = new double[1];
       result[0] = 0.;
       return result;
   }
    
}
