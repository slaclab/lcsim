package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Calorimeter;

/**
 *
 * LCDD binding for CartesianGridXZ segmentation.
 *
 * @author cassell
 */
public class RegularNgonCartesianGridXZ extends LCDDSegmentation
{

    private double gridSizeX;
    private double gridSizeZ;

    RegularNgonCartesianGridXZ(Element node) throws DataConversionException, JDOMException
    {
        super(node);

        Attribute attrib = node.getAttribute("gridSizeX");
        if (attrib == null)
        {
            throw new JDOMException("Required attribute gridSizePhi was not found.");
        }
        gridSizeX = attrib.getDoubleValue();

        attrib = node.getAttribute("gridSizeZ");

        if (attrib == null)
        {
            throw new JDOMException("Required attribute gridSizeZ was not found.");
        }
        gridSizeZ = attrib.getDoubleValue();
    }

    void setSegmentation(Calorimeter cal)
    {
        org.lcsim.geometry.compact.converter.lcdd.util.GridXYZ seg =
                new org.lcsim.geometry.compact.converter.lcdd.util.GridXYZ();
        seg.setGridSizeX(gridSizeX);
        seg.setGridSizeZ(gridSizeZ);
        cal.setSegmentation(seg);
    }
}
