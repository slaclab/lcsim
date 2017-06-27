package org.lcsim.geometry.subdetector;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: PolyhedraEndcapCalorimeter3.java,v 1.7 2012/01/30 13:43:47 jeremy Exp $
 */
public class PolyhedraEndcapCalorimeter3 extends AbstractPolyhedraCalorimeter
{
    double zmin;
    double zmax;

    public PolyhedraEndcapCalorimeter3( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    private void build( Element node ) throws JDOMException
    {        
        Element dimensions = node.getChild( "dimensions" );
        
        System.out.println(getName());

        innerZ = dimensions.getAttribute( "zmin" ).getDoubleValue();
        outerZ = innerZ + getLayering().getThickness();
        zlength = outerZ - innerZ;

        innerRadius = dimensions.getAttribute( "rmin" ).getDoubleValue();
        outerRadius = dimensions.getAttribute( "rmax" ).getDoubleValue();
        outerRadius = outerRadius * Math.cos( Math.PI / nsides );
        
        // Set layering pre-offset.
        getLayering().setOffset( innerZ );
    }

    public boolean isEndcap()
    {
        return true;
    }
}
