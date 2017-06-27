package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class RZFieldMap extends Field
{
   
   /** Creates a new instance of Solenoid */
   public RZFieldMap(String name)
   {
      super("rz_field_map", name);
      setAttribute("lunit","cm");
      setAttribute("funit","kilogauss");
   }
   
   public void setNumBinsR(int numBinsR)
   {
       setAttribute("num_bins_r", String.valueOf(numBinsR));       
   }
   
   public void setNumBinsZ(int numBinsZ)
   {
       setAttribute("num_bins_z", String.valueOf(numBinsZ));
   }
   
   public void setGridSizeR(double gridSizeR)
   {
       setAttribute("grid_size_r", String.valueOf(gridSizeR));
   }
   
   public void setGridSizeZ(double gridSizeZ)
   {
       setAttribute("grid_size_z", String.valueOf(gridSizeZ));
   }
   
   public void addRZBData(double z, double r, double Bz, double Br)
   {       
       Element rzb = new Element("rzB");
       rzb.setAttribute("z", String.valueOf(z));
       rzb.setAttribute("r", String.valueOf(r));
       rzb.setAttribute("Bz", String.valueOf(Bz));
       rzb.setAttribute("Br", String.valueOf(Br));
       addContent(rzb);
   }
   
   public void addRZBData(RZBData rzb)
   {
	   addContent(rzb);
   }
   
   public void setLengthUnit(String lunit)
   {
       setAttribute("lunit", lunit);
   }
   
   public void setFieldUnit(String funit)
   {
       setAttribute("funit", funit);
   }          
}