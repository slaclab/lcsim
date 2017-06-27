package org.lcsim.event.base;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.Cluster;
import org.lcsim.event.ParticleID;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.Track;
import org.lcsim.event.Vertex;

/**
 * Default implementation of ReconstructedParticle
 * @author Norman Graf
 * @version $Id: BaseReconstructedParticle.java,v 1.12 2011/04/14 05:02:26 grefe Exp $
 * Change setParticleIdUsed to check that ParticleID is in the ParticleID list,
 * and if not add it.
 *                    Ron Cassell
 */
public class BaseReconstructedParticle implements ReconstructedParticle
{
    protected int _type;
    protected HepLorentzVector _fourVec = new BasicHepLorentzVector(0.,0.,0.,0.);
    protected double[] _covMatrix = new double[10];
    protected double _mass;
    protected double _charge;
    protected Hep3Vector _referencePoint = new BasicHep3Vector(0.,0.,0.);
    protected List<ParticleID> _particleIds = new ArrayList<ParticleID>();
    protected ParticleID _particleIdUsed = new UnknownParticleID();
    protected double _goodnessOfPid;
    protected List<ReconstructedParticle> _particles = new ArrayList<ReconstructedParticle>();
    protected List<Cluster> _clusters = new ArrayList<Cluster>();
    protected List<Track> _tracks = new ArrayList<Track>();
    protected Vertex _vertex;
    
    /** Creates a new instance of BaseReconstructedParticle */
    public BaseReconstructedParticle()
    {
    }
    
    public BaseReconstructedParticle(double mass) {
    	_mass = mass;
    }
    
    public BaseReconstructedParticle(HepLorentzVector v) {
        _fourVec = v;
    }
    
    public BaseReconstructedParticle(double mass, HepLorentzVector v) {
        _fourVec = v;
    	_mass = mass;
    }
    
    public BaseReconstructedParticle(double E, Hep3Vector v) {
        _fourVec = new BasicHepLorentzVector(E,v);
    }
    
    public BaseReconstructedParticle(double E, double px, double py, double pz) {
        _fourVec = new BasicHepLorentzVector(E, px, py, pz);
    }
    
    public void set4Vector(HepLorentzVector v)
    {
        _fourVec = v;
    }
    
    public void setMass(double m)
    {
        _mass = m;
    }
    
    public void setCharge(double c)
    {
        _charge = c;
    }
    
    public void setType(int t)
    {
        _type = t;
    }
    
    public void setReferencePoint(Hep3Vector v)
    {
        _referencePoint = v;
    }
    
    public void setReferencePoint(double x, double y, double z)
    {
        _referencePoint = new BasicHep3Vector(x, y, z);
    }
    
    public void setParticleIdUsed(ParticleID id)
    {
        if(!_particleIds.contains(id))_particleIds.add(id);
        _particleIdUsed = id;
    }
    
    public void setGoodnessOfPid(double g)
    {
        _goodnessOfPid = g;
    }
    
// ReconstructedParticle interface
    
    /**
     * Type of reconstructed particle.
     *  Check/set collection parameters ReconstructedParticleTypeNames and
     *  ReconstructedParticleTypeValues.
     * @return the type
     */
    public int getType()
    {
        return _type;
    }
    
    /**
     * The magnitude of the reconstructed particle's momentum
     * @return the momentum of this particle.
     */
    public Hep3Vector getMomentum()
    {
        return _fourVec.v3();
    }
    
    /**
     * Energy of the  reconstructed particle
     * @return the energy of this particle in GeV
     */
    public double getEnergy()
    {
        return _fourVec.t();
    }
    
    /**
     * Covariance matrix of the reconstructed particle's 4vector (10 parameters).
     *  Stored as lower triangle matrix of the four momentum (px,py,pz,E), i.e.
     *  cov(px,px), cov(py,px), cov( py,py ) , ....
     * @return covariance matrix as a packed array
     */
    public double[] getCovMatrix()
    {
        return _covMatrix;
    }
    
