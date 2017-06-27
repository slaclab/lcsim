/*
 * GridXYZ.java
 *
 * Created on May 27, 2005, 4:34 PM
 */

package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.DataConversionException;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.Attribute;
import org.lcsim.geometry.compact.converter.lcdd.util.Calorimeter;

/**
 *
 * @author jeremym
 */
public class GridXYZ extends LCDDSegmentation 
{
	private double gridSizeX;
	private double gridSizeY;
	private double gridSizeZ;

	/** Creates a new instance of GridXYZ */
	public GridXYZ(Element node) throws DataConversionException, JDOMException 
	{
		super(node);

		boolean gotOne = false;

		Attribute attrib = node.getAttribute("gridSizeX");

		if ( attrib != null )
		{
			gotOne = true;
			gridSizeX = attrib.getDoubleValue();
		}
		else
		{
			gridSizeX = 0;
		}

		attrib = node.getAttribute("gridSizeY");

		if ( attrib != null )
		{
			gotOne = true;
			gridSizeY = attrib.getDoubleValue();
		}
		else
		{
			gridSizeY = 0;
		}

		attrib = node.getAttribute("gridSizeZ");

		if ( attrib != null )
		{
			gotOne = true;
			gridSizeZ = attrib.getDoubleValue();
		}
		else
		{
			gridSizeZ = 0;
		}

		if ( !gotOne )
		{
			throw new JDOMException("Missing one of required attributes gridSizeX / Y / Z.");
		}
	}

	void setSegmentation(Calorimeter cal)
	{
		org.lcsim.geometry.compact.converter.lcdd.util.GridXYZ g =
			new org.lcsim.geometry.compact.converter.lcdd.util.GridXYZ();

		g.setGridSizeX(gridSizeX);
		g.setGridSizeY(gridSizeY);
		g.setGridSizeZ(gridSizeZ);

		cal.setSegmentation(g);
	}

}
