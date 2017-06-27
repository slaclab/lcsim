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
import org.lcsim.event.TrackerHit;

/**
 *
 * @author tonyj
 * @version $Id: TrackerHitConverter.java,v 1.2 2005/08/04 08:04:29 ngraf Exp $
 */
class TrackerHitConverter implements HepRepCollectionConverter
{
   public boolean canHandle(Class k)
   {
      return TrackerHit.class.isAssignableFrom(k);
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
      
      for (TrackerHit hit : (List<TrackerHit>) collection)
      {
         double[] pos = hit.getPosition();
         HepRepInstance instanceX = factory.createHepRepInstance(instanceTree, typeX);
         instanceX.addAttValue("dEdx",hit.getdEdx());
         instanceX.addAttValue("time",hit.getTime());
         HepRepPoint pp = factory.createHepRepPoint(instanceX,pos[0],pos[1],pos[2]);
      }
   }
}
