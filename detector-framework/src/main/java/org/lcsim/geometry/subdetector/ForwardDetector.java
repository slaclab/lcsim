/*
 * ForwardDetector.java
 * 
 * Created on June 16, 2005, 2:07 PM
 */

package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: ForwardDetector.java,v 1.3 2010/11/30 00:16:29 jeremy Exp $
 */
public class ForwardDetector extends CylindricalEndcapCalorimeter
{
    private double crossingAngle;
    private double outgoingRadius;
    private double incomingRadius;

    /** Creates a new instance of ForwardDetector */
    public ForwardDetector( Element node ) throws JDOMException, DataConversionException
    {
        super( node );

        Element beamElem = node.getChild( "beampipe" );

        if ( beamElem == null )
        {
            throw new JDOMException( "ForwardDetector - Missing required beampipe element." );
        }

        if ( beamElem.getAttribute( "crossing_angle" ) != null )
        {
            crossingAngle = beamElem.getAttribute( "crossing_angle" ).getDoubleValue();
        }
        else
        {
            throw new JDOMException( "ForwardDetector - Missing required crossing_angle attribute." );
        }

        if ( beamElem.getAttribute( "outgoing_r" ) != null )
        {
            outgoingRadius = beamElem.getAttribute( "outgoing_r" ).getDoubleValue();
        }
        else
        {
            throw new JDOMException( "ForwardDetector - Missing required outgoing_r attribute." );
        }

        if ( beamElem.getAttribute( "incoming_r" ) != null )
        {
            incomingRadius = beamElem.getAttribute( "incoming_r" ).getDoubleValue();
        }
        else
        {
            throw new JDOMException( "ForwardDetector - Missing required incoming_r attribute." );
        }
    }

    /**
     * This calls superclass method, which leaves out the beamholes.
     */
    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        super.appendHepRep( factory, heprep );
    }

    public double getCrossingAngle()
    {
        return crossingAngle;
    }

    public double getOutgoingRadius()
    {
        return outgoingRadius;
    }

    public double getIncomingRadius()
    {
        return incomingRadius;
    }
}
