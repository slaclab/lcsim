package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * Simple dipole field in a box.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: BoxDipole.java,v 1.1 2011/06/24 22:17:13 jeremy Exp $
 */
public class BoxDipole extends AbstractFieldMap
{
   private double x, y, z, dx, dy, dz, bx, by, bz;
   private double xmax, ymax, zmax, xmin, ymin, zmin;   

   BoxDipole(Element node) throws JDOMException
   {
      super(node);  

      x = node.getAttribute("x").getDoubleValue();
      y = node.getAttribute("y").getDoubleValue();
      z = node.getAttribute("z").getDoubleValue();
      dx = node.getAttribute("dx").getDoubleValue();
      dy = node.getAttribute("dy").getDoubleValue();
      dz = node.getAttribute("dz").getDoubleValue();
      bx = node.getAttribute("bx").getDoubleValue();
      by = node.getAttribute("by").getDoubleValue();
      bz = node.getAttribute("bz").getDoubleValue();
      
      xmax = x + dx;
      xmin = x - dx;
      ymax = y + dy;
      ymin = y - dy;
      zmax = z + dz;
      zmin = z - dz;
   }

   void getField(double x, double y, double z, BasicHep3Vector field)
   {
       // Check if point is within bounds.
       if (x > xmax || x < xmin)
           return;
       if (y > ymax || y < ymin)
           return;
       if (z > zmax || z < zmin)
           return;
       
       // Add dipole B field values to the existing field.
       double obx = field.x();
       double oby = field.y();
       double obz = field.z();            
       field.setV(bx + obx, by + oby, bz + obz);
   }
}