    /**
     * Mass of the  reconstructed particle, set independently from four vector quantities
     * @return the mass in GeV
     */
    public double getMass()
    {
        return _mass;
    }
    
    /**
     * Charge of the reconstructed particle.
     * @return the charge in units of the electron charge
     */
    public double getCharge()
    {
        return _charge;
    }
    
    /**
     * Reference point of the reconstructedParticle parameters.
     * @return the reference point for this particle in mm
     */
    public Hep3Vector getReferencePoint()
    {
        return _referencePoint;
    }
    
    /**
     * The particle Id's sorted by their likelihood.
     * @see ParticleID
     * @return a list of particle IDs for this particle, sorted by likelihood
     */
    public List<ParticleID> getParticleIDs()
    {
        return _particleIds;
    }
    
    /**
     * The particle Id used for the kinematics of this particle.
     * @see ParticleID
     * @return the most likely identification for this particle
     */
    public ParticleID getParticleIDUsed()
    {
        return _particleIdUsed;
    }
    
    /**
     * The overall goodness of the PID on a scale of [0;1].
     * @return the "goodness" of this identification. not yet defined.
     */
    public double getGoodnessOfPID()
    {
        return _goodnessOfPid;
    }
    
    /**
     * The reconstructed particles that have been combined to this particle.
     * @return a list of ReconstructedParticles if this is a compund ReconstructedParticle
     */
    public List<ReconstructedParticle> getParticles()
    {
        return _particles;
    }
    
    /**
     * The clusters that have been used for this particle.
     * @return the list of calorimeter clusters contributing to this ReconstructedParticle
     */
    public List<Cluster> getClusters()
    {
        return _clusters;
    }
    
    /**
     * The tracks that have been used for this particle.
     * @return the list of tracks contributing to this ReconstructedParticle
     */
    public List<Track> getTracks()
    {
        return _tracks;
    }
    
    /**
     * Add a ParticleID object.
     * @see ParticleID
     * @param pid The ParticleID to associate with this ReconstructedParticle
     */
    public void addParticleID(ParticleID pid)
    {
        _particleIds.add(pid);
        _particleIdUsed = pid;
    }
    
    /**
     * Adds a particle that has been used to create this particle.
     * @param particle A ReconstructedParticle which contributes to this ReconstructedParticle.
     */
    // TODO make sure kinematics of this particle are also updated
    public void addParticle(ReconstructedParticle particle)
    {
        _particles.add(particle);
    }
    
    /**
     * Adds a cluster that has been used to create this particle.
     * @param cluster A Cluster which contributes to this ReconstructedParticle.
     */
    // TODO make sure kinematics of this particle are also updated
    public void addCluster(Cluster cluster)
    {
        _clusters.add(cluster);
    }
    
    /**
     * Adds a track that has been used to create this particle.
     * @param track A Track which contributes to this ReconstructedParticle.
     */
    // TODO make sure kinematics of this particle are also updated
    public void addTrack(Track track)
    {
        _tracks.add(track);
    }
    
    /**
     * Returns this particle's momentum and energy as a four vector
     * @return The four vector representation of this ReconstructedParticle.
     */
    // TODO fix this so that adding tracks or clusters or ReconstructedParticle
    // either updates the four-vector, or it can be set independently.
    public HepLorentzVector asFourVector()
    {
        return _fourVec;
    }
    
    // TODO finish this...
    public String toString()
    {       
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        String pidUsed = "";
        try {
        	pidUsed = String.valueOf(_particleIdUsed.getPDG());
        } catch (NullPointerException e) {
        	pidUsed = "no particle ID defined";
        }
        StringBuffer sb = new StringBuffer(className+": Type: "+_type+" pdgID: "+pidUsed+" \n");
        sb.append("E: "+getEnergy());
        return sb.toString();
    }

   public Vertex getStartVertex()
   {
      return _vertex;
   }
   public void setStartVertex(Vertex vertex)
   {
      _vertex = vertex;
   }
    
}
