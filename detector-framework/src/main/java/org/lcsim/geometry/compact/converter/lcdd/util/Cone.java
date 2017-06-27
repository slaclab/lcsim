package org.lcsim.geometry.compact.converter.lcdd.util;

public class Cone extends Solid 
{
	public Cone(
			String name, 
			double rmin1, 
			double rmin2, 
			double rmax1, 
			double rmax2, 
			double z, 
			double startphi, 
			double deltaphi)
	{
		super("cone", name);
		setAttribute("rmin1", String.valueOf(rmin1));
		setAttribute("rmin2", String.valueOf(rmin2));
		setAttribute("rmax1", String.valueOf(rmax1));
		setAttribute("rmax2", String.valueOf(rmax2));
		setAttribute("z", String.valueOf(z));
		setAttribute("startphi", String.valueOf(startphi));
		setAttribute("deltaphi", String.valueOf(deltaphi));
	}
	
	public Cone(
			String name, 
			double rmin1, 
			double rmin2, 
			double rmax1, 
			double rmax2, 
			double z)
	{
		super("cone", name);
		setAttribute("rmin1", String.valueOf(rmin1));
		setAttribute("rmin2", String.valueOf(rmin2));
		setAttribute("rmax1", String.valueOf(rmax1));
		setAttribute("rmax2", String.valueOf(rmax2));
		setAttribute("z", String.valueOf(z));
		setAttribute("startphi", String.valueOf(0));
		setAttribute("deltaphi", String.valueOf(Math.PI * 2));
	}
}