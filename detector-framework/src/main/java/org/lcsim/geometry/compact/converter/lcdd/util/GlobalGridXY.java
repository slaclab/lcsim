package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author jeremym
 */
public class GlobalGridXY extends Segmentation 
{   
    public GlobalGridXY() 
    {
        super("global_grid_xy");
        setAttribute("grid_size_x", "0.0");
        setAttribute("grid_size_y", "0.0");        
    }
    
    public void setGridSizeX(double gsx) 
    {
        setAttribute("grid_size_x", String.valueOf(gsx));
    }
    
    public void setGridSizeY(double gsy) 
    {
        setAttribute("grid_size_y", String.valueOf(gsy));
    }
}
