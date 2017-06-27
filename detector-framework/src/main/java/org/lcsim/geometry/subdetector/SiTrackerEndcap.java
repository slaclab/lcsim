package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $$Id: SiTrackerEndcap.java,v 1.11 2010/12/03 01:21:39 jeremy Exp $$
 */
public class SiTrackerEndcap extends AbstractTracker
{
    SiTrackerEndcap(Element node) throws JDOMException
    {    	
        super(node);
    }
    
    public boolean isEndcap()
    {
        return true;
    }    
      
    public void appendHepRep(HepRepFactory factory, HepRep heprep)
    {
        // Display layer envelopes.
        DetectorElementToHepRepConverter.convert(getDetectorElement(), factory, heprep, 2, true, getVisAttributes().getColor() );
    }
}
