package org.lcsim.detector.converter.compact;

import hep.physics.vec.Hep3Vector;

import org.lcsim.detector.DetectorElement;
import org.lcsim.geometry.Detector;

/**
 * Wraps a compact detector in a DetectorElement.
 * 
 * @see org.lcsim.geometry.Detector
 * @see org.lcsim.detector.DetectorElement
 * @see org.lcsim.geometry.FieldMap
 * 
 * @author Jeremy McCormick
 * @version $Id: DeDetector.java,v 1.3 2010/11/30 00:16:27 jeremy Exp $
 */
public class DeDetector extends DetectorElement
{
    Detector detector;

    public DeDetector( Detector detector )
    {
        super( detector.getName(), null, "/" );
        this.detector = detector;
    }

    public Hep3Vector getBField( Hep3Vector position )
    {
        return detector.getFieldMap().getField( position );
    }

    public Detector getCompactDetector()
    {
        return detector;
    }
}