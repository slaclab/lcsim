package org.lcsim.util.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.jdom.JDOMException;


/**
 * The default implementation of ElementFactory.
 * @author tonyj
 */
public class DefaultElementFactory implements ElementFactory
{
   private List<Class> classes = new ArrayList<Class>();
   private Map<Class,String> packageMap = new HashMap<Class,String>();
   
   /**
    * Create the default element factory.
    */
   public DefaultElementFactory()
   {}
   
   public <T> T createElement(Class<T> c, Element node, String type) throws JDOMException, ElementCreationException
   {
      if (type != null)
      {
         Class<T> cand = getElementClass(c,type);
         if (cand != null) return create(cand,node);
      }
      for (Class cand  : classes)
      {
         if (c.isAssignableFrom(cand)) return create((Class<? extends T>) cand,node);
      }
      throw new ElementCreationException("Unknown element "+c);
   }
   
   public void register(Class elementClass,String packageName)
   {
      packageMap.put(elementClass,packageName);
   }
   /**
    * Register a class with the factory. Future calls to create any class which is a subclass 
    * of this class will cause a new instance of this class to be created. The class specificed
    * must have a constructor which takes a jdom Element as its argument.
    * @param elementClass The class to register.
    */
   public void register(Class elementClass)
   {
      classes.add(0,elementClass);
   }
   private <T> T create(Class<T> type, Element node) throws ElementCreationException
   {
      try
      {
         Constructor<T> c = type.getDeclaredConstructor(Element.class);
         c.setAccessible(true);
         return c.newInstance(node);
      }
      catch (NoSuchMethodException x)
      {
         throw new ElementCreationException("Could not create element: "+type,x);
      }
      catch (InvocationTargetException x)
      {
         throw new ElementCreationException("Could not create element: "+type,x.getCause());
      }
      catch (InstantiationException x)
      {
         throw new ElementCreationException("Could not create element: "+type,x.getCause());
      }
      catch (IllegalAccessException x)
      {
         throw new ElementCreationException("Could not create element: "+type,x);
      }
   }
   public <T> Class<T> getElementClass(Class<T> type, String name) throws ElementCreationException
   {
      String packageName = packageMap.get(type);
      if (packageName == null) return null;
      String key = packageName+"."+name;
      try
      {
         Class result = Class.forName(key);
         if (!type.isAssignableFrom(result)) throw new ElementCreationException("Element "+key+" is of wrong type");
         return (Class<T>) result;
      }
      catch (ClassNotFoundException x)
      {
         return null;
      }
   }
}