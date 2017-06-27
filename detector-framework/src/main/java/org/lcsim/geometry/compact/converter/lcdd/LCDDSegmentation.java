package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.lcsim.geometry.compact.Segmentation;
import org.lcsim.geometry.compact.converter.lcdd.util.Calorimeter;

/**
 *
 * @author tonyj
 */
abstract class LCDDSegmentation extends Segmentation
{
   LCDDSegmentation(Element node)
   {
      super(node);
   }
   abstract void setSegmentation(Calorimeter cal);
}
