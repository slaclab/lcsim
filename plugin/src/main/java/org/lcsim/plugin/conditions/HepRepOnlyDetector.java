package org.lcsim.plugin.conditions;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepTypeTree;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.util.List;
import org.lcsim.geometry.FieldMap;
import org.lcsim.util.loop.DummyDetector;

/**
 * A "dummy" detector which is only able to provide HepRep's for WIRED for
 * when no other geometry is available.
 * @author tonyj
 */
class HepRepOnlyDetector extends DummyDetector
{
   private HepRep detector;
   private FieldMap field;
   /** Creates a new instance of HeprepOnlyDetector */
   HepRepOnlyDetector(String name, HepRep heprep, double field)
   {
      super(name);
      this.detector = heprep;
      this.field = new SolenoidalField(field);
   }
   
   public void appendHepRep(HepRepFactory factory, HepRep heprep)
   {
      List<HepRepTypeTree> types = detector.getTypeTreeList();
      for (HepRepTypeTree gTypeTree : types) heprep.addTypeTree(gTypeTree);
      
      List<HepRepInstanceTree> instances = detector.getInstanceTreeList();
      for (HepRepInstanceTree gInstanceTree : instances) heprep.addInstanceTree(gInstanceTree);
      
      List<String> layers = detector.getLayerOrder();
      for (String layer : layers ) heprep.addLayer(layer);
   }
   
   public FieldMap getFieldMap()
   {
      return field;
   }
   
   private class SolenoidalField implements FieldMap
   {
      private double[] field;
      SolenoidalField(double zField)
      {
         field = new double[]{0,0,zField};
      }
      public void getField(double[] position, double[] b)
      {
         for (int i=0;i<3;i++) b[i] = field[1];
      }
      
      public double[] getField(double[] position)
      {
         return field;
      }
      
      public Hep3Vector getField(Hep3Vector position, BasicHep3Vector field)
      {
         if (field == null) field = new BasicHep3Vector();
         getField(position.v(),field.v());
         return field;
      }
      
      public Hep3Vector getField(Hep3Vector position)
      {
         return getField(position,null);
      }
   }
}
