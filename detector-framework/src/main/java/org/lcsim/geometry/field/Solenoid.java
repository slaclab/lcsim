package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Solenoidal magnetic field.
 * @author tonyj
 */

public class Solenoid extends AbstractFieldMap
{
   private double[] innerField;
   private double[] outerField;
   private double zmax;
   private double outerRadiusSquared;

   Solenoid(Element node) throws JDOMException
   {
      super(node);  

      double i  = node.getAttribute("inner_field").getDoubleValue();
      innerField = new double[] { 0, 0, i};
      double o = node.getAttribute("outer_field").getDoubleValue();
      outerField = new double[] { 0, 0, o};
      zmax = node.getAttribute("zmax").getDoubleValue();
      double r = node.getAttribute("outer_radius").getDoubleValue();
      outerRadiusSquared = r*r;
   }

   void getField(double x, double y, double z, BasicHep3Vector field)
   {
      if (Math.abs(z)>zmax) field.setV(0,0,0);
      else
      {
         double r2 = x*x + y*y;
         double bz = (r2 > outerRadiusSquared) ? outerField[2] : innerField[2];
         field.setV(0,0,bz);
      }
   }

   public double[] getInnerField()
   {
       return innerField;
   }

   public double[] getOuterField()
   {
       return outerField;
   }

   public double getZMax()
   {
       return zmax;
   }

   public double getOuterRadius2()
   {
       return outerRadiusSquared;
   }
}