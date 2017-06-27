package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepType;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.geometry.HepRepProvider;

/**
 * This class is meant to simplify the inheritance hierarchy for
 * org.lcsim.geometry.subdetector implementation classes.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: AbstractSubdetector.java,v 1.12 2010/12/03 01:21:39 jeremy Exp $
 */
abstract class AbstractSubdetector extends org.lcsim.geometry.compact.Subdetector implements HepRepProvider
{
    /**
     * Creates a new instance of AbstractSubdetector
     */
    public AbstractSubdetector( Element node ) throws JDOMException
    {
        super( node );
    }

    /**
     * HepRepProvider: optional impl for subclass
     */
    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {       
        // Let subclasses implement this.
        
        // TODO Generic implementation.  Requires depth on VisAttributes.
        // DetectorElementToHepRepConverter.convert( getDetectorElement(), factory, heprep, getVisAttributes().getDepth(), isEndcap(), getVisAttributes().getColor() );
    }

    /**
     * Assign color to subdetector HepRep if there are VisAttributes.
     * 
     * @param type The HepRepType for this subdetector.
     */
    /*
    protected void setHepRepColor( HepRepType type )
    {
        if ( getVisAttributes() != null )
        {
            type.addAttValue( "color", getVisAttributes().getColor() );
        }
    }
    */
}