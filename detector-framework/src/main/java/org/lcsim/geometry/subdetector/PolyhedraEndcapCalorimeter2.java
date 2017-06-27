package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: PolyhedraEndcapCalorimeter2.java,v 1.13 2012/01/30 13:43:47 jeremy Exp $
 */
public class PolyhedraEndcapCalorimeter2 extends AbstractPolyhedraCalorimeter
{
    public PolyhedraEndcapCalorimeter2( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    private void build( Element node ) throws JDOMException
    {        
        Element dimensions = node.getChild( "dimensions" );
       
        innerZ = dimensions.getAttribute( "zmin" ).getDoubleValue();
        outerZ = innerZ + getLayering().getThickness();
        zlength = outerZ - innerZ;

        innerRadius = dimensions.getAttribute( "rmin" ).getDoubleValue();
        outerRadius = dimensions.getAttribute( "rmax" ).getDoubleValue();        
    
        // Set layering pre-offset.
        getLayering().setOffset( innerZ );
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        DetectorElementToHepRepConverter.convert( this.getDetectorElement(), factory, heprep, 2, true, getVisAttributes().getColor() );
    }

    public boolean isEndcap()
    {
        return true;
    }
}
