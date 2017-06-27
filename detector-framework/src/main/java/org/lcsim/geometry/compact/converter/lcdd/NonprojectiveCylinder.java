package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Calorimeter;

/**
 *
 * @author jeremym
 */
public class NonprojectiveCylinder extends LCDDSegmentation
{
    private double gridSizePhi;
    private double gridSizeZ;

    NonprojectiveCylinder(Element node) throws DataConversionException, JDOMException
    {
        super(node);

        Attribute attrib = node.getAttribute("gridSizePhi");
        if ( attrib == null )
        {
            throw new JDOMException("Required attribute gridSizePhi was not found.");
        }
        gridSizePhi = attrib.getDoubleValue();

        attrib = node.getAttribute("gridSizeZ");

        if ( attrib == null )
        {
            throw new JDOMException("Required attribute gridSizeZ was not found.");
        }

        gridSizeZ = attrib.getDoubleValue();
    }

    void setSegmentation(Calorimeter cal)
    {
        org.lcsim.geometry.compact.converter.lcdd.util.NonprojectiveCylinder npcyl = new org.lcsim.geometry.compact.converter.lcdd.util.NonprojectiveCylinder();
        npcyl.setGridSizePhi(gridSizePhi);
        npcyl.setGridSizeZ(gridSizeZ);
        cal.setSegmentation(npcyl);
    }
}
