package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author tonyj
 */
public class ProjectiveCylinder extends Segmentation
{
   
   /** Creates a new instance of ProjectiveCylinder */
   public ProjectiveCylinder()
   {
      super("projective_cylinder");
   }
   public void setNTheta(int n)
   {
      setAttribute("ntheta",String.valueOf(n));
   }
   public void setNPhi(int n)
   {
      setAttribute("nphi",String.valueOf(n));
   }   
}
