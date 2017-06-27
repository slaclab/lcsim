package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class Solids extends Element
{
   public Solids()
   {
      super("solids");
   }
   public void addSolid(Solid solid)
   {
      addContent(solid);
   }
}
