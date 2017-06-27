package org.lcsim.mc.fast.reconstructedparticle;

import org.lcsim.event.Vertex;
import org.lcsim.mc.fast.MCFast;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;
import hep.physics.vec.VecOp;
import hep.physics.particle.Particle;
import hep.physics.particle.properties.ParticleType;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.event.Cluster;
import org.lcsim.event.ParticleID;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.Track;
import org.lcsim.mc.fast.tracking.ReconTrack;

import static java.lang.Math.sqrt;
import static java.lang.Math.pow;
import static java.lang.Math.abs;

/**
 *
 * @author ngraf
 */
public class MCFastReconstructedParticle implements ReconstructedParticle {
    // ReconstructedParticle attributes
    private double[] _covMatrix = new double[10];
    private double _mass;
    private double _charge;
    private double e_track;
    private double e_reco;
    private Hep3Vector _referencePoint;
    private Hep3Vector p3_track;
    private List<ParticleID> _particleIds = new ArrayList<ParticleID>();
    private ParticleID _particleIdUsed;
    private double _goodnessOfPid;
    private List<ReconstructedParticle> _particles = new ArrayList<ReconstructedParticle>();
    private List<Cluster> _clusters = new ArrayList<Cluster>();
    private List<Track> _tracks = new ArrayList<Track>();
    private BasicHepLorentzVector p_reco = new BasicHepLorentzVector();
    private BasicHepLorentzVector p_track = new BasicHepLorentzVector();

    public MCFastReconstructedParticle(Track t, ParticleType type, Particle p, Cluster assoc_c, double wtcal, boolean refPoint000) {
        MCFast.log.info(" PDGID= " + type.getPDGID() + " t.getPX,...= " + t.getPX() + " " + t.getPY() + " " + t.getPZ());
        _mass = type.getMass();
        addTrack(t);
        _charge = t.getCharge();
        // Use (0,0,0) for reference point if refPoint000=true
        // Use true point of origin for reference point if refPoint000=false
        if (refPoint000) {
            _referencePoint = new BasicHep3Vector(0, 0, 0);
        } else {
            _referencePoint = p.getOrigin();
        }
        e_track = sqrt(((ReconTrack) t).getDocaMomentumVec(_referencePoint).magnitudeSquared() + _mass * _mass);
        p_track.setV3(e_track, ((ReconTrack) t).getDocaMomentumVec(_referencePoint));
        p3_track = p_track.v3();
        if (assoc_c != null) {
            addCluster(assoc_c);
            MCFast.log.info(" PDGID= " + type.getPDGID() + " e_track= " + e_track + " e_assoc_clus= " + assoc_c.getEnergy());
            MCFast.log.info(" PDGID= " + type.getPDGID() + " _referencePoint= " + _referencePoint.x() + " " + _referencePoint.y() + " " + _referencePoint.z());
            MCFast.log.info(" PDGID= " + type.getPDGID() + " p3_track= " + p3_track.x() + " " + p3_track.y() + " " + p3_track.z());
        } else {
            MCFast.log.info(" assoc_c = null PDGID= " + type.getPDGID());
        }
        if (assoc_c != null && wtcal > 0. && abs(type.getPDGID()) != 11) {
            e_reco = (1. - wtcal) * e_track + wtcal * assoc_c.getEnergy();
            if (e_reco < _mass)
                e_reco = _mass;
            p_reco.setV3(e_reco, VecOp.mult(sqrt(e_reco * e_reco - _mass * _mass), VecOp.unit(p3_track)));
        } else {
            e_reco = e_track;
            p_reco.setV3(e_reco, p3_track);
        }

        addParticleID(new MCFastParticleID(type));
    }

    public MCFastReconstructedParticle(Cluster c, ParticleType type, Particle p) {
        _mass = type.getMass();
        addCluster(c);
        double e = c.getEnergy();
        if (e < _mass) {
            if (e > _mass - 1.e-5 && e > Double.MIN_VALUE) {
                _mass = e - Double.MIN_VALUE;
            } else {
                MCFast.log.warning(" MCFastReconstructedParticle  e < _mass  e= " + e + " _mass= " + _mass);
                MCFast.log.warning(" MCFastReconstructedParticle  type = " + type);
                MCFast.log.warning(" MCFastReconstructedParticle  program will continue, but problem should be fixed ");
                _mass = e - Double.MIN_VALUE;
                // System.exit(0);
            }

        }

        double pm = sqrt(e * e - _mass * _mass);
        // get direction from position of cluster and assume it comes from the origin
        double[] point = c.getPosition();
        double len = sqrt(point[0] * point[0] + point[1] * point[1] + point[2] * point[2]);

        _referencePoint = new BasicHep3Vector(0, 0, 0);

        double px = (pm / len) * (point[0]);
        double py = (pm / len) * (point[1]);
        double pz = (pm / len) * (point[2]);
        p_reco.setV3(e, px, py, pz);
        _charge = 0.;

        addParticleID(new MCFastParticleID(type));
    }

    public MCFastReconstructedParticle(double[] vxd, double[] mom, double mass, double charge, ParticleType type) {
        _mass = mass;
        _charge = charge;
        _referencePoint = new BasicHep3Vector(vxd);
        p3_track = new BasicHep3Vector(mom);
        e_reco = sqrt(pow(_mass, 2) + p3_track.magnitudeSquared());
        p_reco.setV3(e_reco, p3_track);
        MCFast.log.info(" PDGID= " + type.getPDGID() + " e_reco= " + e_reco + " mass= " + _mass);
        MCFast.log.info(" PDGID= " + type.getPDGID() + " _referencePoint= " + _referencePoint.x() + " " + _referencePoint.y() + " " + _referencePoint.z());
        MCFast.log.info(" PDGID= " + type.getPDGID() + " p3_track= " + p3_track.x() + " " + p3_track.y() + " " + p3_track.z());

        addParticleID(new MCFastParticleID(type));
    }

    // ReconstructedParticle interface

    public int getType() {
        return _particleIdUsed.getType();
    }

    public Hep3Vector getMomentum() {
        return p_reco.v3();
    }

    public double getEnergy() {
        return p_reco.t();
    }

    public double[] getCovMatrix() {
        return _covMatrix;
    }

    public double getMass() {
        return _mass;
    }

    public double getCharge() {
        return _charge;
    }

    public Hep3Vector getReferencePoint() {
        return _referencePoint;
    }

    public List<ParticleID> getParticleIDs() {
        return _particleIds;
    }

    public ParticleID getParticleIDUsed() {
        return _particleIdUsed;
    }

    public double getGoodnessOfPID() {
        return _goodnessOfPid;
    }

    public List<ReconstructedParticle> getParticles() {
        return _particles;
    }

    public List<Cluster> getClusters() {
        return _clusters;
    }

    public List<Track> getTracks() {
        return _tracks;
    }

    public void addParticleID(ParticleID pid) {
        _particleIds.add(pid);
        _particleIdUsed = pid;
    }

    public void addParticle(ReconstructedParticle particle) {
        _particles.add(particle);
    }

    public void addCluster(Cluster cluster) {
        _clusters.add(cluster);
    }

    public void addTrack(Track track) {
        _tracks.add(track);
    }

    public HepLorentzVector asFourVector() {
        return p_reco;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("MCFastReconstructedParticle: \n");
        sb.append("E: " + getEnergy());
        return sb.toString();
    }

    public Vertex getStartVertex() {
        return null;
    }
}
