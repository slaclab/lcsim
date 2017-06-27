package org.lcsim.event;
import java.util.List;

/** 
 * The LCIO cluster interface.
 */

public interface Cluster
{
    /** Flagword that defines the type of cluster. Bits 0-15 can be used to denote the subdetectors
     *  that have contributed hits to the cluster. The definition of the bits has to be done 
     *  elsewhere, e.g. in the run header. Bits 16-31 are used internally.
     */
    public int getType();
    
    public int getParticleId();

    /** Energy of the cluster.
     */
    public double getEnergy();
    
    /**
     * Energy error of the cluster.
     */
    public double getEnergyError();

    /** Position of the cluster.
     */
    public double[] getPosition();

    /** Covariance matrix of the position (6 Parameters)
    */
    public double[] getPositionError();

    /** Intrinsic direction of cluster at position: Theta.
     * Not to be confused with direction cluster is seen from IP.
     */
    public double getITheta();

    /** Intrinsic direction of cluster at position: Phi.
     * Not to be confused with direction cluster is seen from IP.
     */
    public double getIPhi();

    /** Number of hits in cluster.  Hits on subclusters should be
     * included, and hits belonging to more than one subclusters
     * should be counted only once.
     */
    public int getSize();

    /** Covariance matrix of the direction (3 Parameters)
     */
    public double[] getDirectionError();

    /** Shape parameters (6 Parameters) - TO DO: definition
     */
    public double[] getShape();

    /** The clusters that have been combined to this cluster.
     */
    public List<Cluster> getClusters();

    /** The hits that have been combined to this cluster.
     *  Only available if CalorimeterHit objects have been saved with
     *  LCIO::RCHBIT_PTR==1.
     *  @see CalorimeterHit
     */
    public List<CalorimeterHit> getCalorimeterHits();

    /** Returns the energy contribution of the hits 
     *  Runs parallel to the CalorimeterHitVec from getCalorimeterHits()
     */
    public double[] getHitContributions();

    /** A vector that holds the energy observed in a particular subdetectors.
     *  The mapping of indices to subdetectors is implementation dependent.
     *  To be used as convenient information or if hits are not stored in 
     *  the data set, e.g. DST or FastMC.
     */
    public double[] getSubdetectorEnergies();
} 

