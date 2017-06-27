package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author tonyj
 */
public class Solenoid extends Field
{  
   /** Creates a new instance of Solenoid */
   public Solenoid(String name)
   {
      super("solenoid", name);
      setAttribute("outer_radius", "world_side");
   }
   
   public void setInnerField(double field)
   {
      setAttribute("inner_field",String.valueOf(field));
   }
   
   public void setOuterField(double field)
   {
      setAttribute("outer_field",String.valueOf(field));
   }
   
   public void setInnerRadius(double radius)
   {
      setAttribute("inner_radius",String.valueOf(radius));
   }
   
   public void setOuterRadius(double radius)
   {
      setAttribute("outer_radius",String.valueOf(radius));
   }
   
   public void setZMax(double z)
   {
      setAttribute("zmax",String.valueOf(z));
      setAttribute("zmin",String.valueOf(-z));
   }
}