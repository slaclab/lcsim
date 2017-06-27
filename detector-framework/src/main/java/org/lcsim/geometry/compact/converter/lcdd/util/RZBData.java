package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 *
 * @author jeremym
 */
public class RZBData extends Element
{	
	   public RZBData(double z, double r, double Bz, double Br)
	   {       
	       super("rzB");
	       setAttribute("z", String.valueOf(z));
	       setAttribute("r", String.valueOf(r));
	       setAttribute("Bz", String.valueOf(Bz));
	       setAttribute("Br", String.valueOf(Br));	       
	   }          
}