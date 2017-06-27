package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.Hep3Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lcsim.event.Cluster;
import org.lcsim.event.ParticleID;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.Track;
import org.lcsim.event.Vertex;
import org.lcsim.event.base.BaseReconstructedParticle;

/**
 *
 * @author Tony Johnson
 * @version $Id: SIOReconstructedParticle.java,v 1.9 2009/03/12 18:07:42 jeremy Exp $
 */
class SIOReconstructedParticle extends BaseReconstructedParticle
{
   private List<SIORef> tempParticles;
   private List<SIORef> tempTracks;
   private List<SIORef> tempClusters;
   private SIORef tempVertex;
   
   SIOReconstructedParticle(SIOInputStream in, int flags, int version) throws IOException
   {
      _type = in.readInt();
      Hep3Vector momentum = new BasicHep3Vector(in.readFloat(),in.readFloat(),in.readFloat());
      double energy = in.readFloat();
      _fourVec = new BasicHepLorentzVector(energy,momentum);
      _covMatrix = new double[10];
      for (int i=0; i<10; i++) _covMatrix[i] = in.readFloat();
      _mass = in.readFloat();
      _charge = in.readFloat();
      _referencePoint = new BasicHep3Vector(in.readFloat(),in.readFloat(),in.readFloat());
      int nPid = in.readInt();
      _particleIds = new ArrayList(nPid);
      for (int i=0; i<nPid; i++)
      {
         ParticleID id = new SIOParticleID(in,flags,version);
         _particleIds.add(id);
         in.readPTag(id);
      }
      _particleIdUsed = (ParticleID) in.readPntr().getObject();
      _goodnessOfPid = in.readFloat();
      int nReco = in.readInt();
      tempParticles = new ArrayList(nReco);
      _particles = null;
      for (int i=0; i<nReco; i++)
      {
         tempParticles.add(in.readPntr());
      }
      int nTracks = in.readInt();
      tempTracks = new ArrayList(nTracks);
      _tracks = null;
      for (int i=0; i<nTracks; i++)
      {
         tempTracks.add(in.readPntr());
      }
      int nClust = in.readInt();
      tempClusters = new ArrayList(nClust);
      _clusters = null;
      for (int i=0; i<nClust; i++)
      {
         tempClusters.add(in.readPntr());
      }
      if (version > 1007)
      {
         tempVertex = in.readPntr();
         _vertex = null;
      }
      in.readPTag(this);
   }
   
   static void write(ReconstructedParticle particle, SIOOutputStream out, int flags) throws IOException
   {
      out.writeInt(particle.getType());
      double[] mom = particle.getMomentum().v();
      for (int i=0; i<3; i++) out.writeFloat((float) mom[i]);
      out.writeFloat((float) particle.getEnergy());
      double[] matrix = particle.getCovMatrix();
      for (int i=0; i<10; i++) out.writeFloat(matrix == null ? 0f : (float)matrix[i]);
      out.writeFloat((float) particle.getMass());
      out.writeFloat((float)particle.getCharge());
      double[] ref = particle.getReferencePoint().v();
      for (int i=0; i<3; i++) out.writeFloat((float)ref[i]);
      List<ParticleID> ids = particle.getParticleIDs();
      //if (!ids.contains(particle.getParticleIDUsed()))
      //  throw new RuntimeException("The used PID with PDG " + particle.getParticleIDUsed().getPDG()
      //		  + " is not in the list of candidate PIDs.");
      out.writeInt(ids.size());
      for (ParticleID pid : ids )
      {
         SIOParticleID.write(pid,out,flags);
         out.writePTag(pid);
      }
      out.writePntr(particle.getParticleIDUsed());
      out.writeFloat((float)particle.getGoodnessOfPID());
      List<ReconstructedParticle> particles = particle.getParticles();
      out.writeInt(particles.size());
      for (ReconstructedParticle child : particles) out.writePntr(child);
      List<Track> tracks = particle.getTracks();
      out.writeInt(tracks.size());
      for (Track track : tracks) out.writePntr(track);
      List<Cluster> clusters = particle.getClusters();
      out.writeInt(clusters.size());
      for (Cluster cluster : clusters) out.writePntr(cluster);
      out.writePntr(particle.getStartVertex());
      out.writePTag(particle);
   }
   
   public List<Cluster> getClusters()
   {
      if (tempClusters != null && _clusters == null)
      {
         _clusters = new ArrayList<Cluster>(tempClusters.size());
         for (SIORef ref : tempClusters) _clusters.add((Cluster) ref.getObject());
         tempClusters = null;
      }
      return _clusters == null ? Collections.<Cluster>emptyList() : _clusters;
   }
   
   public List<ReconstructedParticle> getParticles()
   {
      if (tempParticles != null && _particles == null)
      {
         _particles = new ArrayList<ReconstructedParticle>(tempParticles.size());
         for (SIORef ref : tempParticles) _particles.add((ReconstructedParticle) ref.getObject());
         tempParticles = null;
      }
      return _particles == null ? Collections.<ReconstructedParticle>emptyList() : _particles;
   }
   
   public List<Track> getTracks()
   {
      if (tempTracks != null && _tracks == null)
      {
         _tracks = new ArrayList<Track>(tempTracks.size());
         for (SIORef ref : tempTracks) _tracks.add((Track) ref.getObject());
         tempTracks = null;
      }
      return _tracks == null ? Collections.<Track>emptyList() : _tracks;
   }

   public Vertex getStartVertex()
   {
      if (_vertex == null && tempVertex != null)
      {
         _vertex = (Vertex) tempVertex.getObject();
         tempVertex = null;
      }
      return super.getStartVertex();
   }
}
