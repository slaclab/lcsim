package org.lcsim.event;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;
import java.util.List;

/**
 * Represents a fully reconstructed particle.
 * @author tonyj
 * @version $Id: ReconstructedParticle.java,v 1.3 2007/09/18 03:46:27 tonyj Exp $
 */
public interface ReconstructedParticle
{
   /** Type of reconstructed particle.
    *  Check/set collection parameters ReconstructedParticleTypeNames and
    *  ReconstructedParticleTypeValues.
    */
   public int getType();
   
   /** The magnitude of the reconstructed particle's momentum
    */
   public Hep3Vector getMomentum();
   
   /** Energy of the  reconstructed particle
    */
   public double getEnergy();
   
   /** Covariance matrix of the reconstructed particle's 4vector (10 parameters).
    *  Stored as lower triangle matrix of the four momentum (px,py,pz,E), i.e.
    *  cov(px,px), cov(py,px), cov( py,py ) , ....
    */
   public double[] getCovMatrix();
   
   /** Mass of the  reconstructed particle, set independently from four vector quantities
    */
   public double getMass();
   
   /** Charge of the reconstructed particle.
    */
   public double getCharge();
   
   /** Reference point of the reconstructedParticle parameters.
    */
   public Hep3Vector getReferencePoint();
   
   /** The particle Id's sorted by their likelihood.
    * @see ParticleID
    */
   public List<ParticleID> getParticleIDs();
   
   /** The particle Id used for the kinematics of this particle.
    * @see ParticleID
    */
   public ParticleID getParticleIDUsed();
   
   /** The overall goodness of the PID on a scale of [0;1].
    */
   public double getGoodnessOfPID();
   
   /** The reconstructed particles that have been combined to this particle.
    */
   public List<ReconstructedParticle> getParticles();
   
   /** The clusters that have been used for this particle.
    */
   public List<Cluster> getClusters();
   
   /** The tracks that have been used for this particle.
    */
   public List<Track> getTracks();
   
   /**Add a ParticleID object.
    * @see ParticleID
    */
   public void addParticleID(ParticleID pid);
   
   /**Add a particle that has been used to create this particle.
    */
   public void addParticle(ReconstructedParticle particle);
   
   /**Add a cluster that has been used to create this particle.
    */
   public void addCluster(Cluster cluster);
   
   /**Add a track that has been used to create this particle.
    */
   public void addTrack(Track track);
   
   /**Returns this particles momentum and energy as a four vector
    */
   HepLorentzVector asFourVector();
   
   /**
    * Returns the start vertex, or <code>null</code> if none is known
    */
   public Vertex getStartVertex();
}

