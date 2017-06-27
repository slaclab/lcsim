package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 * The element for box_dipole in LCDD. 
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: BoxDipole.java,v 1.1 2011/06/24 22:17:13 jeremy Exp $
 */
public class BoxDipole extends Field
{
    public BoxDipole(
            String name,
            double x,
            double y,
            double z,
            double dx,
            double dy,
            double dz,
            double bx, 
            double by,
            double bz)
    {
       super("box_dipole", name);
       
       setAttribute("x", Double.toString(x));
       setAttribute("y", Double.toString(y));
       setAttribute("z", Double.toString(z));
       setAttribute("dx", Double.toString(dx));
       setAttribute("dy", Double.toString(dy));
       setAttribute("dz", Double.toString(dz));
       setAttribute("bx", Double.toString(bx));
       setAttribute("by", Double.toString(by));
       setAttribute("bz", Double.toString(bz));
    }    
}