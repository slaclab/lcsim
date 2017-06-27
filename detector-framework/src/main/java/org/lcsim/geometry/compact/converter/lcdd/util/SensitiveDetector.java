package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 * 
 * @author tonyj
 */
public class SensitiveDetector extends RefElement
{
   public SensitiveDetector(String type, String name)
   {
      super(type,name);
      setAttribute("ecut","0.0");
      setAttribute("eunit","MeV");
      setAttribute("verbose","0");
   }  
   public void setIDSpec(IDSpec spec)
   {
      Element element = new Element("idspecref");
      element.setAttribute("ref",spec.getRefName());
      addContent(element);
   }
   public void setHitsCollection(String name)
   {
      setAttribute("hits_collection",name);
   }
}