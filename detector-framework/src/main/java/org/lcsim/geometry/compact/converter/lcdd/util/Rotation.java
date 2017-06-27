package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 *
 * The LCDD rotation element.
 *
 * @author tonyj
 * @author jeremym
 */
public class Rotation extends RefElement
{   
   /** 
    * Creates an identity rotation called <code>name</code>.
    * @param name The unique name of this rotation. 
    */
   public Rotation(String name)
   {
      super("rotation",name);
      setAttribute("x","0.0");
      setAttribute("y","0.0");
      setAttribute("z","0.0");
      setAttribute("unit","radian");
   }
   
   /** 
    * Creates an identity rotation called <code>name</code> with given
    * rotations about the X, Y, and Z axes.
    * @param name The unique name of this rotation. 
    */
   public Rotation(String name, double rx, double ry, double rz)
   {
      super("rotation",name);
      setAttribute("x",Double.toString(rx));
      setAttribute("y",Double.toString(ry));
      setAttribute("z",Double.toString(rz));
      setAttribute("unit","radian");
   } 
   
   /**
    * Create a named rotation called <code>name</code> with XML element <code>element</code>.
    * @throws IllegalArgumentException If element is not called "rotation".
    * @throws RuntimeException If a {@link org.jdom.DataConversionException} is caught when converting.
    * @param name The unique name of this rotation.
    * @param element The XML element to be converted.  It must be called "rotation".
    */
   public Rotation(String name, Element element)
   {
       super("rotation", name);
       
       if (!element.getName().equals("rotation"))
           throw new IllegalArgumentException("expect rotation element but got " + element.getAttributeValue("name"));

       double x,y,z;
       x = y = z = 0.;
       
       try {      
          
           if (element.getAttribute("x") != null)
           {
               x = element.getAttribute("x").getDoubleValue();
           }
           
           if (element.getAttribute("y") != null)
           {
               y = element.getAttribute("y").getDoubleValue();
           }
           
           if (element.getAttribute("z") != null)
           {
               z = element.getAttribute("z").getDoubleValue();
           }
       }
       catch (DataConversionException except)
       {
           throw new RuntimeException(except);
       }
           
       setX(x);
       setY(y);
       setZ(z);
   }
   
   public void setX(double d)
   {
      setAttribute("x",String.valueOf(d));
   }
   public void setY(double d)
   {
      setAttribute("y",String.valueOf(d));
   }    
   public void setZ(double d)
   {
      setAttribute("z",String.valueOf(d));
   }
}
