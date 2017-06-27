/*
 * GridXYZSegmentation.java
 *
 * Created on May 27, 2005, 4:30 PM
 */

package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author jeremym
 */
public class GridXYZ extends Segmentation {
    
    /** Creates a new instance of GridXYZSegmentation */
    public GridXYZ() {
        super("grid_xyz");
        setAttribute("grid_size_x", "0.0");
        setAttribute("grid_size_y", "0.0");
        setAttribute("grid_size_z", "0.0");        
    }
    
    public void setGridSizeX(double gsx) {
        setAttribute("grid_size_x", String.valueOf(gsx));
    }
    
    public void setGridSizeY(double gsy) {
        setAttribute("grid_size_y", String.valueOf(gsy));
    }
    
    public void setGridSizeZ(double gsz) {
        setAttribute("grid_size_z", String.valueOf(gsz));
    }   
}