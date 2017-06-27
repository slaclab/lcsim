package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class FieldMap3D extends Field
{   
   /** Creates a new instance of Solenoid */
   public FieldMap3D(String name, String filename, double xoffset, double yoffset, double zoffset)
   {
      super("field_map_3d", name);
      setAttribute("filename", filename);
      setAttribute("xoffset", Double.toString(xoffset));
      setAttribute("yoffset", Double.toString(yoffset));
      setAttribute("zoffset", Double.toString(zoffset));
   }         
}