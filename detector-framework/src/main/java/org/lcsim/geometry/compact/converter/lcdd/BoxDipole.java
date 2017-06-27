package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;

/**
 * The converter class for adding an LCDD box_dipole element to the document.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: BoxDipole.java,v 1.1 2011/06/24 22:17:13 jeremy Exp $
 */
class BoxDipole extends LCDDField
{
    private Element node;
           
    boolean setExplicitly = false;

    BoxDipole(Element element)
    {
        super(element);
        this.node = element;
    }    

    void addToLCDD(LCDD lcdd) throws JDOMException
    {
        double x = node.getAttribute("x").getDoubleValue();
        double y = node.getAttribute("y").getDoubleValue();
        double z = node.getAttribute("z").getDoubleValue();
        double dx = node.getAttribute("dx").getDoubleValue();
        double dy = node.getAttribute("dy").getDoubleValue();
        double dz = node.getAttribute("dz").getDoubleValue();
        double bx = node.getAttribute("bx").getDoubleValue();
        double by = node.getAttribute("by").getDoubleValue();
        double bz = node.getAttribute("bz").getDoubleValue();
        
        org.lcsim.geometry.compact.converter.lcdd.util.BoxDipole dip = 
            new org.lcsim.geometry.compact.converter.lcdd.util.BoxDipole(
                    getName(), x, y, z, dx, dy, dz, bx, by, bz);
        
        lcdd.add(dip);
    }
}