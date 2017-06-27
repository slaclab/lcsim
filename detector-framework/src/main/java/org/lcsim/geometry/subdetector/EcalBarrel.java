package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: EcalBarrel.java,v 1.11 2010/12/03 01:21:39 jeremy Exp $
 */
public class EcalBarrel extends PolyhedraBarrelCalorimeter
{
    public EcalBarrel( Element element ) throws JDOMException
    {
        super( element );
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        DetectorElementToHepRepConverter.convert( getDetectorElement(), factory, heprep, 2, false, getVisAttributes().getColor() );
    }
}
