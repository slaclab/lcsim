package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author tonyj
 */
public class NonprojectiveCylinder extends Segmentation
{
   
   /** Creates a new instance of NonprojectiveCylinder */
   public NonprojectiveCylinder()
   {
      super("nonprojective_cylinder");
   }
   public void setGridSizeZ(double gsz)
   {
      setAttribute("grid_size_z",String.valueOf(gsz));
   }
   public void setGridSizePhi(double gsp)
   {
      setAttribute("grid_size_phi",String.valueOf(gsp));
   }   
}