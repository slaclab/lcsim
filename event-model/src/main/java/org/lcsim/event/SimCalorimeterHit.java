package org.lcsim.event;

/**
 * Represents one simulated hit in a calorimeter.  It extends the 
 * {@link org.lcsim.event.CalorimeterHit} interface and adds 
 * methods for accessing MC data.
 * 
 * @author Jeremy McCormick
 * @author Tony Johnson
 * @version $Id: SimCalorimeterHit.java,v 1.7 2011/08/24 18:51:17 jeremy Exp $ 
 */
public interface SimCalorimeterHit extends CalorimeterHit
{
    /**
     * Get the number of MC contributions to the hit.
     * The name of this method is misleading if granular contributions
     * were selected, in which case it corresponds to the total number
     * of MC contributions to this hit.  It can be used to find the 
     * maximum index number for iteration over contributions using the methods 
     * {@link #getContributedEnergy(int)}, {@link #getContributedTime(int), 
     * {@link #getMCParticle(int)}, and {@linke #getStepPosition(int)}.
     * @return The number of MCParticle contributions.
     */
    int getMCParticleCount();

    /**
     * Get the MCParticle that caused the shower responsible for this
     * contribution to the hit.
     * @return The MCParticle of the hit contribution.
     */
    MCParticle getMCParticle(int index);

    /**
     * Get the energy in GeV of the i-th contribution to the hit.
     * @return The energy of a contribution.
     */
    double getContributedEnergy(int index);

    /**
     * Get the time in ns of the i-th contribution to the hit.
     * @return The time of the contribution.
     */
    double getContributedTime(int index);

    /**
     * Get the PDG code of the shower particle that caused this
     * contribution.  May be different from the MCParticle's PDG code.
     * @return The shower contribution particle's PDG ID.
     */
    int getPDG(int index);

    /**
     * Get the step position of an MCParticle contribution in Cartesian coordinates.     
     * @param index The index of the contribution.
     * @return The step position in Cartesian coordinates as a float array of length 3.
     */
    float[] getStepPosition(int index);
}