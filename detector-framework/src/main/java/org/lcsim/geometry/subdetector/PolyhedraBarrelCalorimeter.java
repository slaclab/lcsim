package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: PolyhedraBarrelCalorimeter.java,v 1.15 2012/01/30 13:43:47 jeremy Exp $
 */
public class PolyhedraBarrelCalorimeter extends AbstractPolyhedraCalorimeter
{
    public PolyhedraBarrelCalorimeter( Element node ) throws JDOMException
    {
        super( node );

        // Setup parameters from XML.
        zlength = node.getChild( "dimensions" ).getAttribute( "z" ).getDoubleValue();
        outerZ = zlength / 2;
        innerZ = -outerZ;
        innerRadius = node.getChild( "dimensions" ).getAttribute( "rmin" ).getDoubleValue();
        outerRadius = computeBarrelOuterRadius();
    }

    public boolean isBarrel()
    {
        return true;
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        DetectorElementToHepRepConverter.convert( getDetectorElement(), factory, heprep, 2, false, getVisAttributes().getColor() );
    }
}