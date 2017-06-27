package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class Header extends Element
{
   /** Creates a new instance of Header */
   public Header()
   {
      super("header");
      
      addContent(new Detector());
      addContent(new Generator());
      addContent(new Author());
   }
   public void setComment(String comment)
   {
      addContent(new Element("comment").addContent(comment));
   }
   public Detector getDetector()
   {
      return (Detector) getChild("detector");
   }
   public Generator getGenerator()
   {
      return (Generator) getChild("generator");
   }
   public Author getAuthor()
   {
      return (Author) getChild("author");
   }
}