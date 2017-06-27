package org.lcsim.event.util;

import hep.physics.jet.FixNumberOfJetsFinder.NumJetsNotFoundException;
import hep.physics.jet.JadeEJetFinder;
import hep.physics.jet.JetFinder;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.event.EventHeader;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.Vertex;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

/**
 * A simple driver which can be used to find jets from ReconstructedParticles.
 * The resuslting jets are stored in a new collection of ReconstructedParticles.
 * @author tonyj
 * @version $Id: JetDriver.java,v 1.4 2011/05/30 18:57:39 cassell Exp $
 */
public class JetDriver extends Driver
{
   private static final Hep3Vector IP = new BasicHep3Vector(0,0,0);
   private static final String defaultOutputCollectionName  = "Jets";
   private String inputCollectionName;
   private String outputCollectionName = defaultOutputCollectionName;
   private JetFinder finder = defaultJetFinder();
   private boolean continueOnError = false;
   
   /** Creates a new instance of JetFinder with the default properties */
   public JetDriver()
   {
   }
   
   public String getInputCollectionName()
   {
      return inputCollectionName;
   }
   
   /**
    * The name of the input collection to use. If not set (or set to <code>null</code> uses
    * the first collection of ReconstructedParticles found in the event.
    */
   
   public void setInputCollectionName(String inputCollectionName)
   {
      this.inputCollectionName = inputCollectionName;
   }
   
   public String getOutputCollectionName()
   {
      return outputCollectionName;
   }
   
   /**
    * The name of the output collection added to the event. If not set, or set to <code>null</code>,
    * default to "Jets"
    */
   public void setOutputCollectionName(String outputCollectionName)
   {
      this.outputCollectionName = outputCollectionName == null ? defaultOutputCollectionName : outputCollectionName;
   }
   
   public JetFinder getFinder()
   {
      return finder;
   }
   
   /**
    * Set the jet finding algorithm to use
    */
   public void setFinder(JetFinder finder)
   {
      this.finder = finder == null ? defaultJetFinder() : finder;
   }

   public boolean isContinueOnError()
   {
       return continueOnError;
   }

   public void setContinueOnError(boolean continueOnError)
   {
       this.continueOnError = continueOnError;
   }
   
   protected JetFinder defaultJetFinder()
   {
      return new JadeEJetFinder(0.005);
   }
   protected void process(EventHeader event)
   {
      super.process(event);
      
      boolean hist = getHistogramLevel() > 0;
      
      // Find the input reconstructed Particles
      List<ReconstructedParticle> input;
      if (inputCollectionName ==null)
      {
         List<List<ReconstructedParticle>> listOfLists = event.get(ReconstructedParticle.class);
         if (listOfLists.isEmpty()) return;
         input = listOfLists.get(0);
      }
      else
      {
         input = event.get(ReconstructedParticle.class,inputCollectionName);
      }
      // Build a list of 4-vectors from the reconstructed particles
      Map<HepLorentzVector, ReconstructedParticle> map = new HashMap<HepLorentzVector, ReconstructedParticle>();
      for (ReconstructedParticle p : input)
      {
         map.put(p.asFourVector(),p);
      }
      
      // Pass the data to the Jet finder
      int nJets = 0;
      try {
        finder.setEvent(map.keySet());
        nJets = finder.njets();
      } catch (NumJetsNotFoundException x) {
          if (continueOnError) return;
          else throw x;
      }

      
      if (hist)
      {
         AIDA aida = AIDA.defaultInstance();
         aida.cloud1D("JetDriver/nJets").fill(finder.njets());
         for (int i=0; i<finder.njets(); i++) aida.cloud1D("JetDriver/particlesPerJet").fill(finder.nParticlesPerJet(i));
      }
      
      // Loop over the output jets and create a new ReconstructedParticle for each one, pointing back to
      // the original particles
      
      List<ReconstructedParticle> output = new ArrayList<ReconstructedParticle>();
      for (int i=0; i<nJets; i++)
      {
         Jet jet = new Jet(finder.jet(i));
         for (HepLorentzVector pj : (List<HepLorentzVector>) finder.particlesInJet(i))
         {
            jet.addParticle(map.get(pj));
         }
         output.add(jet);
      }
      
      // Add the list of jets to the event
      
      event.put(outputCollectionName, output, ReconstructedParticle.class, 0);
   }
   private class Jet implements ReconstructedParticle
   {
      private HepLorentzVector fourVector;
      private List<ReconstructedParticle> particles = new ArrayList<ReconstructedParticle>();
      private double charge;
      
      Jet(HepLorentzVector fourVector)
      {
         this.fourVector = fourVector;
      }
      
      public void addTrack(org.lcsim.event.Track track)
      {
         throw new UnsupportedOperationException("Add track to jet");
      }
      
      public void addParticleID(org.lcsim.event.ParticleID pid)
      {
         throw new UnsupportedOperationException("Add track to jet");
      }
      
      public void addParticle(ReconstructedParticle particle)
      {
         particles.add(particle);
         charge += particle.getCharge();
      }
      
      public int getType()
      {
         // Fixme: what should we return for Jet?
         return 0;
      }
      
      public List<org.lcsim.event.Track> getTracks()
      {
         return Collections.EMPTY_LIST;
      }
      
      public hep.physics.vec.Hep3Vector getReferencePoint()
      {
         return IP;
      }
      
      public List<ReconstructedParticle> getParticles()
      {
         return particles;
      }
      
      public List<org.lcsim.event.ParticleID> getParticleIDs()
      {
         return Collections.EMPTY_LIST;
      }
      
      public void addCluster(org.lcsim.event.Cluster cluster)
      {
         throw new UnsupportedOperationException("Add track to jet");
      }
      
      public HepLorentzVector asFourVector()
      {
         return fourVector;
      }
      
      public double getCharge()
      {
         return charge;
      }
      
      public List<org.lcsim.event.Cluster> getClusters()
      {
         return Collections.EMPTY_LIST;
      }
      
      public double[] getCovMatrix()
      {
         // Fixme: Does it make sense to return anything here?
         return null;
      }
      
      public double getEnergy()
      {
         return fourVector.t();
      }
      
      public double getGoodnessOfPID()
      {
         return 0;
      }
      
      public double getMass()
      {
         return fourVector.magnitude();
      }
      
      public hep.physics.vec.Hep3Vector getMomentum()
      {
         return fourVector.v3();
      }
      
      public org.lcsim.event.ParticleID getParticleIDUsed()
      {
         return null;
      }
      public Vertex getStartVertex()
      {
         return null;
      }
   }
}
