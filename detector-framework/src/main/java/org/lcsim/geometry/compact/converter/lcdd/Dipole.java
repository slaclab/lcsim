package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDDFactory;

public class Dipole extends LCDDField
{
	private Element node;
	Dipole(Element element)
	{
		super(element);
	    this.node = element;
	}

	void addToLCDD(LCDD lcdd) throws JDOMException
	{
		String name = node.getAttributeValue("name");
		double zmax = node.getAttribute("zmax").getDoubleValue();
		double zmin = node.getAttribute("zmin").getDoubleValue();
		double rmax = node.getAttribute("rmax").getDoubleValue();
		
		int ncoeff = node.getChildren("dipoleCoeff").size();
		double[] coeffs = new double[ncoeff];
		int i = 0;
		for (Object o : node.getChildren("dipoleCoeff"))
		{
			Element e = (Element)o;
			double v = e.getAttribute("value").getDoubleValue();
			coeffs[i] = v;
			i++;
		}
		
		org.lcsim.geometry.compact.converter.lcdd.util.Dipole dipole = 
			LCDDFactory.createDipole(name, zmin, zmax, rmax, coeffs);
		
		lcdd.add(dipole);
	}

}
