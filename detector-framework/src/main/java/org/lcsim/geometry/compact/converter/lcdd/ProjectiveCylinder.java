package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.geometry.compact.converter.lcdd.util.Calorimeter;


/**
 *
 * @author tonyj
 */
public class ProjectiveCylinder extends LCDDSegmentation
{
   private int thetaBins;
   private int phiBins;
   
   ProjectiveCylinder(Element node) throws DataConversionException
   {
      super(node);
      thetaBins = node.getAttribute("thetaBins").getIntValue();
      phiBins = node.getAttribute("phiBins").getIntValue();
   }
   void setSegmentation(Calorimeter cal)
   {
      org.lcsim.geometry.compact.converter.lcdd.util.ProjectiveCylinder cyl = new org.lcsim.geometry.compact.converter.lcdd.util.ProjectiveCylinder();
      cyl.setNTheta(thetaBins);
      cyl.setNPhi(phiBins);
      cal.setSegmentation(cyl);
   }
}