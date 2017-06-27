package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepPoint;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.physics.vec.Hep3Vector;
import java.awt.Color;
import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 *
 * @author tonyj
 * @version $Id: Hep3VectorConverter.java,v 1.1 2006/01/26 00:00:47 tonyj Exp $
 */
class Hep3VectorConverter implements HepRepCollectionConverter
{
   public boolean canHandle(Class k)
   {
      return Hep3Vector.class.isAssignableFrom(k);
   }
   public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree)
   {
      LCMetaData meta = event.getMetaData(collection);
      String name = meta.getName();
      int flags = meta.getFlags();
      
      HepRepType typeX = factory.createHepRepType(typeTree, name);
      typeX.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER);
      typeX.addAttValue("drawAs","Point");
      typeX.addAttValue("color",Color.YELLOW);
      typeX.addAttValue("fill",true);
      typeX.addAttValue("fillColor",Color.RED);
      typeX.addAttValue("MarkName","Box");      
      
      for (Hep3Vector hit : (List<Hep3Vector>) collection)
      {
         HepRepInstance instanceX = factory.createHepRepInstance(instanceTree, typeX);
         HepRepPoint pp = factory.createHepRepPoint(instanceX,hit.x(),hit.y(),hit.z());
      }
   }
}
