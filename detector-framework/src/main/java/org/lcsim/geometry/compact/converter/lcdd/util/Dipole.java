package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

public class Dipole extends Field
{
	public Dipole(String name)
	{
		super("dipole", name);
	}
	
	public void setZMax(double zmax)
	{
		setAttribute("zmax",String.valueOf(zmax));
	}
	
	public void setZMin(double zmin)
	{
		setAttribute("zmin",String.valueOf(zmin));
	}
	
	public void setRMax(double rmax)
	{
		setAttribute("rmax",String.valueOf(rmax));
	}
	
	public void addCoeff(double coeff)
	{
		Element e = new Element("dipole_coeff");
		e.setAttribute("value",String.valueOf(coeff));
		addContent(e);
	}
}