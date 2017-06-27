package org.lcsim.event;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;
import java.util.Map;

/**
 * A representation of a vertex
 * @author tonyj
 */
public interface Vertex
{
    /** Checks if the Vertex is the primary vertex of the event.
     *  Only one primary vertex per event is allowed
     */
    boolean isPrimary();

    /** Type code for the algorithm that has been used to create the vertex - check/set the 
     *  collection parameters AlgorithmName and  AlgorithmType.
     */
    String getAlgorithmType();

     /** Chi squared of the vertex fit.
     */
    double getChi2();

    /** Probability of the vertex fit.
     */
    double getProbability();

    /** Position of the vertex
     */
    Hep3Vector getPosition();

    /** Covariance matrix of the position 
     */
    SymmetricMatrix getCovMatrix();

    /** Additional parameters related to this vertex.
     */
    Map<String,Double> getParameters();

    /** Returns Reconstructed Particle associated to the Vertex
     */
    ReconstructedParticle getAssociatedParticle(); 
}
