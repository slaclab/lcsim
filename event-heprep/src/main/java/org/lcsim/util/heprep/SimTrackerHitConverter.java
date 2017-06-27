package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepPoint;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.graphics.heprep.HepRepInstanceTree;
import java.awt.Color;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;

/**
 *
 * @author tonyj
 * @version $Id: SimTrackerHitConverter.java,v 1.3 2006/04/05 11:54:14 tonyj Exp $
 */
class SimTrackerHitConverter implements HepRepCollectionConverter
{
   public boolean canHandle(Class k)
   {
      return SimTrackerHit.class.isAssignableFrom(k);
   }
   public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree)
   {
      LCMetaData meta = event.getMetaData(collection);
      String name = meta.getName();
      
      HepRepType typeX = factory.createHepRepType(typeTree, name);
      typeX.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER);
      typeX.addAttValue("drawAs","Point");
      typeX.addAttValue("color",Color.GREEN);
      typeX.addAttValue("fill",true);
      typeX.addAttValue("fillColor",Color.GREEN);
      typeX.addAttValue("MarkName","Box");
      typeX.addAttDef("dEdx", "Hit dEdx", "physics", "");
      typeX.addAttDef("time", "Hit time", "physics", "");
      typeX.addAttDef("mcEnergy", "MC Particle Energy", "physics", "");
      
      for (SimTrackerHit hit : (List<SimTrackerHit>) collection)
      {
         double[] pos = hit.getPoint();
         HepRepInstance instanceX = factory.createHepRepInstance(instanceTree, typeX);
         instanceX.addAttValue("dEdx",hit.getdEdx());
         instanceX.addAttValue("time",hit.getTime());
         MCParticle particle = hit.getMCParticle();
         if (particle != null) instanceX.addAttValue("mcEnergy",particle.getEnergy());
         HepRepPoint pp = factory.createHepRepPoint(instanceX,pos[0],pos[1],pos[2]);
      }
   }
}
