package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;

/**
 * Subdetector implementation of SiTrackerBarrel.
 * 
 * For detailed geometry information, get the DetectorElement from the Subdetector.
 * 
 * @see org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter
 * @see org.lcsim.detector.tracker.silicon
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: SiTrackerBarrel.java,v 1.17 2010/12/03 01:21:39 jeremy Exp $
 */
public class SiTrackerBarrel extends AbstractTracker
{
    SiTrackerBarrel( Element node ) throws JDOMException
    {
        super( node );
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        // Display layer envelopes.
        DetectorElementToHepRepConverter.convert( getDetectorElement(), factory, heprep, 2, false, getVisAttributes().getColor() );
    }

    public boolean isBarrel()
    {
        return true;
    }
}
