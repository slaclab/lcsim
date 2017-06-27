package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import java.util.List;
import org.jdom.Element;
import org.jdom.JDOMException;

public class Dipole extends AbstractFieldMap
{
   private double[] coeffs;
   private double zmin;
   private double zmax;
   private double rSquaredMax;
   
   public Dipole(Element node) throws JDOMException
   {
      super(node);
      
      zmin = node.getAttribute("zmin").getDoubleValue();
      zmax = node.getAttribute("zmax").getDoubleValue();
      double rmax = node.getAttribute("rmax").getDoubleValue();
      rSquaredMax = rmax*rmax;
      
      int ncoeff = node.getChildren("dipoleCoeff").size();
      coeffs = new double[ncoeff];
      
      int i = 0;
      for (Element e : (List<Element>) node.getChildren("dipoleCoeff"))
      {
         coeffs[i++] = e.getAttribute("value").getDoubleValue();
      }
   }
   void getField(double x, double y, double z, BasicHep3Vector field)
   {
      double bx = 0;
      double rSquared = x*x + y*y;
      double zPowerI = 1;
      
      // Check if z coordinate is within dipole z bounds.
      if (z > zmin && z < zmax && rSquared < rSquaredMax)
      {
         // Apply all coefficients to this z coordinate.
         for (int i = 0; i < coeffs.length; ++i)
         {
            bx += coeffs[i] * zPowerI;
            zPowerI *= z;
         }
      }
      field.setV(bx,0,0);
   }
}