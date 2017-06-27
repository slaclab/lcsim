package org.lcsim.util.xml;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * An interface that must be implemented by all element factories.
 * 
 * By providing their own implementation of ElementFactory users can cause custom classes to be
 * created by the reader.
 * @author tonyj
 * @version $Id: ElementFactory.java,v 1.1 2005/07/15 02:54:58 jeremy Exp $
 */
public interface ElementFactory
{
   <T> T createElement(Class<T> c, Element node, String type) throws JDOMException, ElementCreationException;
   public static class ElementCreationException extends Exception
   {
      ElementCreationException(String message)
      {
         super(message);
      }
      ElementCreationException(String message, Throwable cause)
      {
         super(message,cause);
      }
   }
}