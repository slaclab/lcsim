package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;

import java.io.IOException;
import org.lcsim.event.ParticleID;

/**
 *
 * @author Tony Johnson
 * @version $Id: SIOParticleID.java,v 1.3 2013/02/01 00:25:54 jeremy Exp $
 */
class SIOParticleID implements ParticleID
{
   SIOParticleID(SIOInputStream in, int flags, int versionr) throws IOException
   {
      likelihood = in.readFloat();
      type = in.readInt();
      pdg = in.readInt();
      algorithmType =in.readInt() ;
      int n = in.readInt();
      parameters = new double[n];
      for (int i=0; i<n; i++) parameters[i] = in.readFloat();
   }

   static void write(ParticleID id, SIOOutputStream out, int flags) throws IOException
   {
         out.writeFloat((float) id.getLikelihood());
         out.writeInt(id.getType());
         out.writeInt(id.getPDG());
         out.writeInt(id.getAlgorithmType());
         double[] pars = id.getParameters();
         int n = pars == null ? 0 : pars.length;
         out.writeInt(n);
         for (int i=0; i<n; i++) out.writeFloat((float)pars[i]);

   }

   public int getAlgorithmType()
   {
      return algorithmType;
   }

   public double getLikelihood()
   {
      return likelihood;
   }

   public int getPDG()
   {
      return pdg;
   }

   public double[] getParameters()
   {
      return parameters;
   }

   public int getType()
   {
      return type;
   }
   private int algorithmType;
   private double likelihood;
   private int pdg;
   private double[] parameters;
   private int type;

}