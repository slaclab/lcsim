package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.physics.vec.Hep3Vector;
import java.awt.Color;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.geometry.Detector;
import org.lcsim.util.swim.HelixSwimmer;


/**
 * @author tonyj
 * @version $Id: ReconstructedParticleConverter.java,v 1.5 2007/08/24 20:09:46 jeremy Exp $
 */
class ReconstructedParticleConverter implements HepRepCollectionConverter
{
   private static final double[] IP = { 0,0,0 };
   private Color[] colors;
   private double zField;
   private HelixSwimmer helix;
   private double trackingRMax;
   private double trackingZMax;
   
   ReconstructedParticleConverter()
   {
      ColorMap cm = new RainbowColorMap();
      colors = new Color[10];
      for (int i=0; i<colors.length; i++) colors[i] = cm.getColor(((double) i)/colors.length,1);
   }
   public boolean canHandle(Class k)
   {
      return ReconstructedParticle.class.isAssignableFrom(k);
   }
   public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree)
   {
      LCMetaData meta = event.getMetaData(collection);
      String name = meta.getName();
      int flags = meta.getFlags();
      
      try {
    	  event.getDetector();
      }
      catch (Exception x)
      {
    	  return;
      }
      
      Detector detector = event.getDetector();
      
      trackingRMax = detector.getConstants().get("tracking_region_radius").getValue();
      trackingZMax = detector.getConstants().get("tracking_region_zmax").getValue();
      
      zField = detector.getFieldMap().getField(IP)[2];
      helix = new HelixSwimmer(zField);
      
      HepRepType typeX = factory.createHepRepType(typeTree, name);
      typeX.addAttValue("layer",LCSimHepRepConverter.PARTICLES_LAYER);
      typeX.addAttValue("drawAs","Line");
      typeX.addAttValue("width",2);
      typeX.addAttDef("momentum","Particle Momentum", "physics", "");
      typeX.addAttDef("type","Particle Type", "physics", "");
      
      boolean jets = name.toLowerCase().contains("jets");
      if (jets)
      {
         HepRepType jetTypeX = factory.createHepRepType(typeX, name+"Particles");
         jetTypeX.addAttValue("layer",LCSimHepRepConverter.PARTICLES_LAYER);
         jetTypeX.addAttValue("drawAs","Line");
         jetTypeX.addAttDef("momentum","Particle Momentum", "physics", "");
         jetTypeX.addAttDef("type","Particle Type", "physics", "");
         
         int i = 0;
         for (ReconstructedParticle jet : (List<ReconstructedParticle>) collection)
         {
            Color jetColor = colors[i%colors.length];
            i += 3;
            
            HepRepInstance instanceX = factory.createHepRepInstance(instanceTree, typeX);
            instanceX.addAttValue("color",jetColor);
            drawParticle(factory, jet,instanceX, 0);
            
            for (ReconstructedParticle p : jet.getParticles())
            {
               HepRepInstance jetParticleX = factory.createHepRepInstance(instanceX, jetTypeX);
               jetParticleX.addAttValue("color",jetColor);
               drawParticle(factory, p, jetParticleX, p.getCharge());
            }
         }
      }
      else
      {
         HepRepType neutralType = factory.createHepRepType(typeX, "Neutral");
         neutralType.addAttValue("color",Color.GREEN);
         
         HepRepType chargedType = factory.createHepRepType(typeX, "Charged");
         chargedType.addAttValue("color",Color.ORANGE);
         
         HepRepInstance charged = factory.createHepRepInstance(instanceTree, typeX);
         HepRepInstance neutral = factory.createHepRepInstance(instanceTree, typeX);
         
         for (ReconstructedParticle p : (List<ReconstructedParticle>) collection)
         {
            double charge = p.getCharge();
            HepRepInstance instanceX = factory.createHepRepInstance(charge == 0 ? neutral : charged, charge == 0 ? neutralType : chargedType);
            drawParticle(factory, p, instanceX, charge);
         }
      }
      
      
   }
   private void drawParticle(HepRepFactory factory, ReconstructedParticle p, HepRepInstance instanceX, double charge)
   {
      Hep3Vector start =  p.getReferencePoint();
      Hep3Vector momentum = p.getMomentum();
      helix.setTrack(momentum, start, (int) charge);
      double distanceToCylinder = helix.getDistanceToCylinder(trackingRMax,trackingZMax);
      
      if (charge == 0 || zField == 0)
      {
         Hep3Vector stop = helix.getPointAtDistance(distanceToCylinder);
         
         factory.createHepRepPoint(instanceX,start.x(),start.y(),start.z());
         factory.createHepRepPoint(instanceX,stop.x(),stop.y(),stop.z());
         instanceX.addAttValue("momentum",p.getEnergy());
         instanceX.addAttValue("type",p.getType());
      }
      else
      {
         double dAlpha = 10; // 1cm
         
         instanceX.addAttValue("momentum",p.getEnergy());
         instanceX.addAttValue("type",p.getType());
         factory.createHepRepPoint(instanceX,start.x(),start.y(),start.z());
         
         for (int k = 1;k<200;k++)
         {
            double d = k*dAlpha;
            if (d>distanceToCylinder) break;
            Hep3Vector point = helix.getPointAtDistance(d);
            factory.createHepRepPoint(instanceX,point.x(),point.y(),point.z());
         }
      }
   }
}