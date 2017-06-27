package org.lcsim.detector.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.XMLConverter;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;

/**
 * 
 * This converter takes a GDML element and converts
 * it to a MaterialElement.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class MaterialElementConverter 
implements XMLConverter
{
	public void convert(Element element) throws JDOMException
	{
		if ( element.getName().equals("element") )
		{
			String name = element.getAttributeValue("name");		
			double z = element.getAttribute("Z").getDoubleValue();
			
			Element atom = element.getChild("atom");
			
			if ( atom != null )
			{
				double a = atom.getAttribute("value").getDoubleValue();
				
				// FIXME: Application of a unit means this doesn't 
				//        end up matching the old materials db!
				//        Leave it out for now.
				
				//double unit = g / mole;
				//if ( atom.getAttribute("unit") != null )
				//{
				//	unit = atom.getAttribute("unit").getDoubleValue();
				//}				
				//a = a * unit;				
				
                // FIXME: Get state, temperature, and pressure from the XML file.
				new MaterialElement(
                        name, 
                        z, 
                        a, 
                        1.0, 
                        IMaterial.Unknown, 
                        IMaterial.defaultTemperature, 
                        IMaterial.defaultPressure);				
			}
			else {
				throw new JDOMException("The MaterialElement <" + name + "> is missing <atom>.");
			}				
		}
		else {
			throw new JDOMException("Invalid element <" + element.getName() + "> for MaterialElementConverter.");
		}
	}	
}