package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Calorimeter;

/**
 *
 * @author jeremym
 */
public class GlobalGridXY extends LCDDSegmentation 
{
    private double gridSizeX;
    private double gridSizeY;

    /** Creates a new instance of GridXYZ */
    public GlobalGridXY(Element node) throws DataConversionException, JDOMException 
    {
        super(node);

        Attribute attrib = node.getAttribute("gridSizeX");

        if ( attrib != null )
        {
            gridSizeX = attrib.getDoubleValue();
        }
        else
        {
            throw new RuntimeException("Missing gridSizeX parameter.");
        }

        attrib = node.getAttribute("gridSizeY");

        if ( attrib != null )
        {            
            gridSizeY = attrib.getDoubleValue();
        }
        else
        {
            throw new RuntimeException("Missing gridSizeY parameter.");
        }
    }

    void setSegmentation(Calorimeter cal)
    {
        org.lcsim.geometry.compact.converter.lcdd.util.GlobalGridXY g =
            new org.lcsim.geometry.compact.converter.lcdd.util.GlobalGridXY();

        g.setGridSizeX(gridSizeX);
        g.setGridSizeY(gridSizeY);

        cal.setSegmentation(g);
    }
}
