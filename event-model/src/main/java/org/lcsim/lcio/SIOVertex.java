package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;
import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.Vertex;
import org.lcsim.event.base.BaseVertex;

/**
 *
 * @author Tony Johnson
 * @version $Id: SIOVertex.java,v 1.4 2010/05/21 10:55:24 grefe Exp $
 */
class SIOVertex extends BaseVertex
{
   private SIORef recPRef;
   private Map<String,Double> parameters;
   
   SIOVertex(SIOInputStream in, int flag, int version, String[] keys, String[] parameterNames) throws IOException
   {
      _isPrimary = in.readInt() != 0;
      _type = keys[in.readInt()];
      _chi2 = in.readFloat();
      _probability = in.readFloat();
      _position = new BasicHep3Vector(in.readFloat(),in.readFloat(),in.readFloat());
      
      double[] covMatrix = new double[6];
      for (int i=0; i<covMatrix.length; i++) covMatrix[i] = in.readFloat();
      _covarianceMatrix = new SymmetricMatrix(3,covMatrix,true);
      int n = in.readInt();
      this.parameters = new HashMap<String,Double>(n);
      for (int i=0; i<n; i++) 
      {
         Float f = in.readFloat();
         if (!f.isNaN() && parameterNames != null) parameters.put(parameterNames[i],Double.valueOf(f));
      }
      recPRef = in.readPntr();
      _aParticle = null;      
      in.readPTag(this);
   }
   static void write(Vertex v, SIOOutputStream out, int flag,  List<String> keys, List<String> parameterNames) throws IOException
   {
      out.writeInt(v.isPrimary()?1:0);
      out.writeInt(keys.indexOf(v.getAlgorithmType()));
      
      out.writeFloat((float) v.getChi2());
      out.writeFloat((float) v.getProbability());
      Hep3Vector vec = v.getPosition();
      out.writeFloat((float) vec.x());
      out.writeFloat((float) vec.y());
      out.writeFloat((float) vec.z());
      double[] covMatrix = v.getCovMatrix().asPackedArray(true);
      for (int i=0; i<6; i++) out.writeFloat((float) covMatrix[i]);
      Map<String,Double> pars = v.getParameters();
      out.writeInt(parameterNames.size());
      for (String name : parameterNames) 
      {
         Double d = pars.get(name);
         out.writeFloat(d==null ? Float.NaN : d.floatValue());
      }
      out.writePntr(v.getAssociatedParticle());
      out.writePTag(v);
   }
   
   public Map<String, Double> getParameters()
   {
      return parameters;
   }
   
   public ReconstructedParticle getAssociatedParticle()
   {
      if (_aParticle == null && recPRef != null)
      {
         _aParticle = (ReconstructedParticle) recPRef.getObject();
         recPRef = null;
      }
      return super.getAssociatedParticle();
   }
}
