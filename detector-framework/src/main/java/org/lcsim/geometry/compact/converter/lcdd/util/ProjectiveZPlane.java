package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author tonyj
 */
public class ProjectiveZPlane extends Segmentation
{
   
   /** Creates a new instance of ProjectiveCylinder */
   public ProjectiveZPlane()
   {
      super("projective_zplane");
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
