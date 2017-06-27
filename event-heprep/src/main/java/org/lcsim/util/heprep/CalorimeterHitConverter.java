package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepPoint;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;

import java.awt.Color;
import java.util.List;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.SimCalorimeterHit;

/**
 *
 * @author tonyj
 * @version $Id: CalorimeterHitConverter.java,v 1.10 2010/05/05 20:36:35 ngraf Exp $
 */
class CalorimeterHitConverter implements HepRepCollectionConverter
{
   private ColorMap hitColorMap = new RainbowColorMap();
   
   public boolean canHandle(Class k)
   {
      return CalorimeterHit.class.isAssignableFrom(k);
   }
   public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree)
   {
      LCMetaData meta = event.getMetaData(collection);
      String name = meta.getName();
      int flags = meta.getFlags();
      
      HepRepType typeX = factory.createHepRepType(typeTree, name);
      typeX.addAttValue("layer",LCSimHepRepConverter.HITS_LAYER);
      typeX.addAttValue("drawAs","Point");
      typeX.addAttValue("color",Color.RED);
      typeX.addAttValue("fill",true);
      typeX.addAttValue("fillColor",Color.RED);
      typeX.addAttValue("MarkName","Box");      
      typeX.addAttDef("energy", "Hit Energy", "physics", "");
      
      double maxEnergy = 0;
      double minEnergy = Double.MAX_VALUE;
      if (hitColorMap != null)
      {
         for (CalorimeterHit hit : (List<CalorimeterHit>) collection)
         {
             double e = 0.;
             if(hit instanceof SimCalorimeterHit)
             {
                 e = hit.getRawEnergy();
             }
             else
             {
                 e = hit.getCorrectedEnergy();
             }

            if (Double.isNaN(e) || Double.isInfinite(e) || e <= 0) continue;
            if (e > maxEnergy) maxEnergy = e;
            if (e < minEnergy) minEnergy = e;
         }
      }
      if (minEnergy == maxEnergy) maxEnergy = minEnergy + 1;
      
      //boolean hasPos = LCIOUtil.bitTest(flags,LCIOConstants.CHBIT_LONG);
      //CalorimeterIDDecoder decoder = hasPos ? null : (CalorimeterIDDecoder) meta.getIDDecoder();
      for (CalorimeterHit hit : (List<CalorimeterHit>) collection)
      {
          double e = 0.;
          if(hit instanceof SimCalorimeterHit)
          {
            e = hit.getRawEnergy();
          }
          else
          {
              e = hit.getCorrectedEnergy();
          }
         if (Double.isNaN(e) || Double.isInfinite(e) || e <= 0) continue;
         //if (!hasPos) decoder.setID(hit.getCellID());
         //double[] pos = hasPos ?  hit.getPosition() : decoder.getPosition();
         double pos[] = null;
         try {
        	 pos = hit.getPosition();
         }
         catch (Exception x)
         {}
         
         if (pos != null)
         {
        	 HepRepInstance instanceX = factory.createHepRepInstance(instanceTree, typeX);
        	 instanceX.addAttValue("energy",e);
        	 if (hitColorMap != null)
        	 {
        		 double v = (Math.log(e)-Math.log(minEnergy))/(Math.log(maxEnergy)-Math.log(minEnergy));
        		 instanceX.addAttValue("color",hitColorMap.getColor(v,1.0f));
        	 }
        	 HepRepPoint pp = factory.createHepRepPoint(instanceX,pos[0],pos[1],pos[2]);
         }
      }
   }
}
